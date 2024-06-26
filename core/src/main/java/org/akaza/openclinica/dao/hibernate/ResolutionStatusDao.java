/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResolutionStatus;
import org.hibernate.query.Query;

public class ResolutionStatusDao extends AbstractDomainDao<ResolutionStatus> {

    @Override
    public Class<ResolutionStatus> domainClass() {
        return ResolutionStatus.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ResolutionStatus findByResolutionStatusId(Integer resolutionStatusId) {
        String query = "from " + getDomainClassName() + " do  where do.resolutionStatusId = :resolutionstatusid";
        Query<ResolutionStatus> q = getCurrentSession().createQuery(query, ResolutionStatus.class);
        q.setInteger("resolutionstatusid", resolutionStatusId);
        return q.uniqueResult();
    }

}
