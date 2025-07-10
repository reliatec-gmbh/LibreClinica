/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;

/**
 *
 * @author ywang (May, 2009)
 *
 */
public class SymbolBean {
    private ArrayList<TranslatedTextBean> translatedTexts;

    public SymbolBean() {
        this.translatedTexts = new ArrayList<TranslatedTextBean>();
    }

    public ArrayList<TranslatedTextBean> getTranslatedText() {
        return translatedTexts;
    }

    public void setTranslatedText(ArrayList<TranslatedTextBean> translatedTexts) {
        this.translatedTexts = translatedTexts;
    }

}