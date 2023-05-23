/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.EventCrfFlag;
import org.hibernate.query.Query;

public class EventCrfFlagDao extends AbstractDomainDao<EventCrfFlag> {

    @Override
    Class<EventCrfFlag> domainClass() {
        return EventCrfFlag.class;
    }

    public EventCrfFlag findByEventCrfPath(int tagId, String path) {
        String query = "from " + getDomainClassName() + " where path = :path and tagId = :tagId";
        Query<EventCrfFlag> q = getCurrentSession()
                .createQuery(query, EventCrfFlag.class)
                .setParameter("path", path)
                .setParameter("tagId", tagId);

        return q.uniqueResult();
    }

}
