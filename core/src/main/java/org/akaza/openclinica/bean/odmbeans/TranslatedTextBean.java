/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class TranslatedTextBean {
    private String text;
    private String xml_lang;
    

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
    
    public void setXmlLang(String lang) {
        this.xml_lang = lang;
    }
    
    public String getXmlLang() {
        return this.xml_lang;
    }
}