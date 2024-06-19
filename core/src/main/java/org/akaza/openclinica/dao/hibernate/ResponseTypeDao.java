/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResponseType;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

public class ResponseTypeDao extends AbstractDomainDao<ResponseType> {

    @Override
    Class<ResponseType> domainClass() {
        // TODO Auto-generated method stub
        return ResponseType.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ResponseType findByResponseTypeName(String name) {
        String query = "from " + getDomainClassName() + " response_type  where response_type.name = :name ";
        Query<ResponseType> q = getCurrentSession().createQuery(query, ResponseType.class);
        q.setString("name", name);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("rawtypes")
    public ResponseType findByItemFormMetaDataId(Integer itemFormMetadataId) {
        String query = "select rt.* from response_type rt, response_set rs, item_form_metadata ifm where ifm.response_set_id=rs.response_set_id"
                + " and rs.response_type_id=rt.response_type_id and ifm.item_form_metadata_id = " + String.valueOf(itemFormMetadataId);
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(ResponseType.class);
        return (ResponseType) q.uniqueResult();
    }

}
