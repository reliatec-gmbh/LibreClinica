/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control;

import java.util.Locale;

import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;

public class DefaultView extends AbstractHtmlView {

    public DefaultView() {
        // TODO Auto-generated constructor stub
    }

    public DefaultView(Locale locale) {
    	// TODO local not used
    }

    public Object render() {
        HtmlSnippets snippets = getHtmlSnippets();
        HtmlBuilder html = new HtmlBuilder();
        html.append(snippets.themeStart());
        html.append(snippets.tableStart());
        html.append(snippets.theadStart());
        html.append(snippets.toolbar());
        html.append(snippets.header());
        html.append(snippets.filter());
        html.append(snippets.theadEnd());
        html.append(snippets.tbodyStart());
        html.append(snippets.body());
        html.append(snippets.tbodyEnd());
        html.append(snippets.footer());
        html.append(snippets.statusBar());
        html.append(snippets.tableEnd());
        html.append(snippets.themeEnd());
        html.append(snippets.initJavascriptLimit());
        return html.toString();
    }
}
