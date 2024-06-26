/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view.form;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 4, 2007
 */
public class FormBuilderTest {

    private static final Logger logger= LoggerFactory.getLogger(FormBuilderTest.class);

    public String createTable() {
        Element root = new Element("table");
        root.setAttribute("border", "0");
        Document doc = new Document(root);
        Element thead = new Element("thead");
        Element th = new Element("th");
        th.addContent("A header");
        th.setAttribute("class", "aka_header_border");
        thead.addContent(th);
        Element th2 = new Element("th");
        th2.addContent("Another header");
        th2.setAttribute("class", "aka_header_border");
        thead.addContent(th2);
        root.addContent(thead);
        Element tr1 = new Element("tr");
        Element td1 = new Element("td");
        td1.setAttribute("valign", "top");
        td1.setAttribute("class", "cellBorders");
        td1.setText("cell contents");
        tr1.addContent(td1);
        root.addContent(tr1);
        XMLOutputter outp = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setOmitDeclaration(true);
        outp.setFormat(format);
        Writer writer = new StringWriter();
        try {
            outp.output(doc, writer);
        } catch (IOException e) {
            logger.error("Error while writing the XML: ", e);
        }
        return writer.toString();
    }

    public static void main(String[] args) {
        FormBuilderTest builder = new FormBuilderTest();
        System.out.println(builder.createTable());
    }
}
