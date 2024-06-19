/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class StudyEventRow extends EntityBeanRow<StudyEventBean, StudyEventRow> {
    // columns:

    public static final int COL_STUDY_SUBJECT_LABEL = 0;

    public static final int COL_START_DATE = 1;

    public static final int COL_SUBJECT_EVENT_STATUS = 2;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(StudyEventRow row, int sortingColumn) {
        if (!row.getClass().equals(StudyEventRow.class)) {
            return 0;
        }

        StudyEventBean thisEvent = bean;
        StudyEventBean argEvent = row.bean;

        int answer = 0;
        switch (sortingColumn) {

        case COL_STUDY_SUBJECT_LABEL:
            answer = thisEvent.getStudySubjectLabel().toLowerCase().compareTo(argEvent.getStudySubjectLabel().toLowerCase());
            break;

        case COL_START_DATE:
            answer = compareDate(thisEvent.getDateStarted(), argEvent.getDateStarted());
            break;

        case COL_SUBJECT_EVENT_STATUS:
            answer = thisEvent.getSubjectEventStatus().compareTo(argEvent.getSubjectEventStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyEventBean thisEvent = (StudyEventBean) bean;
        return thisEvent.getStudySubjectLabel() + " " + thisEvent.getSubjectEventStatus().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<StudyEventRow> generatRowsFromBeans(ArrayList<StudyEventBean> beans) {
        return StudyEventRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<StudyEventRow> generateRowsFromBeans(ArrayList<StudyEventBean> beans) {
        ArrayList<StudyEventRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyEventRow row = new StudyEventRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
