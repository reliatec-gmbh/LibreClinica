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

import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

public class ResponseSetDao extends AbstractDomainDao<ResponseSet> {

    @Override
    Class<ResponseSet> domainClass() {
        // TODO Auto-generated method stub
        return ResponseSet.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ResponseSet findByLabelVersion(String label, Integer version) {
        String query = "from " + getDomainClassName() + " response_set  where response_set.label = :label and response_set.versionId = :version ";
        Query<ResponseSet> q = getCurrentSession().createQuery(query, ResponseSet.class);
        q.setString("label", label);
        q.setInteger("version", version);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<ResponseSet> findAllByItemId(int itemId) {
        String query = "select rs.* from item_form_metadata ifm join response_set rs on ifm.response_set_id = rs.response_set_id " + "where ifm.item_id = "
                + itemId;
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(ResponseSet.class);
        return (List<ResponseSet>) q.list();
    }

}
