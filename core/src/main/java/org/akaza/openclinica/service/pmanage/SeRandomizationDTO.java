/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 * copyright (C) 2026 UMIN (University Hospital Medical Information Network)
 *
 * Author:  Yoshiteru Chiba
 * Notice:  Developed under a service-agreement contract with UMIN;
 *          copyright in this port has been assigned to UMIN. The
 *          author retains moral rights inalienable under Article 59
 *          of the Japanese Copyright Act. See README for full
 *          attribution.
 *
 * Modifications:
 *   - Ported from OpenClinica v3.x; date: 2026-03-11 - 2026-03-12.
 *   - Source content is identical to the OpenClinica original; only this
 *     license/attribution header was added for the LibreClinica port.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 */
package org.akaza.openclinica.service.pmanage;

public class SeRandomizationDTO {

    private Long id = null;
    private String url=null;
    private String username=null;
    private String password=null;
    private Long statusId;
    private String status;
    private String instanceUrl = null;
    private String studyOid = null;
    private String ocUser_username;
    private String ocUser_name;
    private String ocUser_lastname;
    private String ocUser_emailAddress;
    private String studyName;
    private String OpenClinicaVersion;

    public String getOpenClinicaVersion() {
        return OpenClinicaVersion;
    }
    public void setOpenClinicaVersion(String openClinicaVersion) {
        OpenClinicaVersion = openClinicaVersion;
    }
    public String getStudyName() {
        return studyName;
    }
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }
    public String getOcUser_emailAddress() {
        return ocUser_emailAddress;
    }
    public void setOcUser_emailAddress(String ocUser_emailAddress) {
        this.ocUser_emailAddress = ocUser_emailAddress;
    }
    public String getOcUser_username() {
        return ocUser_username;
    }
    public void setOcUser_username(String ocUser_username) {
        this.ocUser_username = ocUser_username;
    }
    public String getOcUser_name() {
        return ocUser_name;
    }
    public void setOcUser_name(String ocUser_name) {
        this.ocUser_name = ocUser_name;
    }
    public String getOcUser_lastname() {
        return ocUser_lastname;
    }
    public void setOcUser_lastname(String ocUser_lastname) {
        this.ocUser_lastname = ocUser_lastname;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Long getStatusId() {
        return statusId;
    }
    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getInstanceUrl() {
        return instanceUrl;
    }
    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }
    public String getStudyOid() {
        return studyOid;
    }
    public void setStudyOid(String studyOid) {
        this.studyOid = studyOid;
    }
}
