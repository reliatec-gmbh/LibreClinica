/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 */
public class DynamicsItemFormMetadataDao extends AbstractDomainDao<DynamicsItemFormMetadataBean> {

    protected static final Logger LOG = LoggerFactory.getLogger(DynamicsItemFormMetadataDao.class);

    @Override
    public Class<DynamicsItemFormMetadataBean> domainClass() {
        return DynamicsItemFormMetadataBean.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DynamicsItemFormMetadataBean findByMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean,
            ItemDataBean itemDataBean) {

        String query = "from DynamicsItemFormMetadataBean metadata where " +
        		"metadata.itemId = :item_id and metadata.eventCrfId = :event_crf_id and " +
                "metadata.itemDataId = :item_data_id order by metadata.id desc ";

        Query<DynamicsItemFormMetadataBean> q = getCurrentSession().createQuery(query, DynamicsItemFormMetadataBean.class);
        q.setInteger("item_id", metadataBean.getItemId());
        q.setInteger("event_crf_id", eventCrfBean.getId());
        q.setInteger("item_data_id", itemDataBean.getId());
        List<DynamicsItemFormMetadataBean> list = q.list();
        /* TODO use uniqueResult (or something similar), if the
         * query returns multiple (equivalent results) use distinct also
         */
        return list.size() !=0 ? list.get(0) : null;
    }


    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public ArrayList <DynamicsItemFormMetadataBean> findByItemAndEventCrfShown(EventCRFBean eventCrfBean,
            int itemId) {

        String query = "from DynamicsItemFormMetadataBean metadata where " +
                "metadata.itemId = :item_id and metadata.eventCrfId = :event_crf_id and " +
                "metadata.showItem = true order by metadata.id desc ";

        Query<DynamicsItemFormMetadataBean> q = getCurrentSession().createQuery(query, DynamicsItemFormMetadataBean.class);
        q.setInteger("item_id", itemId);
        q.setInteger("event_crf_id", eventCrfBean.getId());

        return new ArrayList<>(q.list());
    }


    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DynamicsItemFormMetadataBean findByItemDataBean(ItemDataBean itemDataBean) {
        String query = "from " + getDomainClassName() + " metadata where metadata.itemDataId = :item_data_id ";
        Query<DynamicsItemFormMetadataBean> q = getCurrentSession().createQuery(query, DynamicsItemFormMetadataBean.class);

        q.setInteger("item_data_id", itemDataBean.getId());
        return q.uniqueResult();
    }

        
    public List<Integer> findItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )";

        return queryForIDs(postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";

        return queryForIDs(postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";

        return queryForIDs(postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findHideItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='false'";

        return queryForIDs(postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemDataIdsInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in ( select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId)"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";

        return queryForIDs(postgres, null, sectionId, eventCrfId, crfVersionId);
    }

    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String postgres = "select di.item_id from dyn_item_form_metadata di where di.item_data_id in ("
            + " select ida.item_data_id from item_data ida where ida.event_crf_id = :eventCrfId and ida.item_id in"
            + "       (select ifm.item_id from item_form_metadata ifm where ifm.section_id = :sectionId and ifm.crf_version_id = :crfVersionId"
            + "          and ifm.item_id not in  (select distinct igm.item_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
            + "          and igm.show_group = 'false'"
            + "          and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
            + "        )and (ida.status_id != 5 and ida.status_id != 7) ) and di.show_item = 'true' limit 1" ;

        return CollectionUtils.isNotEmpty(queryForIDs(postgres, null, sectionId, eventCrfId, crfVersionId));
    }

    /**
     * Executes a SQL query to retrieve a list of IDs
     *
     * @param postgresQuery The Postgres version of the query
     * @param groupId groupId
     * @param sectionId sectionId
     * @param eventCrfId eventCrfId
     * @param crfVersionId crfVersionId
     * @return list of IDs
     */
    // TODO update to CriteriaQuery 
    @SuppressWarnings({"deprecation", "rawtypes"})
    protected List<Integer> queryForIDs(String postgresQuery, Integer groupId, Integer sectionId,
            Integer eventCrfId, Integer crfVersionId) {

        Query q = getCurrentSession().createSQLQuery(postgresQuery);
        if (groupId != null) {
            q.setInteger("groupId", groupId);
        }
        if (sectionId != null) {
            q.setInteger("sectionId", sectionId);
        }
        if (eventCrfId != null) {
            q.setInteger("eventCrfId", eventCrfId);
        }
        if (crfVersionId != null) {
            q.setInteger("crfVersionId", crfVersionId);
        }
        return HibernateUtil.queryIDsList(q);
    }

    @Transactional
    public void delete(int eventCrfId) {
        String query = "delete from " + getDomainClassName() + " metadata where metadata.eventCrfId = :eventCrfId";
        Query<?> q = getCurrentSession().createQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
