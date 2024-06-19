/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 *
 * Created on Sep 21, 2005
 */
package org.akaza.openclinica.control.form;

import javax.servlet.http.HttpServletRequest;

/**
 * A Validator for 'calculation' and 'group-calculation' type Items whose
 * fieldNames are always from request attribute.
 *
 * @author ywang (Feb. 2008)
 *
 */
public class ScoreItemValidator extends DiscrepancyValidator {
	
    public ScoreItemValidator(HttpServletRequest request, FormDiscrepancyNotes notes) {
        super(request, notes);
    }

    @Override
    protected String getFieldValue(String fieldName) {
        return (String) request.getAttribute(fieldName);
    }

}
