/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.oid.ItemOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.Item;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;

public class ItemDao extends AbstractDomainDao<Item> {

    @Override
    Class<Item> domainClass() {
        // TODO Auto-generated method stub
        return Item.class;
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public Item findByOcOID(String OCOID) {
        String query = "from " + getDomainClassName() + " item  where item.ocOid = :ocoid ";
        org.hibernate.query.Query<Item> q = getCurrentSession().createQuery(query, Item.class);
        q.setString("ocoid", OCOID);
        return (Item) q.uniqueResult();
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("rawtypes")
    public Item findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct i.* from item i, item_form_metadata ifm,crf_version cv " + "where i.name= '" + name + "' and i.item_id= ifm.item_id "
                + "and ifm.crf_version_id=cv.crf_version_id " + "and cv.crf_id=" + crfId;
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(Item.class);
        return ((Item) q.uniqueResult());
    }
    
  public static final String findAllByCrfVersionIdQuery = "select distinct i.* from item i, item_form_metadata ifm " + "where i.item_id= ifm.item_id "
          + "and ifm.crf_version_id = :crfversionid";

  // TODO update to CriteriaQuery 
  @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
  public List<Item> findAllByCrfVersionId(Integer crfVersionId) {
      NativeQuery q = getCurrentSession().createSQLQuery(findAllByCrfVersionIdQuery).addEntity(Item.class);
      q.setInteger("crfversionid", crfVersionId.intValue());
      return (List<Item>) q.list();
  }

  // TODO update to CriteriaQuery 
  @SuppressWarnings("rawtypes")
    public int getItemDataTypeId(Item item) {
        String query = "select item_data_type_id from item where item_id = " + item.getItemId();
        Query q = getCurrentSession().createSQLQuery(query);
        return ((Number) q.uniqueResult()).intValue();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ArrayList<Item> findByItemGroupCrfVersionOrdered(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct i.* from item i, item_group fg, item_group_metadata fgim " + " where fg.item_group_id= " + String.valueOf(itemGroupId)
                + " and fg.item_group_id=fgim.item_group_id and fgim.crf_version_id= " + String.valueOf(crfVersionId)
                + " and fgim.item_id=i.item_id order by i.item_id";
        NativeQuery q = getCurrentSession().createSQLQuery(query).addEntity(Item.class);
        return (ArrayList<Item>) q.list();
    }

    public String getValidOid(Item item, String crfName, String itemLabel, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new ItemOidGenerator();
        String oid = getOid(item, crfName, itemLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(Item item, String crfName, String itemLabel) {
        OidGenerator oidGenerator = new ItemOidGenerator();
        String oid;
        try {
            oid = item.getOcOid() != null ? item.getOcOid() : oidGenerator.generateOid(crfName, itemLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

}