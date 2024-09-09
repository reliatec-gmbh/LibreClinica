/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
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
