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
public class LocationBean extends ElementOIDBean {
    private String name;
    private MetaDataVersionRefBean metaDataVersionRef;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public MetaDataVersionRefBean getMetaDataVersionRef() {
        return metaDataVersionRef;
    }
    public void setMetaDataVersionRef(MetaDataVersionRefBean metaDataVersionRef) {
        this.metaDataVersionRef = metaDataVersionRef;
    }
}