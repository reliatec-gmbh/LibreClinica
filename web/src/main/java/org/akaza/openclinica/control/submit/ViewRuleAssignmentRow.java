/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/* 
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.web.domain.EntityBeanRow;

/**
 * A help class for ListCRF view to display CRF objects in show table class
 *
 * @author ywang
 * @author jxu
 *
 */
public class ViewRuleAssignmentRow extends EntityBeanRow<RuleSetBean, ViewRuleAssignmentRow> {
    // columns:
    public static final int COL_NAME = 0;

    public static final int COL_CRF = 1;

    public static final int COL_GROUP = 2;

    public static final int COL_ITEM = 3;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(ViewRuleAssignmentRow.class)) {
            return 0;
        }

        RuleSetBean thisRuleSet = (RuleSetBean) bean;
        RuleSetBean argRuleSet = (RuleSetBean) ((ViewRuleAssignmentRow) row).bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_CRF:
            answer = thisRuleSet.getCrfWithVersionName().toLowerCase().compareTo(argRuleSet.getCrfWithVersionName().toLowerCase());
            break;
        case COL_GROUP:
            answer = thisRuleSet.getGroupLabel().toLowerCase().compareTo(argRuleSet.getGroupLabel().toLowerCase());
            break;
        case COL_ITEM:
            answer = thisRuleSet.getItemName().toLowerCase().compareTo(argRuleSet.getItemName().toLowerCase());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        RuleSetBean thisRuleSet = (RuleSetBean) bean;
        return thisRuleSet.getStudyEventDefinitionName() + " " + thisRuleSet.getGroupLabel() + " " + thisRuleSet.getCrfWithVersionName() + " "
            + thisRuleSet.getItemName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList<ViewRuleAssignmentRow> generatRowsFromBeans(ArrayList<RuleSetBean> beans) {
        return ViewRuleAssignmentRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<ViewRuleAssignmentRow> generateRowsFromBeans(List<RuleSetBean> beans) {
        ArrayList<ViewRuleAssignmentRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                ViewRuleAssignmentRow row = new ViewRuleAssignmentRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
}