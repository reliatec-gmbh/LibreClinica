package org.akaza.openclinica.control.submit;

import java.util.Date;

public class SiteJson {
    private String name;
    private String studyName;
    private String identifier;
    private String secondaryIdentifier;
    private String principalInvestigator;
    private String summary;
    private Date protocolDateVerification;
    private Date datePlannedStart;
    private Date datePlannedEnd;
    private int expectedTotalEnrollment;
    private String facilityName;
    private String facilityCity;
    private String facilityState;
    private String facilityZip;
    private String facilityCountry;
    private String facilityContactName;
    private String facilityContactDegree;
    private String facilityContactPhone;
    private String facilityContactEmail;
    private String status = "Available";

    public SiteJson() {
    }

    public SiteJson(String name, String studyName, String identifier, String secondaryIdentifier, String principalInvestigator, String summary, Date protocolDateVerification, Date datePlannedStart, Date datePlannedEnd, int expectedTotalEnrollment, String facilityName, String facilityCity, String facilityState, String facilityZip, String facilityCountry, String facilityContactName, String facilityContactDegree, String facilityContactPhone, String facilityContactEmail) {
        this.name = name;
        this.studyName = studyName;
        this.identifier = identifier;
        this.secondaryIdentifier = secondaryIdentifier;
        this.principalInvestigator = principalInvestigator;
        this.summary = summary;
        this.protocolDateVerification = protocolDateVerification;
        this.datePlannedStart = datePlannedStart;
        this.datePlannedEnd = datePlannedEnd;
        this.expectedTotalEnrollment = expectedTotalEnrollment;
        this.facilityName = facilityName;
        this.facilityCity = facilityCity;
        this.facilityState = facilityState;
        this.facilityZip = facilityZip;
        this.facilityCountry = facilityCountry;
        this.facilityContactName = facilityContactName;
        this.facilityContactDegree = facilityContactDegree;
        this.facilityContactPhone = facilityContactPhone;
        this.facilityContactEmail = facilityContactEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSecondaryIdentifier() {
        return secondaryIdentifier;
    }

    public void setSecondaryIdentifier(String secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getProtocolDateVerification() {
        return protocolDateVerification;
    }

    public void setProtocolDateVerification(Date protocolDateVerification) {
        this.protocolDateVerification = protocolDateVerification;
    }

    public Date getDatePlannedStart() {
        return datePlannedStart;
    }

    public void setDatePlannedStart(Date datePlannedStart) {
        this.datePlannedStart = datePlannedStart;
    }

    public Date getDatePlannedEnd() {
        return datePlannedEnd;
    }

    public void setDatePlannedEnd(Date datePlannedEnd) {
        this.datePlannedEnd = datePlannedEnd;
    }

    public int getExpectedTotalEnrollment() {
        return expectedTotalEnrollment;
    }

    public void setExpectedTotalEnrollment(int expectedTotalEnrollment) {
        this.expectedTotalEnrollment = expectedTotalEnrollment;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityCity() {
        return facilityCity;
    }

    public void setFacilityCity(String facilityCity) {
        this.facilityCity = facilityCity;
    }

    public String getFacilityState() {
        return facilityState;
    }

    public void setFacilityState(String facilityState) {
        this.facilityState = facilityState;
    }

    public String getFacilityZip() {
        return facilityZip;
    }

    public void setFacilityZip(String facilityZip) {
        this.facilityZip = facilityZip;
    }

    public String getFacilityCountry() {
        return facilityCountry;
    }

    public void setFacilityCountry(String facilityCountry) {
        this.facilityCountry = facilityCountry;
    }

    public String getFacilityContactName() {
        return facilityContactName;
    }

    public void setFacilityContactName(String facilityContactName) {
        this.facilityContactName = facilityContactName;
    }

    public String getFacilityContactDegree() {
        return facilityContactDegree;
    }

    public void setFacilityContactDegree(String facilityContactDegree) {
        this.facilityContactDegree = facilityContactDegree;
    }

    public String getFacilityContactPhone() {
        return facilityContactPhone;
    }

    public void setFacilityContactPhone(String facilityContactPhone) {
        this.facilityContactPhone = facilityContactPhone;
    }

    public String getFacilityContactEmail() {
        return facilityContactEmail;
    }

    public void setFacilityContactEmail(String facilityContactEmail) {
        this.facilityContactEmail = facilityContactEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
