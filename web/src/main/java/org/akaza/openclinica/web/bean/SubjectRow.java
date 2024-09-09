/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.bean;

import java.util.ArrayList;

import org.akaza.openclinica.bean.submit.SubjectBean;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SubjectRow extends EntityBeanRow<SubjectBean, SubjectRow> {
    // columns:
    public static final int COL_NAME = 0;

    public static final int COL_GENDER = 1;

    public static final int COL_DATE_CREATED = 2;

    public static final int COL_OWNER = 3;

    public static final int COL_DATE_UPDATED = 4;

    public static final int COL_UPDATER = 5;

    public static final int COL_STATUS = 6;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(SubjectRow row, int sortingColumn) {
        if (!row.getClass().equals(SubjectRow.class)) {
            return 0;
        }

        SubjectBean thisSubject = bean;
        SubjectBean argSubject = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisSubject.getName().toLowerCase().compareTo(argSubject.getName().toLowerCase());
            break;
        case COL_GENDER:
            answer = (thisSubject.getGender() + "").compareTo(argSubject.getGender() + "");
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisSubject.getCreatedDate(), argSubject.getCreatedDate());
            break;
        case COL_OWNER:
            answer = thisSubject.getOwner().getName().toLowerCase().compareTo(argSubject.getOwner().getName().toLowerCase());
            break;
        case COL_DATE_UPDATED:
            answer = compareDate(thisSubject.getUpdatedDate(), argSubject.getUpdatedDate());
            break;
        case COL_UPDATER:
            answer = thisSubject.getUpdater().getName().toLowerCase().compareTo(argSubject.getUpdater().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisSubject.getStatus().compareTo(argSubject.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        SubjectBean thisSubject = bean;
        return thisSubject.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<SubjectRow> generatRowsFromBeans(ArrayList<SubjectBean> beans) {
        return SubjectRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<SubjectRow> generateRowsFromBeans(ArrayList<SubjectBean> beans) {
        ArrayList<SubjectRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                SubjectRow row = new SubjectRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
