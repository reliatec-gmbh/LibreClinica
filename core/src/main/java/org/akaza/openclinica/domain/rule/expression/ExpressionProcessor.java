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
package org.akaza.openclinica.domain.rule.expression;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * @author Krikor Krumlian
 * 
 */
public interface ExpressionProcessor {

    String isRuleAssignmentExpressionValid();

    String isRuleExpressionValid();

    boolean process();

    void setExpression(ExpressionBean e);

    String testEvaluateExpression();

    HashMap<String, String> testEvaluateExpression(HashMap<String, String> testValues);

    void setRespage(ResourceBundle respage);

}
