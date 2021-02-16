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