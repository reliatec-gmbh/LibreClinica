/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.submit;

import java.io.Serializable;
import java.util.ArrayList;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.oid.ItemGroupOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 7, 2007
 */
public class ItemGroupBean extends AuditableEntityBean implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -5562878563981283376L;
	private Integer crfId = 0;
    private ItemGroupMetadataBean meta = new ItemGroupMetadataBean();
    private ArrayList<ItemGroupMetadataBean> itemGroupMetaBeans = new ArrayList<>();
    private String oid;
    private OidGenerator oidGenerator;
    
    public ItemGroupBean() {
        super();
        crfId = 0;
        name = "";
        meta = new ItemGroupMetadataBean();
        oidGenerator = new ItemGroupOidGenerator();
    }

    
    
    
    
    /**
     * @return the crfId
     */
    public Integer getCrfId() {
        return crfId;
    }

    /**
     * @param crfId
     *            the crfId to set
     */
    public void setCrfId(Integer crfId) {
        this.crfId = crfId;
    }

    /**
     * @return the meta
     */
    public ItemGroupMetadataBean getMeta() {
        return meta;
    }

    /**
     * @param meta
     *            the meta to set
     */
    public void setMeta(ItemGroupMetadataBean meta) {
        this.meta = meta;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public ArrayList<ItemGroupMetadataBean> getItemGroupMetaBeans() {
        return itemGroupMetaBeans;
    }

    public void setItemGroupMetaBeans(ArrayList<ItemGroupMetadataBean> itemGroupMetaBeans) {
        this.itemGroupMetaBeans = itemGroupMetaBeans;
    }    
}
