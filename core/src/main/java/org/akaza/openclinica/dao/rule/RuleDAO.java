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
package org.akaza.openclinica.dao.rule;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.rule.RuleBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <p>
 * Manage Rules
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleDAO extends AuditableEntityDAO<RuleBean> {

    private ExpressionDAO expressionDao;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private ExpressionDAO getExpressionDao() {
        return this.expressionDao != null ? this.expressionDao : new ExpressionDAO(ds);
    }

    public RuleDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_id
        this.setTypeExpected(2, TypeNames.STRING); // name
        this.setTypeExpected(3, TypeNames.STRING); // description
        this.setTypeExpected(4, TypeNames.STRING); // oc_oid
        this.setTypeExpected(5, TypeNames.BOOL); // enabled
        this.setTypeExpected(6, TypeNames.INT); // expression_id

        // Standard set of fields
        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id
        this.setTypeExpected(12, TypeNames.INT);// version
        this.setTypeExpected(13, TypeNames.INT);// study_id
    }

    public RuleBean update(RuleBean ruleBean) {
        ruleBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), ruleBean.getName());
        variables.put(new Integer(2), ruleBean.getDescription());
        variables.put(new Integer(3), ruleBean.getUpdaterId());
        variables.put(new Integer(4), ruleBean.getId());
        getExpressionDao().update(ruleBean.getExpression());

        this.executeUpdate(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleBean.setActive(true);
        }

        return ruleBean;
    }

    @Override
    public RuleBean create(RuleBean ruleBean) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        variables.put(new Integer(1), ruleBean.getName());
        variables.put(new Integer(2), ruleBean.getDescription());
        variables.put(new Integer(3), ruleBean.getOid());
        variables.put(new Integer(4), ruleBean.isEnabled());
        variables.put(new Integer(5), getExpressionDao().create(ruleBean.getExpression()).getId());

        variables.put(new Integer(6), new Integer(ruleBean.getOwnerId()));
        variables.put(new Integer(7), new Integer(Status.AVAILABLE.getId()));

        executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            ruleBean.setId(getLatestPK());
        }

        return ruleBean;
    }

    public RuleBean getEntityFromHashMap(HashMap<String, Object> hm) {
        RuleBean ruleBean = new RuleBean();
        this.setEntityAuditInformation(ruleBean, hm);

        ruleBean.setId(((Integer) hm.get("rule_id")).intValue());
        ruleBean.setName(((String) hm.get("name")));
        ruleBean.setOid(((String) hm.get("oc_oid")));
        ruleBean.setEnabled(((Boolean) hm.get("enabled")));
        int expressionId = ((Integer) hm.get("rule_expression_id")).intValue();
        ruleBean.setExpression((ExpressionBean) getExpressionDao().findByPK(expressionId));

        return ruleBean;
    }

    public ArrayList<RuleBean> findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleBean> ruleSetBeans = new ArrayList<RuleBean>();
        for(HashMap<String, Object> hm : alist) {
        	RuleBean ruleSet = (RuleBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleBean ruleBean = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleBean = this.getEntityFromHashMap(alist.get(0));
        }

        return ruleBean;
    }

    public RuleBean findByOid(RuleBean ruleBean) {
        RuleBean ruleBeanInDb = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new String(ruleBean.getOid()));

        String sql = digester.getQuery("findByOid");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleBeanInDb = (RuleBean) this.getEntityFromHashMap(alist.get(0));
        }
        if (alist.isEmpty()) {
            ruleBeanInDb = null;
        }
        return ruleBeanInDb;
    }

    public RuleBean findByOid(String oid) {
        RuleBean ruleBeanInDb = new RuleBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new String(oid));

        String sql = digester.getQuery("findByOid");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleBeanInDb = (RuleBean) this.getEntityFromHashMap(alist.get(0));
        }
        if (alist.isEmpty()) {
            ruleBeanInDb = null;
        }
        return ruleBeanInDb;
    }

    public ArrayList<RuleBean> findByRuleSet(RuleSetBean ruleSet) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer eventCrfBeanId = Integer.valueOf(ruleSet.getId());
        variables.put(new Integer(1), eventCrfBeanId);

        String sql = digester.getQuery("findByRuleSet");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        ArrayList<RuleBean> ruleSetBeans = new ArrayList<RuleBean>();
        for(HashMap<String, Object> hm : alist) {
            RuleBean ruleBean = (RuleBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleBean);
        }
        return ruleSetBeans;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleBean emptyBean() {
		return new RuleBean();
	}

}