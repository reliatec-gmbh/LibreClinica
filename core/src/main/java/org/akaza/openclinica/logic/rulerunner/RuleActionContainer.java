/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;

public class RuleActionContainer implements Comparable<RuleActionBean> {
    RuleActionBean ruleAction;
    ExpressionBean expressionBean;
    ItemDataBean itemDataBean;
    RuleSetBean ruleSetBean;

    public RuleActionContainer(RuleActionBean ruleAction, ExpressionBean expressionBean, ItemDataBean itemDataBean, RuleSetBean ruleSetBean) {
        super();
        this.ruleAction = ruleAction;
        this.expressionBean = expressionBean;
        this.itemDataBean = itemDataBean;
        this.ruleSetBean = ruleSetBean;

    }

    public RuleActionBean getRuleAction() {
        return ruleAction;
    }

    public void setRuleAction(RuleActionBean ruleAction) {
        this.ruleAction = ruleAction;
    }

    public ExpressionBean getExpressionBean() {
        return expressionBean;
    }

    public void setExpressionBean(ExpressionBean expressionBean) {
        this.expressionBean = expressionBean;
    }

    public ItemDataBean getItemDataBean() {
        return itemDataBean;
    }

    public void setItemDataBean(ItemDataBean itemDataBean) {
        this.itemDataBean = itemDataBean;
    }

    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }

    public int compareTo(RuleActionBean o) {
        // TODO Auto-generated method stub
        return 0;
    }
}
