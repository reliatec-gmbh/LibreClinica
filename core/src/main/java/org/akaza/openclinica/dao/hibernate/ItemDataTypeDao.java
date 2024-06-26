/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemDataType;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

public class ItemDataTypeDao extends AbstractDomainDao<ItemDataType> {

    @Override
    Class<ItemDataType> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataType.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ItemDataType findByItemDataTypeCode(String item_data_type_code) {
        String query = "from " + getDomainClassName() + " item_data_type  where item_data_type.code = :itemdatatypecode ";
        Query<ItemDataType> q = getCurrentSession().createQuery(query, ItemDataType.class);
        q.setString("itemdatatypecode", item_data_type_code);
        return (ItemDataType) q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ItemDataType findByItemDataTypeId(int item_data_type_id) {
        String query = "from " + getDomainClassName() + " item_data_type  where item_data_type.itemDataTypeId = :item_data_type_id ";
        Query<ItemDataType> q = getCurrentSession().createQuery(query, ItemDataType.class);
        q.setInteger("item_data_type_id", item_data_type_id);
        ItemDataType result = (ItemDataType) q.uniqueResult();
        return result;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("rawtypes")
    public ItemDataType findByItemId(int item_id) {
        String query = "select idt.* from item_data_type idt join item i on idt.item_data_type_id=i.item_data_type_id where i.item_id = " + item_id;
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(ItemDataType.class);
        ItemDataType result = (ItemDataType) q.uniqueResult();
        return result;
    }
}
