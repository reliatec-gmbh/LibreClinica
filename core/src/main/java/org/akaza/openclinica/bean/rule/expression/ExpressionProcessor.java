/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.rule.expression;

import java.util.HashMap;

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

}
