/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class StudyGroupClassRow extends EntityBeanRow<StudyGroupClassBean, StudyGroupClassRow> {
    // columns:
    public static final int COL_NAME = 0;
    public static final int COL_TYPE = 1;
    public static final int COL_SUBJECT_ASSIGNMENT = 2;
    public static final int COL_STATUS = 3;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(StudyGroupClassRow row, int sortingColumn) {
        if (!row.getClass().equals(StudyGroupClassRow.class)) {
            return 0;
        }

        StudyGroupClassBean thisStudy = bean;
        StudyGroupClassBean argStudy = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisStudy.getName().toLowerCase().compareTo(argStudy.getName().toLowerCase());
            break;
        case COL_TYPE:
            answer = thisStudy.getGroupClassTypeName().toLowerCase().compareTo(argStudy.getGroupClassTypeName().toLowerCase());
            break;
        case COL_SUBJECT_ASSIGNMENT:
            answer = thisStudy.getSubjectAssignment().toLowerCase().compareTo(argStudy.getSubjectAssignment().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisStudy.getStatus().compareTo(argStudy.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyGroupClassBean thisStudy = (StudyGroupClassBean) bean;
        return thisStudy.getName() + " " + thisStudy.getGroupClassTypeName() + " " + thisStudy.getSubjectAssignment();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<StudyGroupClassRow> generatRowsFromBeans(ArrayList<StudyGroupClassBean> beans) {
        return StudyGroupClassRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<StudyGroupClassRow> generateRowsFromBeans(ArrayList<StudyGroupClassBean> beans) {
        ArrayList<StudyGroupClassRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyGroupClassRow row = new StudyGroupClassRow();
                row.setBean((StudyGroupClassBean) beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
