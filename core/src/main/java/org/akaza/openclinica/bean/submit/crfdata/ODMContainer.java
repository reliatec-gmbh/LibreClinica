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
 * ODM Container, the surrounding tag for Clinical Data together with meta data
 * 
 * @author thickerson, 04/2008
 * 
 */
public class ODMContainer {

    private CRFDataPostImportContainer crfDataPostImportContainer;
    private String subjectUniqueIdentifier;
    private String studyUniqueIdentifier;

    public CRFDataPostImportContainer getCrfDataPostImportContainer() {
        return crfDataPostImportContainer;
    }

    public void setCrfDataPostImportContainer(CRFDataPostImportContainer crfDataPostImportContainer) {
        this.crfDataPostImportContainer = crfDataPostImportContainer;
    }

    public String getSubjectUniqueIdentifier() {
        return subjectUniqueIdentifier;
    }

    public void setSubjectUniqueIdentifier(String subjectUniqueIdentifier) {
        this.subjectUniqueIdentifier = subjectUniqueIdentifier;
    }

    public String getStudyUniqueIdentifier() {
        return studyUniqueIdentifier;
    }

    public void setStudyUniqueIdentifier(String studyUniqueIdentifier) {
        this.studyUniqueIdentifier = studyUniqueIdentifier;
    }

}
