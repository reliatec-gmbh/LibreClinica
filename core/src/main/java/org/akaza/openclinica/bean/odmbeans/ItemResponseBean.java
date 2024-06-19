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
 * For details see: https://libreclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;


/**
 *
 * @author ywang (Augest, 2010)
 *
 */

public class ItemResponseBean {
    //attributes
    private String responseType;
    private String responseLayout;
    public String getResponseType() {
        return responseType;
    }
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
    public String getResponseLayout() {
        return responseLayout;
    }
    public void setResponseLayout(String responseLayout) {
        this.responseLayout = responseLayout;
    }
}