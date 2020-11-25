/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * the data access object for instruments in the database.
 * 
 * @author thickerson
 * 
 */
public class CRFDAO extends AuditableEntityDAO<CRFBean> {
    // private DataSource ds;
    // private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_CRF;
    }

    public CRFDAO(DataSource ds) {
        super(ds);
    }

    public CRFDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        // this.setTypeExpected(3,TypeNames.STRING);//label
        this.setTypeExpected(3, TypeNames.STRING);// name
        this.setTypeExpected(4, TypeNames.STRING);// description
        this.setTypeExpected(5, TypeNames.INT);// owner id
        this.setTypeExpected(6, TypeNames.DATE);// created
        this.setTypeExpected(7, TypeNames.DATE);// updated
        this.setTypeExpected(8, TypeNames.INT);// update id
        this.setTypeExpected(9, TypeNames.STRING);// oc_oid
        this.setTypeExpected(10, TypeNames.INT);// study_id
    }

    public CRFBean update(CRFBean cb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(cb.getStatus().getId()));
        // variables.put(Integer.valueOf(2), cb.getLabel());
        variables.put(Integer.valueOf(2), cb.getName());
        variables.put(Integer.valueOf(3), cb.getDescription());
        variables.put(Integer.valueOf(4), Integer.valueOf(cb.getUpdater().getId()));
        variables.put(Integer.valueOf(5), Integer.valueOf(cb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);
        return cb;
    }

    public CRFBean create(CRFBean cb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(cb.getStatus().getId()));
        variables.put(Integer.valueOf(2), cb.getName());
        variables.put(Integer.valueOf(3), cb.getDescription());
        variables.put(Integer.valueOf(4), Integer.valueOf(cb.getOwner().getId()));
        variables.put(Integer.valueOf(5), getValidOid(cb, cb.getName()));
        variables.put(Integer.valueOf(6), cb.getStudyId());
        
        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            cb.setActive(true);
        }
        return cb;
    }

    @Override
    public CRFBean getEntityFromHashMap(HashMap<String, Object> hm) {
        CRFBean eb = new CRFBean();
        // set common informations
        this.setEntityAuditInformation(eb, hm);
        // set crf specific informations
        eb.setId(((Integer) hm.get("crf_id")).intValue());
        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setOid((String) hm.get("oc_oid"));
        eb.setStudyId(((Integer) hm.get("source_study_id")).intValue());
        return eb;
    }

    @Override
    public ArrayList<CRFBean> findAll() {
        return findAllByLimit(false);
    }

    public Integer getCountofActiveCRFs() {
        String sql = digester.getQuery("getCountofCRFs");
        return getCountByQuery(sql, new HashMap<>());
    }

    public ArrayList<CRFBean> findAllByStudy(int studyId) {
    	String queryName = "findAllByStudy";
        HashMap<Integer, Object> variables = variables(studyId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<CRFBean> findAllByLimit(boolean hasLimit) {
    	String queryName;
        if (hasLimit) {
        	queryName = "findAllByLimit";
        } else {
        	queryName = "findAll";
        }
        return executeFindAllQuery(queryName);
    }

    public ArrayList<CRFBean> findAllByStatus(Status status) {
    	String queryName = "findAllByStatus";
        HashMap<Integer, Object> variables = variables(status.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<CRFBean> findAllActiveByDefinition(StudyEventDefinitionBean definition) {
    	String queryName = "findAllActiveByDefinition";
        HashMap<Integer, Object> variables = variables(definition.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<CRFBean> findAllActiveByDefinitions(int studyId) {
    	String queryName = "findAllActiveByDefinitions";
        HashMap<Integer, Object> variables = variables(studyId, studyId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<CRFBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    @Override
    public CRFBean findByPK(int ID) {
        String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    public CRFBean findByItemOid(String itemOid) {
        String queryName = "findByItemOid";
        HashMap<Integer, Object> variables = variables(itemOid);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    public CRFBean findByName(String name) {
        String queryName = "findByName";
        HashMap<Integer, Object> variables = variables(name);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    public CRFBean findAnotherByName(String name, int crfId) {
        String queryName = "findAnotherByName";
        HashMap<Integer, Object> variables = variables(name, crfId);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<CRFBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<CRFBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public CRFBean findByVersionId(int crfVersionId) {
        String queryName = "findByVersionId";
        HashMap<Integer, Object> variables = variables(crfVersionId);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    private String getOid(CRFBean crfBean, String crfName) {

        String oid;
        try {
            oid = crfBean.getOid() != null ? crfBean.getOid() : crfBean.getOidGenerator().generateOid(crfName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CRFBean crfBean, String crfName) {

        String oid = getOid(crfBean, crfName);
        logger.info(oid);
        String oidPreRandomization = oid;
        while (findAllByOid(oid).size() > 0) {
            oid = crfBean.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    public ArrayList<CRFBean> findAllByOid(String oid) {
        String queryName = "findByOID";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindAllQuery(queryName, variables);
    }

    public CRFBean findByOid(String oid) {
        String queryName = "findByOID";
        HashMap<Integer, Object> variables = variables(oid);
        return (CRFBean) executeFindByPKQuery(queryName, variables);
    }

    /**
     * 
     * @param studySubjectId
     * @return
     */
    public Map<Integer, CRFBean> buildCrfById(Integer studySubjectId) {
        String queryName = "buildCrfById";
        HashMap<Integer, Object> variables = variables(studySubjectId);
        ArrayList<CRFBean> beans = executeFindAllQuery(queryName, variables);
        
        Map<Integer, CRFBean> result = beans.stream().collect(Collectors.toMap(CRFBean::getId, b -> b));
        return result;
    }

	@Override
	public CRFBean emptyBean() {
		return new CRFBean();
	}

}
