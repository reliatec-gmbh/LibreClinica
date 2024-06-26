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

import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.hibernate.query.NativeQuery;

public class ItemFormMetadataDao extends AbstractDomainDao<ItemFormMetadata> {

    @Override
    Class<ItemFormMetadata> domainClass() {
        return ItemFormMetadata.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("rawtypes")
	public ItemFormMetadata findByItemCrfVersion(Integer itemId, Integer crfVersionId) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE m.item_id= " + String.valueOf(itemId) + " AND m.crf_version_id= "
                + String.valueOf(crfVersionId);
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        return (ItemFormMetadata) q.uniqueResult();

    }

    public static final String findAllByCrfVersionQuery = "select distinct * from item_form_metadata ifm where ifm.crf_version_id = :crfversionid";

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    public List<ItemFormMetadata> findAllByCrfVersion(int crf_version_id) {
        NativeQuery q = getCurrentSession().createSQLQuery(findAllByCrfVersionQuery).addEntity(ItemFormMetadata.class);
        q.setInteger("crfversionid", crf_version_id);
        return (List<ItemFormMetadata>) q.list();
    }

}
