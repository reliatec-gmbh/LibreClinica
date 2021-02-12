/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.hibernate.query.Query;

public class EventDefinitionCrfDao extends AbstractDomainDao<EventDefinitionCrf> {

    @Override
    Class<EventDefinitionCrf> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrf.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<EventDefinitionCrf> findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from "
                + getDomainClassName()
                + " event_definition_crf where event_definition_crf.studyEventDefinition.studyEventDefinitionId = :studyeventdefinitionid";
        Query<EventDefinitionCrf> q = getCurrentSession().createQuery(query, EventDefinitionCrf.class);
        q.setInteger("studyeventdefinitionid", studyEventDefinitionId);
        return q.list();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<EventDefinitionCrf> findAvailableByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1";
        Query<EventDefinitionCrf> q = getCurrentSession().createQuery(query, EventDefinitionCrf.class);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return q.list();
        
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<EventDefinitionCrf> findSiteHiddenByStudyEventDefStudy(Integer studyEventDefinitionId, Integer studyId) {
        String query = "from " + getDomainClassName() + " do where do.studyEventDefinition.studyEventDefinitionId = :studyeventdefid " + 
                " and do.study.studyId = :studyid and do.statusId = 1 and do.hideCrf = true";
        Query<EventDefinitionCrf> q = getCurrentSession().createQuery(query, EventDefinitionCrf.class);
        q.setInteger("studyeventdefid", studyEventDefinitionId);
        q.setInteger("studyid", studyId);
        return q.list();
        
    }
}
