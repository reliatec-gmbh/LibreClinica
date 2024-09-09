/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.domain.rule.action.ActionType;

import java.util.Comparator;
import java.util.HashMap;

public class RuleActionContainerComparator implements Comparator<RuleActionContainer> {

    HashMap<ActionType, String> order = new HashMap<ActionType, String>();

    public RuleActionContainerComparator() {
        order.put(ActionType.EMAIL, "1");
        order.put(ActionType.FILE_DISCREPANCY_NOTE, "2");
        order.put(ActionType.INSERT, "3");
        order.put(ActionType.SHOW, "4");
        order.put(ActionType.HIDE, "5");
        order.put(ActionType.EVENT,"6");
        order.put(ActionType.NOTIFICATION,"7");
        order.put(ActionType.RANDOMIZE,"8");
    }

    public int compare(RuleActionContainer o1, RuleActionContainer o2) {
        return order.get(o1.getRuleAction().getActionType()).compareTo(order.get(o2.getRuleAction().getActionType()));
    }

}