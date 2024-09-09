/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.service;

public class StudyParamsConfig {
    private StudyParameterValueBean value;
    private StudyParameter parameter;

    public StudyParamsConfig() {
        value = new StudyParameterValueBean();
        parameter = new StudyParameter();
    }

    /**
     * @return Returns the parameter.
     */
    public StudyParameter getParameter() {
        return parameter;
    }

    /**
     * @param parameter
     *            The parameter to set.
     */
    public void setParameter(StudyParameter parameter) {
        this.parameter = parameter;
    }

    /**
     * @return Returns the value.
     */
    public StudyParameterValueBean getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(StudyParameterValueBean value) {
        this.value = value;
    }

}
