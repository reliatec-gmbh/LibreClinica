package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.Date;

public class IRBSiteBean extends EntityBean {
    private static final long serialVersionUID = -1698280401023878479L;
    private int irbSiteId;
    private int siteId;
    private int versionNumber;
    private boolean siteReliesOnCdcIrb;
    private boolean is1572;
    private Date cdcIrbProtocolVersionDate;
    private Date localIrbApprovedProtocol;
    private Date cdcReceivedLocalDocuments;
    private Date siteConsentPackageSendToCdcIrb;
    private Date initialCdcIrbApproval;
    private Date crbApprovalToEnroll;
    private Date irbApproval;
    private Date expirationDate;
    private boolean active;
    private String comments;

    public int getIrbSiteId() {
        return irbSiteId;
    }

    public void setIrbSiteId(int irbSiteId) {
        this.irbSiteId = irbSiteId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public boolean isSiteReliesOnCdcIrb() {
        return siteReliesOnCdcIrb;
    }

    public void setSiteReliesOnCdcIrb(boolean siteReliesOnCdcIrb) {
        this.siteReliesOnCdcIrb = siteReliesOnCdcIrb;
    }

    public boolean isIs1572() {
        return is1572;
    }

    public void setIs1572(boolean is1572) {
        this.is1572 = is1572;
    }

    public Date getCdcIrbProtocolVersionDate() {
        return cdcIrbProtocolVersionDate;
    }

    public void setCdcIrbProtocolVersionDate(Date cdcIrbProtocolVersionDate) {
        this.cdcIrbProtocolVersionDate = cdcIrbProtocolVersionDate;
    }

    public Date getLocalIrbApprovedProtocol() {
        return localIrbApprovedProtocol;
    }

    public void setLocalIrbApprovedProtocol(Date localIrbApprovedProtocol) {
        this.localIrbApprovedProtocol = localIrbApprovedProtocol;
    }

    public Date getCdcReceivedLocalDocuments() {
        return cdcReceivedLocalDocuments;
    }

    public void setCdcReceivedLocalDocuments(Date cdcReceivedLocalDocuments) {
        this.cdcReceivedLocalDocuments = cdcReceivedLocalDocuments;
    }

    public Date getSiteConsentPackageSendToCdcIrb() {
        return siteConsentPackageSendToCdcIrb;
    }

    public void setSiteConsentPackageSendToCdcIrb(Date siteConsentPackageSendToCdcIrb) {
        this.siteConsentPackageSendToCdcIrb = siteConsentPackageSendToCdcIrb;
    }

    public Date getInitialCdcIrbApproval() {
        return initialCdcIrbApproval;
    }

    public void setInitialCdcIrbApproval(Date initialCdcIrbApproval) {
        this.initialCdcIrbApproval = initialCdcIrbApproval;
    }

    public Date getCrbApprovalToEnroll() {
        return crbApprovalToEnroll;
    }

    public void setCrbApprovalToEnroll(Date crbApprovalToEnroll) {
        this.crbApprovalToEnroll = crbApprovalToEnroll;
    }

    public Date getIrbApproval() {
        return irbApproval;
    }

    public void setIrbApproval(Date irbApproval) {
        this.irbApproval = irbApproval;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
