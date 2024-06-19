/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: https://libreclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;



/**
 *
 * @author ywang (Aug, 2010)
 *
 */

public class RelatedInformationBean {
    private String MEDLINEIdentifier;
    private String ResultsReference;
    private String URLReference;
    private String URLDescription;
    
    public String getMEDLINEIdentifier() {
        return MEDLINEIdentifier;
    }
    public void setMEDLINEIdentifier(String mEDLINEIdentifier) {
        MEDLINEIdentifier = mEDLINEIdentifier;
    }
    public String getResultsReference() {
        return ResultsReference;
    }
    public void setResultsReference(String resultsReference) {
        ResultsReference = resultsReference;
    }
    public String getURLReference() {
        return URLReference;
    }
    public void setURLReference(String uRLReference) {
        URLReference = uRLReference;
    }
    public String getURLDescription() {
        return URLDescription;
    }
    public void setURLDescription(String uRLDescription) {
        URLDescription = uRLDescription;
    }
}