/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 * @author jsampson
 */
public class StudyEventDefinitionDAO extends AuditableEntityDAO<StudyEventDefinitionBean> {

    private void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findAllActiveByStudyName = "findAllActiveByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
    	getNextPKName = "findNextKey";
    }

    public StudyEventDefinitionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyEventDefinitionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyEventDefinitionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYEVENTDEFNITION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.BOOL);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
        // int int date date int
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.STRING);
    }

    /**
     * <P>
     * findNextKey, a method to return a simple int from the database.
     *
     * @return int, which is the next primary key for creating a study event
     *         definition.
     */
    public int findNextKey() {
    	return getNextPK();
    }

    private String getOid(StudyEventDefinitionBean sedb) {

        String oid;
        try {
            oid = sedb.getOid() != null ? sedb.getOid() : sedb.getOidGenerator().generateOid(sedb.getName());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudyEventDefinitionBean sedb) {

        String oid = getOid(sedb);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (existStudyEventDefinitionWithOid(oid)) {
            oid = sedb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public StudyEventDefinitionBean create(StudyEventDefinitionBean sedb) {
        // study_event_definition_id ,
        // STUDY_ID, NAME,DESCRIPTION, REPEATING, TYPE, CATEGORY, OWNER_ID,
        // STATUS_ID, DATE_CREATED,ordinal,oid
        sedb.setId(this.findNextKey());
        logger.debug("***id:" + sedb.getId());
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(sedb.getId()));
        variables.put(new Integer(2), new Integer(sedb.getStudyId()));
        variables.put(new Integer(3), sedb.getName());
        variables.put(new Integer(4), sedb.getDescription());
        variables.put(new Integer(5), new Boolean(sedb.isRepeating()));
        variables.put(new Integer(6), sedb.getType());
        variables.put(new Integer(7), sedb.getCategory());
        variables.put(new Integer(8), new Integer(sedb.getOwnerId()));
        variables.put(new Integer(9), new Integer(sedb.getStatus().getId()));
        variables.put(new Integer(10), new Integer(sedb.getOrdinal()));
        variables.put(new Integer(11), getValidOid(sedb));
        this.executeUpdate(digester.getQuery("create"), variables);

        return sedb;
    }

    public StudyEventDefinitionBean update(StudyEventDefinitionBean sedb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(sedb.getStudyId()));
        variables.put(new Integer(2), sedb.getName());
        variables.put(new Integer(3), sedb.getDescription());
        variables.put(new Integer(4), new Boolean(sedb.isRepeating()));
        variables.put(new Integer(5), sedb.getType());
        variables.put(new Integer(6), sedb.getCategory());
        variables.put(new Integer(7), new Integer(sedb.getStatus().getId()));
        variables.put(new Integer(8), new Integer(sedb.getUpdaterId()));
        variables.put(new Integer(9), new Integer(sedb.getOrdinal()));
        variables.put(new Integer(10), new Integer(sedb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);
        return sedb;
    }

    public StudyEventDefinitionBean getEntityFromHashMap(HashMap<String, Object> hm) {
        StudyEventDefinitionBean eb = new StudyEventDefinitionBean();

        this.setEntityAuditInformation(eb, hm);
        // set dates and ints first, then strings
        // create a sub-function in auditable entity dao that can do this?
        Integer sedId = (Integer) hm.get("study_event_definition_id");
        eb.setId(sedId.intValue());

        Integer studyId = (Integer) hm.get("study_id");
        eb.setStudyId(studyId.intValue());
        Integer ordinal = (Integer) hm.get("ordinal");
        eb.setOrdinal(ordinal.intValue());
        Boolean repeating = (Boolean) hm.get("repeating");
        eb.setRepeating(repeating.booleanValue());

        // below functions changed by get entity audit information functions

        /*
         * Integer ownerId = (Integer)hm.get("owner_id");
         * eb.setOwnerId(ownerId.intValue()); Integer updaterId =
         * (Integer)hm.get("update_id"); eb.setUpdaterId(updaterId.intValue());
         * Integer statusId = (Integer)hm.get("status_id");
         * eb.setStatus(Status.get(statusId.intValue()));
         *
         * Date dateCreated = (Date)hm.get("date_created"); Date dateUpdated =
         * (Date)hm.get("date_updated"); eb.setCreatedDate(dateCreated);
         * eb.setUpdatedDate(dateUpdated);
         */
        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setType((String) hm.get("type"));
        eb.setCategory((String) hm.get("category"));
        eb.setOid((String) hm.get("oc_oid"));
        return eb;
    }
    
    /**
    * Checks whether a study event definition with the given OID already exist.
    * 
    * @param oid the study event definition OID
    * @return true if a study event definition with the given OID exists, false otherwise
    */
    public boolean existStudyEventDefinitionWithOid(String oid) {
        StudyEventDefinitionBean foundBean = findByOid(oid);
        return foundBean != null && oid.equals(foundBean.getOid());    	
    }

    public StudyEventDefinitionBean findByOid(String oid) {
    	String queryName = "findByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    /*
     * find by oid and study id - sometimes we have relationships which can't
     * break past the parent study relationship. This 'covering' allows us to
     * query on both the study and the parent study id. added tbh 10/2008 for
     * 2.5.2
     */
    public StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId, int parentStudyId) {
        StudyEventDefinitionBean studyEventDefinitionBean = this.findByOidAndStudy(oid, studyId);
        if (studyEventDefinitionBean == null || !oid.equals(studyEventDefinitionBean.getOid())) {
        	// this is an empty bean since no matching study event definition was found
            studyEventDefinitionBean = this.findByOidAndStudy(oid, parentStudyId);
        }
        return studyEventDefinitionBean;
    }

    private StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId) {
    	String queryName = "findByOidAndStudy";
        HashMap<Integer, Object> variables = variables(oid, studyId, studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    @Override
    public ArrayList<StudyEventDefinitionBean> findAllByStudy(StudyBean study) {
        StudyDAO studyDao = new StudyDAO(this.getDs());

        if (study.getParentStudyId() > 0) {
            // If the study has a parent than it is a site, in this case we
            // should get the event definitions of the parent
            StudyBean parentStudy = studyDao.findByPK(study.getParentStudyId());
            return super.findAllByStudy(parentStudy);
        } else {
            return super.findAllByStudy(study);
        }
    }

    public ArrayList<StudyEventDefinitionBean> findAllWithStudyEvent(StudyBean currentStudy) {
    	String queryName = "findAllWithStudyEvent";
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventDefinitionBean> findAllByCrf(CRFBean crf) {
    	String queryName = "findAllByCrf";
        HashMap<Integer, Object> variables = variables(crf.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventDefinitionBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventDefinitionBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public StudyEventDefinitionBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    /*
     * added tbh, 02/2008 (non-Javadoc)
     *
     * @see org.akaza.openclinica.dao.core.DAOInterface#findByPK(int)
     */
    public StudyEventDefinitionBean findByName(String name) {
    	String queryName = "findByName";
        HashMap<Integer, Object> variables = variables(name);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventDefinitionBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventDefinitionBean> findAllByPermission(Object objCurrentUser, int intActionType) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * @param eventDefinitionCRFId
     *            The id of an event definition crf.
     * @return the study event definition bean for the specified event
     *         definition crf.
     */
    public StudyEventDefinitionBean findByEventDefinitionCRFId(int eventDefinitionCRFId) {
    	String queryName = "findByEventDefinitionCRFId";
        HashMap<Integer, Object> variables = variables(eventDefinitionCRFId);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<StudyEventDefinitionBean> findAllByStudyAndLimit(int studyId) {
    	String queryName = "findAllByStudyAndLimit";
        HashMap<Integer, Object> variables = variables(studyId, studyId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventDefinitionBean> findAllActiveByParentStudyId(int parentStudyId) {
    	String queryName = "findAllActiveByParentStudyId";
        HashMap<Integer, Object> variables = variables(parentStudyId);
        return executeFindAllQuery(queryName, variables);
    }

    /**
     *
     * @param studySubjectId
     * @return
     */
    public Map<Integer, StudyEventDefinitionBean> findByStudySubject(int studySubjectId) {
    	String queryName = "findByStudySubject";
        HashMap<Integer, Object> variables = variables(studySubjectId);
        ArrayList<StudyEventDefinitionBean> beans = executeFindAllQuery(queryName, variables);
        
        return beans.stream().collect(Collectors.toMap(StudyEventDefinitionBean::getId, b -> b));
    }

    /**
     *
     * @param studySubjectId
     * @return
     */
    public HashMap<Integer, Integer> buildMaxOrdinalByStudyEvent(int studySubjectId) {
        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        param.put(1, studySubjectId);

        ArrayList<HashMap<String, Object>> selectResult = select(digester.getQuery("buildMaxOrdinalByStudyEvent"), param);

        HashMap<Integer,Integer> result = new HashMap<Integer, Integer>();

        for(HashMap<String, Object> hm : selectResult) {
            result.put((Integer) hm.get("study_event_definition_id"), (Integer) hm.get("max_ord"));
        }
        return result;
    }

	@Override
	public StudyEventDefinitionBean emptyBean() {
		return new StudyEventDefinitionBean();
	}

}