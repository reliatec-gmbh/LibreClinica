/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.extract.FilterBean;

import java.util.ArrayList;

/**
 * <p>
 * FilterRow.java, an extension on Shai Sachs' tabling classes, by Tom
 * Hickerson.
 * <p>
 * Keep in mind that we declare the columns here, and the compareColumn and
 * getSearchString functions, together with the ability to generate rows from
 * beans. This is used later in the servlet body when you set up the table to be
 * set to the users' HTTP request.
 *
 * @author thickerson
 *
 */
public class FilterRow extends EntityBeanRow<FilterBean, FilterRow> {
    // declare columns first
    public static final int COL_FILTERNAME = 0;
    public static final int COL_FILTERDESC = 1;
    public static final int COL_FILTEROWNER = 2;
    public static final int COL_FILTERCREATEDDATE = 3;
    public static final int COL_STATUS = 4;

    @Override
    protected int compareColumn(FilterRow row, int sortingColumn) {
        if (!row.getClass().equals(FilterRow.class)) {
            return 0;
        }

        FilterBean thisAccount = bean;
        FilterBean argAccount = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_FILTERNAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;
        case COL_FILTERDESC:
            answer = thisAccount.getDescription().toLowerCase().compareTo(argAccount.getDescription().toLowerCase());
            break;
        case COL_FILTEROWNER:
            answer = thisAccount.getOwner().getName().toLowerCase().compareTo(argAccount.getOwner().getName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        case COL_FILTERCREATEDDATE:
            answer = thisAccount.getCreatedDate().compareTo(argAccount.getCreatedDate());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        FilterBean thisAccount = (FilterBean) bean;
        return thisAccount.getName() + " " + thisAccount.getDescription();
    }

    public static ArrayList<FilterRow> generateRowsFromBeans(ArrayList<FilterBean> beans) {
        ArrayList<FilterRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                FilterRow row = new FilterRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    @Override
    public ArrayList<FilterRow> generatRowsFromBeans(ArrayList<FilterBean> beans) {
        return FilterRow.generateRowsFromBeans(beans);
    }
}
