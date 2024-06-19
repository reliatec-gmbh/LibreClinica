/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.admin.AuditEventBean;

import java.util.ArrayList;

/**
 * @author thickerson
 *
 */
public class AuditEventRow extends EntityBeanRow<AuditEventBean, AuditEventRow> {
    public static final int AUDIT_DATE = 0;
    public static final int AUDIT_ACTION = 1;
    public static final int AUDIT_ENTITY = 2;
    // public static final int AUDIT_STUDY = 3;
    // TODO MAKE CUSTOMIZED ROWS FOR EACH ONE, TBH
    // this one will be for user, and therefore not have the user
    // column, tbh
    public static final int AUDIT_STUDY_NAME = 3;
    public static final int AUDIT_SUBJECT_NAME = 4;
    public static final int AUDIT_CHANGES = 5;
    public static final int AUDIT_OTHER_INFO = 6;

    @Override
    protected int compareColumn(AuditEventRow row, int sortingColumn) {
        if (!row.getClass().equals(AuditEventRow.class)) {
            return 0;
        }

        AuditEventBean thisBean = bean;
        AuditEventBean argBean = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case AUDIT_DATE:
            answer = // thisBean.getAuditDate().compareTo(argBean.getAuditDate());
                compareDate(thisBean.getAuditDate(), argBean.getAuditDate());
            break;
        case AUDIT_ACTION:
            answer = thisBean.getReasonForChange().toLowerCase().compareTo(argBean.getReasonForChange().toLowerCase());
            break;
        case AUDIT_ENTITY:
            answer = thisBean.getAuditTable().toLowerCase().compareTo(argBean.getAuditTable().toLowerCase());
            break;
        case AUDIT_STUDY_NAME:
            answer = new Integer(thisBean.getStudyName()).compareTo(new Integer(argBean.getStudyName()));
            break;
        case AUDIT_SUBJECT_NAME:
            answer = new Integer(thisBean.getSubjectName()).compareTo(new Integer(argBean.getSubjectName()));
            break;
        case AUDIT_CHANGES:
            // LEAVE THIS BLANK?
            // answer = thisCRF.getUpdater().getName().toLowerCase().compareTo(
            // argCRF.getUpdater().getName().toLowerCase());
            break;
        case AUDIT_OTHER_INFO:
            // LEAVE THIS BLANK?
            // answer = thisCRF.getStatus().compareTo(argCRF.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        AuditEventBean thisBean = (AuditEventBean) bean;
        return thisBean.getAuditTable() + " " + thisBean.getEntityId();// ?
    }

    @Override
    public ArrayList<AuditEventRow> generatRowsFromBeans(ArrayList<AuditEventBean> beans) {
        return AuditEventRow.generateRowsFromBeans(beans);
    }

    public static ArrayList<AuditEventRow> generateRowsFromBeans(ArrayList<AuditEventBean> beans) {
        ArrayList<AuditEventRow> answer = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            try {
                AuditEventRow row = new AuditEventRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

}
