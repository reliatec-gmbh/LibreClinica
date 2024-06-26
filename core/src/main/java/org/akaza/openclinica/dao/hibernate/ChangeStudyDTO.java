/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

public class ChangeStudyDTO {
    private int studyId;
    private String studyEnvUuid;
    private String siteEnvUuid;

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public String getStudyEnvUuid() {
        return studyEnvUuid;
    }

    public void setStudyEnvUuid(String studyEnvUuid) {
        this.studyEnvUuid = studyEnvUuid;
    }

    public String getSiteEnvUuid() {
        return siteEnvUuid;
    }

    public void setSiteEnvUuid(String siteEnvUuid) {
        this.siteEnvUuid = siteEnvUuid;
    }
}