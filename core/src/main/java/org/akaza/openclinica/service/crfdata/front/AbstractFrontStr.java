/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.crfdata.front;

/**
 * By Default, both delimiter and frontStr are empty.
 * ywang (Aug., 2011)
 */
public abstract class AbstractFrontStr {
    FrontStrDelimiter frontStrDelimiter;
    StringBuffer frontStr;

    public AbstractFrontStr() {
        this.frontStrDelimiter = FrontStrDelimiter.NONE;
        this.frontStr = new StringBuffer();
    }
    public AbstractFrontStr(FrontStrDelimiter frontStrDelimiter) {
        this.frontStrDelimiter = frontStrDelimiter;
        this.frontStr = new StringBuffer();
    }


    public FrontStrDelimiter getFrontStrDelimiter() {
        return frontStrDelimiter;
    }

    public void setFrontStrDelimiter(FrontStrDelimiter frontStrDelimiter) {
        this.frontStrDelimiter = frontStrDelimiter;
    }
    public StringBuffer getFrontStr() {
        return frontStr;
    }
    public void setFrontStr(StringBuffer frontStr) {
        this.frontStr = frontStr;
    }
}