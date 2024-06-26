/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control;

import org.jmesa.core.CoreContext;
import org.jmesa.util.ExportUtils;
import org.jmesa.view.AbstractViewExporter;
import org.jmesa.view.View;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletResponse;

public class OCCsvViewExporter extends AbstractViewExporter {

    String fileName;

    public OCCsvViewExporter(View view, CoreContext coreContext, HttpServletResponse response) {
        super(view, coreContext, response, null);
        if (fileName == null) {
            fileName = ExportUtils.exportFileName(view, getExtensionName());
        }

    }

    public OCCsvViewExporter(View view, CoreContext coreContext, HttpServletResponse response, String fileName) {
        super(view, coreContext, response, fileName);
        this.fileName = fileName + "." + getExtensionName();
    }

    public void export() throws Exception {
        //responseHeaders(getResponse());
        String viewData = (String) getView().render();
        byte[] contents = (viewData).getBytes();
        //ServletOutputStream outputStream = getResponse().getOutputStream();
        File f = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(f, true)) {
        	fos.write(contents);
        	fos.flush();
        }
    }

    @Override
    public String getContextType() {
        return "text/csv";
    }

    @Override
    public String getExtensionName() {
        return "txt";
    }
}
