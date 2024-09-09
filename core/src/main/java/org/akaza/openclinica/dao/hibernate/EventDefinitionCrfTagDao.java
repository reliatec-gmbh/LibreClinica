/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.hibernate.query.Query;

public class EventDefinitionCrfTagDao extends AbstractDomainDao<EventDefinitionCrfTag> {

    @Override
    Class<EventDefinitionCrfTag> domainClass() {
        // TODO Auto-generated method stub
        return EventDefinitionCrfTag.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId= :tagId and active= :active ";
        Query<EventDefinitionCrfTag> q = getCurrentSession().createQuery(query, EventDefinitionCrfTag.class);
        q.setInteger("tagId", tagId);
        q.setString("path", path);
        q.setBoolean("active", active);
        return (EventDefinitionCrfTag) q.uniqueResult();

    }

}
