package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.Date;

public class IRBStudyActionHistoryBean extends EntityBean {
    private int irbStudyActionHistoryId;
    private int studyId;
    private int irbProtocolActionTypeId;
    private String actionLabel;
    private Date effectiveDate;
    private int hrpoAction;
    private int versionNumber;
    private Date versionDate;
    private Date submissionToCdcIrb;
    private Date cdcIrbApproval;
    private Date notificationSentToSites;
    private Date enrollmentPauseDate;
    private Date enrollmentReStartedDate;
    private String reasonForEnrollmentPause;

    public int getIrbStudyActionHistoryId() {
        return irbStudyActionHistoryId;
    }

    public void setIrbStudyActionHistoryId(int irbStudyActionHistoryId) {
        this.irbStudyActionHistoryId = irbStudyActionHistoryId;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public int getIrbProtocolActionTypeId() {
        return irbProtocolActionTypeId;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public void setIrbProtocolActionTypeId(int irbProtocolActionTypeId) {
        this.irbProtocolActionTypeId = irbProtocolActionTypeId;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public int getHrpoAction() {
        return hrpoAction;
    }

    public void setHrpoAction(int hrpoAction) {
        this.hrpoAction = hrpoAction;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public Date getSubmissionToCdcIrb() {
        return submissionToCdcIrb;
    }

    public void setSubmissionToCdcIrb(Date submissionToCdcIrb) {
        this.submissionToCdcIrb = submissionToCdcIrb;
    }

    public Date getCdcIrbApproval() {
        return cdcIrbApproval;
    }

    public void setCdcIrbApproval(Date cdcIrbApproval) {
        this.cdcIrbApproval = cdcIrbApproval;
    }

    public Date getNotificationSentToSites() {
        return notificationSentToSites;
    }

    public void setNotificationSentToSites(Date notificationSentToSites) {
        this.notificationSentToSites = notificationSentToSites;
    }

    public Date getEnrollmentPauseDate() {
        return enrollmentPauseDate;
    }

    public void setEnrollmentPauseDate(Date enrollmentPauseDate) {
        this.enrollmentPauseDate = enrollmentPauseDate;
    }

    public Date getEnrollmentReStartedDate() {
        return enrollmentReStartedDate;
    }

    public void setEnrollmentReStartedDate(Date enrollmentReStartedDate) {
        this.enrollmentReStartedDate = enrollmentReStartedDate;
    }

    public String getReasonForEnrollmentPause() {
        return reasonForEnrollmentPause;
    }

    public void setReasonForEnrollmentPause(String reasonForEnrollmentPause) {
        this.reasonForEnrollmentPause = reasonForEnrollmentPause;
    }
}
