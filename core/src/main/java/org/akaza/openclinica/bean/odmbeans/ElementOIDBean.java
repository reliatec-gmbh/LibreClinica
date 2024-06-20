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
public class ElementOIDBean implements Comparable<ElementOIDBean> {
    private String oid;

    public int compareTo(ElementOIDBean o) {
        return this.oid.compareTo(o.getOid());
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOid() {
        return this.oid;
    }
}