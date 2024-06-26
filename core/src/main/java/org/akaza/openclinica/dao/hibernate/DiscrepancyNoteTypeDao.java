/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.DiscrepancyNoteType;
import org.hibernate.query.Query;

public class DiscrepancyNoteTypeDao extends AbstractDomainDao<DiscrepancyNoteType> {

    @Override
    public Class<DiscrepancyNoteType> domainClass() {
        return DiscrepancyNoteType.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DiscrepancyNoteType findByDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId) {
        String query = "from " + getDomainClassName() + " do  where do.discrepancyNoteTypeId = :discrepancynotetypeid";
        Query<DiscrepancyNoteType> q = getCurrentSession().createQuery(query, DiscrepancyNoteType.class);
        q.setInteger("discrepancynotetypeid", discrepancyNoteTypeId);
        return q.uniqueResult();
    }

}
