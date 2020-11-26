/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 8, 2007
 */
public class ItemGroupDAO extends AuditableEntityDAO<ItemGroupBean> {

    public ItemGroupDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
        this.getNextPKName = "getNextPK";
    }

    public ItemGroupDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemGroupDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.getCurrentPKName = "findCurrentPKValue";
        this.getNextPKName = "getNextPK";
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEM_GROUP;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        /*
         * item_group_id serial NOT NULL, name varchar(255), crf_id numeric NOT
         * NULL, status_id numeric, date_created date, date_updated date,
         * owner_id numeric, update_id numeric,
         */
        this.setTypeExpected(1, TypeNames.INT); // item_group_id
        this.setTypeExpected(2, TypeNames.STRING); // name
        this.setTypeExpected(3, TypeNames.INT);// crf_id
        this.setTypeExpected(4, TypeNames.INT); // status_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE); // date_updated
        this.setTypeExpected(7, TypeNames.INT); // owner_id
        this.setTypeExpected(8, TypeNames.INT); // update_id
        this.setTypeExpected(9, TypeNames.STRING); // oc_oid

    }

    @Override
    public ItemGroupBean update(ItemGroupBean formGroupBean) {
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        /*
         * item_group_id serial NOT NULL, name varchar(255), crf_id numeric NOT
         * NULL, status_id numeric, date_created date, date_updated date,
         * owner_id numeric, update_id numeric,
         */
        variables.put(1, formGroupBean.getName());
        variables.put(2, new Integer(formGroupBean.getCrfId()));
        variables.put(3, formGroupBean.getStatus().getId());
        variables.put(4, formGroupBean.getUpdater().getId());
        variables.put(5, formGroupBean.getId());
        this.executeUpdate(digester.getQuery("update"), variables);
        return formGroupBean;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemGroupBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<ItemGroupBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return new ArrayList<>();
    }

    private String getOid(ItemGroupBean itemGroupBean, String crfName, String itemGroupLabel) {

        String oid;
        try {
            oid = itemGroupBean.getOid() != null ? itemGroupBean.getOid() : itemGroupBean.getOidGenerator().generateOid(crfName, itemGroupLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(ItemGroupBean itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList) {

        String oid = getOid(itemGroup, crfName, itemGroupLabel);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid) != null || oidList.contains(oid)) {
            oid = itemGroup.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    /*
     * name varchar(255), crf_id numeric NOT NULL, status_id numeric,
     * date_created date, date_updated date, owner_id numeric, update_id
     * numeric,
     */
    @Override
    public ItemGroupBean create(ItemGroupBean formGroupBean) {
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        int id = getNextPK();
        variables.put(1, id);
        variables.put(2, formGroupBean.getName());
        variables.put(3, formGroupBean.getCrfId());
        variables.put(4, new Integer(formGroupBean.getStatus().getId()));
        variables.put(5, formGroupBean.getOwner().getId());

        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
        	formGroupBean.setId(id);
        	formGroupBean.setActive(true);
        }
        return formGroupBean;
    }

    public ArrayList<ItemGroupBean> findAll() {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findAll"));
        ArrayList<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }

    // YW 10-30-2007, one item_id might have more than one item_groups
    public ArrayList<ItemGroupBean> findGroupsByItemID(int ID) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, ID);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findGroupsByItemID"), variables);

        ArrayList<ItemGroupBean> formGroupBs = new ArrayList<ItemGroupBean>();
        for (HashMap<String, Object> map : listofMaps) {
            ItemGroupBean bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            formGroupBs.add(bean);
        }
        return formGroupBs;

    }

    public ArrayList<ItemGroupBean> findGroupByCRFVersionIDMap(int Id) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, Id);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findGroupByCRFVersionIDMap"), variables);

        ArrayList<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;

    }

    public EntityBean findByPK(int ID) {
        ItemGroupBean formGroupB = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, ID);

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables);
        for (HashMap<String, Object> map : listofMaps) {
            formGroupB = (ItemGroupBean) this.getEntityFromHashMap(map);

        }
        return formGroupB;
    }

    public EntityBean findByName(String name) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, name);

        String sql = digester.getQuery("findByName");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables);
        for (HashMap<String, Object> map : listofMaps) {
            formGroupBean = (ItemGroupBean) this.getEntityFromHashMap(map);

        }
        return formGroupBean;
    }

    public List<ItemGroupBean> findAllByOid(String oid) {
        // ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findGroupByOid");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        // return rows;
        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : rows) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }

    public ItemGroupBean findByOid(String oid) {
        ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findGroupByOid");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        if (rows != null && rows.size() > 0) {
            itemGroup = (ItemGroupBean) this.getEntityFromHashMap(rows.get(0));
            return itemGroup;
        } else {
            return null;
        }
    }

    public ItemGroupBean findByOidAndCrf(String oid, int crfId) {
        ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), oid);
        variables.put(new Integer(2), new Integer(crfId));
        String sql = digester.getQuery("findGroupByOidAndCrfId");

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        if (rows != null && rows.size() > 0) {
            itemGroup = (ItemGroupBean) this.getEntityFromHashMap(rows.get(0));
            return itemGroup;
        } else {
            return null;
        }
    }

    public List<ItemGroupBean> findGroupByCRFVersionID(int Id) {
        this.unsetTypeExpected();
        setTypesExpected();

        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, Id);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findGroupByCRFVersionID"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }

    public ItemGroupBean findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), groupName);

        ArrayList<HashMap<String, Object>> rows = this.select(digester.getQuery("findGroupByGroupNameCRFVersionID"), variables);
        if (rows != null && rows.size() > 0) {
            return (ItemGroupBean) this.getEntityFromHashMap(rows.get(0));
        } else {
            return null;
        }
    }

    public ItemGroupBean findGroupByItemIdCrfVersionId(int itemId, int crfVersionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(itemId));

        ArrayList<HashMap<String, Object>> rows = this.select(digester.getQuery("findGroupByItemIdCRFVersionID"), variables);
        if (rows != null && rows.size() > 0) {
            return (ItemGroupBean) this.getEntityFromHashMap(rows.get(0));
        } else {
            return null;
        }
    }

    public List<ItemGroupBean> findOnlyGroupsByCRFVersionID(int Id) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, Id);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findOnlyGroupsByCRFVersionID"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }

    public List<ItemGroupBean> findGroupBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findGroupBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }

    public List<ItemGroupBean> findLegitGroupBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findLegitGroupBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }
    public List<ItemGroupBean> findLegitGroupAllBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);
        ArrayList<HashMap<String, Object>> listofMaps = this.select(digester.getQuery("findLegitGroupAllBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (HashMap<String, Object> map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap(map);
            beanList.add(bean);
        }
        return beanList;
    }
    public ItemGroupBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        super.setEntityAuditInformation(formGroupBean, hm);
        formGroupBean.setId((Integer) hm.get("item_group_id"));
        formGroupBean.setName((String) hm.get("name"));
        formGroupBean.setCrfId((Integer) hm.get("crf_id"));
        formGroupBean.setOid((String) hm.get("oc_oid"));

        return formGroupBean;
    }

    public ArrayList<ItemGroupBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList<>();
    }

    public void deleteTestGroup(String name) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), name);
        this.executeUpdate(digester.getQuery("deleteTestGroup"), variables);
    }
    
    public Boolean isItemGroupRepeatingBasedOnAllCrfVersions(String groupOid) {
        HashMap<Integer, Object> variables = variables(groupOid);
        String query = digester.getQuery("isItemGroupRepeatingBasedOnAllCrfVersions");
        Integer count = getCountByQuery(query, variables);
        
    	Boolean result = false;
    	if(count != null) {
            result = count > 0 ? true : false;
    	} 
        return result;
    }
    
    public Boolean isItemGroupRepeatingBasedOnCrfVersion(String groupOid,Integer crfVersion) {
        HashMap<Integer, Object> variables = variables(groupOid, crfVersion);
        String query = digester.getQuery("isItemGroupRepeatingBasedOnCrfVersion");
        Integer count = getCountByQuery(query, variables);
        
    	Boolean result = false;
    	if(count != null) {
            result = count > 0 ? true : false;
    	} 
        return result;
    }
    

    public ItemGroupBean findTopOneGroupBySectionId(int sectionId) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, sectionId);

        String sql = digester.getQuery("findTopOneGroupBySectionId");
        ArrayList<HashMap<String, Object>> listofMaps = this.select(sql, variables);
        for (HashMap<String, Object> map : listofMaps) {
            formGroupBean = (ItemGroupBean) this.getEntityFromHashMap(map);

        }
        return formGroupBean;
    }
    
    @Override
    public ArrayList<HashMap<String, Object>> select(String query, HashMap<Integer, Object> variables) {
        return select(query, variables, true);
    }

	@Override
	public ItemGroupBean emptyBean() {
		return new ItemGroupBean();
	}
}
