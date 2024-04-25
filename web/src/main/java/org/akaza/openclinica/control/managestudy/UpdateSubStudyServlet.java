/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.managestudy;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;
import static org.akaza.openclinica.core.util.ClassCastHelper.asHashMap;

import java.net.MalformedURLException;
import java.util.*;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.LaboratoryBean;
import org.akaza.openclinica.bean.managestudy.LabsForSiteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author jxu
 *
 * @version CVS: $Id: UpdateSubStudyServlet.java,v 1.7 2005/07/05 21:55:58 jxu
 *          Exp $
 */
public class UpdateSubStudyServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2416194262084852024L;
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String INPUT_START_DATE = "startDate";
    public static final String INPUT_VER_DATE = "protocolDateVerification";
    public static final String INPUT_END_DATE = "endDate";
    public static final String FWA_EXPIRATION_DATE = "fwaExpirationDate";
    public static StudyBean parentStudy;

    /**
     * *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.STUDY_LIST, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) session.getAttribute("newStudy");
        parentStudy = sdao.findByPK(study.getParentStudyId());
        //set mail notification for site based on selection for parent study
        study.setMailNotification(parentStudy.getMailNotification());
        study.setContactEmail(parentStudy.getContactEmail());

        logger.info("study from session:" + study.getName() + "\n" + study.getCreatedDate() + "\n");
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toStudyUpdateMembersList());
            LaboratoryDAO laboratoryDAO = new LaboratoryDAO(sm.getDataSource());
            List laboratories = laboratoryDAO.findAll();
            request.setAttribute("laboratories", laboratories);
            CountryDAO countryDAO = new CountryDAO(sm.getDataSource());
            List countries = countryDAO.findAll();
            request.setAttribute("countries", countries);
            FormProcessor fp = new FormProcessor(request);
            logger.info("start date:" + study.getDatePlannedEnd());
            if (study.getDatePlannedEnd() != null) {
                fp.addPresetValue(INPUT_END_DATE, local_df.format(study.getDatePlannedEnd()));
            }
            if (study.getDatePlannedStart() != null) {
                fp.addPresetValue(INPUT_START_DATE, local_df.format(study.getDatePlannedStart()));
            }
            if (study.getProtocolDateVerification() != null) {
                fp.addPresetValue(INPUT_VER_DATE, local_df.format(study.getProtocolDateVerification()));
            }
            if (study.getFwaExpirationDate() != null) {
                fp.addPresetValue(UpdateSubStudyServlet.FWA_EXPIRATION_DATE,
                        local_df.format(study.getFwaExpirationDate()));
            }

            setPresetValues(fp.getPresetValues());
            forwardPage(Page.UPDATE_SUB_STUDY);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirmStudy();
                // issue 3348
                // } else if ("submit".equalsIgnoreCase(action)) {
                // submitStudy();

            }
        }
    }

    /**
     * Validates the first section of study and save it into study bean * *
     *
     * @param request
     * @param response
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
        //v.addValidation("prinInvestigator", Validator.NO_BLANKS);
        String startDate = fp.getString(INPUT_START_DATE);
		if (!(startDate == null || startDate.trim().isEmpty())) {
            v.addValidation(INPUT_START_DATE, Validator.IS_A_DATE);
        }
        String endDate = fp.getString(INPUT_END_DATE);
		if (!(endDate == null || endDate.trim().isEmpty())) {
            v.addValidation(INPUT_END_DATE, Validator.IS_A_DATE);
        }
        //String contactEmail = fp.getString("facConEmail");
		//if (!(contactEmail == null || contactEmail.trim().isEmpty())) {
        //    v.addValidation("facConEmail", Validator.IS_A_EMAIL);
        //}
        String verDate = fp.getString(INPUT_VER_DATE);
        if (!(verDate == null || verDate.trim().isEmpty())) {
            v.addValidation(INPUT_VER_DATE, Validator.IS_A_DATE);
        }
        String fwaExpirationDate = fp.getString(FWA_EXPIRATION_DATE);
        // v.addValidation("statusId", Validator.IS_VALID_TERM);
        v.addValidation("secondProId", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facAddress1", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 100);
        v.addValidation("facAddress2", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 100);
        v.addValidation("facAddress3", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 100);
        v.addValidation("facAddress4", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 100);
        v.addValidation("facCity", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        v.addValidation("facState", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 20);
        v.addValidation("facZip", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        v.addValidation("facCountry", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
        //v.addValidation("facConName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        //v.addValidation("facConDegree", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        //v.addValidation("facConPhone", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
        //v.addValidation("facConEmail", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

        v.addValidation("facAddress1", Validator.NO_BLANKS);
        v.addValidation("subSite", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3);
        v.addValidation("contractNumber", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 20);
        v.addValidation("consortiumName", Validator.NO_BLANKS_SET, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);
        v.addValidation("consortiumName", Validator.NO_BLANKS);
        v.addValidation("locationType", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 14);
        v.addValidation("locationType", Validator.NO_BLANKS);
        v.addValidation("fwaInstitution", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 200);
        v.addValidation("fwaInstitution", Validator.NO_BLANKS);
        v.addValidation("fwaNumber", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);
        v.addValidation("fwaNumber", Validator.NO_BLANKS);
        v.addValidation("fwaExpirationDate", Validator.IS_A_DATE);
        v.addValidation("laboratoryId", Validator.NO_BLANKS);
        errors = v.validate();

        // >> tbh
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        ArrayList<StudyBean> allStudies = studyDAO.findAll();
        StudyBean oldStudy = (StudyBean) session.getAttribute("newStudy");
        for (StudyBean thisBean : allStudies) {
            if (fp.getString("uniqueProId").trim().equals(thisBean.getIdentifier()) && !thisBean.getIdentifier().equals(oldStudy.getIdentifier())) {
                Validator.addError(errors, "uniqueProId", resexception.getString("unique_protocol_id_existed"));
            }
        }
        // << tbh #3999 08/2009
        if (fp.getString("name").trim().length() > 100) {
            Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
        }
        if (fp.getString("uniqueProId").trim().length() > 30) {
            Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
        }
/*      if (fp.getString("description").trim().length() > 255) {
            Validator.addError(errors, "description", resexception.getString("maximum_lenght_brief_summary_255"));
        }
        if (fp.getString("prinInvestigator").trim().length() > 255) {
            Validator.addError(errors, "prinInvestigator", resexception.getString("maximum_lenght_principal_investigator_255"));
        }*/
        if (fp.getInt("expectedTotalEnrollment") <= 0) {
            Validator.addError(errors, "expectedTotalEnrollment", respage.getString("expected_total_enrollment_must_be_a_positive_number"));
        }
        if (fp.getString("facAddress1").trim().length() > 200) {
            Validator.addError(errors, "facAddress1", resexception.getString("maximum_length_facility_adddress_1_100"));
        }
        if (fp.getString("facAddress2").trim().length() > 200) {
            Validator.addError(errors, "facAddress2", resexception.getString("maximum_length_facility_adddress_2_100"));
        }
        if (fp.getString("facAddress3").trim().length() > 200) {
            Validator.addError(errors, "facAddress3", resexception.getString("maximum_length_facility_adddress_3_100"));
        }
        if (fp.getString("facAddress4").trim().length() > 200) {
            Validator.addError(errors, "facAddress4", resexception.getString("maximum_length_facility_adddress_4_100"));
        }
        if (fp.getStringArray("laboratoryId").isEmpty()) {
            Validator.addError(errors, "laboratoryId", respage.getString("laboratory_selection_shouldnt_be_empty"));
        }
        if (parentStudy.getStatus().equals(Status.LOCKED)) {
            if (fp.getInt("statusId") != Status.LOCKED.getId()) {
                Validator.addError(errors, "statusId", respage.getString("study_locked_site_status_locked"));
            }
        }
        // else if (parentStudy.getStatus().equals(Status.FROZEN)) {
        // if (fp.getInt("statusId") != Status.AVAILABLE.getId()) {
        // Validator.addError(errors, "statusId",
        // respage.getString("study_locked_site_status_frozen"));
        // }
        // }

        StudyBean study = createStudyBean();
        session.setAttribute("newStudy", study);

        if (errors.isEmpty()) {
            logger.info("no errors");
            // forwardPage(Page.CONFIRM_UPDATE_SUB_STUDY);
            submitStudy();
        } else {

        	StudyBean studyCheck = (StudyBean) session.getAttribute("newStudy");
            parentStudy = studyDAO.findByPK(studyCheck.getParentStudyId());
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
            String participateFormStatus = spvdao.findByHandleAndStudy(parentStudy.getId(), "participantPortal").getValue();
            request.setAttribute("participateFormStatus",participateFormStatus );
     
            logger.info("has validation errors");
            fp.addPresetValue(INPUT_START_DATE, startDate);
            fp.addPresetValue(INPUT_VER_DATE, verDate);
            fp.addPresetValue(INPUT_END_DATE, endDate);
            fp.addPresetValue(FWA_EXPIRATION_DATE, fwaExpirationDate);
            /*
            try {
                local_df.parse(fp.getString(INPUT_START_DATE));
                fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
            }
            // tbh 3946 07/2009
            try {
                local_df.parse(fp.getString(INPUT_VER_DATE));
                fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
            }
            // >> tbh
            try {
                local_df.parse(fp.getString(INPUT_END_DATE));
                fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
            } catch (ParseException pe) {
                fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
            }
			if (study.getFwaExpirationDate() != null) {
				fp.addPresetValue(UpdateSubStudyServlet.FWA_EXPIRATION_DATE,
						local_df.format(study.getFwaExpirationDate()));
			}
            */
            setPresetValues(fp.getPresetValues());
            request.setAttribute("formMessages", errors);
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toStudyUpdateMembersList());
            LaboratoryDAO laboratoryDAO = new LaboratoryDAO(sm.getDataSource());
            List laboratories = laboratoryDAO.findAll();
            request.setAttribute("laboratories", laboratories);
            CountryDAO countryDAO = new CountryDAO(sm.getDataSource());
            List countries = countryDAO.findAll();
            request.setAttribute("countries", countries);
            forwardPage(Page.UPDATE_SUB_STUDY);
        }

    }

    /**
     * Constructs study bean from request
     *
     * @param request
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

        String startDate = fp.getString("startDate");
		if(!(startDate == null || startDate.trim().isEmpty()))
            study.setDatePlannedStart(fp.getDate("startDate"));
        else
            study.setDatePlannedStart(null);
        String endDate = fp.getString("endDate");
		if(!(endDate == null || endDate.trim().isEmpty()))
            study.setDatePlannedEnd(fp.getDate("endDate"));
        else
            study.setDatePlannedEnd(null);
        String verDate = fp.getString(INPUT_VER_DATE);
		if(!(verDate == null || verDate.trim().isEmpty()))
            study.setProtocolDateVerification(fp.getDate(INPUT_VER_DATE));
        else
            study.setProtocolDateVerification(null);

        study.setFacilityCity(fp.getString("facCity"));
        study.setFacilityContactDegree(fp.getString("facConDrgree"));
        study.setFacilityName(fp.getString("facName"));
        study.setFacilityAddress1(fp.getString("facAddress1"));
        study.setFacilityAddress2(fp.getString("facAddress2"));
        study.setFacilityAddress3(fp.getString("facAddress3"));
        study.setFacilityAddress4(fp.getString("facAddress4"));
        study.setFacilityContactEmail(fp.getString("facConEmail"));
        study.setFacilityContactPhone(fp.getString("facConPhone"));
        //study.setFacilityContactName(fp.getString("facConName"));
        //study.setFacilityContactDegree(fp.getString("facConDegree"));
        study.setFacilityCountry(fp.getString("facCountry"));
        //study.setFacilityRecruitmentStatus(fp.getString("facRecStatus"));
        study.setFacilityState(fp.getString("facState"));
        study.setFacilityZip(fp.getString("facZip"));
        // study.setStatusId(fp.getInt("statusId"));
        study.setStatus(Status.get(fp.getInt("statusId")));

        study.setFwaInstitution(fp.getString("fwaInstitution"));
        study.setFwaNumber(fp.getString("fwaNumber"));
        study.setFwaExpirationDate(fp.getDate("fwaExpirationDate"));
        study.setSubSite(fp.getString("subSite"));
        study.setContractNumber(fp.getString("contractNumber"));
        study.setSiteType(fp.getString("siteType"));
        study.setConsortiumNames(fp.getStringArray("consortiumName"));
        study.setLocationType(fp.getString("locationType"));
        study.setActive(fp.getString("active").equals("on"));
        study.setLaboratoryIds(fp.getStringArray("laboratoryId"));
        // YW 10-12-2007 <<
        study.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
        study.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
        study.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
        study.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));
        // YW >>

        ArrayList<StudyParamsConfig> parameters = study.getStudyParameters();

        for (int i = 0; i < parameters.size(); i++) {
            StudyParamsConfig scg = parameters.get(i);
            String value = fp.getString(scg.getParameter().getHandle());
            logger.info("get value:" + value);
            scg.getValue().setStudyId(study.getId());
            scg.getValue().setParameter(scg.getParameter().getHandle());
            scg.getValue().setValue(value);
        }

        return study;
    }

    /**
     * Inserts the new study into databa *
     * @throws MalformedURLException *
     */
    private void submitStudy() throws MalformedURLException {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) session.getAttribute("newStudy");
        ArrayList<StudyParamsConfig> parameters = study.getStudyParameters();
        /*
         * logger.info("study bean to be updated:\n");
         * logger.info(study.getName()+ "\n" + study.getCreatedDate() + "\n" +
         * study.getIdentifier() + "\n" + study.getParentStudyId()+ "\n" +
         * study.getSummary()+ "\n" + study.getPrincipalInvestigator()+ "\n" +
         * study.getDatePlannedStart()+ "\n" + study.getDatePlannedEnd()+ "\n" +
         * study.getFacilityName()+ "\n" + study.getFacilityCity()+ "\n" +
         * study.getFacilityState()+ "\n" + study.getFacilityZip()+ "\n" +
         * study.getFacilityCountry()+ "\n" +
         * study.getFacilityRecruitmentStatus()+ "\n" +
         * study.getFacilityContactName()+ "\n" +
         * study.getFacilityContactEmail()+ "\n" +
         * study.getFacilityContactPhone()+ "\n" +
         * study.getFacilityContactDegree());
         */

        // study.setCreatedDate(new Date());
        study.setUpdatedDate(new Date());
        study.setUpdater(ub);
        sdao.update(study);

        //String[] labids = request.getParameterValues("laboratoryId");
        LabsForSiteDAO lfsDao = new LabsForSiteDAO(sm.getDataSource());
        List<String> labids = study.getLaboratoryIds();
        //study.setLaboratoryIds(labids);
        ArrayList<LabsForSiteBean> allfs = lfsDao.findBySiteId(study.getId());
        for (LabsForSiteBean lfs : allfs) {
            if (!labids.contains(String.valueOf(lfs.getLaboratory_id()))) {
                lfsDao.delete(lfs);
            }
        }
        for (String labid : labids) {
            int ilabid =  Integer.parseInt(labid);
            ArrayList<LabsForSiteBean> lfsForSiteAndLab = lfsDao.findBySiteIdAndLabId(study.getId(), ilabid);
            if ((lfsForSiteAndLab.size()>1)){
                //once we detected multiplicity of references, remove the redindant ones
                for (int i = 1; i < lfsForSiteAndLab.size(); i++) {
                    lfsDao.delete(lfsForSiteAndLab.get(i));
                }
            } else if (lfsForSiteAndLab.isEmpty()) {
                LabsForSiteBean eb = lfsDao.emptyBean();
                eb.setLaboratory_id(ilabid);
                eb.setSite_id(study.getId());
                lfsDao.create(eb);
            }
        }

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        for (int i = 0; i < parameters.size(); i++) {
            StudyParamsConfig config = parameters.get(i);
            StudyParameterValueBean spv = config.getValue();

            StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
            if (spv1.getId() > 0) {
                spv = spvdao.update(spv);
            } else {
                spv = spvdao.create(spv);
            }
            // spv = (StudyParameterValueBean)spvdao.update(config.getValue());

        }

        submitSiteEventDefinitions(study);

        //   session.removeAttribute("newStudy");
        //    session.removeAttribute("parentName");
        //    session.removeAttribute("definitions");
        //     session.removeAttribute("sdvOptions");
        addPageMessage(respage.getString("the_site_has_been_updated_succesfully"));
        String fromListSite = (String) session.getAttribute("fromListSite");
        if (fromListSite != null && fromListSite.equals("yes")) {
            //       session.removeAttribute("fromListSite");
            forwardPage(Page.SITE_LIST_SERVLET);
        } else {
            //      session.removeAttribute("fromListSite");
            forwardPage(Page.STUDY_LIST_SERVLET);
        }

    }

    private void submitSiteEventDefinitions(StudyBean site) throws MalformedURLException {
        FormProcessor fp = new FormProcessor(request);
        Validator v = new Validator(request);
        HashMap<String, Boolean> changes = new HashMap<String, Boolean>();
        HashMap<String, Boolean> changeStatus = asHashMap(session.getAttribute("changed"), String.class, Boolean.class);

        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<>();
        
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

        StudyBean parentStudyBean;
        if (site.getParentStudyId()==0){
        	parentStudyBean = site;
        }else{
            StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
            parentStudyBean = studyDAO.findByPK(site.getParentStudyId());
        }
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList <EventDefinitionCRFBean> eventDefCrfList = edcdao.findAllActiveSitesAndStudiesPerParentStudy(parentStudyBean.getId());

        ArrayList <EventDefinitionCRFBean> toBeCreatedEventDefBean = new ArrayList<>();
        ArrayList <EventDefinitionCRFBean> toBeUpdatedEventDefBean = new ArrayList<>();
        ArrayList <EventDefinitionCRFBean> edcsInSession = new ArrayList<EventDefinitionCRFBean>();
        seds = asArrayList(session.getAttribute("definitions"), StudyEventDefinitionBean.class);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
        String participateFormStatus = spvdao.findByHandleAndStudy(parentStudyBean.getId(), "participantPortal").getValue();
        if (participateFormStatus.equals("enabled")) 	baseUrl();
        request.setAttribute("participateFormStatus",participateFormStatus );

        for (StudyEventDefinitionBean sed : seds) {
            ArrayList<EventDefinitionCRFBean> edcs = asArrayList(sed.getCrfs(), EventDefinitionCRFBean.class);

            int start = 0;
            for (EventDefinitionCRFBean edcBean : edcs) {

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
                   
                    if(changeStatus!=null && changeStatus.get(sed.getId() + "-" + edcBean.getId())!=null){ 
                    	changed = changeStatus.get(sed.getId() + "-" + edcBean.getId());
                        edcBean.setSubmissionUrl(submissionUrl);
                    }


                    boolean isRequired = !(requiredCRF == null || requiredCRF.trim().isEmpty()) && "yes".equalsIgnoreCase(requiredCRF.trim()) ? true : false;
                    boolean isDouble = !(doubleEntry == null || doubleEntry.trim().isEmpty()) && "yes".equalsIgnoreCase(doubleEntry.trim()) ? true : false;
                    boolean hasPassword = !(electronicSignature == null || electronicSignature.trim().isEmpty()) && "yes".equalsIgnoreCase(electronicSignature.trim()) ? true : false;
                    boolean isHide = !(hideCRF == null || hideCRF.trim().isEmpty()) && "yes".equalsIgnoreCase(hideCRF.trim()) ? true : false;

                    logger.debug("crf name : {}", edcBean.getCrfName());
                    logger.debug("submissionUrl: {}", submissionUrl);
                    
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
                        if (!submissionUrl.equals(edcBean.getSubmissionUrl())) {
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
                        
                        
                        if (changed) {
                            edcBean.setUpdater(ub);
                            edcBean.setUpdatedDate(new Date());
                            logger.debug("update for site");
                            toBeUpdatedEventDefBean.add(edcBean);
                         //   edcdao.update(edcBean);
                        }
                    } else {
                        // only if definition-crf has been modified, will it be
                        // saved for the site
                        int defaultId = defaultVersionId > 0 ? defaultVersionId : edcBean.getDefaultVersionId();
                        int dbDefaultVersionId = edcBean.getDefaultVersionId();
                        changed = changed || (defaultId != dbDefaultVersionId);
                        changed = changed || (isRequired != edcBean.isRequiredCRF());
                        changed = changed || (isDouble != edcBean.isDoubleEntry());
                        changed = changed || (hasPassword != edcBean.isElectronicSignature());
                        changed = changed || (isHide != edcBean.isHideCrf());
                        changed = changed || (!submissionUrl.equals(""));                        
                        changed = changed || (selectedVersionIdListSize > 0 && selectedVersionIdListSize != edcBean.getVersions().size());
                        changed = changed || (sdvId > 0 && sdvId != edcBean.getSourceDataVerification().getCode());

                        if (changed) {
                            CRFVersionBean defaultVersion = cvdao.findByPK(defaultId);
                            edcBean.setDefaultVersionId(defaultId);
                            edcBean.setDefaultVersionName(defaultVersion.getName());
                            edcBean.setRequiredCRF(isRequired);
                            edcBean.setDoubleEntry(isDouble);
                            edcBean.setElectronicSignature(hasPassword);
                            edcBean.setHideCrf(isHide);
                            edcBean.setSubmissionUrl(submissionUrl);

                            if (selectedVersionIdListSize > 0 && selectedVersionIdListSize != edcBean.getVersions().size()) {
                                String[] ids = selectedVersionIds.split(",");
                                ArrayList<Integer> idList = new ArrayList<Integer>();
                                for (String id : ids) {
                                    idList.add(Integer.valueOf(id));
                                }
                                edcBean.setSelectedVersionIdList(idList);
                                edcBean.setSelectedVersionIds(selectedVersionIds);
                            }
                            if (sdvId > 0 && sdvId != edcBean.getSourceDataVerification().getCode()) {
                                edcBean.setSourceDataVerification(SourceDataVerification.getByCode(sdvId));
                            }
                       //     edcBean.setParentId(edcBean.getId());
                            edcBean.setStudyId(site.getId());
                            edcBean.setUpdater(ub);
                            edcBean.setUpdatedDate(new Date());
                            logger.debug("create for the site");
                            toBeCreatedEventDefBean.add(edcBean);
                   //         edcdao.create(edcBean);
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

        if (!errors.isEmpty()) {
            logger.info("has errors");
            StudyBean study = createStudyBean();
            session.setAttribute("newStudy", study);
            request.setAttribute("formMessages", errors);
            session.setAttribute("changed", changes);
            forwardPage(Page.UPDATE_SUB_STUDY);
        }else{  
            for (EventDefinitionCRFBean toBeCreated: toBeCreatedEventDefBean){
                  toBeCreated.setParentId(toBeCreated.getId());
            	edcdao.create(toBeCreated);
            }
            for (EventDefinitionCRFBean toBeUpdated: toBeUpdatedEventDefBean){
            	edcdao.update(toBeUpdated);
            }
            

        }
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
            v.addValidation("submissionUrl" + order, Validator.NO_SPACES_ALLOWED);
            EventDefinitionCRFBean sessionBean = null;
            boolean isExist = false;
            for (EventDefinitionCRFBean eventDef : eventDefCrfList) {
                sessionBean = edcsInSession.get(i);
                logger.debug("iter:           {} --db: {}", eventDef.getId(), eventDef.getSubmissionUrl());
                logger.debug("edcsInSession:  {}--session: {}", sessionBean.getId(), sessionBean.getSubmissionUrl());
            	if(sessionBean.getSubmissionUrl() == null || sessionBean.getSubmissionUrl().trim().equals("")) {
            	    break;
            	}else{
                    if ((eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() != sessionBean.getId()))
                            ||(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId())&& eventDef.getId() == 0)) {
                        v.addValidation("submissionUrl" + order, Validator.SUBMISSION_URL_NOT_UNIQUE);
                        sed.setPopulated(true);
                        logger.debug("Duplicate *****************");
                        isExist = true;
                       break;
                    }else if(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId())) {
                        logger.debug("Not Duplicate **********");
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


}
