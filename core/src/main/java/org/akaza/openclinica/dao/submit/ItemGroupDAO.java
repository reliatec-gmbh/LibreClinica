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
        ItemGroupBean findByOid = findByOid(oid);
		while (!(findByOid == null || findByOid.getOid() != oid) || oidList.contains(oid)) {
            oid = itemGroup.getOidGenerator().randomizeOid(oidPreRandomization);
            findByOid = findByOid(oid);
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
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    // YW 10-30-2007, one item_id might have more than one item_groups
    public ArrayList<ItemGroupBean> findGroupsByItemID(int ID) {
    	String queryName = "findGroupsByItemID";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindAllQuery(queryName, variables);

    }

    public ArrayList<ItemGroupBean> findGroupByCRFVersionIDMap(int Id) {
    	String queryName = "findGroupByCRFVersionIDMap";
        HashMap<Integer, Object> variables = variables(Id);
        return executeFindAllQuery(queryName, variables);

    }

    public EntityBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    public EntityBean findByName(String name) {
    	String queryName = "findByName";
        HashMap<Integer, Object> variables = variables(name);
        return executeFindByPKQuery(queryName, variables);
    }

    public List<ItemGroupBean> findAllByOid(String oid) {
    	String queryName = "findGroupByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindAllQuery(queryName, variables);
    }

    public ItemGroupBean findByOid(String oid) {
    	String queryName = "findGroupByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    public ItemGroupBean findByOidAndCrf(String oid, int crfId) {
    	String queryName = "findGroupByOidAndCrfId";
        HashMap<Integer, Object> variables = variables(oid, crfId);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<ItemGroupBean> findGroupByCRFVersionID(int Id) {
    	String queryName = "findGroupByCRFVersionID";
        HashMap<Integer, Object> variables = variables(Id);
        return executeFindAllQuery(queryName, variables);
    }

    public ItemGroupBean findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId) {
    	String queryName = "findGroupByGroupNameCRFVersionID";
        HashMap<Integer, Object> variables = variables(crfVersionId, groupName);
        return executeFindByPKQuery(queryName, variables);
    }

    public ItemGroupBean findGroupByItemIdCrfVersionId(int itemId, int crfVersionId) {
    	String queryName = "findGroupByItemIdCRFVersionID";
        HashMap<Integer, Object> variables = variables(crfVersionId, itemId);
        return executeFindByPKQuery(queryName, variables);
    }

    public List<ItemGroupBean> findOnlyGroupsByCRFVersionID(int Id) {
    	String queryName = "findOnlyGroupsByCRFVersionID";
        HashMap<Integer, Object> variables = variables(Id);
        return executeFindAllQuery(queryName, variables);
    }

    public List<ItemGroupBean> findGroupBySectionId(int sectionId) {
    	String queryName = "findGroupBySectionId";
        HashMap<Integer, Object> variables = variables(sectionId);
        return executeFindAllQuery(queryName, variables);
    }

    public List<ItemGroupBean> findLegitGroupBySectionId(int sectionId) {
    	String queryName = "findLegitGroupBySectionId";
        HashMap<Integer, Object> variables = variables(sectionId);
        return executeFindAllQuery(queryName, variables);
    }
    
    public List<ItemGroupBean> findLegitGroupAllBySectionId(int sectionId) {
    	String queryName = "findLegitGroupAllBySectionId";
        HashMap<Integer, Object> variables = variables(sectionId);
        return executeFindAllQuery(queryName, variables);
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
        HashMap<Integer, Object> variables = variables(name);
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
