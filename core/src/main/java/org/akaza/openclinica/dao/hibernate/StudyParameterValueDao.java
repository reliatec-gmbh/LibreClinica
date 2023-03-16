/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.hibernate.query.Query;


public class StudyParameterValueDao extends AbstractDomainDao<StudyParameterValue> {
	
    @Override
    public Class<StudyParameterValue> domainClass() {
        return StudyParameterValue.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
	public StudyParameterValue findByStudyIdParameter(int studyId, String parameter) {
        String query = "from " + getDomainClassName() + " study_parameter_value where study_parameter_value.study.studyId = :studyid and study_parameter_value.studyParameter = :parameter ";
        Query<StudyParameterValue> q = getCurrentSession().createQuery(query, StudyParameterValue.class);
        q.setInteger("studyid", studyId);
        q.setString("parameter", parameter);
        return q.uniqueResult();
    }
}
