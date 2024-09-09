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
import java.util.List;

import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DisplayStudyEventRow extends EntityBeanRow<DisplayStudyEventBean, DisplayStudyEventRow> {

    // columns:

    public static final int COL_EVENT = 0;

    public static final int COL_START_DATE = 1;

    // public static final int COL_LAST_UPDATED= 2;

    public static final int COL_LOCATION = 3;

    public static final int COL_SUBJECT_EVENT_STATUS = 4;

    public static final int COL_STATUS = 5;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(DisplayStudyEventRow row, int sortingColumn) {
        if (!row.getClass().equals(DisplayStudyEventRow.class)) {
            return 0;
        }

        DisplayStudyEventBean thisEvent = bean;
        DisplayStudyEventBean argEvent = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_EVENT:
            answer =
                thisEvent.getStudyEvent().getStudyEventDefinition().getName().toLowerCase().compareTo(
                        argEvent.getStudyEvent().getStudyEventDefinition().getName().toLowerCase());
            break;
        case COL_START_DATE:
            answer = compareDate(thisEvent.getStudyEvent().getDateStarted(), argEvent.getStudyEvent().getDateStarted());
            break;
        // case COL_LAST_UPDATED:
        // answer =
        // compareDate(thisEvent.getStudyEvent().getUpdatedDate(),argEvent.getStudyEvent().getUpdatedDate());
        // break;
        case COL_LOCATION:
            answer = thisEvent.getStudyEvent().getLocation().toLowerCase().compareTo(argEvent.getStudyEvent().getLocation().toLowerCase());
            break;
        case COL_SUBJECT_EVENT_STATUS:
            answer = thisEvent.getStudyEvent().getSubjectEventStatus().compareTo(argEvent.getStudyEvent().getSubjectEventStatus());
            break;
        case COL_STATUS:
            answer = thisEvent.getStudyEvent().getStatus().compareTo(argEvent.getStudyEvent().getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        DisplayStudyEventBean thisEvent = (DisplayStudyEventBean) bean;
        return thisEvent.getStudyEvent().getStudyEventDefinition().getName() + " " + thisEvent.getStudyEvent().getLocation();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<DisplayStudyEventRow> generatRowsFromBeans(ArrayList<DisplayStudyEventBean> beans) {
        return DisplayStudyEventRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<DisplayStudyEventRow> generateRowsFromBeans(List<DisplayStudyEventBean> beans) {
        ArrayList<DisplayStudyEventRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                DisplayStudyEventRow row = new DisplayStudyEventRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
