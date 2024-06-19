/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: https://libreclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;

/**
 *
 * @author ywang (May, 2009)
 *
 */
public class BasicDefinitionsBean extends ElementOIDBean {
    private ArrayList<MeasurementUnitBean> measurementUnits;

    public BasicDefinitionsBean() {
        this.measurementUnits = new ArrayList<MeasurementUnitBean>();
    }

    public ArrayList<MeasurementUnitBean> getMeasurementUnits() {
        return measurementUnits;
    }

    public void setMeasurementUnits(ArrayList<MeasurementUnitBean> measurementUnits) {
        this.measurementUnits = measurementUnits;
    }

}
