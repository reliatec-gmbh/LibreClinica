/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control;

import java.util.HashMap;
import java.util.Map;

import org.jmesa.view.html.editor.DroplistFilterEditor;

/**
 * Extends JMesa's {@link DroplistFilterEditor} to display in the text box the label of the selected option (not its
 * value).
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class DropdownFilter extends DroplistFilterEditor {

    private final Map<String, String> optionDecoder = new HashMap<String, String>();

    @Override
    protected void addOption(Option option) {
        String value = option.getValue();
        String label = option.getLabel();
        optionDecoder.put(label, value);
        super.addOption(new Option(label, label));
    }

    public Map<String, String> getDecoder() {
        return optionDecoder;
    }

}
