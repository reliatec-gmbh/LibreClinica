/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
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
