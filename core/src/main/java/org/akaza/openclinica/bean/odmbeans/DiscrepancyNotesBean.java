/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;


/**
 *
 * @author ywang (May, 2010)
 *
 */
public class DiscrepancyNotesBean {
    private String entityID;
    private ArrayList<DiscrepancyNoteBean> dns = new ArrayList<DiscrepancyNoteBean>();
    
    public String getEntityID() {
        return entityID;
    }
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }
    public ArrayList<DiscrepancyNoteBean> getDiscrepancyNotes() {
        return dns;
    }
    public void setDiscrepancyNotes(ArrayList<DiscrepancyNoteBean> dns) {
        this.dns = dns;
    }
}