package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.Date;

public class IRBStudyBean extends EntityBean {
    private static final long serialVersionUID = -1708289501333878479L;
    private int irbStudyId;
    private int studyId;
    private String cdcIrbProtocolNumber;
    private Date version1ProtocolDate;
    private String protocolOfficer;
    private Date submittedCdcIrb;
    private Date approvalByCdcIrb;
    private Date cdcIrbExpirationDate;

    public int getIrbStudyId() {
        return irbStudyId;
    }

    public void setIrbStudyId(int irbStudyId) {
        this.irbStudyId = irbStudyId;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public String getCdcIrbProtocolNumber() {
        return cdcIrbProtocolNumber;
    }

    public void setCdcIrbProtocolNumber(String cdcIrbProtocolNumber) {
        this.cdcIrbProtocolNumber = cdcIrbProtocolNumber;
    }

    public Date getVersion1ProtocolDate() {
        return version1ProtocolDate;
    }

    public void setVersion1ProtocolDate(Date version1ProtocolDate) {
        this.version1ProtocolDate = version1ProtocolDate;
    }

    public String getProtocolOfficer() {
        return protocolOfficer;
    }

    public void setProtocolOfficer(String protocol_officer) {
        this.protocolOfficer = protocol_officer;
    }

    public Date getSubmittedCdcIrb() {
        return submittedCdcIrb;
    }

    public void setSubmittedCdcIrb(Date submittedCdcIrb) {
        this.submittedCdcIrb = submittedCdcIrb;
    }

    public Date getApprovalByCdcIrb() {
        return approvalByCdcIrb;
    }

    public void setApprovalByCdcIrb(Date approvalByCdcIrb) {
        this.approvalByCdcIrb = approvalByCdcIrb;
    }

    public Date getCdcIrbExpirationDate() {
        return cdcIrbExpirationDate;
    }

    public void setCdcIrbExpirationDate(Date cdcIrbExpirationDate) {
        this.cdcIrbExpirationDate = cdcIrbExpirationDate;
    }
}
