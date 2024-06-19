/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

/**
 * The filter for matching the values of an event CRF's status (as in "completed" or "data entry started").
 */
public class CrfStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {
        //This value is presents a large HTML string specifying a hidden form element
        //This String will be searched for a certain statusId: <input type="hidden" statusId="1" />

        // String item = String.valueOf(itemValue);
        // String statusId = getStatusId(item);
        // String filter = String.valueOf(filterValue);

        // return filter.equalsIgnoreCase(statusId);
        return true;
    }

    	/*
    private String getStatusId(String field) {
        int ind = field.indexOf("statusId=\"");
        return field.substring(ind + 10, ind + 11);
    }*/
}
