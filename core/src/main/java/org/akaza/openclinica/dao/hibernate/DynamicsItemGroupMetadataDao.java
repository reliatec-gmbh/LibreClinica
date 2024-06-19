/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Transactional;

public class DynamicsItemGroupMetadataDao extends AbstractDomainDao<DynamicsItemGroupMetadataBean>{

    @Override 
    public Class<DynamicsItemGroupMetadataBean> domainClass() {
        return DynamicsItemGroupMetadataBean.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        Query<DynamicsItemGroupMetadataBean> q = getCurrentSession().createQuery(query, DynamicsItemGroupMetadataBean.class);
        q.setInteger("id", metadataBean.getId());
        q.setInteger("item_group_id", metadataBean.getItemGroupId());
        q.setInteger("event_crf_id", eventCrfBean.getId());
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        Query<DynamicsItemGroupMetadataBean> q = getCurrentSession().createQuery(query, DynamicsItemGroupMetadataBean.class);
        q.setInteger("id", metadataBean.getId());
        q.setInteger("item_group_id", metadataBean.getItemGroupId());
        q.setInteger("event_crf_id", eventCrfBeanId);
        return q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 'false'"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 'true' limit 1";
        
        org.hibernate.query.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        /* TODO use uniqueResult (or something similar), if the
         * query returns multiple (equivalent results) use distinct also
         */
        return q.list() != null && q.list().size() > 0;
    }

    @Transactional
    public void delete(int eventCrfId) {
        String query = "delete from " + getDomainClassName() + " metadata where metadata.eventCrfId = :eventCrfId";
        Query<?> q = getCurrentSession().createQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
