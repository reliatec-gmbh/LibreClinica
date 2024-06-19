/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/*
 * Copyright 2003-2008 Akaza Research
 *
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <p>
 * CRFVersionDAO.java, the data access object for versions of instruments in the database. Each of these are related to
 * Sections, a versioning map that links them with Items, and an Event, which then links to a Study.
 * 
 * @author thickerson
 * 
 * 
 */
public class CRFVersionDAO extends AuditableEntityDAO<CRFVersionBean> {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_CRFVERSION;
    }

    public CRFVersionDAO(DataSource ds) {
        super(ds);
    }

    public CRFVersionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public CRFVersionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    public CRFVersionBean update(CRFVersionBean ib) {
        // UPDATE CRF_VERSION SET CRF_ID=?,STATUS_ID=?,NAME=?,
        // DESCRIPTION=?,DATE_UPDATED=NOW(),UPDATE_ID=?,REVISION_NOTES =? WHERE
        // CRF_VERSION_ID=?
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(ib.getCrfId()));
        variables.put(new Integer(2), new Integer(ib.getStatus().getId()));
        variables.put(new Integer(3), ib.getName());
        variables.put(new Integer(4), ib.getDescription());
        variables.put(new Integer(5), new Integer(ib.getUpdater().getId()));
        variables.put(new Integer(6), ib.getRevisionNotes());
        variables.put(new Integer(7), new Integer(ib.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);
        return ib;
    }

    public CRFVersionBean create(CRFVersionBean cvb) {
        // "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," +
        // "OWNER_ID,REVISION_NOTES,OC_OID) "
        // + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," +
        // "(SELECT CRF_ID FROM CRF WHERE NAME='"
        // + crfName + "'),1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";

        // <sql>INSERT INTO CRF_VERSION (CRF_ID, STATUS_ID, NAME,
        // DESCRIPTION, OWNER_ID,
        // DATE_CREATED, REVISION_NOTES)
        // VALUES (?,?,?,?,?,NOW(),?)</sql>

        HashMap<Integer, Object> variables = new HashMap<>();
        // variables.put(Integer.valueOf(2), cb.getLabel());
        variables.put(Integer.valueOf(1), Integer.valueOf(cvb.getCrfId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(cvb.getStatus().getId()));
        variables.put(Integer.valueOf(3), cvb.getName());
        variables.put(Integer.valueOf(4), cvb.getDescription());
        variables.put(Integer.valueOf(5), Integer.valueOf(cvb.getOwner().getId()));
        variables.put(Integer.valueOf(6), cvb.getRevisionNotes());
        variables.put(Integer.valueOf(7), getValidOid(cvb, cvb.getName(), cvb.getOid()));
        variables.put(Integer.valueOf(8), cvb.getXform());
        variables.put(Integer.valueOf(9), cvb.getXformName());

        // am i the only one who runs their daos' unit tests after I change
        // things, tbh?
        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            cvb.setActive(true);
        }
        return cvb;

    }

    @Override
    public void setTypesExpected() {
        // crf_version_id serial NOT NULL,
        // crf_id numeric NOT NULL,
        // name varchar(255),
        // description varchar(4000),

        // revision_notes varchar(255),
        // status_id numeric,
        // date_created date,

        // date_updated date,
        // owner_id numeric,
        // update_id numeric,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);

        this.setTypeExpected(8, TypeNames.DATE);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.STRING);
    }

    @Override
    public CRFVersionBean getEntityFromHashMap(HashMap<String, Object> hm) {
        // CRF_VERSION_ID NAME DESCRIPTION
        // CRF_ID STATUS_ID DATE_CREATED DATE_UPDATED
        // OWNER_ID REVISION_NUMBER UPDATE_ID
        CRFVersionBean eb = new CRFVersionBean();
        super.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("crf_version_id")).intValue());

        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setCrfId(((Integer) hm.get("crf_id")).intValue());
        eb.setRevisionNotes((String) hm.get("revision_notes"));
        eb.setOid((String) hm.get("oc_oid"));
        eb.setXform((String) hm.get("xform"));
        eb.setXformName((String) hm.get("xform_name"));
        return eb;
    }

    @Override
    public ArrayList<CRFVersionBean> findAll() {
    	String queryName = "findAll";
    	return executeFindAllQuery(queryName);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<CRFVersionBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    public ArrayList<CRFVersionBean> findAllByCRF(int crfId) {
    	String queryName = "findAllByCRF";
        HashMap<Integer, Object> variables = variables(crfId);
    	return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<CRFVersionBean> findAllActiveByCRF(int crfId) {
    	String queryName = "findAllActiveByCRF";
        HashMap<Integer, Object> variables = variables(crfId);
    	return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<ItemBean> findItemFromMap(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap<Integer, Object> variables = variables(versionId);
        String query = digester.getQuery("findItemFromMap");
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        ArrayList<ItemBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            ItemBean eb = new ItemBean();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            Integer ownerId = (Integer) hm.get("owner_id");
            UserAccountBean owner = getUserById(ownerId.intValue());
            eb.setOwner(owner);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<ItemBean> findItemUsedByOtherVersion(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap<Integer, Object> variables = variables(versionId);
        String query = digester.getQuery("findItemUsedByOtherVersion");
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        ArrayList<ItemBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            ItemBean eb = new ItemBean();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            Integer ownerId = (Integer) hm.get("owner_id");
            UserAccountBean owner = getUserById(ownerId.intValue());
            eb.setOwner(owner);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<ItemBean> findNotSharedItemsByVersion(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap<Integer, Object> variables = variables(versionId, versionId);
        String query = digester.getQuery("findNotSharedItemsByVersion");
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        ArrayList<ItemBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            ItemBean eb = new ItemBean();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            Integer ownerId = (Integer) hm.get("owner_id");
            UserAccountBean owner = getUserById(ownerId.intValue());
            eb.setOwner(owner);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<CRFVersionBean> findDefCRFVersionsByStudyEvent(int studyEventDefinitionId) {
        String queryName = "findDefCRFVersionsByStudyEvent";
        HashMap<Integer, Object> variables = variables(studyEventDefinitionId);
        return executeFindAllQuery(queryName, variables);
    }

    public boolean isItemUsedByOtherVersion(int versionId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(versionId);
        String query = digester.getQuery("isItemUsedByOtherVersion");
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        return alist != null && alist.size() > 0;
    }

    public boolean hasItemData(int itemId) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(itemId);
        String query = digester.getQuery("hasItemData");
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        return alist != null && alist.size() > 0;
    }

    @Override
    public CRFVersionBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
    	return (CRFVersionBean) executeFindByPKQuery(queryName, variables);
    }

    public CRFVersionBean findByFullName(String version, String crfName) {
        String queryName = "findByFullName";
        HashMap<Integer, Object> variables = variables(version, crfName);
    	return (CRFVersionBean) executeFindByPKQuery(queryName, variables);
    }

    /**
     * Deletes a CRF version
     */
    public void delete(int id) {
        HashMap<Integer, Object> variables = variables(id);

        String sql = digester.getQuery("delete");
        this.executeUpdate(sql, variables);
    }

    /**
     * Generates all the delete queries for deleting a version
     * 
     * @param versionId
     * @param items
     */
    public ArrayList<String> generateDeleteQueries(int versionId, ArrayList<ItemBean> items) {
        ArrayList<String> sqls = new ArrayList<>();
        String sql = digester.getQuery("deleteScdItemMetadataByVersion") + versionId + ")";
        sqls.add(sql);
        sql = digester.getQuery("deleteItemMetaDataByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteSectionsByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteItemMapByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteItemGroupMetaByVersion") + versionId;
        sqls.add(sql);

        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);
            sql = digester.getQuery("deleteItemsByVersion") + item.getId();
            sqls.add(sql);
        }

        sql = digester.getQuery("deleteResponseSetByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteCrfVersionMediaByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("delete") + versionId;
        sqls.add(sql);
        return sqls;

    }

    private String getOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

        String oid;
        try {
            oid = crfVersion.getOid() != null ? crfVersion.getOid() : crfVersion.getOidGenerator().generateOid(crfName, crfVersionName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

        String oid = getOid(crfVersion, crfName, crfVersionName);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findAllByOid(oid).size() > 0) {
            oid = crfVersion.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public ArrayList<CRFVersionBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<CRFVersionBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

    public ArrayList<CRFVersionBean> findAllByOid(String oid) {
    	String queryName = "findAllByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindAllQuery(queryName, variables);
    }

    public int getCRFIdFromCRFVersionId(int CRFVersionId) {
        int answer = 0;

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(CRFVersionId);

        String sql = digester.getQuery("getCRFIdFromCRFVersionId");
        ArrayList<HashMap<String, Object>> rows = select(sql, variables);

        if (rows.size() > 0) {
            HashMap<String, Object> h = rows.get(0);
            answer = ((Integer) h.get("crf_id")).intValue();
        }
        return answer;
    }

    public ArrayList<CRFVersionBean> findAllByCRFId(int CRFId) {
    	String queryName = "findAllByCRFId";
        HashMap<Integer, Object> variables = variables(CRFId);
        return executeFindAllQuery(queryName, variables);
    }

    public Integer findCRFVersionId(int crfId, String versionName) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        String query = digester.getQuery("findCRFVersionId");
        HashMap<Integer, Object> variables = variables(crfId, versionName);
        ArrayList<HashMap<String, Object>> result = this.select(query, variables);
        
        Integer crfVersionId = null;
        if (result != null && result.size() > 0) {
            HashMap<String, Object> map = result.get(0);
            crfVersionId = (Integer) map.get("crf_version_id");
        }
        return crfVersionId;
    }

    public CRFVersionBean findByOid(String oid) {
        String queryName = "findByOID";
        HashMap<Integer, Object> variables = variables(oid);        
        return (CRFVersionBean) executeFindByPKQuery(queryName, variables);
    }

    /**
     * 
     * @param studySubjectId
     * @return
     */
    public Map<Integer, CRFVersionBean> buildCrfVersionById(Integer studySubjectId) {
    	String queryName = "buildCrfVersionById";
        HashMap<Integer, Object> variables = variables(studySubjectId);
        ArrayList<CRFVersionBean> beans = executeFindAllQuery(queryName, variables);
        
        Map<Integer, CRFVersionBean> result = beans.stream().collect(Collectors.toMap(CRFVersionBean::getId, b -> b));
        return result;
    }

	@Override
	public CRFVersionBean emptyBean() {
		return new CRFVersionBean();
	}

}