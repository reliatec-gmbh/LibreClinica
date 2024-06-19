/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.restful;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;

/**
 * R
 * @author jnyayapathi
 *
 */
public class MetadataCollectorResource {

    private static final int INDENT_LEVEL = 2;
    
	private DataSource dataSource;
	private RuleSetRuleDao ruleSetRuleDao;
	private CoreResources coreResources;

	// TODO: remove me - Testing purposes
	private StudyDao studyDaoHib;

	public StudyDao getStudyDaoHib() {
	return studyDaoHib;
}

	public void setStudyDaoHib(StudyDao studyDaoHib) {
	this.studyDaoHib = studyDaoHib;
}


	public CoreResources getCoreResources() {
	return coreResources;
}

	public void setCoreResources(CoreResources coreResources) {
	this.coreResources = coreResources;
}

	public RuleSetRuleDao getRuleSetRuleDao() {
	return ruleSetRuleDao;
}

	public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
	this.ruleSetRuleDao = ruleSetRuleDao;
}

	public StudyDAO getStudyDao() {
		return new StudyDAO(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public MetadataCollectorResource() {
		// NOOP
	}
	
	public String collectODMMetadata(String studyOID) {
		
		StudyBean studyBean = getStudyDao().findByOid(studyOID);
		
	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmBean = mdc.getODMBean();
		odmBean.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
		odmBean.setXmlnsList(xmlnsList);
		odmBean.setODMVersion("oc1.3");
        mdc.setODMBean(odmBean);
     	adc.setOdmbean(odmBean);
        mdc.collectFileData();
   		adc.collectFileData();
        
        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(getCoreResources());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);
		
        return report.getXmlOutput().toString().trim();
	}

	public String collectODMMetadataJson(String studyOID) {
		net.sf.json.xml.XMLSerializer xmlSerializer = new XMLSerializer();
		JSON json = xmlSerializer.read(collectODMMetadata(studyOID));
		return json.toString(INDENT_LEVEL);
	}

	public JSON collectODMMetadataJson(String studyOID, String formVersionOID) {
		net.sf.json.xml.XMLSerializer xmlSerializer = new XMLSerializer();
		return xmlSerializer.read(collectODMMetadataForForm(studyOID,formVersionOID));
	}
	
	public String collectODMMetadataJsonString(String studyOID, String formVersionOID) {
		net.sf.json.xml.XMLSerializer xmlSerializer = new XMLSerializer();
		JSON json = xmlSerializer.read(collectODMMetadataForForm(studyOID,formVersionOID));
		return json.toString(INDENT_LEVEL);
	}

	public String collectODMMetadataForForm(String studyOID, String formVersionOID) {

		StudyBean studyBean = getStudyDao().findByOid(studyOID);

		// Study is active when it exists in persisted state in database
		if (studyBean.isActive()) {
			studyBean = populateStudyBean(studyBean);
		}

	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmBean = mdc.getODMBean();
		odmBean.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
		odmBean.setXmlnsList(xmlnsList);
		odmBean.setODMVersion("oc1.3");
        mdc.setODMBean(odmBean);
        adc.setOdmbean(odmBean);
		// When triggered out ouf study context (studyOID = *) (CRF management)
        if (!studyBean.isActive()) {
			mdc.collectFileData(formVersionOID);
		}
        else {
			mdc.collectFileData();
		}
        adc.collectFileData();
        
        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(getCoreResources());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);
        
		return report.getXmlOutput().toString().trim();
	}

	public FullReportBean collectODMMetadataForClinicalData(String studyOID,
															String formVersionOID,
															LinkedHashMap<String,OdmClinicalDataBean> clinicalDataMap) {

		StudyBean studyBean = getStudyDao().findByOid(studyOID);

		// Study is active when it exists in persisted state in database
		if (studyBean.isActive()) {
			studyBean = populateStudyBean(studyBean);
		}

		MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao());
		AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
		MetaDataCollector.setTextLength(200);

		ODMBean odmBean = mdc.getODMBean();
		odmBean.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
		ArrayList<String> xmlnsList = new ArrayList<>();
		xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
		//xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
		xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
		xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
		odmBean.setXmlnsList(xmlnsList);
		odmBean.setODMVersion("oc1.3");
		mdc.setODMBean(odmBean);
		adc.setOdmbean(odmBean);

		// When triggered out ouf study context (studyOID = *)
		if (!studyBean.isActive()) {
			mdc.collectFileData(formVersionOID);
		}
		else {
			mdc.collectFileData();
		}
		adc.collectFileData();

		FullReportBean report = new FullReportBean();
		report.setAdminDataMap(adc.getOdmAdminDataMap());
		report.setOdmStudyMap(mdc.getOdmStudyMap());
		report.setCoreResources(getCoreResources());
		report.setOdmBean(mdc.getODMBean());
		//report.setClinicalData(odmClinicalDataBean);

		report.setClinicalDataMap(clinicalDataMap);
		report.setODMVersion("oc1.3");

		return report;
	}
	
	private StudyBean populateStudyBean(StudyBean studyBean) {

		StudyParameterValueDAO spvDao = new StudyParameterValueDAO(this.dataSource);
		ArrayList<StudyParamsConfig> studyParameters = spvDao.findParamConfigByStudy(studyBean);

		studyBean.setStudyParameters(studyParameters);
		StudyConfigService scs = new StudyConfigService(this.dataSource);
		
		if (studyBean.getParentStudyId() <= 0) { // top study
			studyBean = scs.setParametersForStudy(studyBean);
		} else {
			studyBean.setParentStudyName((getStudyDao().findByPK(studyBean.getParentStudyId())).getName());
			studyBean = scs.setParametersForSite(studyBean);
		}
		 
		return studyBean;
	}
	
}
