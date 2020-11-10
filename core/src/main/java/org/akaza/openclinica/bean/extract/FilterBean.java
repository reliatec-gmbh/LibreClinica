/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

/**
 * FilterBean.java, meant to take the place of Query Bean.
 *
 * @author thickerson
 *
 *
 */

public class FilterBean extends AuditableEntityBean {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5087332538903484795L;
	private String description;
    private String SQLStatement;
    private String explanation;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSQLStatement() {
        return SQLStatement;
    }

    public void setSQLStatement(String statement) {
        SQLStatement = statement;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
