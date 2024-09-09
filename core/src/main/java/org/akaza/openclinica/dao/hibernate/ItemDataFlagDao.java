/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import org.hibernate.query.Query;

public class ItemDataFlagDao extends AbstractDomainDao<ItemDataFlag> {

    @Override
    Class<ItemDataFlag> domainClass() {
        return ItemDataFlag.class;
    }

    // TODO update to CriteriaQuery 
    public List<ItemDataFlag> findAllByEventCrfPath(int tag_id, String eventCrfPath) {

        String query = "from " + getDomainClassName() + " where " +
                "tag_id = :tag_id and path like :eventCrfPath";
        
        Query<ItemDataFlag> q = getCurrentSession()
                .createQuery(query, ItemDataFlag.class)
                .setParameter("tag_id", tag_id)
                .setParameter("eventCrfPath", eventCrfPath + ".%");
        
        return q.list();
    }

    // TODO update to CriteriaQuery 
    public ItemDataFlag findByItemDataPath(int tag_id, String itemDataPath) {

        String query = "from " + getDomainClassName() + " where " +
                "tag_id = :tag_id and path = :itemDataPath";
        
        Query<ItemDataFlag> q = getCurrentSession()
                .createQuery(query, ItemDataFlag.class)
                .setParameter("tag_id", tag_id)
                .setParameter("itemDataPath", itemDataPath);
        
        return q.uniqueResult();
    }
    
}
