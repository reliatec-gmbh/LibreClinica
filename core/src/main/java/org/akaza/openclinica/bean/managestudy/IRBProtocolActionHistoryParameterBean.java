package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

public class IRBProtocolActionHistoryParameterBean extends EntityBean {
    private static final long serialVersionUID = -5198523403410118479L;

    private int irbProtocolActionHistoryParameterId;
    private String action;
    private boolean cdcIrbProtocolVersionDate;
    private boolean version;
    private boolean siteSubmittedToLocalIrb;
    private boolean localIrbApproval;
    private boolean siteSendsDocsToCrb;
    private boolean packageSentToCdcIrb;
    private boolean cdcApprovalAcknowledgment;
    private boolean enrollmentPauseDate;
    private boolean enrollmentReStartedDate;
    private boolean reasonForEnrollmentPause;


    public int getIrbProtocolActionHistoryParameterId() {
        return irbProtocolActionHistoryParameterId;
    }

    public void setIrbProtocolActionHistoryParameterId(int irbProtocolActionHistoryParameterId) {
        this.irbProtocolActionHistoryParameterId = irbProtocolActionHistoryParameterId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean getCdcIrbProtocolVersionDate() {
        return cdcIrbProtocolVersionDate;
    }

    public void setCdcIrbProtocolVersionDate(boolean cdcIrbProtocolVersionDate) {
        this.cdcIrbProtocolVersionDate = cdcIrbProtocolVersionDate;
    }

    public boolean getVersion() {
        return version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }

    public boolean getSiteSubmittedToLocalIrb() {
        return siteSubmittedToLocalIrb;
    }

    public void setSiteSubmittedToLocalIrb(boolean siteSubmittedToLocalIrb) {
        this.siteSubmittedToLocalIrb = siteSubmittedToLocalIrb;
    }

    public boolean getLocalIrbApproval() {
        return localIrbApproval;
    }

    public void setLocalIrbApproval(boolean localIrbApproval) {
        this.localIrbApproval = localIrbApproval;
    }

    public boolean getSiteSendsDocsToCrb() {
        return siteSendsDocsToCrb;
    }

    public void setSiteSendsDocsToCrb(boolean siteSendsDocsToCrb) {
        this.siteSendsDocsToCrb = siteSendsDocsToCrb;
    }

    public boolean getPackageSentToCdcIrb() {
        return packageSentToCdcIrb;
    }

    public void setPackageSentToCdcIrb(boolean packageSentToCdcIrb) {
        this.packageSentToCdcIrb = packageSentToCdcIrb;
    }

    public boolean getCdcApprovalAcknowledgment() {
        return cdcApprovalAcknowledgment;
    }

    public void setCdcApprovalAcknowledgment(boolean cdcApprovalAcknowledgment) {
        this.cdcApprovalAcknowledgment = cdcApprovalAcknowledgment;
    }

    public boolean getEnrollmentPauseDate() {
        return enrollmentPauseDate;
    }

    public void setEnrollmentPauseDate(boolean enrollmentPauseDate) {
        this.enrollmentPauseDate = enrollmentPauseDate;
    }

    public boolean getEnrollmentReStartedDate() {
        return enrollmentReStartedDate;
    }

    public void setEnrollmentReStartedDate(boolean enrollmentReStartedDate) {
        this.enrollmentReStartedDate = enrollmentReStartedDate;
    }

    public boolean getReasonForEnrollmentPause() {
        return reasonForEnrollmentPause;
    }

    public void setReasonForEnrollmentPause(boolean reasonForEnrollmentPause) {
        this.reasonForEnrollmentPause = reasonForEnrollmentPause;
    }

}

