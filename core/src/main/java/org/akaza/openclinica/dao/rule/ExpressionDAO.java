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
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.rule.expression.Context;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <p>
 * Manage Rules
 * </p>
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class ExpressionDAO extends AuditableEntityDAO<ExpressionBean> {

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public ExpressionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public ExpressionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_EXPRESSION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_expression_id
        this.setTypeExpected(2, TypeNames.STRING); // value
        this.setTypeExpected(3, TypeNames.INT); // context

        // Standard set of fields
        this.setTypeExpected(4, TypeNames.INT);// owner_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE);// date_updated
        this.setTypeExpected(7, TypeNames.INT);// updater_id
        this.setTypeExpected(8, TypeNames.INT);// status_id
        this.setTypeExpected(9, TypeNames.INT);// version
    }

    public ExpressionBean update(ExpressionBean expressionBean) {
        expressionBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), expressionBean.getContext().getCode());
        variables.put(new Integer(2), expressionBean.getValue());
        variables.put(new Integer(3), expressionBean.getUpdaterId());
        variables.put(new Integer(4), expressionBean.getId());

        this.executeUpdate(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            expressionBean.setActive(true);
        }

        return expressionBean;
    }

    public ExpressionBean create(ExpressionBean expressionBean) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), expressionBean.getContext().getCode());
        variables.put(new Integer(2), expressionBean.getValue());

        variables.put(new Integer(3), new Integer(expressionBean.getOwnerId()));
        variables.put(new Integer(4), new Integer(Status.AVAILABLE.getId()));

        executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            expressionBean.setId(getLatestPK());
        }

        return expressionBean;
    }

    public ExpressionBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ExpressionBean expressionBean = new ExpressionBean();
        this.setEntityAuditInformation(expressionBean, hm);

        expressionBean.setId(((Integer) hm.get("rule_expression_id")).intValue());
        expressionBean.setContext(Context.getByCode(((Integer) hm.get("context"))));
        expressionBean.setValue(((String) hm.get("value")));

        return expressionBean;
    }

    /*
     * Do not return All
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAll()
     */
     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ExpressionBean> findAll() {
        throw new RuntimeException("Not implemented");
    }

    public ExpressionBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ExpressionBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ExpressionBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
     /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ExpressionBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public ExpressionBean emptyBean() {
		return new ExpressionBean();
	}

}
