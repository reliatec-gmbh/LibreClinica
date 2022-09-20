/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.managestudy;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;
import static org.akaza.openclinica.core.util.ClassCastHelper.asHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.CRFRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

/**
 * Processes request to add new CRFs info study event definition
 * 
 * @author jxu
 */
public class AddCRFToDefinitionServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5188093285323751984L;

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

        addPageMessage(respage.getString("no_have_permission_to_update_study_event_definition") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String actionName = request.getParameter("actionName");
        String submit = request.getParameter("Submit");

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        ArrayList<CRFBean> crfs = cdao.findAllByStatus(Status.AVAILABLE);
        ArrayList<EventDefinitionCRFBean> edcs = asArrayList(session.getAttribute("eventDefinitionCRFs"), EventDefinitionCRFBean.class);
        if (edcs == null) {
            edcs = new ArrayList<>();
        }
        HashMap<Integer, EventDefinitionCRFBean> crfIds = new HashMap<>();
        for (int i = 0; i < edcs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
            Integer crfId = new Integer(edc.getCrfId());
            crfIds.put(crfId, edc);
        }
        for (int i = 0; i < crfs.size(); i++) {
            CRFBean crf = (CRFBean) crfs.get(i);
            if (crfIds.containsKey(new Integer(crf.getId()))) {
                crf.setSelected(true);
            }
        }
        session.setAttribute("crfsWithVersion", crfs);
        if(submit!=null){
            addCRF();
        }else{
            if (actionName == null || actionName.trim().isEmpty()) {
                FormProcessor fp = new FormProcessor(request);
                EntityBeanTable table = fp.getEntityBeanTable();
                ArrayList<CRFRow> allRows = CRFRow.generateRowsFromBeans(crfs);
                String[] columns =
                    { resword.getString("CRF_name"), resword.getString("date_created"), resword.getString("owner"), resword.getString("date_updated"),
                        resword.getString("last_updated_by"), resword.getString("selected") };
                table.setColumns(new ArrayList<String>(Arrays.asList(columns)));
                table.hideColumnLink(5);
                table.setQuery("AddCRFToDefinition", new HashMap<>());
                table.setRows(allRows);
                table.computeDisplay();

                request.setAttribute("table", table);
                forwardPage(Page.UPDATE_EVENT_DEFINITION2);
            }else if(actionName.equalsIgnoreCase("next")){
                Integer pageNumber = Integer.valueOf(request.getParameter("pageNum"));
                if (pageNumber != null) {
                    if (pageNumber.intValue() == 2) {
                        String nextListPage = request.getParameter("next_list_page");
                        if (nextListPage != null && nextListPage.equalsIgnoreCase("true")) {
                            confirmDefinition();
                        }
                    } else {
                        confirmDefinition();
                    }
                }
            }
        }
    }

    private void confirmDefinition() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        Map<Integer, String> tmpCRFIdMap = asHashMap(session.getAttribute("tmpCRFIdMap"), Integer.class, String.class);
        if (tmpCRFIdMap == null) {
            tmpCRFIdMap = new HashMap<>();
        }
        ArrayList<CRFBean> crfsWithVersion = asArrayList(session.getAttribute("crfsWithVersion"), CRFBean.class);
        for (int i = 0; i < crfsWithVersion.size(); i++) {
            int id = fp.getInt("id" + i);
            String name = fp.getString("name" + i);
            String selected = fp.getString("selected" + i);
            if (!(selected == null || selected.trim().isEmpty()) && "yes".equalsIgnoreCase(selected.trim())) {
                tmpCRFIdMap.put(id, name);
            } else {
                if (tmpCRFIdMap.containsKey(id)) {
                    tmpCRFIdMap.remove(id);
                }
            }
        }
        session.setAttribute("tmpCRFIdMap", tmpCRFIdMap);

        EntityBeanTable table = fp.getEntityBeanTable();
        ArrayList<CRFRow> allRows = CRFRow.generateRowsFromBeans(crfsWithVersion);
        String[] columns =
            { resword.getString("CRF_name"), resword.getString("date_created"), resword.getString("owner"), resword.getString("date_updated"),
                resword.getString("last_updated_by"), resword.getString("selected") };
        table.setColumns(new ArrayList<String>(Arrays.asList(columns)));
        table.hideColumnLink(5);
        StudyEventDefinitionBean def1 = (StudyEventDefinitionBean) session.getAttribute("definition");
        HashMap<String, Object> args = new HashMap<>();
        args.put("actionName", "next");
        args.put("pageNum", "1");
        args.put("name", def1.getName());
        args.put("repeating", new Boolean(def1.isRepeating()).toString());
        args.put("category", def1.getCategory());
        args.put("description", def1.getDescription());
        args.put("type", def1.getType());
        table.setQuery("AddCRFToDefinition", args);
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        forwardPage(Page.UPDATE_EVENT_DEFINITION2);
    }
    
    private void addCRF() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        ArrayList<CRFBean> crfArray = new ArrayList<>();
        Map<Integer, String> tmpCRFIdMap = asHashMap(session.getAttribute("tmpCRFIdMap"), Integer.class, String.class);
        if (tmpCRFIdMap == null) {
            tmpCRFIdMap = new HashMap<>();
        }
        ArrayList<CRFBean> crfsWithVersion = asArrayList(session.getAttribute("crfsWithVersion"), CRFBean.class);
        for (int i = 0; i < crfsWithVersion.size(); i++) {
            int id = fp.getInt("id" + i);
            String name = fp.getString("name" + i);
            String selected = fp.getString("selected" + i);
            if (!(selected == null || selected.trim().isEmpty()) && "yes".equalsIgnoreCase(selected.trim())) {
                logger.info("one crf selected");
                CRFBean cb = new CRFBean();
                cb.setId(id);
                cb.setName(name);

                // only find active verions
                ArrayList<CRFVersionBean> versions = vdao.findAllActiveByCRF(cb.getId());
                cb.setVersions(versions);

                crfArray.add(cb);
            } else {
                if (tmpCRFIdMap.containsKey(id)) {
                    tmpCRFIdMap.remove(id);
                }
            }
        }

        for (Integer id : tmpCRFIdMap.keySet()) {
            String name = tmpCRFIdMap.get(id);
            boolean isExists = false;
            for (CRFBean cb : crfArray) {
                if (id == cb.getId()) {
                    isExists = true;
                    break;
                }
            }
            if (!isExists) {
                CRFBean cb = new CRFBean();
                cb.setId(id);
                cb.setName(name);
                // only find active verions
                ArrayList<CRFVersionBean> versions = vdao.findAllActiveByCRF(cb.getId());
                cb.setVersions(versions);
                crfArray.add(cb);
            }
        }
        session.removeAttribute("tmpCRFIdMap");
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    


        if (crfArray.size() == 0) {// no crf seleted
            addPageMessage(respage.getString("no_new_CRF_added"));
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            request.setAttribute("participateFormStatus",participateFormStatus );

            sed.setCrfs(new ArrayList<>());
            session.setAttribute("definition", sed);
            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        } else {

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled")) baseUrl();
            
            request.setAttribute("participateFormStatus",participateFormStatus );

            ArrayList<EventDefinitionCRFBean> edcs = asArrayList(session.getAttribute("eventDefinitionCRFs"), EventDefinitionCRFBean.class);
            int ordinalForNewCRF = edcs.size();
            for(int i=0; i<crfArray.size(); i++){
                CRFBean crf = (CRFBean)crfArray.get(i);
                EventDefinitionCRFBean edcBean = new EventDefinitionCRFBean();
                edcBean.setCrfId(crf.getId());
                edcBean.setCrfName(crf.getName());
                edcBean.setStudyId(ub.getActiveStudyId());
                edcBean.setStatus(Status.AVAILABLE);
                edcBean.setStudyEventDefinitionId(sed.getId());
                edcBean.setStudyId(ub.getActiveStudyId());
                edcBean.setSourceDataVerification(SourceDataVerification.NOTREQUIRED);
                ordinalForNewCRF = ordinalForNewCRF + 1;
                edcBean.setOrdinal(ordinalForNewCRF);
                edcBean.setVersions(crf.getVersions());

                CRFVersionBean defaultVersion1 = (CRFVersionBean) vdao.findByPK(edcBean.getDefaultVersionId());
                edcBean.setDefaultVersionName(defaultVersion1.getName());

                ordinalForNewCRF++;
                edcs.add(edcBean);
            }
            session.setAttribute("eventDefinitionCRFs", edcs);
            ArrayList<String> sdvOptions = new ArrayList<String>();
            sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
            request.setAttribute("sdvOptions", sdvOptions);
            addPageMessage(respage.getString("has_have_been_added_need_confirmation"));
            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        }
    }
}    
