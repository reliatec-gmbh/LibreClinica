/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */
public class GlobalVariablesBean {
    private String studyName;
    private String studyDescription;
    private String protocolName;

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getStudyName() {
        return this.studyName;
    }

    public void setStudyDescription(String description) {
        this.studyDescription = description;
    }

    public String getStudyDescription() {
        return this.studyDescription;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolName() {
        return this.protocolName;
    }
}