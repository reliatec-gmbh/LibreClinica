/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control;

import java.util.Collection;

import org.jmesa.core.CoreContext;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.AbstractExportView;
import org.jmesa.view.component.Table;

public class XmlView extends AbstractExportView {

    public XmlView(Table table, CoreContext coreContext) {
        super(table, coreContext);
        //setTable(table);
        //setCoreContext(coreContext);
    }

    public byte[] getBytes() {
        String render = (String) render();
        return render.getBytes();
    }

    public Object render() {
        StringBuilder data = new StringBuilder();
        
        int rowcount = 0;
        Collection<?> items = getCoreContext().getPageItems();
        for (Object item : items) {
            rowcount++;

            if (rowcount == 1)
                data.append("" + ItemUtils.getItemValue(item, "ruleSetRuleId"));
            else
                data.append("," + ItemUtils.getItemValue(item, "ruleSetRuleId"));

        }

        return data.toString();
    }
}
