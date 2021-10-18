/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.core.util.ItemGroupCrvVersionUtil;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 */
public class ItemDAO extends AuditableEntityDAO<ItemBean> {

    public ItemDAO(DataSource ds) {
        super(ds);
    }

    public ItemDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEM;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.BOOL);// phi status
        this.setTypeExpected(6, TypeNames.INT);// data type id
        this.setTypeExpected(7, TypeNames.INT);// reference type id
        this.setTypeExpected(8, TypeNames.INT);// status id
        this.setTypeExpected(9, TypeNames.INT);// owner id
        this.setTypeExpected(10, TypeNames.DATE);// created
        this.setTypeExpected(11, TypeNames.DATE);// updated
        this.setTypeExpected(12, TypeNames.INT);// update id
        this.setTypeExpected(13, TypeNames.STRING);// oc_oid
        // types from item_form_metadata or crf_version
        this.setTypeExpected(14, TypeNames.INT);// crf_version_id, ordinal
        this.setTypeExpected(15, TypeNames.STRING);// name
    }

    @Override
    public ItemBean update(ItemBean ib) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, ib.getName());
        variables.put(2, ib.getDescription());
        variables.put(3, ib.getUnits());
        variables.put(4, ib.isPhiStatus());
        variables.put(5, ib.getItemDataTypeId());
        variables.put(6, ib.getItemReferenceTypeId());
        variables.put(7, ib.getStatus().getId());
        variables.put(8, ib.getUpdaterId());
        variables.put(9, ib.getId());
        this.executeUpdate(digester.getQuery("update"), variables);
        return ib;
    }

    @Override
    public ItemBean create(ItemBean ib) {
        // per the create sql statement
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, ib.getName());
        variables.put(2, ib.getDescription());
        variables.put(3, ib.getUnits());
        variables.put(4, ib.isPhiStatus());
        variables.put(5, ib.getItemDataTypeId());
        variables.put(6, ib.getItemReferenceTypeId());
        variables.put(7, ib.getStatus().getId());
        variables.put(8, ib.getOwnerId());
        // date_created=now() in Postgres
        this.executeUpdate(digester.getQuery("create"), variables);
        // set the id here????
        return ib;
    }

    private String getOid(ItemBean itemBean, String crfName, String itemLabel) {
        String oid;
        try {
            oid = itemBean.getOid() != null ? itemBean.getOid() : itemBean.getOidGenerator().generateOid(crfName, itemLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public Integer getCountofActiveItems() {
        String query = digester.getQuery("getCountofItems");
        return getCountByQuery(query, new HashMap<>());
    }

    public String getValidOid(ItemBean itemBean, String crfName, String itemLabel, ArrayList<String> oidList) {
        String oid = getOid(itemBean, crfName, itemLabel);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid).size() > 0 || oidList.contains(oid)) {
            oid = itemBean.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    public ItemBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ItemBean eb = new ItemBean();
        // below inserted to find out a class cast exception, tbh
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Integer statusId = (Integer) hm.get("status_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        Integer updateId = (Integer) hm.get("update_id");

        eb.setCreatedDate(dateCreated);
        eb.setUpdatedDate(dateUpdated);
        eb.setStatus(Status.get(statusId));
        
        // Even thou it is deprecated it creates more performant lazy loading behaviour
        eb.setOwnerId(ownerId);
        // Disabled because it generates additional SQL query
        ///UserAccountBean owner = getUserAccountDAO().findByPK(ownerId);
        //eb.setOwner(owner);
        // Even thou it is deprecated it creates more performant lazy loading behaviour
        eb.setUpdaterId(updateId);
        // Disabled because it generates additional SQL query
        //UserAccountBean updater = getUserAccountDAO().findByPK(updateId);
        //eb.setUpdater(updater);

        // something to trip over
        // something else to trip over
        // eb = (ItemBean)this.getEntityAuditInformation(hm);
        eb.setName((String) hm.get("name"));
        eb.setId((Integer) hm.get("item_id"));
        eb.setDescription((String) hm.get("description"));
        eb.setUnits((String) hm.get("units"));
        eb.setPhiStatus((Boolean) hm.get("phi_status"));
        eb.setItemDataTypeId((Integer) hm.get("item_data_type_id"));
        eb.setItemReferenceTypeId((Integer) hm.get("item_reference_type_id"));
        // logger.info("item name|date type id" + eb.getName() + "|" + eb.getItemDataTypeId());
        eb.setDataType(ItemDataType.get(eb.getItemDataTypeId()));
        eb.setOid((String) hm.get("oc_oid"));
        // the rest should be all set
        return eb;
    }

    public ArrayList<ItemBean> findByOid(String oid) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, oid);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findItemByOid"), variables, true);

        ArrayList<ItemBean> beanList = new ArrayList<>();
        ItemBean bean;
        for (HashMap<String, Object> hm : listofMaps) {
            bean = this.getEntityFromHashMap(hm);
            beanList.add(bean);
        }
        return beanList;
    }

    @Override
    public ArrayList<ItemBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<ItemBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<ItemBean> findAllParentsBySectionId(int sectionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        return this.executeFindAllQuery("findAllParentsBySectionId", variables);
    }

    public ArrayList<ItemBean> findAllNonRepeatingParentsBySectionId(int sectionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        return this.executeFindAllQuery("findAllNonRepeatingParentsBySectionId", variables);
    }

    public ArrayList<ItemBean> findAllBySectionId(int sectionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        return this.executeFindAllQuery("findAllBySectionId", variables);
    }

    public ArrayList<ItemBean> findAllBySectionIdOrderedByItemFormMetadataOrdinal(int sectionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        return this.executeFindAllQuery("findAllBySectionIdOrderedByItemFormMetadataOrdinal", variables);
    }
    
    public ArrayList<ItemBean> findAllUngroupedParentsBySectionId(int sectionId, int crfVersionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        variables.put(2, crfVersionId);

        return this.executeFindAllQuery("findAllUngroupedParentsBySectionId", variables);
    }

    public ArrayList<ItemBean> findAllItemsByVersionId(int versionId) {
    	HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, versionId);

        return this.executeFindAllQuery("findAllItemsByVersionId", variables);
    }

    public ArrayList<Integer> findAllVersionsByItemId(int itemId) {
    	HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, itemId);
        String sql = digester.getQuery("findAllVersionsByItemId");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables, true);
        ArrayList<Integer> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            Integer versionId = (Integer) hm.get("crf_version_id");
            al.add(versionId);
        }
        return al;

    }

    public List<ItemBean> findAllItemsByGroupId(int id, int crfVersionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        variables.put(2, crfVersionId);
        String sql = digester.getQuery("findAllItemsByGroupId");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables, true);
        List<ItemBean> beanList = new ArrayList<>();
        ItemBean bean;
        for (HashMap<String, Object> hm : listofMaps) {
            bean = this.getEntityFromHashMap(hm);
            beanList.add(bean);
        }
        return beanList;
    }

    public List<ItemBean> findAllItemsByGroupIdOrdered(int id, int crfVersionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        variables.put(2, crfVersionId);
        String sql = digester.getQuery("findAllItemsByGroupIdOrdered");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables, true);
        List<ItemBean> beanList = new ArrayList<>();
        ItemBean bean;
        for (HashMap<String, Object> hm : listofMaps) {
            bean = this.getEntityFromHashMap(hm);
            beanList.add(bean);
        }
        return beanList;
    }
    
    public List<ItemBean> findAllItemsByGroupIdAndSectionIdOrdered(int id, int crfVersionId , int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        variables.put(2, sectionId);
        variables.put(3, crfVersionId);
        String sql = digester.getQuery("findAllItemsByGroupIdAndSectionIdOrdered");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables, true);
        List<ItemBean> beanList = new ArrayList<>();
        ItemBean bean;
        for (HashMap<String, Object> hm : listofMaps) {
            bean = this.getEntityFromHashMap(hm);
            beanList.add(bean);
        }
        return beanList;
    }
    
    public List<ItemBean> findAllItemsByGroupIdForPrint(int id, int crfVersionId,int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        variables.put(2, crfVersionId);
        variables.put(3,sectionId);
        String sql = digester.getQuery("findAllItemsByGroupIdForPrint");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables, true);
        List<ItemBean> beanList = new ArrayList<>();
        ItemBean bean;
        for (HashMap<String, Object> hm : listofMaps) {
            bean = this.getEntityFromHashMap(hm);
            beanList.add(bean);
        }
        return beanList;
    }
    
    public ItemBean findItemByGroupIdandItemOid(int id, String itemOid) {
        ItemBean bean;
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);
        variables.put(2, itemOid);
        String sql = digester.getQuery("findItemByGroupIdandItemOid");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables, true);
        if (rows != null && rows.size() > 0) {
            bean = this.getEntityFromHashMap(rows.get(0));
            return bean;
        } else {
            return null;
        }
    }

    public ArrayList<ItemBean> findAllActiveByCRF(CRFBean crf) {
        HashMap<Integer, Object> variables = new HashMap<>();
        this.setTypesExpected();
        this.setTypeExpected(14, TypeNames.INT);// crf_version_id
        this.setTypeExpected(15, TypeNames.STRING);// version name
        variables.put(1, crf.getId());
        String sql = digester.getQuery("findAllActiveByCRF");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables, true);
        ArrayList<ItemBean> al = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            ItemBean eb = this.getEntityFromHashMap(hm);
            Integer versionId = (Integer) hm.get("crf_version_id");
            String versionName = (String) hm.get("cvname");
            ItemFormMetadataBean imf = new ItemFormMetadataBean();
            imf.setCrfVersionName(versionName);
            // logger.info("versionName" + imf.getCrfVersionName());
            imf.setCrfVersionId(versionId);
            eb.setItemMeta(imf);
            al.add(eb);
        }
        return al;
    }

    public ItemBean findByPK(int id) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, id);

        String sql = digester.getQuery("findByPK");
        
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables, true);
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0));
        }
        return eb;
    }

    public ItemBean findByName(String name) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, name);

        String sql = digester.getQuery("findByName");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables, true);
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0));
        }
        return eb;
    }

    public ItemBean findByNameAndCRFId(String name, int crfId) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, name);
        variables.put(2, crfId);

        String sql = digester.getQuery("findByNameAndCRFId");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables, true);
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0));
        }
        return eb;
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<ItemBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public ArrayList<ItemBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Finds the children of an item in a given CRF Version, sorted by columnNumber in ascending order.
     * 
     * @param parentId The id of the children's parent.
     * @param crfVersionId The id of the event CRF in which the children belong to this parent.
     * @return An array of ItemBeans, where each ItemBean represents a child of the parent
     *         and the array is sorted by columnNumber in ascending order.
     */
    public ArrayList<ItemBean> findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, parentId);
        variables.put(2, crfVersionId);

        return this.executeFindAllQuery("findAllByParentIdAndCRFVersionId", variables);
    }

    public int findAllRequiredByCRFVersionId(int crfVersionId) {
        HashMap<Integer, Object> variables = variables(crfVersionId);
        String query = digester.getQuery("findAllRequiredByCRFVersionId");
        return getCountByQueryOrDefault(query, variables, 0);
    }

    public ArrayList<ItemBean> findAllRequiredBySectionId(int sectionId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        return this.executeFindAllQuery("findAllRequiredBySectionId", variables);
    }
    
    public Map<String,Integer> mapAllItemNameAndItemIdInSection(Integer sectionId) {
        Map<String,Integer> nameIdMap = new HashMap<>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); //item_id
        this.setTypeExpected(2, TypeNames.STRING); //(item)name
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        String sql = digester.getQuery("findIdAndNamesInSection");
        ArrayList<HashMap<String, Object>> rows = select(sql, variables, true);
        for (HashMap<String, Object> row : rows) {
            Integer id = (Integer) row.get("item_id");
            String name = (String) row.get("name");
            nameIdMap.put(name, id);
        }
        return nameIdMap;
    }
    
    public Map<String,String> mapAllChildAndParentNameInSection(Integer sectionId) {
        Map<String,String> nameMap = new HashMap<>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);//(item)name
        this.setTypeExpected(2, TypeNames.INT);//item_id
        this.setTypeExpected(3, TypeNames.STRING);//parent_name
        this.setTypeExpected(4, TypeNames.INT);//parent_id
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        variables.put(2, sectionId);
        String sql = digester.getQuery("findChildAndParentNamesInSection");
        ArrayList<HashMap<String, Object>> rows = select(sql, variables, true);
        for (HashMap<String, Object> row : rows) {
            String cn = (String) row.get("name");
            String pn = (String) row.get("parent_name");
            nameMap.put(cn, pn);
        }
        return nameMap;
    }

    /**
     * select name, ordinal, oc_oid, item_data_id, i.item_id as item_id, value
     * from item_data id, item i
     * where id.item_id=i.item_id and event_crf_id = ? order  by i.item_id,ordinal;
     */
    public ArrayList<ItemBean> findAllWithItemDataByCRFVersionId(int crfVersionId,int eventCRFId) {
        this.unsetTypeExpected();
        
        this.setTypeExpected(1, TypeNames.STRING);//(item)name
        this.setTypeExpected(2, TypeNames.INT);//ordinal
        this.setTypeExpected(3, TypeNames.STRING);//oc_oid
        this.setTypeExpected(4, TypeNames.INT);//item_data_id
        this.setTypeExpected(5, TypeNames.INT);//item_id
        this.setTypeExpected(6, TypeNames.STRING);//(item)value
        
        
        ArrayList<ItemBean> answer = new ArrayList<>();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, crfVersionId);
        variables.put(2, eventCRFId);
        
        String sql = digester.getQuery("findAllWithItemDataByCRFVersionId");
        ArrayList<HashMap<String, Object>> rows = super.select(sql, variables);

        int cur_item_id = 0;
        ItemBean item_bean = null;
        for (HashMap<String, Object> row : rows) {

            Integer id = (Integer) row.get("item_id");
            if (cur_item_id != id) {
            	item_bean = new ItemBean();
            	answer.add(item_bean);
            	cur_item_id = id;
            	item_bean.setId(cur_item_id);
            	item_bean.setName((String) row.get("name"));
            	item_bean.setOid((String) row.get("oc_oid"));
            }

            ItemDataBean item_data_bean = new ItemDataBean();
            item_data_bean.setValue((String) row.get("value"));
            item_data_bean.setOrdinal((Integer) row.get("ordinal"));
            item_data_bean.setId((Integer) row.get("item_data_id"));
            item_data_bean.setItemId(cur_item_id);

            if (item_bean != null) {
                item_bean.addItemDataElement(item_data_bean);
            }
        }

        return answer;
    }

    /**
     * select distinct item.name as item_name, item_group.name as group_name,  item_group.oc_oid as group_oid,
     * crf_version.name as version_name, crf_version.status_id as v_status
     * from item, item_group, item_group_metadata, crf_version
     * where item.item_id= item_group_metadata.item_id  and item_group_metadata.item_group_id = item_group.item_group_id
     * and item_group_metadata.crf_version_id = crf_version.crf_version_id
     * and item_group.crf_id =(select crf_id from CRF where name=?) order by item.name;
     */
    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemGroupCRFVersionMetadataByCRFId(String  crfName) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);//(crf)name
        this.setTypeExpected(2, TypeNames.STRING);//(crf)name
        this.setTypeExpected(3, TypeNames.STRING);//(crf)name
        this.setTypeExpected(4, TypeNames.STRING);//(crf)name
        this.setTypeExpected(5, TypeNames.INT);//(crf)name
        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, crfName);
       
        String sql = digester.getQuery("findAllWithItemGroupCRFVersionMetadataByCRFId");
        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables, true);

        ArrayList<ItemGroupCrvVersionUtil> item_group_crfversion_info = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            ItemGroupCrvVersionUtil item = new ItemGroupCrvVersionUtil();
            item.setItemName((String) row.get("item_name"));
            item.setCrfVersionName((String) row.get("version_name"));
            item.setCrfVersionStatus((Integer) row.get("v_status"));
            item.setGroupName((String) row.get("group_name"));
            item.setGroupOID((String) row.get("group_oid"));
            item_group_crfversion_info.add(item);
        }

        return item_group_crfversion_info;
    }
    
    /**
     * select distinct item.name as item_name, item.description as description, item.item_id as item_id,
     * item.item_data_type_id as data_type, item.oc_oid as item_oid,
     * item_group.name as group_name, item_group.oc_oid as group_oid ,
     * crf_version.name as version_name, crf_version.status_id as v_status
     */
    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemDetailsGroupCRFVersionMetadataByCRFId( String  crfName) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);//(crf)name
        this.setTypeExpected(2, TypeNames.STRING);//(crf)description
        this.setTypeExpected(3, TypeNames.INT);//(crf)item_id
        this.setTypeExpected(4, TypeNames.INT);//(crf)data_type
        this.setTypeExpected(5, TypeNames.STRING);//(crf)item_oid
        this.setTypeExpected(6, TypeNames.STRING);//(crf)group_name
        this.setTypeExpected(7, TypeNames.STRING);//(crf)group_oid
        this.setTypeExpected(8, TypeNames.STRING);//(crf)version_name
        this.setTypeExpected(9, TypeNames.INT);//(crf)v_status
        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, crfName);
       
        String sql = digester.getQuery("findAllWithItemDetailsGroupCRFVersionMetadataByCRFId");
        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables, true);

        ArrayList<ItemGroupCrvVersionUtil> item_group_crfversion_info = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            ItemGroupCrvVersionUtil item = new ItemGroupCrvVersionUtil();
            item.setItemName((String) row.get("item_name"));
            item.setCrfVersionName((String) row.get("version_name"));
            item.setCrfVersionStatus((Integer) row.get("v_status"));
            item.setGroupName((String) row.get("group_name"));
            item.setGroupOID((String) row.get("group_oid"));
            this.setTypeExpected(2, TypeNames.STRING);//(crf)
            this.setTypeExpected(3, TypeNames.INT);//(crf)item_id
            this.setTypeExpected(4, TypeNames.INT);//(crf)data_type
            this.setTypeExpected(5, TypeNames.STRING);//(crf)item_oid
         
            item.setItemDescription((String) row.get("description"));
            item.setItemOID((String) row.get("item_oid"));
            ItemDataType itemDataType = ItemDataType.get((Integer) row.get("data_type"));
            
            item.setItemDataType(itemDataType.getDescription());
            item.setId((Integer) row.get("item_id"));
            item_group_crfversion_info.add(item);
        }
        
        return item_group_crfversion_info;
    }

	@Override
	public ItemBean emptyBean() {
		return new ItemBean();
	}
	
}
