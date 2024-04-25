/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

/**
 * @author thickerson
 */
public class LabsForSiteBean extends AuditableEntityBean {
    /**
     * 
     */
    private static final long serialVersionUID = 64657567645756756L;
    private int id = 0;
    private int site_id = 0;
    private int laboratory_id = 0;


    public int getLaboratory_id() {
        return laboratory_id;
    }

    public void setLaboratory_id(int laboratory_id) {
        this.laboratory_id = laboratory_id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getSite_id() {
        return site_id;
    }

    public void setSite_id(int site_id) {
        this.site_id = site_id;
    }
}
