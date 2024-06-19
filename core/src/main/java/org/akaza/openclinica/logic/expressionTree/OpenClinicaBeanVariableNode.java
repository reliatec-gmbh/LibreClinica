/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/* 
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import java.util.TimeZone;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rule.expression.ExpressionBeanService;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Krikor Krumlian
 * 
 */
public class OpenClinicaBeanVariableNode extends ExpressionNode {
    String number;
    ExpressionBeanService expressionBeanService;
    ExpressionBeanObjectWrapper expressionBeanObjectWrapper;

    OpenClinicaBeanVariableNode(String val) {
        number = val;
        // validate();
    }

    OpenClinicaBeanVariableNode(String val, ExpressionBeanService expressionBeanService) {
        this.expressionBeanService = expressionBeanService;
        number = val;
        // validate();
    }

    OpenClinicaBeanVariableNode(String val, ExpressionBeanService expressionBeanService, OpenClinicaExpressionParser parser) {
        setExpressionParser(parser);
        this.expressionBeanService = expressionBeanService;
        number = val;
        // validate();
    }

    OpenClinicaBeanVariableNode(String val, ExpressionBeanObjectWrapper expressionBeanObjectWrapper, OpenClinicaExpressionParser parser) {
        setExpressionParser(parser);
        this.expressionBeanObjectWrapper = expressionBeanObjectWrapper;
        number = val;
        // validate();
    }

    @Override
    String getNumber() {
        return number;

    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {

    	//TODO: do something
        return null;

    }

    @Override
    Object calculate() throws OpenClinicaSystemException {
        // The value of the node is the number that it contains.
        // return number;
        validate();
        Object variableValue = calculateVariable();
        if (variableValue != null) {
            return variableValue;
        }
        else {
        	variableValue = calculateStatus();
        	if(variableValue!=null)
        		return variableValue;
        }
        return getExpressionBeanService().evaluateExpression(number);
    }

    void validate() throws OpenClinicaSystemException {
       // TODO: Create validation
    }

    private Object calculateStatus(){
    	if(number.equals("Scheduled")){
    		return SubjectEventStatus.SCHEDULED;
    	}
    	return null;
    }
    private Object calculateVariable() {
        if (number.equals("_CURRENT_DATE")) {
        	String ssTimeZone= getExpressionBeanService().getSSTimeZone();
        if (ssTimeZone.equals("") || ssTimeZone == null) 	
        	ssTimeZone = TimeZone.getDefault().getID();
      
            DateTimeZone ssZone = DateTimeZone.forID(ssTimeZone);
            DateMidnight dm = new DateMidnight(ssZone);
            DateTimeFormatter fmt = ISODateTimeFormat.date();
            return fmt.print(dm);
        }
        return null;
    }

    @Override
    void printStackCommands() {
        // On a stack machine, just push the number onto the stack.
        logger.info("  Push " + number);
    }

    private ExpressionBeanService getExpressionBeanService() {
        expressionBeanService = this.expressionBeanService != null ? expressionBeanService : new ExpressionBeanService(expressionBeanObjectWrapper);
        return expressionBeanService;
    }

}