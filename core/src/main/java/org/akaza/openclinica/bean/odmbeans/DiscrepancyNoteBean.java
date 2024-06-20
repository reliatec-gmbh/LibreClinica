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
import java.util.Date;
import java.util.List;

/**
 *
 * @author ywang (March, 2010)
 *
 */
public class DiscrepancyNoteBean extends ElementOIDBean {
    private String status;
    private String noteType;
    private Date dateUpdated;
    private int numberOfChildNotes;
    private List<ChildNoteBean> childNotes = new ArrayList<ChildNoteBean>();
    private String entityName;
    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getNoteType() {
        return noteType;
    }
    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
    public Date getDateUpdated() {
        return dateUpdated;
    }
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    public int getNumberOfChildNotes() {
        return numberOfChildNotes;
    }
    public void setNumberOfChildNotes(int numberOfChildNotes) {
        this.numberOfChildNotes = numberOfChildNotes;
    }
    public List<ChildNoteBean> getChildNotes() {
        return childNotes;
    }
    public void setChildNotes(ArrayList<ChildNoteBean> childNotes) {
        this.childNotes = childNotes;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String attribute) {
        this.entityName = attribute;
    }
}