package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

public class IRBStudyActionHistoryParameterBean extends EntityBean  {
    private int irbStudyActionHistoryParameterId;
    private String action;
    private boolean effectiveDate;
    private boolean hrpoAction;
    private boolean versionNumber;
    private boolean versionDate;
    private boolean submissionToCdcIrb;
    private boolean cdcIrbApproval;
    private boolean notificationSentToSites;
    private boolean enrollmentPauseDate;
    private boolean enrollmentReStartedDate;
    private boolean reasonForEnrollmentPause;

    public int getIrbStudyActionHistoryParameterId() {
        return irbStudyActionHistoryParameterId;
    }

    public void setIrbStudyActionHistoryParameterId(int irbStudyActionHistoryParameterId) {
        this.irbStudyActionHistoryParameterId = irbStudyActionHistoryParameterId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(boolean effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public boolean getHrpoAction() {
        return hrpoAction;
    }

    public void setHrpoAction(boolean hrpoAction) {
        this.hrpoAction = hrpoAction;
    }

    public boolean getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(boolean versionNumber) {
        this.versionNumber = versionNumber;
    }

    public boolean getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(boolean versionDate) {
        this.versionDate = versionDate;
    }

    public boolean getSubmissionToCdcIrb() {
        return submissionToCdcIrb;
    }

    public void setSubmissionToCdcIrb(boolean submissionToCdcIrb) {
        this.submissionToCdcIrb = submissionToCdcIrb;
    }

    public boolean getCdcIrbApproval() {
        return cdcIrbApproval;
    }

    public void setCdcIrbApproval(boolean cdcIrbApproval) {
        this.cdcIrbApproval = cdcIrbApproval;
    }

    public boolean getNotificationSentToSites() {
        return notificationSentToSites;
    }

    public void setNotificationSentToSites(boolean notificationSentToSites) {
        this.notificationSentToSites = notificationSentToSites;
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
