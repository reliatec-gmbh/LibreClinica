package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.dao.managestudy.IRBProtocolActionHistoryDAO;
import org.akaza.openclinica.dao.managestudy.IRBProtocolActionHistoryParameterDAO;
import org.akaza.openclinica.dao.managestudy.IRBSiteDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class IRBSiteServlet extends SecureController {
    private IRBSiteDAO irbSiteDAO;
    private ArrayList<IRBProtocolActionHistoryParameterBean> irbProtocolActionHistoryParameter;
    private static final String INPUT_VERSION_NUMBER = "version_number";
    private static final String INPUT_SITE_RELIES_ON_CDC_IRB = "site_relies_on_cdc_irb";
    private static final String INPUT_IS_1572  = "is_1572";
    private static final String INPUT_PROTOCOL_VERSION_DATE  = "cdc_irb_protocol_version_date";
    private static final String INPUT_LOCAL_IRB_APPROVED_PROTOCOL  = "local_irb_approved_protocol";
    private static final String INPUT_CDC_RECEIVED_LOCAL_DOCUMENTS  = "cdc_received_local_documents";
    private static final String INPUT_SITE_CONSENT_PACKAGE_SENT_TO_CDC_IRB = "site_consent_package_send_to_cdc_irb";
    private static final String INPUT_INITIAL_CDC_IRB_APPROVAL  = "initial_cdc_irb_approval";
    private static final String INPUT_CRB_APPROVAL_TO_ENROLL  = "crb_approval_to_enroll";
    private static final String INPUT_IRB_APPROVAL  = "irb_approval";
    private static final String INPUT_EXPIRATION_DATE  = "expiration_date";
    private static final String INPUT_ACTIVE  = "active";
    private static final String INPUT_COMMENTS  = "comments";
    public static final String INPUT_H_PROTOCOL_ACTION_HISTORY_ID = "h_protocol_action_history_id";
    private static final String INPUT_H_PROTOCOL_ACTION_TYPE_ID  = "h_protocol_action_type_id";
    private static final String INPUT_H_VERSION_DATE  = "h_version_date";
    private static final String INPUT_H_VERSION_NUMBER  = "h_version_number";
    private static final String INPUT_H_SITE_SUBMITTED_TO_LOCAL_IRB  = "h_site_submitted_to_local_irb";
    private static final String INPUT_H_TO_LOCAL_IRB  = "h_local_irb_approval";
    private static final String INPUT_H_RECEIVED_DOCS_FROM_SITES  = "h_received_docs_from_sites";
    private static final String INPUT_H_PACKAGE_SENT_TO_CDC_IRB  = "h_package_sent_to_cdc_irb";
    private static final String INPUT_H_CDC_APPROVAL  = "h_cdc_approval";
    private static final String INPUT_H_ENROLLMENT_PAUSE_DATE  = "h_enrollment_pause_date";
    private static final String INPUT_H_ENROLLMENT_RESTARTED_DATE  = "h_enrollment_restarted_date";
    private static final String INPUT_H_REASON_FOR_ENROLLMENT_PAUSED  = "h_reason_for_enrollment_paused";

    private IRBProtocolActionHistoryDAO protocolActionHistoryDAO;

    private IRBProtocolActionHistoryParameterDAO irbProtocolActionHistoryParameterDAO;

    private IRBSiteDAO getIRBSiteDAO() {
        if(irbSiteDAO==null) irbSiteDAO = new IRBSiteDAO(sm.getDataSource());

        return irbSiteDAO;
    }

    private IRBProtocolActionHistoryDAO getProtocolActionHistoryDAO() {
        if(protocolActionHistoryDAO ==null)
            protocolActionHistoryDAO = new IRBProtocolActionHistoryDAO(sm.getDataSource());

        return protocolActionHistoryDAO;
    }

    private IRBProtocolActionHistoryParameterDAO getIrbProtocolActionHistoryParameter() {
        if(irbProtocolActionHistoryParameterDAO == null)
            irbProtocolActionHistoryParameterDAO = new IRBProtocolActionHistoryParameterDAO(sm.getDataSource());

        return irbProtocolActionHistoryParameterDAO;
    }
    private void populateFormProcessorFromSiteBean(FormProcessor fp, IRBSiteBean irbSiteBean) {
        Locale locale = LocaleResolver.getLocale(request);
        SimpleDateFormat sdf= new SimpleDateFormat("dd-MMM-yyyy", locale);

        fp.addPresetValue("studyId", irbSiteBean.getSiteId());
        fp.addPresetValue(INPUT_VERSION_NUMBER, irbSiteBean.getVersionNumber());
        fp.addPresetValue(INPUT_SITE_RELIES_ON_CDC_IRB, irbSiteBean.isSiteReliesOnCdcIrb());
        fp.addPresetValue(INPUT_IS_1572, irbSiteBean.isIs1572());
        fp.addPresetValue(INPUT_PROTOCOL_VERSION_DATE,
                irbSiteBean.getCdcIrbProtocolVersionDate()!=null?
                        sdf.format(irbSiteBean.getCdcIrbProtocolVersionDate()): "");
        fp.addPresetValue(INPUT_LOCAL_IRB_APPROVED_PROTOCOL,
                irbSiteBean.getLocalIrbApprovedProtocol()!=null?
                        sdf.format(irbSiteBean.getLocalIrbApprovedProtocol()): "");
        fp.addPresetValue(INPUT_CDC_RECEIVED_LOCAL_DOCUMENTS,
                irbSiteBean.getCdcReceivedLocalDocuments()!=null?
                        sdf.format(irbSiteBean.getCdcReceivedLocalDocuments()): "");
        fp.addPresetValue(INPUT_SITE_CONSENT_PACKAGE_SENT_TO_CDC_IRB,
                irbSiteBean.getSiteConsentPackageSendToCdcIrb()!=null?
                        sdf.format(irbSiteBean.getSiteConsentPackageSendToCdcIrb()): "");
        fp.addPresetValue(INPUT_INITIAL_CDC_IRB_APPROVAL,
                irbSiteBean.getInitialCdcIrbApproval()!=null?
                        sdf.format(irbSiteBean.getInitialCdcIrbApproval()): "");
        fp.addPresetValue(INPUT_CRB_APPROVAL_TO_ENROLL,
                irbSiteBean.getCrbApprovalToEnroll()!=null?
                        sdf.format(irbSiteBean.getCrbApprovalToEnroll()): "");
        fp.addPresetValue(INPUT_IRB_APPROVAL,
                irbSiteBean.getIrbApproval()!=null?
                        sdf.format(irbSiteBean.getIrbApproval()): "");
        fp.addPresetValue(INPUT_EXPIRATION_DATE,
                irbSiteBean.getExpirationDate()!=null?
                        sdf.format(irbSiteBean.getExpirationDate()): "");
        fp.addPresetValue(INPUT_ACTIVE, irbSiteBean.isActive());
        fp.addPresetValue(INPUT_COMMENTS, irbSiteBean.getComments());
    }


    private IRBProtocolActionHistoryBean createOrUpdateProtocolActionHistory() throws NumberFormatException, OpenClinicaException {
        String stringProtocolActionHistoryId =
                request.getParameter(INPUT_H_PROTOCOL_ACTION_HISTORY_ID);
        int studyProtocolActionHistoryId = -1;
        try {
            studyProtocolActionHistoryId = Integer.parseInt(stringProtocolActionHistoryId);
        }
        catch (NumberFormatException ex) {
            //Don't do anything.
        }


        IRBProtocolActionHistoryBean irbProtocolActionHistoryBean = null;
        if(studyProtocolActionHistoryId>0) {
            irbProtocolActionHistoryBean = (IRBProtocolActionHistoryBean) getProtocolActionHistoryDAO()
                    .findByPK(studyProtocolActionHistoryId);
        }
        if(irbProtocolActionHistoryBean == null)
            irbProtocolActionHistoryBean = new IRBProtocolActionHistoryBean();

        String stringSiteId = request.getParameter("siteId");
        irbProtocolActionHistoryBean.setIrbSiteId(Integer.parseInt(stringSiteId));
        irbProtocolActionHistoryBean.setIrbProtocolActionTypeId(
                intValueOrZero(INPUT_H_PROTOCOL_ACTION_TYPE_ID));
        irbProtocolActionHistoryBean.setVersionDate(dateValueOrNull(INPUT_H_VERSION_DATE));
        irbProtocolActionHistoryBean.setVersionNumber(intValueOrZero(INPUT_H_VERSION_NUMBER));
        irbProtocolActionHistoryBean.setSiteSubmittedToLocalIrb(dateValueOrNull(INPUT_H_SITE_SUBMITTED_TO_LOCAL_IRB));
        irbProtocolActionHistoryBean.setLocalIrbApproval(dateValueOrNull(INPUT_H_TO_LOCAL_IRB));
        irbProtocolActionHistoryBean.setReceivedDocsFromSites(dateValueOrNull(INPUT_H_RECEIVED_DOCS_FROM_SITES));
        irbProtocolActionHistoryBean.setPackageSentToCdcIrb(dateValueOrNull(INPUT_H_PACKAGE_SENT_TO_CDC_IRB));
        irbProtocolActionHistoryBean.setCdcApproval(dateValueOrNull(INPUT_H_CDC_APPROVAL));
        irbProtocolActionHistoryBean.setEnrollmentPauseDate(dateValueOrNull(INPUT_H_ENROLLMENT_PAUSE_DATE));
        irbProtocolActionHistoryBean.setEnrollmentRestartedDate(dateValueOrNull(INPUT_H_ENROLLMENT_RESTARTED_DATE));
        irbProtocolActionHistoryBean.setReasonForEnrollmentPaused(INPUT_H_REASON_FOR_ENROLLMENT_PAUSED);

        if(irbProtocolActionHistoryBean.getIrbProtocolActionHistoryId()<1)
            irbProtocolActionHistoryBean =
                    getProtocolActionHistoryDAO().create(irbProtocolActionHistoryBean);
        else
            irbProtocolActionHistoryBean =
                    getProtocolActionHistoryDAO().update(irbProtocolActionHistoryBean);

        return irbProtocolActionHistoryBean;
    }

    private HashMap<String, ArrayList<String>> validateIrbSiteRequest() {
        FormDiscrepancyNotes discNotes;

        discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        if (discNotes == null) {
            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        }
        DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);

        v.addValidation(INPUT_VERSION_NUMBER, Validator.IS_A_NUMBER);
        v.addValidation(INPUT_LOCAL_IRB_APPROVED_PROTOCOL, Validator.IS_A_DATE);
        v.addValidation(INPUT_PROTOCOL_VERSION_DATE, Validator.IS_A_DATE);
        v.addValidation(INPUT_CDC_RECEIVED_LOCAL_DOCUMENTS, Validator.IS_A_DATE);
        v.addValidation(INPUT_SITE_CONSENT_PACKAGE_SENT_TO_CDC_IRB, Validator.IS_A_DATE);
        v.addValidation(INPUT_INITIAL_CDC_IRB_APPROVAL, Validator.IS_A_DATE);
        v.addValidation(INPUT_CRB_APPROVAL_TO_ENROLL, Validator.IS_A_DATE);
        v.addValidation(INPUT_IRB_APPROVAL, Validator.IS_A_DATE);
        v.addValidation(INPUT_EXPIRATION_DATE, Validator.IS_A_DATE);

        return v.validate();
    }

    private HashMap<String, ArrayList<String>> validateProtocolActionHistory() {
        FormDiscrepancyNotes discNotes;

        discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        if (discNotes == null) {
            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        }
        DiscrepancyValidator v = new DiscrepancyValidator(request, discNotes);

        String stringProtocolActionHistoryType =
                request.getParameter(INPUT_H_PROTOCOL_ACTION_TYPE_ID);
        int protocolActionHistoryType = -1;
        try {
            protocolActionHistoryType = Integer.parseInt(stringProtocolActionHistoryType);
        }
        catch (NumberFormatException ex) {
            //Don't do anything.
        }
        protocolActionHistoryType--;

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getCdcIrbProtocolVersionDate()) {
            v.addValidation(INPUT_H_VERSION_DATE, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_VERSION_DATE, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_VERSION_DATE, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getVersion()) {
            v.addValidation(INPUT_H_VERSION_NUMBER, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_VERSION_NUMBER, Validator.LENGTH_NUMERIC_COMPARISON,
                    NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 4);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getSiteSubmittedToLocalIrb()) {
            v.addValidation(INPUT_H_SITE_SUBMITTED_TO_LOCAL_IRB, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_SITE_SUBMITTED_TO_LOCAL_IRB, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_SITE_SUBMITTED_TO_LOCAL_IRB, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getLocalIrbApproval()) {
            v.addValidation(INPUT_H_TO_LOCAL_IRB, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_TO_LOCAL_IRB, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_TO_LOCAL_IRB, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getSiteSendsDocsToCrb()) {
            v.addValidation(INPUT_H_RECEIVED_DOCS_FROM_SITES, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_RECEIVED_DOCS_FROM_SITES, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_RECEIVED_DOCS_FROM_SITES, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getPackageSentToCdcIrb()) {
            v.addValidation(INPUT_H_PACKAGE_SENT_TO_CDC_IRB, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_PACKAGE_SENT_TO_CDC_IRB, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_PACKAGE_SENT_TO_CDC_IRB, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getCdcApprovalAcknowledgment()) {
            v.addValidation(INPUT_H_CDC_APPROVAL, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_CDC_APPROVAL, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_CDC_APPROVAL, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getEnrollmentPauseDate()) {
            v.addValidation(INPUT_H_ENROLLMENT_PAUSE_DATE, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_ENROLLMENT_PAUSE_DATE, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_ENROLLMENT_PAUSE_DATE, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getEnrollmentReStartedDate()) {
            v.addValidation(INPUT_H_ENROLLMENT_RESTARTED_DATE, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_ENROLLMENT_RESTARTED_DATE, Validator.IS_A_DATE);
            v.addValidation(INPUT_H_ENROLLMENT_RESTARTED_DATE, Validator.DATE_IN_PAST);
        }

        if (irbProtocolActionHistoryParameter.get(protocolActionHistoryType).getReasonForEnrollmentPause()) {
            v.addValidation(INPUT_H_REASON_FOR_ENROLLMENT_PAUSED, Validator.NO_BLANKS);
            v.addValidation(INPUT_H_REASON_FOR_ENROLLMENT_PAUSED, Validator.LENGTH_NUMERIC_COMPARISON,
                    NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 120);
        }

        return v.validate();
    }

    private ArrayList<HashMap<String, String>> getProtocolActionHistory(int siteId) {
        Locale locale = LocaleResolver.getLocale(request);
        SimpleDateFormat sdf= new SimpleDateFormat("dd-MMM-yyyy", locale);

        ArrayList<IRBProtocolActionHistoryBean> protocolActionHistory =
                getProtocolActionHistoryDAO().findBySiteId(siteId);
        ArrayList<HashMap<String, String>> protocolActionHistoryFormatted = new ArrayList<>();

        for(IRBProtocolActionHistoryBean sb: protocolActionHistory) {
            HashMap<String, String> row = new HashMap<>();
            row.put("id", Integer.toString(sb.getIrbProtocolActionHistoryId()));
            row.put("actionTypeId", Integer.toString(sb.getIrbProtocolActionTypeId()));
            row.put("action", sb.getAction());
            row.put("siteId", Integer.toString(sb.getIrbSiteId()));
            row.put("versionDate", sb.getVersionDate()!=null? sdf.format(sb.getVersionDate()):"");
            row.put("versionNumber", Integer.toString(sb.getVersionNumber()));
            row.put("siteSubmittedToLocalIrb",
                    sb.getSiteSubmittedToLocalIrb()!=null?
                    sdf.format(sb.getSiteSubmittedToLocalIrb()):"");
            row.put("localIrbApproval",
                    sb.getLocalIrbApproval()!=null?
                    sdf.format(sb.getLocalIrbApproval()):"");
            row.put("receivedDocsFromSites",
                    sb.getReceivedDocsFromSites()!=null?
                    sdf.format(sb.getReceivedDocsFromSites()):"");
            row.put("packageSentToCdcIrb",
                    sb.getPackageSentToCdcIrb()!=null?
                    sdf.format(sb.getPackageSentToCdcIrb()):"");
            row.put("cdcApproval",
                    sb.getCdcApproval()!=null?
                    sdf.format(sb.getCdcApproval()):"");
            row.put("enrollmentPauseDate",
                    sb.getEnrollmentPauseDate()!=null?
                    sdf.format(sb.getEnrollmentPauseDate()):"");
            row.put("enrollmentRestartedDate",
                    sb.getEnrollmentRestartedDate()!=null?
                    sdf.format(sb.getEnrollmentRestartedDate()):"");
            row.put("reasonForEnrollmentPaused", sb.getReasonForEnrollmentPaused());

            protocolActionHistoryFormatted.add(row);
        }

        return protocolActionHistoryFormatted;
    }

    @Override
    protected void processRequest() throws Exception {
        String stringSiteId = request.getParameter("siteId");
        if(stringSiteId==null || stringSiteId.isEmpty()) {
            addPageMessage("Invalid site id");
            forwardPage(Page.VIEW_STUDY);
            return;
        }
        int siteId = Integer.parseInt(stringSiteId);

        FormProcessor fp = new FormProcessor(request);
        Locale locale = LocaleResolver.getLocale(request);
        SimpleDateFormat sdf= new SimpleDateFormat("dd-MMM-yyyy", locale);

        irbProtocolActionHistoryParameter = getIrbProtocolActionHistoryParameter().findAll();

        request.setAttribute("openEditorOnStartup", false);
        request.setAttribute("protocolActionHistoryParameter", irbProtocolActionHistoryParameter);
        request.setAttribute("protocolActionHistory", getProtocolActionHistory(siteId));
        request.setAttribute("siteId", siteId);

        IRBSiteBean irbSiteBean;

        if(request.getMethod().compareToIgnoreCase("POST")==0) {
            if(request.getParameter("action") != null &&
                    (request.getParameter("action").compareToIgnoreCase("saveProtocolActionEditor")==0)) {
                HashMap<String, ArrayList<String>> errors = validateProtocolActionHistory();

                if (!errors.isEmpty()) {
                    setInputMessages(errors);
                    fp.clearPresetValues();
                    setPresetValuesFromRequest(fp);
                    request.setAttribute("openEditorOnStartup", true);
                    //load current IrbStudyBean, this step is necessary because
                    //the form for irb study must be also populated
                    irbSiteBean = getIRBSiteDAO().findBySiteId(siteId);
                    if(irbSiteBean==null) irbSiteBean = getIRBSiteDAO().emptyBean();
                    populateFormProcessorFromSiteBean(fp, irbSiteBean);

                    setPresetValues(fp.getPresetValues());

                    // addPageMessage("Validation errors were found when saving the IRB Study data");
                    forwardPage(Page.IRB_SITE);
                    return;
                }

                createOrUpdateProtocolActionHistory();
                request.setAttribute("protocolActionHistory", getProtocolActionHistory(siteId));
                addPageMessage("Protocol action history saved");
                forwardPage(Page.IRB_SITE);
                return;
            }
            else {
                HashMap<String, ArrayList<String>> errors = validateIrbSiteRequest();

                if (!errors.isEmpty()) {
                    setInputMessages(errors);
                    fp.clearPresetValues();
                    setPresetValuesFromRequest(fp);
                    setPresetValues(fp.getPresetValues());

                    addPageMessage("Validation errors were found when saving the IRB Site data");
                    forwardPage(Page.IRB_SITE);
                    return;
                }
                irbSiteBean = createOrUpdateIRBSiteBean();
                //forwardPage(Page.MANAGE_STUDY_MODULE);
                addPageMessage("IRB Site approval saved");
                forwardPage(Page.STUDY_LIST_SERVLET);
                return;
            }
        }

        irbSiteBean = getIRBSiteDAO().findBySiteId(siteId);

        request.setAttribute("irbSiteBean", irbSiteBean);
        populateFormProcessorFromSiteBean(fp, irbSiteBean);

        setPresetValues(fp.getPresetValues());

        forwardPage(Page.IRB_SITE);
    }

    private IRBSiteBean createOrUpdateIRBSiteBean() throws NumberFormatException, OpenClinicaException {
        String stringSiteId = request.getParameter("siteId");
        int siteId = Integer.parseInt(stringSiteId);

        IRBSiteBean irbSiteBean = getIRBSiteDAO().findBySiteId(siteId);
        if(irbSiteBean == null) irbSiteBean = new IRBSiteBean();
        if(irbSiteBean.getIrbSiteId()<1) irbSiteBean.setSiteId(siteId);

        irbSiteBean.setVersionNumber(intValueOrZero(INPUT_VERSION_NUMBER));
        irbSiteBean.setSiteReliesOnCdcIrb(intValueOrZero(INPUT_SITE_RELIES_ON_CDC_IRB)==1);
        irbSiteBean.setIs1572(intValueOrZero(INPUT_IS_1572)==1);
        irbSiteBean.setCdcIrbProtocolVersionDate(dateValueOrNull(INPUT_PROTOCOL_VERSION_DATE));
        irbSiteBean.setLocalIrbApprovedProtocol(dateValueOrNull(INPUT_LOCAL_IRB_APPROVED_PROTOCOL));
        irbSiteBean.setCdcReceivedLocalDocuments(dateValueOrNull(INPUT_CDC_RECEIVED_LOCAL_DOCUMENTS));
        irbSiteBean.setSiteConsentPackageSendToCdcIrb(dateValueOrNull(INPUT_SITE_CONSENT_PACKAGE_SENT_TO_CDC_IRB));
        irbSiteBean.setInitialCdcIrbApproval(dateValueOrNull(INPUT_INITIAL_CDC_IRB_APPROVAL));
        irbSiteBean.setCrbApprovalToEnroll(dateValueOrNull(INPUT_CRB_APPROVAL_TO_ENROLL));
        irbSiteBean.setIrbApproval(dateValueOrNull(INPUT_IRB_APPROVAL));
        irbSiteBean.setExpirationDate(dateValueOrNull(INPUT_EXPIRATION_DATE));
        irbSiteBean.setActive(intValueOrZero(INPUT_ACTIVE)==1);
        irbSiteBean.setComments(request.getParameter(INPUT_COMMENTS));

        if(irbSiteBean.getIrbSiteId()<1)
            irbSiteBean = getIRBSiteDAO().create(irbSiteBean);
        else
            irbSiteBean = getIRBSiteDAO().update(irbSiteBean);
        addPageMessage("IRB Site saved");

        return irbSiteBean;
    }


    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }

    private Date dateValueOrNull(String field) {
        Date retval = null;
        if(request.getParameter(field) == null) return null;

        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        try {
            retval = dateTimeFormatter.parse(request.getParameter(field));
        }
        catch (ParseException ex) {
            //ignore the error, we'll return null at the next statement
        }

        return retval;
    }

    private int intValueOrZero(String field) {
        int retval = 0;
        if(request.getParameter(field)!= null && !request.getParameter(field).isEmpty()) {
            retval = Integer.parseInt(request.getParameter(field));
        }

        return retval;
    }
}
