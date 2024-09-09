/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Custom tag used to escape special characters for text rendered as JavaScript values.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class JavaScriptEscapeTag extends SimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptEscapeTag.class);

    private String key;

    private LocalizationContext bundle;

    public void setKey(String key) {
        this.key = key;
    }

    public void setBundle(LocalizationContext bundle) {
        this.bundle = bundle;
    }

    @Override
    public void doTag() throws JspException, IOException {
        super.doTag();
        String value = bundle.getResourceBundle().getString(key);
        if (value == null) {
            LOG.warn("The key {} could not be found in the resource bundle", key);
            getJspContext().getOut().print("[" + key  + "]");
        } else {
            // Escape single quotes
            getJspContext().getOut().print(value.replaceAll("\'", "\\\\'"));
        }

    }
}
