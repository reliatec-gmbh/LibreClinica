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
public class OdmStudyBean extends ElementOIDBean {
    private GlobalVariablesBean globalVariables;
    private BasicDefinitionsBean basicDefinitions;
    private MetaDataVersionBean metaDataVersion;
    
    private String parentStudyOID;

    public OdmStudyBean() {
        globalVariables = new GlobalVariablesBean();
        basicDefinitions = new BasicDefinitionsBean();
        metaDataVersion = new MetaDataVersionBean();
    }

    public void setGlobalVariables(GlobalVariablesBean gv) {
        this.globalVariables = gv;
    }

    public GlobalVariablesBean getGlobalVariables() {
        return this.globalVariables;
    }

    public void setMetaDataVersion(MetaDataVersionBean metadataversion) {
        this.metaDataVersion = metadataversion;
    }

    public MetaDataVersionBean getMetaDataVersion() {
        return this.metaDataVersion;
    }

    public BasicDefinitionsBean getBasicDefinitions() {
        return basicDefinitions;
    }

    public void setBasicDefinitions(BasicDefinitionsBean basicDefinitions) {
        this.basicDefinitions = basicDefinitions;
    }

    public String getParentStudyOID() {
        return parentStudyOID;
    }

    public void setParentStudyOID(String parentStudyOID) {
        this.parentStudyOID = parentStudyOID;
    }
}