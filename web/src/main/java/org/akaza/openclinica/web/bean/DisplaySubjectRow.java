/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.submit.DisplaySubjectBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DisplaySubjectRow extends EntityBeanRow<DisplaySubjectBean, DisplaySubjectRow> {
    // columns:
    public static final int COL_NAME = 0;

    public static final int COL_SUBJECT_IDS = 1;

    public static final int COL_GENDER = 2;

    public static final int COL_DATE_CREATED = 3;

    public static final int COL_OWNER = 4;

    public static final int COL_DATE_UPDATED = 5;

    public static final int COL_UPDATER = 6;

    public static final int COL_STATUS = 7;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(DisplaySubjectRow row, int sortingColumn) {
        if (!row.getClass().equals(DisplaySubjectRow.class)) {
            return 0;
        }

        DisplaySubjectBean thisSubject = bean;
        DisplaySubjectBean argSubject = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisSubject.getSubject().getName().toLowerCase().compareTo(argSubject.getSubject().getName().toLowerCase());
            break;
        case COL_SUBJECT_IDS:
            answer = thisSubject.getStudySubjectIds().compareTo(argSubject.getStudySubjectIds());
            break;
        case COL_GENDER:
            answer = (thisSubject.getSubject().getGender() + "").compareTo(argSubject.getSubject().getGender() + "");
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisSubject.getSubject().getCreatedDate(), argSubject.getSubject().getCreatedDate());
            break;
        case COL_OWNER:
            answer = thisSubject.getSubject().getOwner().getName().toLowerCase().compareTo(argSubject.getSubject().getOwner().getName().toLowerCase());
            break;
        case COL_DATE_UPDATED:
            answer = compareDate(thisSubject.getSubject().getUpdatedDate(), argSubject.getSubject().getUpdatedDate());
            break;
        case COL_UPDATER:
            answer = thisSubject.getSubject().getUpdater().getName().toLowerCase().compareTo(argSubject.getSubject().getUpdater().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisSubject.getSubject().getStatus().compareTo(argSubject.getSubject().getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        DisplaySubjectBean thisSubject = (DisplaySubjectBean) bean;
        return thisSubject.getSubject().getName() + " " + thisSubject.getStudySubjectIds();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<DisplaySubjectRow> generatRowsFromBeans(ArrayList<DisplaySubjectBean> beans) {
        return DisplaySubjectRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<DisplaySubjectRow> generateRowsFromBeans(ArrayList<DisplaySubjectBean> beans) {
        ArrayList<DisplaySubjectRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                DisplaySubjectRow row = new DisplaySubjectRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
