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
 * <P>For chain up elements so string could be passed to the front-end</P>
 * ywang (Aug, 2011)
 */
public enum FrontStrDelimiter {
    NONE(""), INNER("---");

    private String code;

    FrontStrDelimiter(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}