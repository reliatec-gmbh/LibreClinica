/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */

package org.akaza.openclinica.dao.rule.action;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;
import org.akaza.openclinica.bean.rule.action.ActionType;
import org.akaza.openclinica.bean.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.bean.rule.action.EmailActionBean;
import org.akaza.openclinica.bean.rule.action.RuleActionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <p>
 * Manage Actions
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleActionDAO extends AuditableEntityDAO<RuleActionBean> {

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleActionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public RuleActionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULE_ACTION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_action_id
        this.setTypeExpected(2, TypeNames.INT); // rule_set_rule_id
        this.setTypeExpected(3, TypeNames.INT); // action_type
        this.setTypeExpected(4, TypeNames.BOOL); // expression_evaluates_to
        this.setTypeExpected(5, TypeNames.STRING); // message
        this.setTypeExpected(6, TypeNames.STRING); // email_to

        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id

    }

    @Override
    public RuleActionBean update(RuleActionBean ruleBean) {
        ruleBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), ruleBean.getName());

        this.executeUpdate(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleBean.setActive(true);
        }

        return ruleBean;
    }

    @Override
    public RuleActionBean create(RuleActionBean eb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        RuleActionBean ruleAction = null;

        if (eb instanceof DiscrepancyNoteActionBean) {
            DiscrepancyNoteActionBean dnActionBean = (DiscrepancyNoteActionBean) eb;
            Boolean expressionEvaluates = dnActionBean.getExpressionEvaluatesTo() == null ? true : dnActionBean.getExpressionEvaluatesTo();
            variables.put(new Integer(1), new Integer(dnActionBean.getRuleSetRule().getId()));
            variables.put(new Integer(2), dnActionBean.getActionType().getCode());
            variables.put(new Integer(3), expressionEvaluates);
            variables.put(new Integer(4), dnActionBean.getMessage());

            variables.put(new Integer(5), new Integer(dnActionBean.getOwnerId()));
            variables.put(new Integer(6), new Integer(Status.AVAILABLE.getId()));

            executeUpdateWithPK(digester.getQuery("create_dn"), variables, nullVars);
            if (isQuerySuccessful()) {
                dnActionBean.setId(getLatestPK());
            }

            ruleAction = dnActionBean;
        }

        if (eb instanceof EmailActionBean) {
            EmailActionBean emailActionBean = (EmailActionBean) eb;
            Boolean expressionEvaluates = emailActionBean.getExpressionEvaluatesTo() == null ? true : emailActionBean.getExpressionEvaluatesTo();
            variables.put(new Integer(1), new Integer(emailActionBean.getRuleSetRule().getId()));
            variables.put(new Integer(2), emailActionBean.getActionType().getCode());
            variables.put(new Integer(3), expressionEvaluates);
            variables.put(new Integer(4), emailActionBean.getMessage());
            variables.put(new Integer(5), emailActionBean.getTo());

            variables.put(new Integer(6), new Integer(emailActionBean.getOwnerId()));
            variables.put(new Integer(7), new Integer(Status.AVAILABLE.getId()));

            executeUpdateWithPK(digester.getQuery("create_email"), variables, nullVars);
            if (isQuerySuccessful()) {
                emailActionBean.setId(getLatestPK());
            }

            ruleAction = emailActionBean;
        }

        return ruleAction;
    }

    public RuleActionBean getEntityFromHashMap(HashMap<String, Object> hm) {

        int actionTypeId = ((Integer) hm.get("action_type")).intValue();
        ActionType actionType = ActionType.getByCode(actionTypeId);
        RuleActionBean ruleAction;

        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            ruleAction = new DiscrepancyNoteActionBean();
            ((DiscrepancyNoteActionBean) ruleAction).setMessage(((String) hm.get("message")));
            break;
        case EMAIL:
            ruleAction = new EmailActionBean();
            ((EmailActionBean) ruleAction).setMessage(((String) hm.get("message")));
            ((EmailActionBean) ruleAction).setTo(((String) hm.get("email_to")));
            break;
        default:
        	 ruleAction = null;
        }

        this.setEntityAuditInformation(ruleAction, hm);
        ruleAction.setActionType(actionType);
        ruleAction.setId(((Integer) hm.get("rule_action_id")).intValue());
        ruleAction.setExpressionEvaluatesTo(((Boolean) hm.get("expression_evaluates_to")).booleanValue());

        return ruleAction;
    }

    public ArrayList<RuleActionBean> findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleActionBean> ruleSetBeans = new ArrayList<RuleActionBean>();
        for(HashMap<String, Object> hm : alist) {
            RuleActionBean ruleSet = this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleActionBean action = new RuleActionBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            action = this.getEntityFromHashMap(alist.get(0));
        }

        return action;
    }

    public ArrayList<RuleActionBean> findByRuleSetRule(RuleSetRuleBean ruleSetRule) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer ruleSetRuleId = Integer.valueOf(ruleSetRule.getId());
        variables.put(new Integer(1), ruleSetRuleId);

        String sql = digester.getQuery("findByRuleSetRule");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        ArrayList<RuleActionBean> ruleActionBeans = new ArrayList<RuleActionBean>();
        for(HashMap<String, Object> hm : alist) {
            RuleActionBean ruleActionBean = this.getEntityFromHashMap(hm);
            ruleActionBean.setRuleSetRule(ruleSetRule);
            ruleActionBeans.add(ruleActionBean);
        }
        return ruleActionBeans;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleActionBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleActionBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleActionBean> findAllByPermission(Object objCurrentUser, int intActionType) {
       throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleActionBean emptyBean() {
		return new RuleActionBean();
	}
}