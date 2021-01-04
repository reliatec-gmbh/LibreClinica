/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.form;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jxu
 *
 * 
 */
// TODO duplicate of the version in the web module?
public class FormDiscrepancyNotes {
    private HashMap<String, ArrayList<DiscrepancyNoteBean>> fieldNotes;
    private HashMap<Object, Object> numExistingFieldNotes;
    private HashMap<Integer, ArrayList<String>> idNotes;

    public FormDiscrepancyNotes() {
        fieldNotes = new HashMap<>();
        numExistingFieldNotes = new HashMap<>();
        idNotes = new HashMap<>();
    }

    public void addNote(String field, DiscrepancyNoteBean note) {
        ArrayList<DiscrepancyNoteBean> notes = getNotes(field);
        notes.add(note);
        fieldNotes.put(field, notes);
    }

    /** want to map entity Id with field names
     * So we know if an entity has discrepancy note giving entity id
     * @param entityId
     * @param field
     */
    public void addIdNote(int entityId, String field) {
        ArrayList<String> notes;
        if (idNotes.containsKey(entityId)) {
            notes = idNotes.get(entityId);
        } else {
            notes = new ArrayList<>();
        }
        if (notes != null) {
            notes.add(field);
        }
        idNotes.put(entityId, notes);
    }

    public boolean hasNote(String field) {
        ArrayList<DiscrepancyNoteBean> notes = getNotes(field);
        return notes != null && notes.size() > 0;
    }

    public ArrayList<DiscrepancyNoteBean> getNotes(String field) {
        ArrayList<DiscrepancyNoteBean> notes;
        if (fieldNotes.containsKey(field)) {
            notes = fieldNotes.get(field);
        } else {
            notes = new ArrayList<>();
        }
        return notes;
    }

    public void setNumExistingFieldNotes(String field, int num) {
        numExistingFieldNotes.put(field, new Integer(num));
    }

    public int getNumExistingFieldNotes(String field) {
        if (numExistingFieldNotes.containsKey(field)) {
            Integer numInt = (Integer) numExistingFieldNotes.get(field);
            if (numInt != null) {
                return numInt.intValue();
            }
        }
        return 0;
    }

    /**
     * @return Returns the numExistingFieldNotes.
     */
    public HashMap<Object, Object> getNumExistingFieldNotes() {
        return numExistingFieldNotes;
    }

    /**
     * @return the fieldNotes
     */
    public HashMap<String, ArrayList<DiscrepancyNoteBean>> getFieldNotes() {
        return fieldNotes;
    }

    /**
     * @param fieldNotes the fieldNotes to set
     */
    public void setFieldNotes(HashMap<String, ArrayList<DiscrepancyNoteBean>> fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    /**
     * @param numExistingFieldNotes the numExistingFieldNotes to set
     */
    public void setNumExistingFieldNotes(HashMap<Object, Object> numExistingFieldNotes) {
        this.numExistingFieldNotes = numExistingFieldNotes;
    }

    /**
     * @return the idNotes
     */
    public HashMap<Integer, ArrayList<String>> getIdNotes() {
        return idNotes;
    }

    /**
     * @param idNotes the idNotes to set
     */
    public void setIdNotes(HashMap<Integer, ArrayList<String>> idNotes) {
        this.idNotes = idNotes;
    }
}