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
 * @author ywang (May, 2009)
 * 
 */
public class MeasurementUnitBean extends ElementOIDBean {
    private String name;
    private SymbolBean symbol;

    public MeasurementUnitBean() {
        this.symbol = new SymbolBean();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolBean getSymbol() {
        return symbol;
    }

    public void setSymbol(SymbolBean symbol) {
        this.symbol = symbol;
    }
}