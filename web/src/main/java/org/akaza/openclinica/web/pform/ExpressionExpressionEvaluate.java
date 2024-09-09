/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform;

public class ExpressionExpressionEvaluate {
	private String expression;
	private boolean expressionEvaluate;
	
	
	public ExpressionExpressionEvaluate() {
	}
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public boolean isExpressionEvaluate() {
		return expressionEvaluate;
	}
	public void setExpressionEvaluate(boolean expressionEvaluate) {
		this.expressionEvaluate = expressionEvaluate;
	}
	
	

}
