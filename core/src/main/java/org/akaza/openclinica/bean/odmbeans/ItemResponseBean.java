/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
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