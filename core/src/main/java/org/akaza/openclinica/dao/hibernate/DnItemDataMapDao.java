/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.hibernate.query.Query;

public class DnItemDataMapDao extends AbstractDomainDao<DnItemDataMap> {

    @Override
    Class<DnItemDataMap> domainClass() {
        return DnItemDataMap.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public List<DnItemDataMap> findByItemData(Integer itemDataId) {
        String query = "from " + getDomainClassName() + " do where do.itemData.itemDataId = :itemdataid ";
        Query<DnItemDataMap> q = getCurrentSession().createQuery(query, DnItemDataMap.class);
        q.setInteger("itemdataid", itemDataId);
        return q.list();
    }
}
