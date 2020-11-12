/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/*
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * LibreClinica is distributed under the
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.service.rule.expression;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.apache.commons.lang.time.DateUtils;
//import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionBeanService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @SuppressWarnings("unused")
	private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN = "[A-Z_0-9]+|[A-Z_0-9]+\\[(ALL|[1-9]\\d*)\\]$";
    @SuppressWarnings("unused")
	private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN_WITH_ORDINAL = "[A-Z_0-9]+\\[(END|ALL|[1-9]\\d*)\\]$";

    DataSource ds;
    Pattern[] pattern;
    Pattern[] rulePattern;
    Pattern[] ruleActionPattern;
    ExpressionBeanObjectWrapper expressionBeanWrapper;

    public static String STUDYEVENTKEY="SE";

    public ExpressionBeanService(DataSource ds) {
        init(ds, null);
    }

    public ExpressionBeanService(ExpressionBeanObjectWrapper expressionBeanWrapper) {
        init(expressionBeanWrapper.getDs(), expressionBeanWrapper);
    }

    
    private void init(DataSource ds, ExpressionBeanObjectWrapper expressionBeanWrapper) {
        //TODO add stuff here
        this.ds = ds;
        this.expressionBeanWrapper = expressionBeanWrapper;

    }

    private boolean checkIfForScheduling(String value){
    	boolean test = false;
    	if(value.startsWith(STUDYEVENTKEY)&& (value.endsWith(ExpressionService.STARTDATE)||value.endsWith(ExpressionService.STATUS)))
    	{
    		test = true;
    	}
    	return test;
    }
    
    public String evaluateExpression(String test){
        String value ="";
        String temp = null;
        String oid = null;
        int index = 0;
        // TODO fix this
        logger.debug("Test :: {}", test);

        if(checkIfForScheduling(test)){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//TODO: get the format from data format properties.??
        	index = test.indexOf(".");
        	oid = test.substring(0,index);
        	temp = test.substring(index,test.length());
        	StudyEvent studyEvent = getStudyEventFromOID(oid);
        	
        	if(ExpressionService.STARTDATE.endsWith(temp)){
        		if(studyEvent!=null){
        		value = sdf.format(DateUtils.truncate((java.util.Date)studyEvent.getDateStart(), Calendar.DATE));
        		value = value.replace(("00:00:00.0"),"");
                value = value.trim();
        		}
        	}
        }
        return value;
    }

    public StudyEvent getStudyEventFromOID(String oid)
    {
    	Integer subjectId = expressionBeanWrapper.getStudySubjectBeanId();
    	StudyEvent studyEvent = null;
        	if (oid.contains("["))
        	{
        		int leftBracketIndex = oid.indexOf("[");
        		int rightBracketIndex = oid.indexOf("]");
        		int ordinal =  Integer.valueOf(oid.substring(leftBracketIndex + 1,rightBracketIndex));
        		studyEvent= expressionBeanWrapper.getStudyEventDaoHib().fetchByStudyEventDefOIDAndOrdinal(oid.substring(0,leftBracketIndex), ordinal, subjectId);
        	}	
        	else studyEvent= expressionBeanWrapper.getStudyEventDaoHib().fetchByStudyEventDefOIDAndOrdinal(oid, 1, subjectId);
        return studyEvent;
    }
  
    public void setExpressionBeanWrapper(ExpressionBeanObjectWrapper expressionBeanWrapper) {
        this.expressionBeanWrapper = expressionBeanWrapper;
    }
    public String getSSTimeZone(){
        Integer subjectId = expressionBeanWrapper.getStudySubjectBeanId();
        logger.debug(" subjectId {} : ", subjectId);
        if(subjectId ==null) return null;     
        StudySubjectBean ssBean = (StudySubjectBean) getStudySubjectDao().findByPK(subjectId);
          return ssBean.getTime_zone().trim();
        }

    private StudySubjectDAO getStudySubjectDao() {
        return  new StudySubjectDAO(ds);
    }

}
