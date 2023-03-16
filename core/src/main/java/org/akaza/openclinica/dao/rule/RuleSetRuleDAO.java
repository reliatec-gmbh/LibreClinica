/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */

package org.akaza.openclinica.dao.rule;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.RuleBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

/**
 * <p>
 * Manage RuleSets
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleSetRuleDAO extends AuditableEntityDAO<RuleSetRuleBean> {

    private RuleDAO ruleDao;
    private RuleSetDAO ruleSetDao;
    private RuleSetRuleAuditDAO ruleSetRuleAuditDao;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleSetRuleDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private RuleDAO getRuleDao() {
        return this.ruleDao != null ? this.ruleDao : new RuleDAO(ds);
    }

    private RuleSetDAO getRuleSetDao() {
        return this.ruleSetDao != null ? this.ruleSetDao : new RuleSetDAO(ds);
    }

    public RuleSetRuleDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    private RuleSetRuleAuditDAO getRuleSetRuleAuditDao() {
        return this.ruleSetRuleAuditDao != null ? this.ruleSetRuleAuditDao : new RuleSetRuleAuditDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET_RULE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_set_rule_id
        this.setTypeExpected(2, TypeNames.INT);// rule_set_id
        this.setTypeExpected(3, TypeNames.INT);// rule_id
        this.setTypeExpected(4, TypeNames.INT);// owner_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE);// date_updated
        this.setTypeExpected(7, TypeNames.INT);// updater_id
        this.setTypeExpected(8, TypeNames.INT);// status_id

    }

    /**
     * Don't understand why I have to implement this if I don't need to. I understand the motive but with complex Object graphs it is not always CRUD.
     * 
     * @param eb
     * @return
     */
    @Override
    public RuleSetRuleBean update(RuleSetRuleBean eb) throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeByRuleSet(RuleSetBean eb) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), ruleSetBean.getUpdaterId());
        variables.put(new Integer(2), Status.DELETED.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        executeUpdate(digester.getQuery("updateStatusByRuleSet"), variables);

    }

    public void autoRemoveByRuleSet(RuleSetBean eb, UserAccountBean ub) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), ruleSetBean.getUpdaterId());
        variables.put(new Integer(2), Status.AUTO_DELETED.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        variables.put(new Integer(4), Status.AVAILABLE.getId());
        executeUpdate(digester.getQuery("updateStatusByRuleSetAuto"), variables);

    }

    public void autoRestoreByRuleSet(RuleSetBean eb, UserAccountBean ub) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.AVAILABLE.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        variables.put(new Integer(4), Status.AUTO_DELETED.getId());
        executeUpdate(digester.getQuery("updateStatusByRuleSetAuto"), variables);

    }

    public void remove(RuleSetRuleBean eb, UserAccountBean ub) {

        RuleSetRuleBean ruleSetRuleBean = eb;
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.DELETED.getId());
        variables.put(new Integer(3), ruleSetRuleBean.getId());
        executeUpdate(digester.getQuery("updateStatus"), variables);
        if (isQuerySuccessful()) {
            ruleSetRuleBean.setStatus(Status.DELETED);
            getRuleSetRuleAuditDao().create(ruleSetRuleBean, ub);
        }

    }

    public void restore(RuleSetRuleBean eb, UserAccountBean ub) {

        RuleSetRuleBean ruleSetRuleBean = eb;
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.AVAILABLE.getId());
        variables.put(new Integer(3), ruleSetRuleBean.getId());
        executeUpdate(digester.getQuery("updateStatus"), variables);

        if (isQuerySuccessful()) {
            ruleSetRuleBean.setStatus(Status.AVAILABLE);
            getRuleSetRuleAuditDao().create(ruleSetRuleBean, ub);
        }

    }

    /*
     * I am going to attempt to use this create method as we use the saveOrUpdate method in Hibernate.
     */
    @Override
    public RuleSetRuleBean create(RuleSetRuleBean ruleSetRuleBean) {
        RuleBean ruleBean = new RuleBean();
        ruleBean.setOid(ruleSetRuleBean.getOid());

        if (ruleSetRuleBean.getId() == 0) {
            HashMap<Integer, Object> variables = new HashMap<>();
            HashMap<Integer, Integer> nullVars = new HashMap<>();
            variables.put(new Integer(1), ruleSetRuleBean.getRuleSetBean().getId());
            variables.put(new Integer(2), getRuleDao().findByOid(ruleBean).getId());
            variables.put(new Integer(3), new Integer(ruleSetRuleBean.getOwnerId()));
            variables.put(new Integer(4), new Integer(Status.AVAILABLE.getId()));

            executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
            if (isQuerySuccessful()) {
                ruleSetRuleBean.setId(getLatestPK());
            }

        }
        // persist rules if exist
        // createRuleSetRules(ruleSetBean);

        return ruleSetRuleBean;
    }

    public RuleSetRuleBean getEntityFromHashMap(HashMap<String, Object> hm) {
    	return getEntityFromHashMap(hm, false);
    }

    public RuleSetRuleBean getEntityFromHashMap(HashMap<String, Object> hm, Boolean getRuleSet) {
        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        this.setEntityAuditInformation(ruleSetRuleBean, hm);

        ruleSetRuleBean.setId(((Integer) hm.get("rule_set_rule_id")).intValue());
        if (getRuleSet) {
            int ruleSetBeanId = ((Integer) hm.get("rule_set_id")).intValue();
            ruleSetRuleBean.setRuleSetBean(getRuleSetDao().findByPK(ruleSetBeanId));
        }
        int ruleBeanId = ((Integer) hm.get("rule_id")).intValue();
        ruleSetRuleBean.setRuleBean(getRuleDao().findByPK(ruleBeanId));

        return ruleSetRuleBean;
    }

    public ArrayList<RuleSetRuleBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    public EntityBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<RuleSetRuleBean> findByRuleSet(RuleSetBean ruleSet) {
    	String queryName = "findByRuleSetId";
        HashMap<Integer, Object> variables = variables(ruleSet.getId());
        ArrayList<RuleSetRuleBean> beans = executeFindAllQuery(queryName, variables);
        if(beans != null) {
        	beans.stream().forEach(ruleSetRule -> ruleSetRule.setRuleSetBean(ruleSet));
        }
        return beans;
    }

    public ArrayList<RuleSetRuleBean> findByRuleSetAndRule(RuleSetBean ruleSet, RuleBean rule) {
    	String queryName = "findByRuleSetIdAndRuleId";
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, ruleSet.getId());
        variables.put(2, Status.AVAILABLE.getId());
        variables.put(3, rule.getId());
        
        ArrayList<RuleSetRuleBean> beans = executeFindAllQuery(queryName, variables);
        if(beans != null) {
        	beans.stream().forEach(ruleSetRule -> ruleSetRule.setRuleSetBean(ruleSet));
        }
        return beans;
    }

    public RuleSetRuleBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        Integer studyEventDefinitionId = Integer.valueOf(studyEventDefinition.getId());
    	String queryName = "findByStudyEventDefinition";
        HashMap<Integer, Object> variables = variables(studyEventDefinitionId);
        return executeFindByPKQuery(queryName, variables);
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetRuleBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleSetRuleBean emptyBean() {
		return new RuleSetRuleBean();
	}

}