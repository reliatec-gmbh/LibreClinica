/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.view.tags;

import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: Nov 14, 2008
 * Time: 3:18:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertTag extends SimpleTagSupport {
    private String message;
    @Override
    public void doTag() throws JspException, IOException {
        JspContext context = getJspContext();
        JspWriter tagWriter = context.getOut();
        StringBuilder builder = new StringBuilder("");

        List<String> messages = asArrayList(context.findAttribute("pageMessages"), String.class);
        if(messages != null){
            for(String message : messages){
                builder.append(message);
                builder.append("<br />");
            }

        }
        tagWriter.println(builder.toString());


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String messages) {
        this.message = messages;
    }
}
