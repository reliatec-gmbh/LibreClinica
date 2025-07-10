/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.extract;

import java.util.ArrayList;

/**
 * @author ywang
 */
public class XMLReportBean extends ReportBean<String>  {
    private ArrayList<String> dataLines;

    public XMLReportBean(ArrayList<String>  xml) {
        dataLines = new ArrayList<>(xml);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < dataLines.size(); ++i) {
            buffer.append(dataLines.get(i));
            buffer.append(System.getProperty("line.separator"));
        }
        return buffer.toString();
    }

    public ArrayList<String>  getXML() {
        return dataLines;
    }

    public void setXML(ArrayList<String> xml) {
        dataLines = xml;
    }
}