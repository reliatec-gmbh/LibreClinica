package org.akaza.openclinica.control.submit;

import java.util.Date;

public class EnrollmentData {
    private String gender;
    private boolean cavitation;
    private String weight;
    private String weightUnit;
    private String age;
    private String dob;
    private String enrollmentDate;
    private String treatmentStartDate;
    private String studyID;
    private String siteID;
    private String subjectID;

    public EnrollmentData() {
    }

    public EnrollmentData(String gender, boolean cavitation, String weight, String age, String enrollmentDate, String treatmentStartDate, String studyID, String siteID, String weightUnit, String subjectID, String dob) {
        this.gender = gender;
        this.cavitation = cavitation;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.age = age;
        this.enrollmentDate = enrollmentDate;
        this.treatmentStartDate = treatmentStartDate;
        this.studyID = studyID;
        this.siteID = siteID;
        this.subjectID = subjectID;
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean getCavitation() {
        return cavitation;
    }

    public void setCavitation(boolean cavitation) {
        this.cavitation = cavitation;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

         public String getWeightUnit() {
            return weightUnit;
        }

        public void setWeightUnit(String weightUnit) {
            this.weightUnit = weightUnit;
        }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getTreatmentStartDate() {
        return treatmentStartDate;
    }

    public void setTreatmentStartDate(String treatmentStartDate) {
        this.treatmentStartDate = treatmentStartDate;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public String getSiteNumber() {
        return siteID;
    }

    public void setSiteID(String siteNumber) {
        this.siteID = siteNumber;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }
    public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
}
