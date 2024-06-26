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

import org.akaza.openclinica.bean.admin.TriggerBean;

/**
 * 
 * @author thickerson, dec 2008
 *
 */
public class TriggerRow extends EntityBeanRow<TriggerBean, TriggerRow> {
	// columns:
    public static final int COL_TRIGGER_NAME = 0;
    public static final int COL_LAST_FIRED_DATE = 1;
    public static final int COL_NEXT_FIRED_DATE = 2;
    public static final int COL_DESCRIPTION = 3;
	public static final int COL_PERIOD = 4;
	public static final int COL_DATASET_NAME = 5;
	public static final int COL_STUDY_NAME = 6;
	
	@Override
	protected int compareColumn(TriggerRow row, int sortingColumn) {
		if (!row.getClass().equals(TriggerRow.class)) {
            return 0;
        }

        TriggerBean thisTrigger = bean;
        TriggerBean argTrigger = row.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_TRIGGER_NAME:
            answer = thisTrigger.getFullName().toLowerCase().compareTo(argTrigger.getFullName().toLowerCase());
            break;
        case COL_LAST_FIRED_DATE:
            answer = thisTrigger.getPreviousDate().compareTo(argTrigger.getPreviousDate());
            break;
        case COL_NEXT_FIRED_DATE:
            answer = thisTrigger.getNextDate().compareTo(argTrigger.getNextDate());
            break;
        case COL_DESCRIPTION:
            answer = thisTrigger.getDescription().compareTo(argTrigger.getDescription());
            break;
		case COL_PERIOD:
            answer = thisTrigger.getPeriodToRun().compareTo(argTrigger.getPeriodToRun());
            break;
		case COL_DATASET_NAME:
            answer = thisTrigger.getDatasetName().compareTo(argTrigger.getDatasetName());
            break;
		case COL_STUDY_NAME:
			answer = thisTrigger.getStudyName().compareTo(argTrigger.getStudyName());
        }

        return answer;
	}

	@Override
	public ArrayList<TriggerRow> generatRowsFromBeans(ArrayList<TriggerBean> beans) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getSearchString() {
        TriggerBean thisTrigger = (TriggerBean) bean;
        return thisTrigger.getFullName() + " " + thisTrigger.getDescription() + " " + thisTrigger.getPeriodToRun() + " " + thisTrigger.getDatasetName();
    }
	
	public static ArrayList<TriggerRow> generateRowsFromBeans(ArrayList<TriggerBean> beans) {
        ArrayList<TriggerRow> answer = new ArrayList<>();
        
        for (int i = 0; i < beans.size(); i++) {
            try {
                TriggerRow row = new TriggerRow();
                row.setBean(beans.get(i));
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }
	
	

}
