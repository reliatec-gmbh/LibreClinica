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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
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
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * @author jxu
 * 
 *         Creates a sub study of user's current active study
 * 
 *         Modified by ywang: [10-10-2007], enable setting overidable study
 *         parameters of a sub study.
 */
public class CreateSubStudyServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8890957594406780315L;

	EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    public static final String INPUT_VER_DATE = "protocolDateVerification";
    public static final String INPUT_START_DATE = "startDate";
    public static final String INPUT_END_DATE = "endDate";

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
        // checkStudyFrozen(Page.SITE_LIST_SERVLET,
        // respage.getString("current_study_frozen"));
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + "\n" + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SITE_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String action = request.getParameter("action");
        session.setAttribute("sdvOptions", this.setSDVOptions());

        if (action == null || action.trim().isEmpty()) {
            if (currentStudy.getParentStudyId() > 0) {
                addPageMessage(respage.getString("you_cannot_create_site_itself_site"));

                forwardPage(Page.SITE_LIST_SERVLET);
            } else {
                StudyBean newStudy = new StudyBean();
                newStudy.setParentStudyId(currentStudy.getId());
                // get default facility info from property file
                newStudy.setFacilityName(SQLInitServlet.getField(CreateStudyServlet.FAC_NAME));
                newStudy.setFacilityCity(SQLInitServlet.getField(CreateStudyServlet.FAC_CITY));
                newStudy.setFacilityState(SQLInitServlet.getField(CreateStudyServlet.FAC_STATE));
                newStudy.setFacilityCountry(SQLInitServlet.getField(CreateStudyServlet.FAC_COUNTRY));
                newStudy.setFacilityContactDegree(SQLInitServlet.getField(CreateStudyServlet.FAC_CONTACT_DEGREE));
                newStudy.setFacilityContactEmail(SQLInitServlet.getField(CreateStudyServlet.FAC_CONTACT_EMAIL));
                newStudy.setFacilityContactName(SQLInitServlet.getField(CreateStudyServlet.FAC_CONTACT_NAME));
                newStudy.setFacilityContactPhone(SQLInitServlet.getField(CreateStudyServlet.FAC_CONTACT_PHONE));
                newStudy.setFacilityZip(SQLInitServlet.getField(CreateStudyServlet.FAC_ZIP));

                List<StudyParamsConfig> parentConfigs = currentStudy.getStudyParameters();
                // logger.info("parentConfigs size:" + parentConfigs.size());
                ArrayList<StudyParamsConfig> configs = new ArrayList<>();

                for (StudyParamsConfig scg : parentConfigs) {
                    // StudyParamsConfig scg = (StudyParamsConfig)
                    // parentConfigs.get(i);
                    if (scg != null) {
                        // find the one that sub study can change
                        if (scg.getValue().getId() > 0 && scg.getParameter().isOverridable()) {
                            logger.info("parameter:" + scg.getParameter().getHandle());
                            logger.info("value:" + scg.getValue().getValue());
                            // YW 10-12-2007, set overridable study parameters
                            // for a site
                            if (scg.getParameter().getHandle().equalsIgnoreCase("interviewerNameRequired")) {
                                scg.getValue().setValue(fp.getString("interviewerNameRequired"));
                            } else if (scg.getParameter().getHandle().equalsIgnoreCase("interviewerNameDefault")) {
                                scg.getValue().setValue(fp.getString("interviewerNameDefault"));
                            } else if (scg.getParameter().getHandle().equalsIgnoreCase("interviewDateRequired")) {
                                scg.getValue().setValue(fp.getString("interviewDateRequired"));
                            } else if (scg.getParameter().getHandle().equalsIgnoreCase("interviewDateDefault")) {
                                scg.getValue().setValue(fp.getString("interviewDateDefault"));
                            }
                            // YW >>
                            configs.add(scg);
                        }
                    }

                }
                newStudy.setStudyParameters(configs);

                // YW 10-12-2007 <<
//                newStudy.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
//                newStudy.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
//                newStudy.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
//                newStudy.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));
                // YW >>

                // BWP 3169 1-12-2008 <<
                newStudy.getStudyParameterConfig().setInterviewerNameEditable(currentStudy.getStudyParameterConfig().getInterviewerNameEditable());
                newStudy.getStudyParameterConfig().setInterviewerNameDefault(currentStudy.getStudyParameterConfig().getInterviewerNameDefault());
                newStudy.getStudyParameterConfig().setInterviewDateEditable(currentStudy.getStudyParameterConfig().getInterviewDateEditable());
                newStudy.getStudyParameterConfig().setInterviewDateDefault(currentStudy.getStudyParameterConfig().getInterviewDateDefault());
                // >>

                try {
                    local_df.parse(fp.getString(INPUT_START_DATE));
                    fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
                } catch (ParseException pe) {
                    fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
                }
                try {
                    local_df.parse(fp.getString(INPUT_END_DATE));
                    fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
                } catch (ParseException pe) {
                    fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
                }
                // tbh 3946 07/2009
                try {
                    local_df.parse(fp.getString(INPUT_VER_DATE));
                    fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
                } catch (ParseException pe) {
                    fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
                }
                // >> tbh
                setPresetValues(fp.getPresetValues());

                session.setAttribute("newStudy", newStudy);
                session.setAttribute("definitions", this.initDefinitions(newStudy));
                request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
                request.setAttribute("statuses", Status.toActiveArrayList());
                forwardPage(Page.CREATE_SUB_STUDY);
            }

        } else {
            if ("confirm".equalsIgnoreCase(action)) {
            	// reset the event StudyEventDefinitionBeans, otherwise the changes won't be recognized propably
                StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
            	session.setAttribute("definitions", this.initDefinitions(newStudy));
                confirmStudy();

            } else if ("back".equalsIgnoreCase(action)) {
                StudyBean newStudy = (StudyBean) session.getAttribute("newStudy");
                try {
                    fp.addPresetValue(INPUT_START_DATE, local_df.format(newStudy.getDatePlannedEnd()));
                } catch (Exception pe) {
                    fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
                }
                try {
                    fp.addPresetValue(INPUT_END_DATE, local_df.format(newStudy.getDatePlannedStart()));
                } catch (Exception pe) {
                    fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
                }
                try {
                    fp.addPresetValue(INPUT_VER_DATE, local_df.format(newStudy.getProtocolDateVerification()));
                } catch (Exception pe) {
                    fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
                }
                setPresetValues(fp.getPresetValues());
                request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
                request.setAttribute("statuses", Status.toActiveArrayList());

                forwardPage(Page.CREATE_SUB_STUDY);
            } else if ("submit".equalsIgnoreCase(action)) {
                submitStudy();
            }
        }
    }

    /**
     * Validates the first section of study and save it into study bean
     * 
     * @throws Exception
     */
    private void confirmStudy() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("uniqueProId", Validator.NO_BLANKS);

        // >> tbh
        // v.addValidation("description", Validator.NO_BLANKS);
        // << tbh, #3943, 07/2009
        v.addValidation("prinInvestigator", Validator.NO_BLANKS);
        String startDate = fp.getString(INPUT_START_DATE);
		if (!(startDate == null || startDate.trim().isEmpty())) {
            v.addValidation(INPUT_START_DATE, Validator.IS_A_DATE);
        }
        String endDate = fp.getString(INPUT_END_DATE);
		if (!(endDate == null || endDate.trim().isEmpty())) {
            v.addValidation(INPUT_END_DATE, Validator.IS_A_DATE);
        }
        String contactEmail = fp.getString("facConEmail");
		if (!(contactEmail == null || contactEmail.trim().isEmpty())) {
            v.addValidation("facConEmail", Validator.IS_A_EMAIL);
        }
        String verDate = fp.getString(INPUT_VER_DATE);
		if (!(verDate == null || verDate.trim().isEmpty())) {
            v.addValidation(INPUT_VER_DATE, Validator.IS_A_DATE);
        }
        // v.addValidation("statusId", Validator.IS_VALID_TERM);
        v.addValidation("secondProId", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facCity", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facState", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 20);
        v.addValidation("facZip", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        v.addValidation("facCountry", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        v.addValidation("facConName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConDegree", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConPhone", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facConEmail", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

    //    errors = v.validate();

        // >> tbh
 /*       StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        ArrayList<StudyBean> allStudies = (ArrayList<StudyBean>) studyDAO.findAll();
        for (StudyBean thisBean : allStudies) {
            if (fp.getString("uniqueProId").trim().equals(thisBean.getIdentifier())) {
                v.addError(errors, "uniqueProId", resexception.getString("unique_protocol_id_existed"));
            }
        }
        // << tbh #3999 08/2009
        if (fp.getString("name").trim().length() > 100) {
         //   Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
        }
        if (fp.getString("uniqueProId").trim().length() > 30) {
            Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
        }
        if (fp.getString("description").trim().length() > 255) {
            Validator.addError(errors, "description", resexception.getString("maximum_lenght_brief_summary_255"));
        }
        if (fp.getString("prinInvestigator").trim().length() > 255) {
            Validator.addError(errors, "prinInvestigator", resexception.getString("maximum_lenght_principal_investigator_255"));
        }
        if (fp.getInt("expectedTotalEnrollment") <= 0) {
            Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
        }*/

    
        StudyBean newSite = this.createStudyBean();
        StudyBean parentStudy = new StudyDAO(sm.getDataSource()).findByPK(newSite.getParentStudyId());
        session.setAttribute("newStudy", newSite);
        session.setAttribute("definitions", this.createSiteEventDefinitions(parentStudy,v));

        if (errors.isEmpty()) {
            logger.info("no errors");
            forwardPage(Page.CONFIRM_CREATE_SUB_STUDY);

        }/* else {
            try {
                local_df.parse(fp.getString(INPUT_START_DATE));
                fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
            }
            try {
                local_df.parse(fp.getString(INPUT_END_DATE));
                fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
            }
            try {
                local_df.parse(fp.getString(INPUT_VER_DATE));
                fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
            }
            setPresetValues(fp.getPresetValues());
            logger.info("has validation errors");
            request.setAttribute("formMessages", errors);
            // request.setAttribute("facRecruitStatusMap",
            // CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toActiveArrayList());
            forwardPage(Page.CREATE_SUB_STUDY);
        }
*/
    }

    /**
     * Constructs study bean from request
     * 
     * @return
     */
    private StudyBean createStudyBean() {
        FormProcessor fp = new FormProcessor(request);
        StudyBean study = (StudyBean) session.getAttribute("newStudy");
        study.setName(fp.getString("name"));
        study.setIdentifier(fp.getString("uniqueProId"));
        study.setSecondaryIdentifier(fp.getString("secondProId"));
        study.setSummary(fp.getString("description"));
        study.setPrincipalInvestigator(fp.getString("prinInvestigator"));
        study.setExpectedTotalEnrollment(fp.getInt("expectedTotalEnrollment"));
        java.util.Date startDate = null;
        java.util.Date endDate = null;
        java.util.Date protocolDate = null;
        try {
            local_df.setLenient(false);
            startDate = local_df.parse(fp.getString("startDate"));

        } catch (ParseException fe) {
            startDate = study.getDatePlannedStart();
            logger.info(fe.getMessage());
        }
        study.setDatePlannedStart(startDate);

        try {
            local_df.setLenient(false);
            endDate = local_df.parse(fp.getString("endDate"));

        } catch (ParseException fe) {
            endDate = study.getDatePlannedEnd();
        }
        study.setDatePlannedEnd(endDate);

        // >> tbh 3946 07/2009
        try {
            local_df.setLenient(false);
            protocolDate = local_df.parse(fp.getString(INPUT_VER_DATE));

        } catch (ParseException fe) {
            protocolDate = study.getProtocolDateVerification();
        }
        study.setProtocolDateVerification(protocolDate);
        // << tbh
        study.setFacilityCity(fp.getString("facCity"));
        study.setFacilityContactDegree(fp.getString("facConDrgree"));
        study.setFacilityName(fp.getString("facName"));
        study.setFacilityContactEmail(fp.getString("facConEmail"));
        study.setFacilityContactPhone(fp.getString("facConPhone"));
        study.setFacilityContactName(fp.getString("facConName"));
        study.setFacilityContactDegree(fp.getString("facConDegree"));
        study.setFacilityCountry(fp.getString("facCountry"));
        // study.setFacilityRecruitmentStatus(fp.getString("facRecStatus"));
        study.setFacilityState(fp.getString("facState"));
        study.setFacilityZip(fp.getString("facZip"));
        study.setStatus(Status.get(fp.getInt("statusId")));

        ArrayList<StudyParamsConfig> parameters = study.getStudyParameters();

        for (int i = 0; i < parameters.size(); i++) {
            StudyParamsConfig scg = parameters.get(i);
            String value = fp.getString(scg.getParameter().getHandle());
            logger.info("get value:" + value);
            scg.getValue().setParameter(scg.getParameter().getHandle());
            scg.getValue().setValue(value);
        }

        // YW 10-12-2007 <<
        study.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
        study.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
        study.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
        study.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));
        // YW >>

        return study;
    }

    /**
     * Inserts the new study into database
     * 
     */
    private void submitStudy() throws IOException {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) session.getAttribute("newStudy");

        ArrayList<StudyParamsConfig> parameters = study.getStudyParameters();
        logger.info("study bean to be created:\n");
        logger.info(study.getName() + "\n" + study.getIdentifier() + "\n" + study.getParentStudyId() + "\n" + study.getSummary() + "\n"
            + study.getPrincipalInvestigator() + "\n" + study.getDatePlannedStart() + "\n" + study.getDatePlannedEnd() + "\n" + study.getFacilityName() + "\n"
            + study.getFacilityCity() + "\n" + study.getFacilityState() + "\n" + study.getFacilityZip() + "\n" + study.getFacilityCountry() + "\n"
            + study.getFacilityRecruitmentStatus() + "\n" + study.getFacilityContactName() + "\n" + study.getFacilityContactEmail() + "\n"
            + study.getFacilityContactPhone() + "\n" + study.getFacilityContactDegree());

        study.setOwner(ub);
        study.setCreatedDate(new Date());
        StudyBean parent = sdao.findByPK(study.getParentStudyId());
        study.setType(parent.getType());
        // YW 10-10-2007, enable setting site status
        study.setStatus(study.getStatus());
        // YW >>
        //set mail notification for site based on selection for parent study
        study.setMailNotification(parent.getMailNotification());
        study.setContactEmail(parent.getContactEmail());
        
        study.setGenetic(parent.isGenetic());
        study = sdao.create(study);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        for (int i = 0; i < parameters.size(); i++) {
            StudyParamsConfig config = parameters.get(i);
            StudyParameterValueBean spv = config.getValue();
            spv.setStudyId(study.getId());
            spv = spvdao.create(config.getValue());
        }

        // YW << here only "collectDob" and "genderRequired" have been corrected
        // for sites.
        StudyParameterValueBean spv = new StudyParameterValueBean();
        StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parent.getId(), "collectDob");
        spv.setStudyId(study.getId());
        spv.setParameter("collectDob");
        spv.setValue(parentSPV.getValue());
        spvdao.create(spv);

        parentSPV = spvdao.findByHandleAndStudy(parent.getId(), "genderRequired");
        spv.setParameter("genderRequired");
        spv.setValue(parentSPV.getValue());
        spvdao.create(spv);
        // YW >>

        this.submitSiteEventDefinitions(study);

        // switch user to the newly created site
        //session.setAttribute("study", session.getAttribute("newStudy"));
        //currentStudy = (StudyBean) session.getAttribute("study");

        session.removeAttribute("newStudy");
        addPageMessage(respage.getString("the_new_site_created_succesfully_current"));
        ArrayList<String> pageMessages = asArrayList(request.getAttribute(PAGE_MESSAGE), String.class);
        session.setAttribute("pageMessages", pageMessages);
        response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE.getFileName());

    }

    private ArrayList<StudyEventDefinitionBean> createSiteEventDefinitions(StudyBean site, Validator v) throws MalformedURLException {
        FormProcessor fp = new FormProcessor(request);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
        ArrayList <EventDefinitionCRFBean> edcsInSession = new ArrayList<EventDefinitionCRFBean>();

        StudyBean parentStudyBean;
        if (site.getParentStudyId()==0){
        	parentStudyBean = site;
        }else{
            StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
             parentStudyBean = studyDAO.findByPK(site.getParentStudyId());          	
        }
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList <EventDefinitionCRFBean> eventDefCrfList =edcdao.findAllActiveSitesAndStudiesPerParentStudy(parentStudyBean.getId());

        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        StudyBean parentStudy = new StudyDAO(sm.getDataSource()).findByPK(site.getParentStudyId());
        seds = asArrayList(session.getAttribute("definitions"), StudyEventDefinitionBean.class);
        if (seds == null || seds.size() <= 0) {
            StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource());
            seds = sedDao.findAllByStudy(parentStudy);
        }
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        HashMap<String, Boolean> changes = new HashMap<String, Boolean>();
        for (StudyEventDefinitionBean sed : seds) {
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled")) baseUrl();
            request.setAttribute("participateFormStatus",participateFormStatus );

            ArrayList<EventDefinitionCRFBean> edcs = asArrayList(sed.getCrfs(), EventDefinitionCRFBean.class);
            
            int start = 0;
            for (EventDefinitionCRFBean edcBean : edcs) {
            	EventDefinitionCRFBean persistEventDefBean = (EventDefinitionCRFBean) edcdao.findByPK(edcBean.getId());
            	            	
                int edcStatusId = edcBean.getStatus().getId();
                if (edcStatusId == 5 || edcStatusId == 7) {
                } else {
                    String order = start + "-" + edcBean.getId();
                    int defaultVersionId = fp.getInt("defaultVersionId" + order);
                    String requiredCRF = fp.getString("requiredCRF" + order);
                    String doubleEntry = fp.getString("doubleEntry" + order);
                    String electronicSignature = fp.getString("electronicSignature" + order);
                    String hideCRF = fp.getString("hideCRF" + order);
                    int sdvId = fp.getInt("sdvOption" + order);
                    String submissionUrl = fp.getString("submissionUrl" + order);

                    ArrayList<String> selectedVersionIdList = fp.getStringArray("versionSelection" + order);
                    int selectedVersionIdListSize = selectedVersionIdList.size();
                    String selectedVersionIds = "";
                    if (selectedVersionIdListSize > 0) {
                        for (String id : selectedVersionIdList) {
                            selectedVersionIds += id + ",";
                        }
                        selectedVersionIds = selectedVersionIds.substring(0, selectedVersionIds.length() - 1);
                    }

                    boolean changed = false;
                    boolean isRequired = !(requiredCRF == null || requiredCRF.trim().isEmpty()) && "yes".equalsIgnoreCase(requiredCRF.trim()) ? true : false;
                    boolean isDouble = !(doubleEntry == null || doubleEntry.trim().isEmpty()) && "yes".equalsIgnoreCase(doubleEntry.trim()) ? true : false;
                    boolean hasPassword = !(electronicSignature == null || electronicSignature.trim().isEmpty()) && "yes".equalsIgnoreCase(electronicSignature.trim()) ? true : false;
                    boolean isHide = !(hideCRF == null || hideCRF.trim().isEmpty()) && "yes".equalsIgnoreCase(hideCRF.trim()) ? true : false;

                    if (edcBean.getParentId() > 0) {
                        int dbDefaultVersionId = edcBean.getDefaultVersionId();
                        if (defaultVersionId != dbDefaultVersionId) {
                            changed = true;
                            CRFVersionBean defaultVersion = cvdao.findByPK(defaultVersionId);
                            edcBean.setDefaultVersionId(defaultVersionId);
                            edcBean.setDefaultVersionName(defaultVersion.getName());
                        }
                        if (isRequired != edcBean.isRequiredCRF()) {
                            changed = true;
                            edcBean.setRequiredCRF(isRequired);
                        }
                        if (isDouble != edcBean.isDoubleEntry()) {
                            changed = true;
                            edcBean.setDoubleEntry(isDouble);
                        }
                        if (hasPassword != edcBean.isElectronicSignature()) {
                            changed = true;
                            edcBean.setElectronicSignature(hasPassword);
                        }
                        if (isHide != edcBean.isHideCrf()) {
                            changed = true;
                            edcBean.setHideCrf(isHide);
                        }
                        if (!submissionUrl.equals(edcBean.getSubmissionUrl()) || !submissionUrl.equals(persistEventDefBean.getSubmissionUrl())) {
                            changed = true;
                            edcBean.setSubmissionUrl(submissionUrl);
                        }

                        if (!(selectedVersionIds == null || selectedVersionIds.trim().isEmpty()) && !selectedVersionIds.equals(edcBean.getSelectedVersionIds())) {
                            changed = true;
                            String[] ids = selectedVersionIds.split(",");
                            ArrayList<Integer> idList = new ArrayList<Integer>();
                            for (String id : ids) {
                                idList.add(Integer.valueOf(id));
                            }
                            edcBean.setSelectedVersionIdList(idList);
                            edcBean.setSelectedVersionIds(selectedVersionIds);
                        }
                        if (sdvId > 0 && sdvId != edcBean.getSourceDataVerification().getCode()) {
                            changed = true;
                            edcBean.setSourceDataVerification(SourceDataVerification.getByCode(sdvId));
                        }
                    } else {
                        // only if definition-crf has been modified, will it be
                        // saved for the site
                        int defaultId = defaultVersionId > 0 ? defaultVersionId : edcBean.getDefaultVersionId();
                        if (defaultId != defaultVersionId) {
                            changed = true;
                            CRFVersionBean defaultVersion = cvdao.findByPK(defaultVersionId);
                            edcBean.setDefaultVersionId(defaultVersionId);
                            edcBean.setDefaultVersionName(defaultVersion.getName());
                        }
                        if (isRequired != edcBean.isRequiredCRF()) {
                            changed = true;
                            edcBean.setRequiredCRF(isRequired);
                            edcBean.setSubmissionUrl(submissionUrl);
                        }
                        if (isDouble != edcBean.isDoubleEntry()) {
                            changed = true;
                            edcBean.setDoubleEntry(isDouble);
                            edcBean.setSubmissionUrl(submissionUrl);
                        }
                        if (hasPassword != edcBean.isElectronicSignature()) {
                            changed = true;
                            edcBean.setElectronicSignature(hasPassword);
                            edcBean.setSubmissionUrl(submissionUrl);
                        }
                        if (isHide != edcBean.isHideCrf()) {
                            changed = true;
                            edcBean.setHideCrf(isHide);
                            edcBean.setSubmissionUrl(submissionUrl);
                        }
                        if (!submissionUrl.equals("")) {
                            changed = true;
                            edcBean.setSubmissionUrl(submissionUrl);
                        }
						if (sdvId > 0 && sdvId != edcBean.getSourceDataVerification().getCode()) {
							changed = true;
							edcBean.setSourceDataVerification(SourceDataVerification.getByCode(sdvId));
							edcBean.setSubmissionUrl(submissionUrl);
						}
						if (selectedVersionIdListSize > 0
								&& selectedVersionIdListSize != edcBean.getVersions().size()) {
							changed = true;
							String[] ids = selectedVersionIds.split(",");
							ArrayList<Integer> idList = new ArrayList<Integer>();
							for (String id : ids) {
								idList.add(Integer.valueOf(id));
							}
							edcBean.setSelectedVersionIdList(idList);
							edcBean.setSelectedVersionIds(selectedVersionIds);
							edcBean.setSubmissionUrl(submissionUrl);

						}
                    }
                    changes.put(sed.getId() + "-" + edcBean.getId(), changed);
                    ++start;
                }
                edcsInSession.add(edcBean);
            }
        	sed.setPopulated(false);
            eventDefCrfList = validateSubmissionUrl(edcsInSession,eventDefCrfList,v,sed);
            edcsInSession.clear();


       }
       errors = v.validate();
       StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
       ArrayList<StudyBean> allStudies = studyDAO.findAll();
       for (StudyBean thisBean : allStudies) {
           if (fp.getString("uniqueProId").trim().equals(thisBean.getIdentifier())) {
               Validator.addError(errors, "uniqueProId", resexception.getString("unique_protocol_id_existed"));
           }
       }
       // << tbh #3999 08/2009
       if (fp.getString("name").trim().length() > 100) {
        //   Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
       }
       if (fp.getString("uniqueProId").trim().length() > 30) {
           Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
       }
       if (fp.getString("description").trim().length() > 255) {
           Validator.addError(errors, "description", resexception.getString("maximum_lenght_brief_summary_255"));
       }
       if (fp.getString("prinInvestigator").trim().length() > 255) {
           Validator.addError(errors, "prinInvestigator", resexception.getString("maximum_lenght_principal_investigator_255"));
       }
       if (fp.getInt("expectedTotalEnrollment") <= 0) {
           Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
       }

       if (!errors.isEmpty()) {
    //       logger.info("has errors");
           StudyBean study = createStudyBean();
           session.setAttribute("newStudy", study);
           session.setAttribute("definitions", seds);
           request.setAttribute("formMessages", errors);
        /*   try {
               local_df.parse(fp.getString(INPUT_START_DATE));
               fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
           } catch (ParseException pe) {
               fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
           }
           try {
               local_df.parse(fp.getString(INPUT_END_DATE));
               fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
           } catch (ParseException pe) {
               fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
           }
           try {
               local_df.parse(fp.getString(INPUT_VER_DATE));
               fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
           } catch (ParseException pe) {
               fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
           }*/
   //        setPresetValues(fp.getPresetValues());
           logger.info("has validation errors");
    //       request.setAttribute("formMessages", errors);
           // request.setAttribute("facRecruitStatusMap",
           // CreateStudyServlet.facRecruitStatusMap);
     //      request.setAttribute("statuses", Status.toActiveArrayList());
     //      forwardPage(Page.CREATE_SUB_STUDY);
           
           
           forwardPage(Page.CREATE_SUB_STUDY);
       }
        
        session.setAttribute("changed", changes);
        return seds;
    }

    private void submitSiteEventDefinitions(StudyBean site) throws MalformedURLException {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    

        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        seds = asArrayList(session.getAttribute("definitions"), StudyEventDefinitionBean.class);
        HashMap<String, Boolean> changes = asHashMap(session.getAttribute("changed"), String.class, Boolean.class);
        for (StudyEventDefinitionBean sed : seds) {
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            request.setAttribute("participateFormStatus",participateFormStatus );
            if (participateFormStatus.equals("enabled")) 	baseUrl();

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
            ArrayList<EventDefinitionCRFBean> edcs = asArrayList(sed.getCrfs(), EventDefinitionCRFBean.class);
            for (EventDefinitionCRFBean edcBean : edcs) {
           

                int edcStatusId = edcBean.getStatus().getId();
                if (edcStatusId == 5 || edcStatusId == 7) {
                } else {
                    boolean changed = changes.get(sed.getId() + "-" + edcBean.getId());
                    if (changed) {
                        edcBean.setParentId(edcBean.getId());
                        edcBean.setStudyId(site.getId());
                        edcBean.setUpdater(ub);
                        edcBean.setUpdatedDate(new Date());
                        logger.debug("create for the site");
                        edcdao.create(edcBean);
                    }
                }
            }
        }
        session.removeAttribute("definitions");
        session.removeAttribute("changed");
        session.removeAttribute("sdvOptions");
    }

    private ArrayList<StudyEventDefinitionBean> initDefinitions(StudyBean site) throws MalformedURLException {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    

        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        StudyBean parentStudy = new StudyDAO(sm.getDataSource()).findByPK(site.getParentStudyId());
        seds = sedDao.findAllByStudy(parentStudy);
        for (StudyEventDefinitionBean sed : seds) {
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled")) 	baseUrl();
            request.setAttribute("participateFormStatus",participateFormStatus );

            int defId = sed.getId();
            ArrayList<EventDefinitionCRFBean> edcs =
                edcdao.findAllByDefinitionAndSiteIdAndParentStudyId(defId, site.getId(), parentStudy.getId());
            ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
            // sed.setCrfNum(edcs.size());
            for (EventDefinitionCRFBean edcBean : edcs) {
                CRFBean cBean = cdao.findByPK(edcBean.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                edcBean.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));
            	

                int edcStatusId = edcBean.getStatus().getId();
                CRFBean crf = cdao.findByPK(edcBean.getCrfId());
                int crfStatusId = crf.getStatusId();
                if (edcStatusId == 5 || edcStatusId == 7 || crfStatusId == 5 || crfStatusId == 7) {
                } else {
                    ArrayList<CRFVersionBean> versions = cvdao.findAllActiveByCRF(edcBean.getCrfId());
                    edcBean.setVersions(versions);
                    edcBean.setCrfName(crf.getName());
                    CRFVersionBean defaultVersion = cvdao.findByPK(edcBean.getDefaultVersionId());
                    edcBean.setDefaultVersionName(defaultVersion.getName());
                    edcBean.setSubmissionUrl("");

/*                    EventDefinitionCRFBean eBean = (EventDefinitionCRFBean) edcdao.findByPK(edcBean.getId());
                    if (eBean.isActive()){ 
                    	edcBean.setSubmissionUrl(eBean.getSubmissionUrl());
                    }else{
                        edcBean.setSubmissionUrl("");
                    }
*/                    
                    String sversionIds = edcBean.getSelectedVersionIds();
                    ArrayList<Integer> idList = new ArrayList<Integer>();
                    if (sversionIds.length() > 0) {
                        String[] ids = sversionIds.split("\\,");
                        for (String id : ids) {
                            idList.add(Integer.valueOf(id));
                        }
                    }
                    edcBean.setSelectedVersionIdList(idList);
                    defCrfs.add(edcBean);
                }
            }
            logger.debug("definitionCrfs size=" + defCrfs.size() + " total size=" + edcs.size());
            sed.setCrfs(defCrfs);
            sed.setCrfNum(defCrfs.size());
        }
        return seds;
    }

    private ArrayList<String> setSDVOptions() {
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        return sdvOptions;
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }
    


    public ArrayList <EventDefinitionCRFBean> validateSubmissionUrl(ArrayList <EventDefinitionCRFBean> edcsInSession ,ArrayList <EventDefinitionCRFBean> eventDefCrfList ,Validator v, StudyEventDefinitionBean sed){
       	for (int i = 0; i < edcsInSession.size(); i++) {
            String order = i + "-" + edcsInSession.get(i).getId();
            v.addValidation("submissionUrl"+ order, Validator.NO_SPACES_ALLOWED);	
            EventDefinitionCRFBean sessionBean=null;
            boolean isExist = false;
            for (EventDefinitionCRFBean eventDef : eventDefCrfList){ 
            		  sessionBean = edcsInSession.get(i);
            		 
            		System.out.println("iter:           "+eventDef.getId()+            "--db:    "+eventDef.getSubmissionUrl()); 
            		System.out.println("edcsInSession:  "+sessionBean.getId()  + "--session:"+sessionBean.getSubmissionUrl()); 
            		System.out.println();
            	if(sessionBean.getSubmissionUrl().trim().equals("") || sessionBean.getSubmissionUrl().trim() ==null){
            		break;
            	}else{
                if (eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() != sessionBean.getId())){
                	v.addValidation("submissionUrl"+ order, Validator.SUBMISSION_URL_NOT_UNIQUE);
                	sed.setPopulated(true);
                   System.out.println("Duplicate ****************************");
                	isExist = true;
            	   break;
            	}else if(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId())){
                	System.out.println("Not Duplicate  ***********");
                	isExist = true;
            		break;
            	}
            	  }
            }
            	if(!isExist){ 
            		eventDefCrfList.add(sessionBean);
            	}
        }
    	return eventDefCrfList;
    }
    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
