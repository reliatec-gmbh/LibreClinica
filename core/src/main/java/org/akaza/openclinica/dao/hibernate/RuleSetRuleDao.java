/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.HideActionBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.RandomizeActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class RuleSetRuleDao extends AbstractDomainDao<RuleSetRuleBean> {

    private CoreResources coreResources;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    
    @Override
    public Class<RuleSetRuleBean> domainClass() {
        return RuleSetRuleBean.class;
    }

    // TODO update to CriteriaQuery 
    public ArrayList<RuleSetRuleBean> findByRuleSetBeanAndRuleBean(RuleSetBean ruleSetBean, RuleBean ruleBean) {
        String query = "from " + getDomainClassName() + " ruleSetRule  where ruleSetRule.ruleSetBean = :ruleSetBean" + " AND ruleSetRule.ruleBean = :ruleBean ";
        Query<RuleSetRuleBean> q = getCurrentSession().createQuery(query, RuleSetRuleBean.class);
        q.setParameter("ruleSetBean", ruleSetBean);
        q.setParameter("ruleBean", ruleBean);
        return new ArrayList<>(q.list());
    }
    
    /**
     * Use this method carefully as we force an eager fetch. It is also annotated with 
     * Transactional so it can be called from Quartz threads. 
     * @param studyId
     * @return List of RuleSetRuleBeans 
     */
    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    @Transactional
    public ArrayList<RuleSetRuleBean> findByRuleSetStudyIdAndStatusAvail(Integer studyId) {
        String query = "from " + getDomainClassName() + " ruleSetRule  where ruleSetRule.ruleSetBean.studyId = :studyId and status = :status ";
        Query<RuleSetRuleBean> q = getCurrentSession().createQuery(query, RuleSetRuleBean.class);
        
        q.setInteger("studyId", studyId);
        q.setParameter("status", org.akaza.openclinica.domain.Status.AVAILABLE);
        
        q.setCacheable(true);
        q.setCacheRegion(getDomainClassName());
        //JN: enabling statistics for hibernate queries etc... to monitor the performance
        
        Statistics stats = getSessionFactory().getStatistics();
        logger.info("EntityRuleSet"+ stats.getEntityInsertCount());
        logger.info(stats.getQueryExecutionMaxTimeQueryString());
        logger.info("hit count"+stats.getSecondLevelCacheHitCount());
        stats.logSummary();
        
 
        
        ArrayList<RuleSetRuleBean> ruleSetRules = new ArrayList<>(q.list());
        // Forcing eager fetch of actions & their properties
        for (RuleSetRuleBean ruleSetRuleBean : ruleSetRules) {
            for (RuleActionBean action : ruleSetRuleBean.getActions()) {
                if (action instanceof RandomizeActionBean) {
                    ((RandomizeActionBean) action).getProperties().size();
                }
                if (action instanceof InsertActionBean) {
                    ((InsertActionBean) action).getProperties().size();
                }
                if (action instanceof ShowActionBean) {
                    ((ShowActionBean) action).getProperties().size();
                }
                if (action instanceof HideActionBean) {
                    ((HideActionBean) action).getProperties().size();
                }
                if (action instanceof EventActionBean) {
                    ((EventActionBean) action).getProperties().size();
                }

            }
        }
        return ruleSetRules;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("rawtypes")
	public int getCountWithFilter(final ViewRuleAssignmentFilter filter) {

        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            "select COUNT(DISTINCT(rsr.id)) from rule_set_rule rsr " + " join rule_set rs on rs.id = rsr.rule_set_id "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule r on r.id = rsr.rule_id "
                + " join rule_expression rer on r.rule_expression_id = rer.id " + " join rule_action ra on ra.rule_set_rule_id = rsr.id " + " where ";

        query += filter.execute("");
        NativeQuery q = getCurrentSession().createSQLQuery(query);

        return ((Number) q.uniqueResult()).intValue();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<RuleSetRuleBean> getWithFilterAndSort(final ViewRuleAssignmentFilter filter, final ViewRuleAssignmentSort sort, final int rowStart,
            final int rowEnd) {

        String select =
            "select DISTINCT(rsr.id),rsr.rule_set_id,rsr.rule_id,rsr.owner_id,rsr.date_created, rsr.date_updated, rsr.update_id, rsr.status_id,rsr.version,i.name as iname,re.value as revalue,sed.name as sedname,c.name as cname,cv.name as cvname,ig.name as igname,rer.value as rervalue,r.oc_oid as rocoid,r.description as rdescription,r.name as rname ,rs.run_schedule,rs.run_time from rule_set_rule rsr ";

        String query =
            select + " join rule_set rs on rs.id = rsr.rule_set_id "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule r on r.id = rsr.rule_id "
                + " join rule_expression rer on r.rule_expression_id = rer.id " + " join rule_action ra on ra.rule_set_rule_id = rsr.id " + " where ";

        query += filter.execute("");
        query += sort.execute("");
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setFirstResult(rowStart);
        q.setMaxResults(rowEnd - rowStart);
        return new ArrayList<RuleSetRuleBean>(q.list());
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("unchecked")
	public int getCountByStudy(StudyBean study) {
        String query =
            "select COUNT(*) from rule_set_rule rsr " + " join rule_set rs on rs.id = rsr.rule_set_id "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule r on r.id = rsr.rule_id "
                + " join rule_expression rer on r.rule_expression_id = rer.id " + " join rule_action ra on ra.rule_set_rule_id = rsr.id " + " where rs.study_id = " + study.getId() + "  AND  rsr.status_id = 1";

        NativeQuery<BigInteger> q = getCurrentSession().createSQLQuery(query);
        return q.uniqueResult().intValue();
    }


    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }
}
