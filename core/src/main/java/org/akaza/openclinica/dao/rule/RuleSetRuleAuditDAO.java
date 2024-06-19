/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/* 
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.dao.rule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleAuditBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaException;

public class RuleSetRuleAuditDAO extends EntityDAO<RuleSetRuleAuditBean> {

    RuleSetDAO ruleSetDao;
    RuleSetRuleDAO ruleSetRuleDao;
    UserAccountDAO userAccountDao;

    public RuleSetRuleAuditDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
    }

    private RuleSetRuleDAO getRuleSetRuleDao() {
        return this.ruleSetRuleDao != null ? this.ruleSetRuleDao : new RuleSetRuleDAO(ds);
    }

    private UserAccountDAO getUserAccountDao() {
        return this.userAccountDao != null ? this.userAccountDao : new UserAccountDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESETRULE_AUDIT;
    }

    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.DATE);// date_updated
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.INT);

    }

    public RuleSetRuleAuditBean getEntityFromHashMap(HashMap<String, Object> hm) {
        RuleSetRuleAuditBean ruleSetRuleAudit = new RuleSetRuleAuditBean();
        ruleSetRuleAudit.setId((Integer) hm.get("rule_set_rule_audit_id"));
        int ruleSetRuleId = (Integer) hm.get("rule_set_rule_id");
        int userAccountId = (Integer) hm.get("updater_id");
        int statusId = (Integer) hm.get("status_id");
        Date dateUpdated = (Date) hm.get("date_updated");
        ruleSetRuleAudit.setDateUpdated(dateUpdated);
        ruleSetRuleAudit.setStatus(Status.get(statusId));
        ruleSetRuleAudit.setRuleSetRuleBean((RuleSetRuleBean) getRuleSetRuleDao().findByPK(ruleSetRuleId));
        ruleSetRuleAudit.setUpdater((UserAccountBean) getUserAccountDao().findByPK(userAccountId));

        return ruleSetRuleAudit;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleAuditBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleAuditBean> findAll() throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    public RuleSetRuleAuditBean findByPK(int id) throws OpenClinicaException {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
    	String queryName = "findAllByRuleSet";
        HashMap<Integer, Object> variables = variables(ruleSet.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public RuleSetRuleAuditBean create(RuleSetRuleBean ruleSetRuleBean, UserAccountBean ub) {
        // INSERT INTO rule_set_rule_audit (rule_set_rule_id, status_id,updater_id,date_updated) VALUES (?,?,?,?,?)
        RuleSetRuleAuditBean ruleSetRuleAudit = new RuleSetRuleAuditBean();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, ruleSetRuleBean.getId());
        variables.put(2, ruleSetRuleBean.getStatus().getId());
        variables.put(3, ub.getId());

        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            ruleSetRuleAudit.setRuleSetRuleBean(ruleSetRuleBean);
            ruleSetRuleAudit.setId(getCurrentPK());
            ruleSetRuleAudit.setStatus(ruleSetRuleBean.getStatus());
            ruleSetRuleAudit.setUpdater(ub);
        }
        return ruleSetRuleAudit;
    }

    /**
     * NOT IMPLEMENTED
     */
    @Override
    public RuleSetRuleAuditBean create(RuleSetRuleAuditBean ruleSetRuleBean) throws OpenClinicaException {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    @Override
    public RuleSetRuleAuditBean update(RuleSetRuleAuditBean eb) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleAuditBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleAuditBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleSetRuleAuditBean emptyBean() {
		return new RuleSetRuleAuditBean();
	}

}
