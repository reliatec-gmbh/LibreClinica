/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.rule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.rule.RuleSetAuditBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaException;

public class RuleSetAuditDAO extends EntityDAO<RuleSetAuditBean> {

    RuleSetDAO ruleSetDao;
    UserAccountDAO userAccountDao;

    public RuleSetAuditDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
        // this.getNextPKName = "getNextPK";
    }

    private RuleSetDAO getRuleSetDao() {
        return this.ruleSetDao != null ? this.ruleSetDao : new RuleSetDAO(ds);
    }

    private UserAccountDAO getUserAccountDao() {
        return this.userAccountDao != null ? this.userAccountDao : new UserAccountDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET_AUDIT;
    }

    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.DATE);// date_updated
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.INT);

    }

    public RuleSetAuditBean getEntityFromHashMap(HashMap<String, Object> hm) {
        RuleSetAuditBean ruleSetAudit = new RuleSetAuditBean();
        ruleSetAudit.setId((Integer) hm.get("rule_set_audit_id"));
        int ruleSetId = (Integer) hm.get("rule_set_id");
        int userAccountId = (Integer) hm.get("updater_id");
        int statusId = (Integer) hm.get("status_id");
        Date dateUpdated = (Date) hm.get("date_updated");
        ruleSetAudit.setDateUpdated(dateUpdated);
        ruleSetAudit.setStatus(Status.get(statusId));
        ruleSetAudit.setRuleSetBean((RuleSetBean) getRuleSetDao().findByPK(ruleSetId));
        ruleSetAudit.setUpdater((UserAccountBean) getUserAccountDao().findByPK(userAccountId));

        return ruleSetAudit;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetAuditBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetAuditBean> findAll() throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    public RuleSetAuditBean findByPK(int id) throws OpenClinicaException {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(id);
        RuleSetAuditBean ruleSetAudit = executeFindByPKQuery(queryName, variables);
        if(ruleSetAudit != null && id != ruleSetAudit.getId()) {
        	// this is just an empty bean so no rule set audit bean was found
        	ruleSetAudit = null;
        }
        return ruleSetAudit;
    }

    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
    	String queryName = "findAllByRuleSet";
        HashMap<Integer, Object> variables = variables(ruleSet.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public RuleSetAuditBean create(RuleSetBean ruleSetBean, UserAccountBean ub) {
        // INSERT INTO rule_set_audit (rule_set_audit_id,rule_set_id, status_id,updater_id,date_updated) VALUES (?,?,?,?,?)
        RuleSetAuditBean ruleSetAudit = new RuleSetAuditBean();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, ruleSetBean.getId());
        variables.put(2, ruleSetBean.getStatus().getId());
        variables.put(3, ub.getId());

        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            ruleSetAudit.setRuleSetBean(ruleSetBean);
            ruleSetAudit.setId(getCurrentPK());
            ruleSetAudit.setStatus(ruleSetBean.getStatus());
            ruleSetAudit.setUpdater(ub);
        }
        return ruleSetAudit;
    }
    
    public RuleSetAuditBean create(RuleSetBean ruleSetBean) throws OpenClinicaException {
        UserAccountBean userAccount = new UserAccountBean();
        userAccount.setId(ruleSetBean.getUpdaterId());
        return create(ruleSetBean, userAccount);
    }
    
    /**
     * NOT IMPLEMENTED
     */
    public RuleSetAuditBean create(RuleSetAuditBean eb) throws OpenClinicaException {
    	// implementation not reasonable
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public RuleSetAuditBean update(RuleSetAuditBean eb) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetAuditBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetAuditBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleSetAuditBean emptyBean() {
		return new RuleSetAuditBean();
	}

}
