/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * Created on Sep 1, 2005
 *
 *
 */
package org.akaza.openclinica.logic.masking;

import org.akaza.openclinica.bean.masking.MaskingBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.logic.core.BusinessEvaluator;
import org.akaza.openclinica.logic.core.BusinessRule;
import org.akaza.openclinica.logic.masking.rules.MaskSubjectDOBRule;

/**
 * @author thickerson
 *
 *
 */
public class SubjectMaskingEvaluator extends BusinessEvaluator {
    protected MaskingBean mBean;

    public SubjectMaskingEvaluator(SubjectBean sb, MaskingBean mBean) {
        super(sb);
        assertRuleSet();
        this.mBean = mBean;
    }

    @Override
    public void assertRuleSet() {
        // TODO accept a MaskingObject from a DAO and have a big if-then
        // chain????
        // ruleSet.add(new maskDOBRule());
        if (mBean.getRuleMap().containsKey("org.akaza.openclinica.logic.masking.rule.MaskSubjectDOBRule"))
            ruleSet.add(new MaskSubjectDOBRule());
    }

    @Override
    protected void evaluateRuleSet() {
        // can modify this as necessary? tbh
        synchronized (this) {
            for(BusinessRule bRule : ruleSet) {
                if (bRule.isPropertyTrue(bRule.getClass().getName())) {
                    bRule.doAction(businessObject);
                }
            }
            hasBeenUpdated = false;
        }
    }
}
