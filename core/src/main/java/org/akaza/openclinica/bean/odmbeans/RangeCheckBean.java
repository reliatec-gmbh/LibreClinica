/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;

/**
 *
 * @author ywang (May, 2008)
 *
 */

public class RangeCheckBean {
    private String comparator;
    private String softHard;
    private String checkValue;
    private TranslatedTextBean errorMessage;
    private ArrayList<ElementRefBean> measurementUnitRefs;

    public RangeCheckBean() {
        errorMessage = new TranslatedTextBean();
        measurementUnitRefs = new ArrayList<ElementRefBean>();
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public String getComparator() {
        return this.comparator;
    }

    public void setSoftHard(String constraint) {
        this.softHard = constraint;
    }

    public String getSoftHard() {
        return this.softHard;
    }

    public void setCheckValue(String value) {
        this.checkValue = value;
    }

    public String getCheckValue() {
        return this.checkValue;
    }

    public void setErrorMessage(TranslatedTextBean errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TranslatedTextBean getErrorMessage() {
        return this.errorMessage;
    }

    public ArrayList<ElementRefBean> getMeasurementUnitRefs() {
        return measurementUnitRefs;
    }

    public void setMeasurementUnitRefs(ArrayList<ElementRefBean> measurementUnitRefs) {
        this.measurementUnitRefs = measurementUnitRefs;
    }
}