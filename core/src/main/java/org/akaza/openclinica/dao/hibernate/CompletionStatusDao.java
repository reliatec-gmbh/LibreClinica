/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CompletionStatus;
import org.hibernate.query.Query;

public class CompletionStatusDao extends AbstractDomainDao<CompletionStatus> {

    @Override
    Class<CompletionStatus> domainClass() {
        return CompletionStatus.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public CompletionStatus findByCompletionStatusId(int completion_status_id) {
        String query = "from " + getDomainClassName() + " completion_status  where completion_status.completionStatusId = :completionstatusid ";
        Query<CompletionStatus> q = getCurrentSession().createQuery(query, CompletionStatus.class);
        q.setInteger("completionstatusid", completion_status_id);
        return q.uniqueResult();
    }


}
