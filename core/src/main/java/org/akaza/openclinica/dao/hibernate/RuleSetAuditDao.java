/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.hibernate.query.Query;

import java.util.ArrayList;

public class RuleSetAuditDao extends AbstractDomainDao<RuleSetAuditBean> {

    @Override
    public Class<RuleSetAuditBean> domainClass() {
        return RuleSetAuditBean.class;
    }

    // TODO update to CriteriaQuery 
    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSetAudit  where ruleSetAudit.ruleSetBean = :ruleSet  ";
        Query<RuleSetAuditBean> q = getCurrentSession().createQuery(query, RuleSetAuditBean.class);
        q.setParameter("ruleSet", ruleSet);
        return new ArrayList<>(q.list());
    }
}
