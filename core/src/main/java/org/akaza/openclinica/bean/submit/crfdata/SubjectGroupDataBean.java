/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.submit.crfdata;

/**
 * 
 * @author ywang (Nov, 2008)
 * 
 */
public class SubjectGroupDataBean {
    private String studyGroupClassId;
    private String studyGroupClassName;
    private String studyGroupName;

    public void setStudyGroupClassId(String studyGroupClassId) {
        this.studyGroupClassId = studyGroupClassId;
    }

    public String getStudyGroupClassId() {
        return this.studyGroupClassId;
    }

    public void setStudyGroupClassName(String studyGroupClassName) {
        this.studyGroupClassName = studyGroupClassName;
    }

    public String getStudyGroupClassName() {
        return this.studyGroupClassName;
    }

    public void setStudyGroupName(String studyGroupName) {
        this.studyGroupName = studyGroupName;
    }

    public String getStudyGroupName() {
        return this.studyGroupName;
    }
}