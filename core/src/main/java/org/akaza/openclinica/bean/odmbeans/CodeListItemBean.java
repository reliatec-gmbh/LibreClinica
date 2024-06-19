/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * LibreClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: https://libreclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class CodeListItemBean {
    private TranslatedTextBean decode;
    private String codedValue;
    
    public CodeListItemBean() {
        decode = new TranslatedTextBean();
    }

    public void setDecode(TranslatedTextBean decode) {
        this.decode = decode;
    }

    public TranslatedTextBean getDecode() {
        return this.decode;
    }

    public void setCodedValue(String value) {
        this.codedValue = value;
    }

    public String getCodedVale() {
        return this.codedValue;
    }
}