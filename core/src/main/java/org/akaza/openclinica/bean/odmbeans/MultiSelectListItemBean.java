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
 * @author ywang (March, 2010)
 *
 */
public class MultiSelectListItemBean  {
    private String codedOptionValue;
    private TranslatedTextBean decode;
    
    public String getCodedOptionValue() {
        return codedOptionValue;
    }
    public void setCodedOptionValue(String codedOptionValue) {
        this.codedOptionValue = codedOptionValue;
    }
    public TranslatedTextBean getDecode() {
        return decode;
    }
    public void setDecode(TranslatedTextBean decode) {
        this.decode = decode;
    }
}