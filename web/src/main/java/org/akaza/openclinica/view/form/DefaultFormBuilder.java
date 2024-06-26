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
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 15, 2007
 */
public class DefaultFormBuilder implements FormBuilder {
    private Logger logger= LoggerFactory.getLogger(getClass().getName());

    public DefaultFormBuilder() {
        super();
    }

    public DefaultFormBuilder(List<Object> displayItems) {
    	// TODO displayItems not used
    }

    public String createMarkup() {
        Element root = createTable();
        Document doc = new Document(root);
        Element thead = this.createThead();
        Element th = this.createThCell("Col 1");
        Element th2 = this.createThCell("Col 2");
        thead.addContent(th);
        thead.addContent(th2);
        root.addContent(thead);
        Element tfoot = this.createTfoot();
        Element trFoot = this.createRow();
        Element tdFoot = this.createCell("Some content in the footer");
        tdFoot.setAttribute("colspan", "2");
        trFoot.addContent(tdFoot);
        tfoot.addContent(trFoot);
        root.addContent(tfoot);
        Element tbody = this.createTbody();
        Element tr1 = this.createRow();
        Element td1 = this.createCell("Some content");
        td1.setAttribute("valign", "top");
        tr1.addContent(td1);
        Element td2 = this.createCell("Some more content");
        td1.setAttribute("valign", "top");
        tr1.addContent(td2);
        tbody.addContent(tr1);
        root.addContent(tbody);

        XMLOutputter outp = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setOmitDeclaration(true);
        outp.setFormat(format);
        Writer writer = new StringWriter();
        try {
            outp.output(doc, writer);
        } catch (IOException e) {
            logger.error("Error while writing the XML", e);
        }
        return writer.toString();
    }

    public Element createTable() {
        Element root = new Element("table");
        root.setAttribute("border", "0");
        root.setAttribute("cellspacing", "0");
        root.setAttribute("cellpadding", "0");
        return root;
    }

    public Element createThead() {
        return new Element("thead");

    }

    public Element createThCell() {
        return new Element("th");
    }

    public Element createThCell(String content) {
        return new Element("th").addContent(content);
    }

    public Element createColGroup() {
        return new Element("colgroup");
    }

    public Element createTbody() {
        return new Element("tbody");
    }

    public Element createTfoot() {
        return new Element("tfoot");
    }

    public Element createRow() {
        return new Element("tr");
    }

    public Element createCell() {
        return new Element("td");
    }

    public Element createCell(String content) {
        return new Element("td").addContent(content);
    }

    public Element setClassNames(Element styledElement) {
        // Just returns the Element unstyled
        return styledElement;
    }

    public static void main(String[] args) {
        DefaultFormBuilder dfb = new DefaultFormBuilder();
        System.out.println(dfb.createMarkup());
    }
}
