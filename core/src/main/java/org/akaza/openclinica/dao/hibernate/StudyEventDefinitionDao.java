/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.hibernate.query.Query;


public class StudyEventDefinitionDao extends AbstractDomainDao<StudyEventDefinition> {
	
    @Override
    public Class<StudyEventDefinition> domainClass() {
        return StudyEventDefinition.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public StudyEventDefinition findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from " + getDomainClassName() + " study_event_definition  where study_event_definition.studyEventDefinitionId = :studyeventdefinitionid ";
        Query<StudyEventDefinition> q = getCurrentSession().createQuery(query, StudyEventDefinition.class);
        q.setInteger("studyeventdefinitionid", studyEventDefinitionId);
        return q.uniqueResult();
    }
}
