/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull " + 
            "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(DiscrepancyNote.class);
        return (List<DiscrepancyNote>) q.list();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        Query<DiscrepancyNote> q = getCurrentSession().createQuery(query, DiscrepancyNote.class);
        q.setInteger("discrepancynoteid", discrepancyNoteId);
        return q.uniqueResult();
    }


}
