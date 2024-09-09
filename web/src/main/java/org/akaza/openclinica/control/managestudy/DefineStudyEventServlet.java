/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;
import static org.akaza.openclinica.core.util.ClassCastHelper.asHashMap;
import static org.akaza.openclinica.core.util.ClassCastHelper.getAsType;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.CRFRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.apache.commons.lang.StringUtils;

/**
 * @author jxu
 * 
 * Defines a new study event
 */
public class DefineStudyEventServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4421505680553105618L;
	EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"));

        if (currentStudy.getParentStudyId() > 0) {
            addPageMessage(respage.getString("SED_may_only_added_top_level") + respage.getString("please_contact_sysadmin_questions"));
            throw new InsufficientPermissionException(Page.STUDY_EVENT_DEFINITION_LIST, resexception.getString("not_top_study"), "1");
        }

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_persmission_add_SED_to_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.STUDY_EVENT_DEFINITION_LIST, resexception.getString("not_study_director"), "1");

    }

    /**
     * Processes the 'define study event' request
     * 
     */
    @Override
    public void processRequest() throws Exception {
        String actionName = request.getParameter("actionName");
        ArrayList<CRFBean> crfsWithVersion = asArrayList(session.getAttribute("crfsWithVersion"), CRFBean.class);
        if (crfsWithVersion == null) {
            crfsWithVersion = new ArrayList<>();
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
            ArrayList<CRFBean> crfs = cdao.findAllByStatus(Status.AVAILABLE);

            for (int i = 0; i < crfs.size(); i++) {
                CRFBean crf = (CRFBean) crfs.get(i);
                ArrayList<CRFVersionBean> versions = cvdao.findAllByCRFId(crf.getId());
                if (!versions.isEmpty()) {
                    crfsWithVersion.add(crf);
                }

            }
            session.setAttribute("crfsWithVersion", crfsWithVersion);
        }
        if (actionName == null || actionName.trim().isEmpty()) {
            StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
            sed.setStudyId(currentStudy.getId());
            session.setAttribute("definition", sed);
            session.removeAttribute("tmpCRFIdMap");
            forwardPage(Page.DEFINE_STUDY_EVENT1);
        } else {
            if ("confirm".equalsIgnoreCase(actionName)) {
                confirmWholeDefinition();

            } else if ("submit".equalsIgnoreCase(actionName)) {
                // put a try catch in here to fix task # 1642 in Mantis, added
                // 092007 tbh
                try {
                    Integer nextAction = Integer.valueOf(request.getParameter("nextAction"));
                    if (nextAction != null) {
                        if (nextAction.intValue() == 1) {
                            session.removeAttribute("definition");
                            addPageMessage(respage.getString("the_new_event_definition_creation_cancelled"));
                            forwardPage(Page.LIST_DEFINITION_SERVLET);
                        } else if (nextAction.intValue() == 2) {
                            submitDefinition();
                            ArrayList<String> pageMessages = asArrayList(request.getAttribute(PAGE_MESSAGE), String.class);
                            session.setAttribute(PAGE_MESSAGE, pageMessages);
                            response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE.getFileName());
                        } else {
                            logger.debug("actionName ==> 3");
                            submitDefinition();
                            StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
                            sed.setStudyId(currentStudy.getId());
                            session.setAttribute("definition", sed);
                            forwardPage(Page.DEFINE_STUDY_EVENT1);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("nextAction value is not proper: ", e);
                    addPageMessage(respage.getString("the_new_event_definition_creation_cancelled"));
                    forwardPage(Page.LIST_DEFINITION_SERVLET);
                } catch (NullPointerException e) {
                    logger.error("Process in study event is not defined properly: ", e);
                    addPageMessage(respage.getString("the_new_event_definition_creation_cancelled"));
                    forwardPage(Page.LIST_DEFINITION_SERVLET);
                }
                // above added 092007 tbh

            } else if ("next".equalsIgnoreCase(actionName)) {
                Integer pageNumber = Integer.valueOf(request.getParameter("pageNum"));
                if (pageNumber != null) {
                    if (pageNumber.intValue() == 2) {
                        String nextListPage = request.getParameter("next_list_page");
                        if (nextListPage != null && nextListPage.equalsIgnoreCase("true")) {
                            confirmDefinition1();
                        } else {
                            confirmDefinition2();
                        }
                    } else {
                        confirmDefinition1();
                    }
                } else {
                    if (session.getAttribute("definition") == null) {
                        StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
                        sed.setStudyId(currentStudy.getId());
                        session.setAttribute("definition", sed);
                    }
                    forwardPage(Page.DEFINE_STUDY_EVENT1);
                }
            }
        }
    }

    /**
     * Validates the first section of definition inputs
     * 
     * @throws Exception
     */
    private void confirmDefinition1() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        
        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);

        errors = v.validate();

        session.setAttribute("definition", createStudyEventDefinition());

        if (errors.isEmpty()) {
            logger.debug("no errors in the first section");
            /*
             * The tmpCRFIdMap will hold all the selected CRFs in the session
             * when the user is navigating through the list. This has been done
             * so that when the user moves to the next page of CRF list, the
             * selection made in the previous page doesn't get lost.
             */
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
                    // Removing the elements from session which has been
                    // deselected.
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
            StudyEventDefinitionBean def1 = getAsType(session.getAttribute("definition"), StudyEventDefinitionBean.class);
            HashMap<String, Object> args = new HashMap<>();
            args.put("actionName", "next");
            args.put("pageNum", "1");
            args.put("name", URLEncoder.encode(def1.getName(),"UTF-8"));
            args.put("repeating", new Boolean(def1.isRepeating()).toString());
            args.put("category", def1.getCategory());
            args.put("description", URLEncoder.encode(def1.getDescription(),"UTF-8"));
            args.put("type", def1.getType());
            table.setQuery("DefineStudyEvent", args, true);
            table.setRows(allRows);
            table.computeDisplay();

            request.setAttribute("table", table);
            
             forwardPage(Page.DEFINE_STUDY_EVENT2);

        } else {
            logger.debug("has validation errors in the first section");
            request.setAttribute("formMessages", errors);
            forwardPage(Page.DEFINE_STUDY_EVENT1);
        }
    }

    /**
     * Validates the entire definition
     * 
     * @throws Exception
     */
    private void confirmWholeDefinition() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        

        
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = new ArrayList<>();
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        for (int i = 0; i < sed.getCrfs().size(); i++) {
            EventDefinitionCRFBean edcBean = new EventDefinitionCRFBean();
            int crfId = fp.getInt("crfId" + i);
            int defaultVersionId = fp.getInt("defaultVersionId" + i);
            edcBean.setCrfId(crfId);
            edcBean.setDefaultVersionId(defaultVersionId);
            CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
            edcBean.setDefaultVersionName(defaultVersion.getName());

            String crfName = fp.getString("crfName" + i);
            // String crfLabel = fp.getString("crfLabel" + i);
            edcBean.setCrfName(crfName);
            // edcBean.setCrfLabel(crfLabel);

            String requiredCRF = fp.getString("requiredCRF" + i);
            String doubleEntry = fp.getString("doubleEntry" + i);
            String decisionCondition = fp.getString("decisionCondition" + i);
            String electronicSignature = fp.getString("electronicSignature" + i);
            String participantForm = fp.getString("participantForm" + i);
            String allowAnonymousSubmission = fp.getString("allowAnonymousSubmission" + i);
            String submissionUrl = fp.getString("submissionUrl" + i);
            String offline = fp.getString("offline" + i);

            String hiddenCrf = fp.getString("hiddenCrf" + i);
            // hideCRF is false by default in the bean
            if (!(hiddenCrf == null || hiddenCrf.trim().isEmpty()) && "yes".equalsIgnoreCase(hiddenCrf.trim())) {
                edcBean.setHideCrf(true);
            } else {
                edcBean.setHideCrf(false);
            }
            String sdvOption = fp.getString("sdvOption" + i);
            if (!StringUtils.isBlank(sdvOption)) {
                int id = Integer.valueOf(sdvOption);
                edcBean.setSourceDataVerification(SourceDataVerification.getByCode(id));
            }
            if (!StringUtils.isBlank(requiredCRF) && "yes".equalsIgnoreCase(requiredCRF.trim())) {
                edcBean.setRequiredCRF(true);
            } else {
                edcBean.setRequiredCRF(false);
            }
            if (!StringUtils.isBlank(participantForm) && "yes".equalsIgnoreCase(participantForm.trim())) {
                edcBean.setParticipantForm(true);
            } else {
                edcBean.setParticipantForm(false);
            }
            // when participant form is not selected, force allow anonymous to be not selected
            if (edcBean.isParticipantForm() && !StringUtils.isBlank(allowAnonymousSubmission) && "yes".equalsIgnoreCase(allowAnonymousSubmission.trim())) {
                edcBean.setAllowAnonymousSubmission(true);
            } else {
                edcBean.setAllowAnonymousSubmission(false);
            }
            if (!StringUtils.isBlank(offline) && "yes".equalsIgnoreCase(offline.trim())) {
                edcBean.setOffline(true);
            } else {
                edcBean.setOffline(false);
            }
            if (!StringUtils.isBlank(doubleEntry) && "yes".equalsIgnoreCase(doubleEntry.trim())) {
                edcBean.setDoubleEntry(true);
            } else {
                edcBean.setDoubleEntry(false);
            }
            if (!StringUtils.isBlank(decisionCondition) && "yes".equalsIgnoreCase(decisionCondition.trim())) {
                edcBean.setDecisionCondition(true);
            } else {
                edcBean.setDecisionCondition(false);
            }

            if (!StringUtils.isBlank(electronicSignature) && "yes".equalsIgnoreCase(electronicSignature.trim())) {
                edcBean.setElectronicSignature(true);
            } else {
                edcBean.setElectronicSignature(false);
            }
            // only update submission url when participant form and allow anonymous was selected,
            // otherwise keep old value for history sake
            // also useful to protect from naughty submission not coming from our html form
            if (edcBean.isParticipantForm() && edcBean.isAllowAnonymousSubmission()) {
                edcBean.setSubmissionUrl(submissionUrl.trim());
            }
            ArrayList <CRFVersionBean> versions = cvdao.findAllByCRFId(crfId);
            edcBean.setVersions(versions);

            String nullString = "";
            // process null values
            ArrayList<NullValue> nulls = NullValue.toArrayList();
            for (int a = 0; a < nulls.size(); a++) {
                NullValue n = (NullValue) nulls.get(a);
                String myNull = fp.getString(n.getName().toLowerCase() + i);
                if (!StringUtils.isBlank(myNull) && "yes".equalsIgnoreCase(myNull.trim())) {
                    nullString = nullString + n.getName().toUpperCase() + ",";
                }

            }

            edcBean.setNullValues(nullString);
            edcBean.setStudyId(ub.getActiveStudyId());
            eventDefinitionCRFs.add(edcBean);
        }
        

        
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
             request.setAttribute("participateFormStatus",participateFormStatus );
             if (participateFormStatus.equals("enabled")) baseUrl();
   
             request.setAttribute("participateFormStatus",participateFormStatus );
        
        request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
        session.setAttribute("edCRFs", eventDefinitionCRFs);// not used on page
    
        ArrayList <EventDefinitionCRFBean>  edcsInSession = asArrayList(session.getAttribute("edCRFs"), EventDefinitionCRFBean.class);
        int parentStudyId=sed.getStudyId();
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList <EventDefinitionCRFBean> eventDefCrfList =(ArrayList <EventDefinitionCRFBean>) edcdao.findAllActiveSitesAndStudiesPerParentStudy(parentStudyId);
       
        if(eventDefCrfList.size()!=0)
        validateSubmissionUrl(edcsInSession,eventDefCrfList,v);
        errors = v.validate();

        if (!errors.isEmpty()) {
            ArrayList<String> sdvOptions = new ArrayList<String>();
            sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
            request.setAttribute("sdvOptions", sdvOptions);

            logger.info("has errors");
           session.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
            request.setAttribute("formMessages", errors);
            forwardPage(Page.DEFINE_STUDY_EVENT4);
        } 
        
        forwardPage(Page.DEFINE_STUDY_EVENT_CONFIRM);

    }

    /**
     * Constructs study bean from request-first section
     * 
     * @return
     */
    private StudyEventDefinitionBean createStudyEventDefinition() {
    	

        FormProcessor fp = new FormProcessor(request);
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        
        try{
	        String name = URLDecoder.decode(request.getParameter("name"), "UTF-8");
	        String description = URLDecoder.decode(request.getParameter("description"), "UTF-8");
	    
	        sed.setName(name);
	        sed.setDescription(description);
        }
        catch(Exception e){
        	//leaving old code here 
        	sed.setName(fp.getString("name"));
	        sed.setDescription(fp.getString("description"));
        }
        // YW <<
        String temp = fp.getString("repeating");
        if ("true".equalsIgnoreCase(temp) || "1".equals(temp)) {
            sed.setRepeating(true);
        } else if ("false".equalsIgnoreCase(temp) || "0".equals(temp)) {
            sed.setRepeating(false);
        }
        // YW >>
        sed.setCategory(fp.getString("category"));
        
        sed.setType(fp.getString("type"));
        return sed;

    }

    private void confirmDefinition2() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        ArrayList<CRFBean> crfArray = new ArrayList<>();
        Map<Integer, String> tmpCRFIdMap = asHashMap(session.getAttribute("tmpCRFIdMap"), Integer.class, String.class);
        // trying to avoid NPE not sure why we would get it there ((tmpCRFIdMap.containsKey(id))), tbh
        if (tmpCRFIdMap == null) {
            tmpCRFIdMap = new HashMap<>();
        }
        

        
        ArrayList<CRFBean> crfsWithVersion = asArrayList(session.getAttribute("crfsWithVersion"), CRFBean.class);
        for (int i = 0; i < crfsWithVersion.size(); i++) {
            int id = fp.getInt("id" + i);
            String name = fp.getString("name" + i);
            String selected = fp.getString("selected" + i);
            if (!StringUtils.isBlank(selected) && "yes".equalsIgnoreCase(selected.trim())) {
                logger.debug("one crf selected");
                CRFBean cb = new CRFBean();
                cb.setId(id);
                cb.setName(name);

                // only find active versions
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
        
        if (crfArray.size() == 0) {
            addPageMessage(respage.getString("no_CRF_selected_for_definition_add_later"));
            StudyEventDefinitionBean sed = getAsType(session.getAttribute("definition"), StudyEventDefinitionBean.class);
            sed.setCrfs(new ArrayList<>());
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
             request.setAttribute("participateFormStatus",participateFormStatus );
            session.setAttribute("definition", sed);
            request.setAttribute("eventDefinitionCRFs", new ArrayList<>());
            session.setAttribute("edCRFs", new ArrayList<>());
            forwardPage(Page.DEFINE_STUDY_EVENT_CONFIRM);
        } else {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
            sed.setCrfs(crfArray);
            session.setAttribute("eventDefinitionCRFs", new ArrayList<>());
            session.setAttribute("definition", sed);
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled")) baseUrl();

            request.setAttribute("participateFormStatus",participateFormStatus );

            ArrayList<String> sdvOptions = new ArrayList<String>();
            sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
            request.setAttribute("sdvOptions", sdvOptions);
            forwardPage(Page.DEFINE_STUDY_EVENT3);
        }

    }

    /**
     * Inserts the new study into database NullPointer catch added by tbh
     * 092007, mean to fix task #1642 in Mantis
     * 
     */
    private void submitDefinition() throws NullPointerException {
        StudyEventDefinitionDAO edao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        // added tbh 092007, to catch bug # 1531
        if (sed.getName() == "" || sed.getName() == null) {
            throw new NullPointerException();
        }
        logger.debug("Definition bean to be created:" + sed.getName() + sed.getStudyId());

        // fine the last one's ordinal
        ArrayList<StudyEventDefinitionBean> defs = edao.findAllByStudy(currentStudy);
        if (defs == null || defs.isEmpty()) {
            sed.setOrdinal(1);
        } else {
            int lastCount = defs.size() - 1;
            StudyEventDefinitionBean last = (StudyEventDefinitionBean) defs.get(lastCount);
            sed.setOrdinal(last.getOrdinal() + 1);
        }
        sed.setOwner(ub);
        sed.setCreatedDate(new Date());
        sed.setStatus(Status.AVAILABLE);
        StudyEventDefinitionBean sed1 = (StudyEventDefinitionBean) edao.create(sed);

        EventDefinitionCRFDAO cdao = new EventDefinitionCRFDAO(sm.getDataSource());
        CRFDAO crfdao = new CRFDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = new ArrayList<>();
        if (session.getAttribute("edCRFs") != null) {
            eventDefinitionCRFs = asArrayList(session.getAttribute("edCRFs"), EventDefinitionCRFBean.class);
        }
        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edc = eventDefinitionCRFs.get(i);
            edc.setOwner(ub);
            edc.setCreatedDate(new Date());
            edc.setStatus(Status.AVAILABLE);
            edc.setStudyEventDefinitionId(sed1.getId());
            edc.setOrdinal(i + 1);
            StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(sed.getId());
            CRFBean cBean = (CRFBean) crfdao.findByPK(edc.getCrfId());                
            String crfPath=sedBean.getOid()+"."+cBean.getOid();
            getEventDefinitionCrfTagService().saveEventDefnCrfOfflineTag(2, crfPath, edc ,sedBean);
          
            cdao.create(edc);
        }

        session.removeAttribute("definition");
        session.removeAttribute("edCRFs");
        session.removeAttribute("crfsWithVersion");

        addPageMessage(respage.getString("the_new_event_definition_created_succesfully"));

    }

    public void validateSubmissionUrl(ArrayList <EventDefinitionCRFBean> edcsInSession ,ArrayList <EventDefinitionCRFBean> eventDefCrfList ,Validator v){
    	for (int i = 0; i < edcsInSession.size(); i++) {
            v.addValidation("submissionUrl" + i, Validator.NO_SPACES_ALLOWED);
            EventDefinitionCRFBean sessionBean = null;
            boolean isExist = false;
            for (EventDefinitionCRFBean eventDef : eventDefCrfList) {
                sessionBean = edcsInSession.get(i);
                if (!sessionBean.isAllowAnonymousSubmission() || !sessionBean.isParticipantForm()) {
                    isExist = true;
                    break;
                }
                logger.debug("iter:           {}--db:    {}", eventDef.getId(), eventDef.getSubmissionUrl());
                logger.debug("edcsInSession:  {}--session: {}", sessionBean.getId(), sessionBean.getSubmissionUrl());
            	if (sessionBean.getSubmissionUrl() == null || sessionBean.getSubmissionUrl().equals("")) {
            		break;
            	} else {
                    if (eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() != sessionBean.getId()) ||
                            (eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId()) && sessionBean.getId() == 0)) {
                        v.addValidation("submissionUrl"+ i, Validator.SUBMISSION_URL_NOT_UNIQUE);
                        logger.debug("Duplicate *************************");
                        isExist = true;
                       break;
                    } else if(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId()) && sessionBean.getId() != 0) {
                        logger.debug("Not Duplicate *********");
                        isExist = true;
                        break;
                    }
                }
            }
            if (!isExist) {
                eventDefCrfList.add(sessionBean);
            }
        }
    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
