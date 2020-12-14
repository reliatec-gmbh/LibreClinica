/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Subject;
import org.hibernate.query.Query;

public class SubjectDao extends AbstractDomainDao<Subject> {

    @Override
    Class<Subject> domainClass() {
        // TODO Auto-generated method stub
        return Subject.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public Subject findBySubjectId(Integer subjectId) {
        String query = "from " + getDomainClassName() + " do  where do.subjectId = :subject_id ";
        Query<Subject> q = getCurrentSession().createQuery(query, Subject.class);
        q.setInteger("subject_id", subjectId);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public Subject findByUniqueIdentifier(String uniqueIdentifier) {
        String query = "from " + getDomainClassName() + " do  where do.uniqueIdentifier = :unique_identifier ";
        Query<Subject> q = getCurrentSession().createQuery(query, Subject.class);
        q.setString("unique_identifier", uniqueIdentifier);
        return q.uniqueResult();
    }
}
