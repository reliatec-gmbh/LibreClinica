/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author jxu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewSiteServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -432895800917682385L;
	EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        int siteId = request.getParameter("id") == null ? 0 : Integer.valueOf(request.getParameter("id"));
        if (currentStudy.getId() == siteId) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        String idString = "";
        if (request.getAttribute("siteId") == null) {
            idString = request.getParameter("id");
        } else {
            idString = request.getAttribute("siteId").toString();
        }
        logger.info("site id:" + idString);
        if (idString == null || idString.trim().isEmpty()) {
            addPageMessage(respage.getString("please_choose_a_site_to_edit"));
            forwardPage(Page.SITE_LIST_SERVLET);
        } else {
            int siteId = Integer.valueOf(idString.trim()).intValue();
            StudyBean study = (StudyBean) sdao.findByPK(siteId);

            checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());
            // if (currentStudy.getId() != study.getId()) {

            ArrayList<StudyParamsConfig> configs = new ArrayList<>();
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            configs = spvdao.findParamConfigByStudy(study);
            study.setStudyParameters(configs);

            // }

            String parentStudyName = "";
            if (study.getParentStudyId() > 0) {
                StudyBean parent = (StudyBean) sdao.findByPK(study.getParentStudyId());
                parentStudyName = parent.getName();
            }
            request.setAttribute("parentName", parentStudyName);
            request.setAttribute("siteToView", study);
            request.setAttribute("idToSort", request.getAttribute("idToSort"));
            viewSiteEventDefinitions(study);

            forwardPage(Page.VIEW_SITE);
        }
    }

    private void viewSiteEventDefinitions(StudyBean siteToView) throws MalformedURLException {
        int siteId = siteToView.getId();
        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        seds = sedDao.findAllByStudy(siteToView);
        for (StudyEventDefinitionBean sed : seds) {
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();       
            request.setAttribute("participateFormStatus",participateFormStatus );            
            if (participateFormStatus.equals("enabled")) baseUrl();
        
            request.setAttribute("participateFormStatus",participateFormStatus );

            int defId = sed.getId();
            ArrayList<EventDefinitionCRFBean> edcs =
                (ArrayList<EventDefinitionCRFBean>) edcdao.findAllByDefinitionAndSiteIdAndParentStudyId(defId, siteId, siteToView.getParentStudyId());
            ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
            for (EventDefinitionCRFBean edcBean : edcs) {
                CRFBean cBean = (CRFBean) cdao.findByPK(edcBean.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                edcBean.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));
                
                CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) cvdao.findAllActiveByCRF(edcBean.getCrfId());
                edcBean.setVersions(versions);
                edcBean.setCrfName(crf.getName());
                CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                edcBean.setDefaultVersionName(defaultVersion.getName());
                String sversionIds = edcBean.getSelectedVersionIds();
                ArrayList<Integer> idList = new ArrayList<Integer>();
                String idNames = "";
                if (sversionIds.length() > 0) {
                    String[] ids = sversionIds.split("\\,");
                    for (String id : ids) {
                        idList.add(Integer.valueOf(id));
                        for (CRFVersionBean v : versions) {
                            if (v.getId() == Integer.valueOf(id)) {
                                idNames += v.getName() + ",";
                                break;
                            }
                        }
                    }
                    idNames = idNames.substring(0, idNames.length() - 1);
                }
                if(edcBean.getParentId()<1){
                	edcBean.setSubmissionUrl("");
                }
                edcBean.setSelectedVersionIdList(idList);
                edcBean.setSelectedVersionNames(idNames);
                defCrfs.add(edcBean);
            }
            sed.setCrfs(defCrfs);
            sed.setCrfNum(defCrfs.size());
        }

        request.setAttribute("definitions", seds);
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        request.setAttribute("sdvOptions", sdvOptions);

    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
