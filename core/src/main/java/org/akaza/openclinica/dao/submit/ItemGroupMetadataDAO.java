/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 10, 2007
 */
public class ItemGroupMetadataDAO extends EntityDAO<ItemGroupMetadataBean> {
    public ItemGroupMetadataDAO(DataSource ds) {
        super(ds);
        // this.getCurrentPKName="findCurrentPKValue";
        this.getNextPKName = "getNextPK";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEM_GROUP_METADATA;
    }

    public void setTypesExpected() {
        // item_group_metadata_id serial NOT NULL,
        // item_group_id numeric NOT NULL,
        // header varchar(255),
        // subheader varchar(255),
        // layout varchar(100),
        // repeat_number numeric,
        // repeat_max numeric,
        // repeat_array varchar(255),
        // row_start_number numeric,
        // crf_version_id numeric NOT NULL,
        // item_id numeric NOT NULL,
        // ordinal numeric NOT NULL,
        // borders numeric,
        // show_group boolean,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.BOOL);
        this.setTypeExpected(15, TypeNames.BOOL);
    }

    @Override
    public ItemGroupMetadataBean getEntityFromHashMap(HashMap<String, Object> hm) {
        ItemGroupMetadataBean meta = new ItemGroupMetadataBean();
        meta.setId((Integer) hm.get("item_group_metadata_id"));
        meta.setItemGroupId((Integer) hm.get("item_group_id"));
        meta.setHeader((String) hm.get("header"));
        meta.setSubheader((String) hm.get("subheader"));
        meta.setLayout((String) hm.get("layout"));
        meta.setRepeatNum((Integer) hm.get("repeat_number"));
        meta.setRepeatMax((Integer) hm.get("repeat_max"));
        meta.setRepeatArray((String) hm.get("repeat_array"));
        meta.setRowStartNumber((Integer) hm.get("row_start_number"));
        meta.setCrfVersionId((Integer) hm.get("crf_version_id"));
        meta.setItemId((Integer) hm.get("item_id"));
        meta.setOrdinal((Integer) hm.get("ordinal"));
        meta.setBorders((Integer) hm.get("borders"));
        meta.setShowGroup(((Boolean) hm.get("show_group")).booleanValue());
        meta.setRepeatingGroup(((Boolean) hm.get("repeating_group")).booleanValue());
        return meta;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemGroupMetadataBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemGroupMetadataBean> findAll() throws OpenClinicaException {
    	throw new RuntimeException("Not implemented");
    }

	public ItemGroupMetadataBean findByPK(int id) throws OpenClinicaException {
		String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindByPKQuery(queryName, variables);
    }

    public ItemGroupMetadataBean findByItemAndCrfVersion(Integer itemId, Integer crfVersionId) {
    	String queryName = "findByItemIdAndCrfVersionId";
        HashMap<Integer, Object> variables = variables(itemId, crfVersionId);
        return executeFindByPKQuery(queryName, variables);
    }

    @Override
    public ItemGroupMetadataBean create(ItemGroupMetadataBean igMetaBean) throws OpenClinicaException {
        // INSERT INTO item_group_metadata (item_group_id,
        // header, subheader,layout,repeat_number,repeat_max,
        // repeat_array, row_start_number,crf_version_id,
        // item_id, ordinal,borders, show_group)
        // VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        int id = getNextPK();
        variables.put(1, id);
        variables.put(2, igMetaBean.getItemGroupId());
        variables.put(3, igMetaBean.getHeader());
        variables.put(4, igMetaBean.getSubheader());
        variables.put(5, igMetaBean.getLayout());
        variables.put(6, igMetaBean.getRepeatNum());
        variables.put(7, igMetaBean.getRepeatMax());
        variables.put(8, igMetaBean.getRepeatArray());
        variables.put(9, igMetaBean.getRowStartNumber());
        variables.put(10, igMetaBean.getCrfVersionId());
        variables.put(11, igMetaBean.getItemId());
        variables.put(12, igMetaBean.getOrdinal());
        variables.put(13, igMetaBean.getBorders());
        variables.put(14, new Boolean(igMetaBean.isShowGroup()));

        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
        	igMetaBean.setId(id);
        }
        return igMetaBean;

    }

    public ArrayList<ItemGroupMetadataBean> findMetaByGroupAndSection(int itemGroupId, int crfVersionId, int sectionId) {
    	String queryName = "findMetaByGroupAndSection";
        HashMap<Integer, Object> variables = variables(itemGroupId, crfVersionId, sectionId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<ItemGroupMetadataBean> findMetaByGroupAndCrfVersion(int itemGroupId, int crfVersionId) {
    	String queryName = "findMetaByGroupAndCrfVersion";
        HashMap<Integer, Object> variables = variables(itemGroupId, crfVersionId);
        return executeFindAllQuery(queryName, variables);
    }
    
    
    public ArrayList<ItemGroupMetadataBean> findMetaByGroupAndSectionForPrint(int itemGroupId, int crfVersionId, int sectionId) {
    	String queryName = "findMetaByGroupAndSectionForPrint";
        HashMap<Integer, Object> variables = variables(itemGroupId, crfVersionId, sectionId);
        return executeFindAllQuery(queryName, variables);
    }
    
    /**
     * NOT IMPLEMENTED
     */
    @Override
    public ItemGroupMetadataBean update(ItemGroupMetadataBean eb) throws OpenClinicaException {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemGroupMetadataBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<ItemGroupMetadataBean> findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        throw new RuntimeException("Not implemented");
    }

    /**
     *
     * @param crfVersionId
     * @return
     */
    public boolean versionIncluded(int crfVersionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        String queryName = "findThisCrfVersionId";
        HashMap<Integer, Object> variables = variables(crfVersionId);
        ArrayList<HashMap<String, Object>> al = this.select(digester.getQuery(queryName), variables);

        if (al.size() > 0) {
            HashMap<String, Object> h = al.get(0);
            if (((Integer) h.get("crf_version_id")).intValue() == crfVersionId) {
                return true;
            }
        }

        return false;
    }

	public List<ItemGroupMetadataBean> findByCrfVersion(Integer crfVersionId) {
		String queryName = "findByCrfVersionId";
		HashMap<Integer, Object> variables = variables(crfVersionId);
		return executeFindAllQuery(queryName, variables);
	}

	@Override
	public ItemGroupMetadataBean emptyBean() {
		return new ItemGroupMetadataBean();
	}

}
