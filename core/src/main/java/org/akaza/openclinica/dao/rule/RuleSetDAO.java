/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */

package org.akaza.openclinica.dao.rule;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.expression.Context;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.service.rule.expression.ExpressionService;

/**
 * <p>
 * Manage RuleSets
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleSetDAO extends AuditableEntityDAO<RuleSetBean> {

    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private ExpressionDAO expressionDao;
    private CRFDAO crfDao;
    private CRFVersionDAO crfVersionDao;
    private ExpressionService expressionService;
    private RuleSetRuleDAO ruleSetRuleDao;
    private RuleSetAuditDAO ruleSetAuditDao;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleSetDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return this.studyEventDefinitionDAO != null ? this.studyEventDefinitionDAO : new StudyEventDefinitionDAO(ds);
    }

    private CRFDAO getCrfDao() {
        return this.crfDao != null ? this.crfDao : new CRFDAO(ds);
    }

    private CRFVersionDAO getCrfVersionDao() {
        return this.crfVersionDao != null ? this.crfVersionDao : new CRFVersionDAO(ds);
    }

    private RuleSetAuditDAO getRuleSetAuditDao() {
        return this.ruleSetAuditDao != null ? this.ruleSetAuditDao : new RuleSetAuditDAO(ds);
    }

    private ExpressionDAO getExpressionDao() {
        return this.expressionDao != null ? this.expressionDao : new ExpressionDAO(ds);
    }

    private ExpressionService getExpressionService() {
        return this.expressionService != null ? this.expressionService : new ExpressionService(ds);
    }

    private RuleSetRuleDAO getRuleSetRuleDao() {
        return this.ruleSetRuleDao != null ? this.ruleSetRuleDao : new RuleSetRuleDAO(ds);
    }

    public RuleSetDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // ruleset_id
        this.setTypeExpected(2, TypeNames.INT);// expression_id
        this.setTypeExpected(3, TypeNames.INT);// study_event_definition_id
        this.setTypeExpected(4, TypeNames.INT);// crf_id
        this.setTypeExpected(5, TypeNames.INT);// crf_version_id
        this.setTypeExpected(6, TypeNames.INT);// study_id
        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id

    }

    public RuleSetBean update(RuleSetBean ruleSetBean) {

        ruleSetBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        this.executeUpdate(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
        }

        return ruleSetBean;
    }

    public EntityBean remove(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), new Integer(ub.getId()));
        variables.put(new Integer(2), new Integer(Status.DELETED.getId()));
        variables.put(new Integer(3), new Integer(ruleSetBean.getId()));

        this.executeUpdate(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRemoveByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.DELETED);
            getRuleSetAuditDao().create(ruleSetBean, ub);

        }

        return ruleSetBean;
    }

    public EntityBean restore(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), new Integer(ub.getId()));
        variables.put(new Integer(2), new Integer(Status.AVAILABLE.getId()));
        variables.put(new Integer(3), new Integer(ruleSetBean.getId()));

        this.executeUpdate(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRestoreByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.AVAILABLE);
            getRuleSetAuditDao().create(ruleSetBean, ub);
        }

        return ruleSetBean;
    }

    /*
     * I am going to attempt to use this create method as we use the saveOrUpdate method in Hibernate.
     */
    public RuleSetBean create(RuleSetBean ruleSetBean) {
        if (ruleSetBean.getId() == 0) {
            HashMap<Integer, Object> variables = new HashMap<>();
            HashMap<Integer, Integer> nullVars = new HashMap<>();
            variables.put(new Integer(1), getExpressionDao().create(ruleSetBean.getTarget()).getId());
            variables.put(new Integer(2), new Integer(ruleSetBean.getStudyEventDefinition().getId()));
            if (ruleSetBean.getCrf() == null) {
                nullVars.put(new Integer(3), new Integer(Types.INTEGER));
                variables.put(new Integer(3), null);
        } else {
                variables.put(new Integer(3), new Integer(ruleSetBean.getCrf().getId()));
            }
            if (ruleSetBean.getCrfVersion() == null) {
                nullVars.put(new Integer(4), new Integer(Types.INTEGER));
                variables.put(new Integer(4), null);
            } else {
                variables.put(new Integer(4), new Integer(ruleSetBean.getCrfVersion().getId()));
            }
            variables.put(new Integer(5), new Integer(ruleSetBean.getStudy().getId()));
            variables.put(new Integer(6), new Integer(ruleSetBean.getOwnerId()));
            variables.put(new Integer(7), new Integer(Status.AVAILABLE.getId()));

            executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
            if (isQuerySuccessful()) {
                ruleSetBean.setId(getLatestPK());
            }

        }
        return ruleSetBean;
    }

    public RuleSetBean getEntityFromHashMap(HashMap<String, Object> hm) {
        RuleSetBean ruleSetBean = new RuleSetBean();
        this.setEntityAuditInformation(ruleSetBean, hm);

        ruleSetBean.setId(((Integer) hm.get("rule_set_id")).intValue());
        int expressionId = ((Integer) hm.get("rule_expression_id")).intValue();
        ExpressionBean expression = (ExpressionBean) getExpressionDao().findByPK(expressionId);
        ruleSetBean.setTarget(expression);
        ruleSetBean.setOriginalTarget(expression);
        ruleSetBean.setItemGroup(getExpressionService().getItemGroupExpression(ruleSetBean.getTarget().getValue()));
        ruleSetBean.setItem(getExpressionService().getItemExpression(ruleSetBean.getTarget().getValue(), ruleSetBean.getItemGroup()));
        int studyEventDefenitionId = ((Integer) hm.get("study_event_definition_id")).intValue();
        ruleSetBean.setStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(studyEventDefenitionId));
        int crfId = ((Integer) hm.get("crf_id")).intValue();
        ruleSetBean.setCrf((CRFBean) getCrfDao().findByPK(crfId));
        if ((Integer) hm.get("crf_version_id") != 0) {
            int crfVersionId = ((Integer) hm.get("crf_version_id")).intValue();
            ruleSetBean.setCrfVersion((CRFVersionBean) getCrfVersionDao().findByPK(crfVersionId));
        } else {
            ruleSetBean.setCrfVersion(null);
        }

        return ruleSetBean;
    }

    public RuleSetBean findByExpression(RuleSetBean ruleSetBean) {
        RuleSetBean ruleSetBeanInDb = new RuleSetBean();
        Context c = ruleSetBean.getTarget().getContext() == null ? Context.OC_RULES_V1 : ruleSetBean.getTarget().getContext();
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), c.getCode());
        variables.put(new Integer(2), ruleSetBean.getTarget().getValue());

        String sql = digester.getQuery("findByExpression");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleSetBeanInDb = (RuleSetBean) this.getEntityFromHashMap(alist.get(0));
        }
        if (alist.isEmpty()) {
            ruleSetBeanInDb = null;
        }
        return ruleSetBeanInDb;
    }

    private int getStudyId(StudyBean currentStudy) {
        return currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
    }

    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));

        String sql = digester.getQuery("findByCrfId");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionStudyAndStudyEventDefinition(CRFVersionBean crfVersionBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfVersionBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));
        variables.put(new Integer(3), sed.getId());

        String sql = digester.getQuery("findByCrfVersionStudyAndStudyEventDefinition");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), getStudyId(currentStudy));
        variables.put(new Integer(2), sed.getId());
        variables.put(new Integer(3), crfVersion.getId());
        variables.put(new Integer(4), crfBean.getId());
        variables.put(new Integer(5), crfBean.getId());

        String sql = digester.getQuery("findByCrfVersionOrCrfStudyAndStudyEventDefinition");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfStudyAndStudyEventDefinition(CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));
        variables.put(new Integer(3), sed.getId());

        String sql = digester.getQuery("findByCrfStudyAndStudyEventDefinition");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    @Override
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), getStudyId(currentStudy));

        String sql = digester.getQuery("findAllByStudy");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();
        for (HashMap<String, Object> hm : alist) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap(hm);
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public RuleSetBean findByPK(int ID) {
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap(alist.get(0));
        }
        return ruleSetBean;
    }

    public RuleSetBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer studyEventDefinitionId = Integer.valueOf(studyEventDefinition.getId());
        variables.put(new Integer(1), studyEventDefinitionId);

        String sql = digester.getQuery("findByStudyEventDefinition");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        if (alist != null && alist.size() > 0) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap(alist.get(0));
        }
        return ruleSetBean;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<RuleSetBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public RuleSetBean emptyBean() {
		return new RuleSetBean();
	}

}