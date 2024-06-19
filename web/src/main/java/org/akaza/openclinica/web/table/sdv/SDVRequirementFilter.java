/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.table.sdv;

import org.akaza.openclinica.domain.SourceDataVerification;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2009
 */
public class SDVRequirementFilter extends DroplistFilterEditor {

    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        String optionA = SourceDataVerification.AllREQUIRED.toString() + " & " + SourceDataVerification.PARTIALREQUIRED.toString();
        options.add(new Option(optionA, optionA));
        for (SourceDataVerification sdv : SourceDataVerification.values()) {
            if (sdv != SourceDataVerification.NOTAPPLICABLE) {
                options.add(new Option(sdv.toString(), sdv.toString()));
            }
        }

        return options;
    }
}
