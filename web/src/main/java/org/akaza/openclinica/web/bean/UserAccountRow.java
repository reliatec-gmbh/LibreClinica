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

import org.akaza.openclinica.bean.login.UserAccountBean;

/**
 * @author ssachs
 */
public class UserAccountRow extends EntityBeanRow<UserAccountBean, UserAccountRow> {
    // columns:
    public static final int COL_USERNAME = 0;
    public static final int COL_FIRSTNAME = 1;
    public static final int COL_LASTNAME = 2;
    public static final int COL_STATUS = 3;
    public static final int COL_AUTHTYPE = 4;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(UserAccountRow row, int sortingColumn) {
        if (!row.getClass().equals(UserAccountRow.class)) {
            return 0;
        }

        UserAccountBean thisAccount = bean;
        UserAccountBean argAccount = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_USERNAME:
            answer = thisAccount.getName().toLowerCase().compareTo(argAccount.getName().toLowerCase());
            break;
        case COL_FIRSTNAME:
            answer = thisAccount.getFirstName().toLowerCase().compareTo(argAccount.getFirstName().toLowerCase());
            break;
        case COL_LASTNAME:
            answer = thisAccount.getLastName().toLowerCase().compareTo(argAccount.getLastName().toLowerCase());
            break;
        case COL_STATUS:
            answer = thisAccount.getStatus().compareTo(argAccount.getStatus());
            break;
        case COL_AUTHTYPE:
            answer = thisAccount.getAuthtype().compareTo(argAccount.getAuthtype());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        UserAccountBean thisAccount = bean;
        return thisAccount.getName() + " " + thisAccount.getFirstName() + " " + thisAccount.getLastName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<UserAccountRow> generatRowsFromBeans(ArrayList<UserAccountBean> beans) {
        return UserAccountRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<UserAccountRow> generateRowsFromBeans(ArrayList<UserAccountBean> beans) {
        ArrayList<UserAccountRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                UserAccountRow row = new UserAccountRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
}