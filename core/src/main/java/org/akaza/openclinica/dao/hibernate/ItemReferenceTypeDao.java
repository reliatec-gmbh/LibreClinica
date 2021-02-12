/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemReferenceType;
import org.hibernate.query.Query;

public class ItemReferenceTypeDao extends AbstractDomainDao<ItemReferenceType> {

    @Override
    Class<ItemReferenceType> domainClass() {
        // TODO Auto-generated method stub
        return ItemReferenceType.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ItemReferenceType findByItemReferenceTypeId(int item_reference_type_id) {
        String query = "from " + getDomainClassName() + " item_reference_type  where item_reference_type.itemReferenceTypeId = :itemreferencetypeid ";
        Query<ItemReferenceType> q = getCurrentSession().createQuery(query, ItemReferenceType.class);
        q.setInteger("itemreferencetypeid", item_reference_type_id);
        return q.uniqueResult();
    }

}
