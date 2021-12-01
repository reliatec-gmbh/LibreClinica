/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.dao.StudySubjectSDVFilter;
import org.akaza.openclinica.dao.StudySubjectSDVSort;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.apache.commons.lang.StringUtils;

/**
 * @author jxu
 *
 */
public class StudySubjectDAO extends AuditableEntityDAO<StudySubjectBean> {

    public void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPK";
    }

    public StudySubjectDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudySubjectDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYSUBJECT;
    }

    @Override
    public void setTypesExpected() {
        // study_subject_id | integer | not null default
        // nextval('public.study_subject_study_subject_id_seq'::text)
        // label | character varying(30) |
        // secondary_label | character varying(30) |
        // subject_id | numeric |
        // study_id | numeric |
        // status_id | numeric |
        // enrollment_date | date |
        // date_created | date |
        // date_updated | date |
        // owner_id | numeric |
        // update_id | numeric |

        this.unsetTypeExpected();
        int ind = 1;
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // study_subject_id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // label
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // secondary_label
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // subject_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // study_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // status_id

        this.setTypeExpected(ind, TypeNames.DATE);
        ind++; // enrollment_date
        this.setTypeExpected(ind, TypeNames.TIMESTAMP);
        ind++; // date_created
        this.setTypeExpected(ind, TypeNames.TIMESTAMP);
        ind++; // date_updated
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // owner_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // update_id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // oc oid
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // time_zone
        // this.setTypeExpected(ind, TypeNames.INT);
        // ind++;
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public StudySubjectBean getEntityFromHashMap(HashMap<String, Object> hm) {
        StudySubjectBean eb = emptyBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_SUBJECT_ID, LABEL, SUBJECT_ID, STUDY_ID
        // STATUS_ID, DATE_CREATED, OWNER_ID, STUDY_GROUP_ID
        // DATE_UPDATED, UPDATE_ID
        Integer ssid = (Integer) hm.get("study_subject_id");
        eb.setId(ssid);

        eb.setLabel((String) hm.get("label"));
        eb.setSubjectId((Integer) hm.get("subject_id"));
        eb.setStudyId((Integer) hm.get("study_id"));
        // eb.setStudyGroupId(((Integer) hm.get("study_group_id")).intValue());
        eb.setEnrollmentDate((Date) hm.get("enrollment_date"));
        eb.setSecondaryLabel((String) hm.get("secondary_label"));
        eb.setOid((String) hm.get("oc_oid"));
        eb.setStudyName((String) hm.get("unique_identifier"));
        // eb.setEventStartDate((Date) hm.get("date_start"));
        // eb.setActive(true);
        eb.setTime_zone((String) hm.get("time_zone"));
        return eb;
    }

    public ArrayList<StudySubjectBean> getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        HashMap<Integer, Object> variables = variables(studySubjectId, studyId, parentStudyId);
        return executeFindAllQuery("getGroupByStudySubject", variables);
    }

    @Override
    public ArrayList<StudySubjectBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    public ArrayList<StudySubjectBean> findAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter, StudySubjectSDVSort sort, int rowStart, int rowEnd) {
        this.setTypesExpected();
        
        String sql = digester.getQuery("findAllByStudySDV");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += ")x) where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }
        
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId);
        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);

        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public int countAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter) {
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId);
        String query = digester.getQuery("countAllByStudySDV");
        query += filter.execute("");
        Integer result = getCountByQuery(query, variables);
        if (result == null) {
        	result = 0;
        }
        return result;
    }

    public int findTheGreatestLabel() {
    	ArrayList<StudySubjectBean> answer = findAll();

        int greatestLabel = 0;
        for (StudySubjectBean studySubjectBean : answer) {
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(studySubjectBean.getLabel());
            } catch (NumberFormatException ne) {
                logger.trace("StudySubjectID is not integer, will be omitted during the search for greatest SSID", ne);
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        
        return greatestLabel;
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudySubjectBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<StudySubjectBean> findAllByStudyOrderByLabel(StudyBean sb) {
    	String queryName = "findAllByStudyOrderByLabel";
        HashMap<Integer, Object> variables = variables(sb.getId(), sb.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudySubjectBean> findAllActiveByStudyOrderByLabel(StudyBean sb) {
    	String queryName = "findAllActiveByStudyOrderByLabel";
        HashMap<Integer, Object> variables = variables(sb.getId(), sb.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudySubjectBean> findAllWithStudyEvent(StudyBean currentStudy) {
    	String queryName = "findAllWithStudyEvent";
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudySubjectBean> findAllBySubjectId(int subjectId) {
    	String queryName = "findAllBySubjectId";
        HashMap<Integer, Object> variables = variables(subjectId);
        return executeFindAllQuery(queryName, variables);
    }

    public StudySubjectBean findAnotherBySameLabel(String label, int studyId, int studySubjectId) {
    	String queryName = "findAnotherBySameLabel";
        HashMap<Integer, Object> variables = variables(label, studyId, studySubjectId);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudySubjectBean findAnotherBySameLabelInSites(String label, int studyId, int studySubjectId) {
    	String queryName = "findAnotherBySameLabelInSites";
        HashMap<Integer, Object> variables = variables(label, studyId, studySubjectId);
        return executeFindByPKQuery(queryName, variables);
    }

    @Override
    public StudySubjectBean findByPK(int ID) {
        this.setTypesExpected();
        // type for 'unique_identifier' from the subject table
        setTypeExpected(14, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(ID);

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        StudySubjectBean eb = emptyBean();
        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0));
        }

        return eb;
    }

    public StudySubjectBean findByLabelAndStudy(String label, StudyBean study) {
    	String queryName = "findByLabelAndStudy";
        HashMap<Integer, Object> variables = variables(label, study.getId(), study.getId());
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Finds a study subject which has the same label provided in the same study
     *
     * @param label study subject id
     * @param studyId study id
     * @param id id
     * @return StudySubjectBean
     */
    public StudySubjectBean findSameByLabelAndStudy(String label, int studyId, int id) {
    	String queryName = "findSameByLabelAndStudy";

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, label);
        variables.put(2, studyId);
        variables.put(3, studyId);
        variables.put(4, id);
        
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Create a study subject (that is, enroll a subject in a study).
     *
     * @param sb The study subject to create.
     * @return The study subject with id set to the insert id if the operation was successful, or 0 otherwise.
     * @throws OpenClinicaException open clinica exception
     */
    public StudySubjectBean create(StudySubjectBean sb) throws OpenClinicaException {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        int ind = 1;
        variables.put(ind++, sb.getLabel());
        variables.put(ind++, sb.getSubjectId());
        variables.put(ind++, sb.getStudyId());
        variables.put(ind++, sb.getStatus().getId());        
        // Date_created is now()
        variables.put(ind++, sb.getOwner().getId());
        
        Date enrollmentDate = sb.getEnrollmentDate();
        if (enrollmentDate == null) {
            nullVars.put(ind, Types.DATE);
            variables.put(ind, null);
            ind++;
        } else {
        	variables.put(ind++, enrollmentDate);
        }
        variables.put(ind++, sb.getSecondaryLabel());
        variables.put(ind++, getValidOid(sb));

        this.executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(ds);
        ArrayList<SubjectGroupMapBean> groupMaps = sb.getStudyGroupMaps();
        for(SubjectGroupMapBean sgmb : groupMaps) {
            sgmb = sgmdao.create(sgmb);
            if (sgmdao.isQuerySuccessful()) {
                sgmb.setId(sgmdao.getCurrentPK());
            }
        }

        return sb;
    }

    public StudySubjectBean createWithoutGroup(StudySubjectBean sb) throws OpenClinicaException {
        return create(sb);
    }

    /**
     * Creates a valid OID for the StudySubject
     */
    private String getOid(StudySubjectBean ssb) {
        String oid;
        try {
            oid = ssb.getOid() != null ? ssb.getOid() : ssb.getOidGenerator().generateOid(ssb.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudySubjectBean ssb) {
        String oid = getOid(ssb);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while(existStudySubjectWithOid(oid)) {
            oid = ssb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    public StudySubjectBean findByOidAndStudy(String oid, int studyId) {
    	String queryName = "findByOidAndStudy";
        HashMap<Integer, Object> variables = variables(oid, studyId, studyId);
        return executeFindByPKQuery(queryName, variables);
    }
    
    /**
    * Checks whether a study subject with the given OID already exist.
    * 
    * @param oid the study subject OID
    * @return true if a study subject with the given OID exists, false otherwise
    */
    public boolean existStudySubjectWithOid(String oid) {
    	StudySubjectBean foundBean = findByOid(oid);
        return foundBean != null && oid.equals(foundBean.getOid());    	
    }

    public StudySubjectBean findByOid(String oid) {
    	String queryName = "findByOid";
        HashMap<Integer, Object> variables = variables(oid);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(StudyBean currentStudy, FindSubjectsFilter filter, FindSubjectsSort sort, int rowStart, int rowEnd) {
        setTypesExpected();
        // type for 'unique_identifier' from the subject table
        setTypeExpected(14, TypeNames.STRING);
        
        String partialSql;
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");
        // Order by Clause for the defect id 0005480

        partialSql = sort.execute("");
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
        	if (partialSql.equals("")) {
                sql += " ORDER BY SS.label )x)where r between " + (rowStart + 1) + " and " + rowEnd;
            }
        	else {
                sql += ")x)where r between " + (rowStart + 1) + " and " + rowEnd;
            }
            sql = sql + partialSql;
        } else {
        	sql = sql + partialSql;
            if (partialSql.equals("")) {
                sql = sql + "  ORDER BY SS.label LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
            } else {
                sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
            }
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountofStudySubjectsAtStudyOrSite(StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId());
        String query = digester.getQuery("getCountofStudySubjectsAtStudyOrSite");
        return getCountByQuery(query, variables);
    }

    public Integer getTotalCountStudySubjectForCrfMigration(CRFVersionBean sourceCrfVersionBean , CRFVersionBean targetCrfVersionBean ,ArrayList<String> studyEventDefnlist ,ArrayList<String>  sitelist) {
        HashMap<Integer, Object> variables = new HashMap<>();
        String eventStr =StringUtils.join(studyEventDefnlist, ",");
        String siteStr =StringUtils.join(sitelist, ",");
        variables.put(1, sourceCrfVersionBean.getId());
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        String query = digester.getQuery("getTotalCountStudySubjectForCrfMigration");
        return getCountByQuery(query, variables);
    }
    
    public Integer getTotalEventCrfCountForCrfMigration(CRFVersionBean sourceCrfVersionBean , CRFVersionBean targetCrfVersionBean ,ArrayList<String> studyEventDefnlist ,ArrayList<String>  sitelist) {
        HashMap<Integer, Object> variables = new HashMap<>();
        String eventStr =StringUtils.join(studyEventDefnlist, ",");
        String siteStr =StringUtils.join(sitelist, ",");
        variables.put(1, sourceCrfVersionBean.getId());
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        String query = digester.getQuery("getTotalEventCrfCountForCrfMigration");
        return getCountByQuery(query, variables);
    }
    
    public Integer getCountofStudySubjectsAtStudy(StudyBean currentStudy) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, currentStudy.getId());
        variables.put(2, currentStudy.getId());
        String query = digester.getQuery("getCountofStudySubjectsAtStudy");
        return getCountByQuery(query, variables);
    }

    public Integer getCountofStudySubjects(StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountofStudySubjects");
        return getCountByQuery(query, variables);
    }

    public Integer getCountofStudySubjectsBasedOnStatus(StudyBean currentStudy, Status status) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId(), status.getId());
        String query = digester.getQuery("getCountofStudySubjectsBasedOnStatus");
        return getCountByQuery(query, variables);
    }

    public Integer getCountWithFilter(ListDiscNotesSubjectFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithFilterListDiscNotes");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListDiscNotesSubjectFilter filter, ListDiscNotesSubjectSort sort,
            int rowStart, int rowEnd) {
        setTypesExpected();

        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSortListDiscNotes");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x)  where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(ListDiscNotesForCRFFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithFilterListDiscNotes");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListDiscNotesForCRFFilter filter, ListDiscNotesForCRFSort sort,
            int rowStart, int rowEnd) {
        setTypesExpected();

        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSortListDiscNotes");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x)  where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(FindSubjectsFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithFilter");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(StudyBean currentStudy, StudyAuditLogFilter filter, StudyAuditLogSort sort, int rowStart, int rowEnd) {
        setTypesExpected();
        this.setTypeExpected(14, TypeNames.DATE);
        this.setTypeExpected(15, TypeNames.STRING);
        this.setTypeExpected(16, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSortAuditLog");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x) where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(StudyAuditLogFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithFilterAuditLog");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListEventsForSubjectFilter filter, ListEventsForSubjectSort sort,
            int rowStart, int rowEnd) {
        setTypesExpected();

        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += ")x) where r between " + (rowStart + 1) + " and " + rowEnd + " ";
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<>();
        for (HashMap<String, Object> hm : rows) {
            StudySubjectBean studySubjectBean = this.getEntityFromHashMap(hm);
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(ListEventsForSubjectFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountWithFilter");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    /**
     * Updates a StudySubject
     */
    @Override
    public StudySubjectBean update(StudySubjectBean eb) {
        Connection con = null;
        return update(eb, con);
   }

    /* this function allows to run transactional updates for an action*/
    public StudySubjectBean update(StudySubjectBean sb, Connection con) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();

        // UPDATE study_subject SET LABEL=?, SUBJECT_ID=?, STUDY_ID=?,
        // STATUS_ID=?, ENROLLMENT_DATE=?, DATE_UPDATED=?,
        // UPDATE_ID=?, SECONDARY_LABEL=? WHERE STUDY_SUBJECT_ID=?
        int ind = 1;
        variables.put(ind++, sb.getLabel());
        variables.put(ind++, sb.getSubjectId());
        variables.put(ind++, sb.getStudyId());
        variables.put(ind++, sb.getStatus().getId());
        Date enrollmentDate = sb.getEnrollmentDate();
        if (enrollmentDate == null) {
            nullVars.put(ind, Types.DATE);
            variables.put(ind, null);
            ind++;
        } else {
            variables.put(ind++, enrollmentDate);
        }
        // date_updated is set to now()
        // variables.put(new Integer(ind), new java.util.Date());
        variables.put(ind++, sb.getUpdater().getId());
        variables.put(ind++, sb.getSecondaryLabel());
        if (sb.getTime_zone() == null || sb.getTime_zone().equals("")) {
            nullVars.put(ind, TypeNames.STRING);
            variables.put(ind, "");
            ind++;
        } else {
            variables.put(ind++, sb.getTime_zone());
        }
        variables.put(ind++, sb.getId());

        String sql = digester.getQuery("update");
        if (con == null) {
        	this.executeUpdate(sql, variables, nullVars);
        } else {
        	this.executeUpdate(sql, variables, nullVars, con);
        }
        return sb;
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudySubjectBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    @Override
    public ArrayList<StudySubjectBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public StudySubjectBean findBySubjectIdAndStudy(int subjectId, StudyBean study) {
        setTypesExpected();
        // type for 'unique_identifier' from the study table
        setTypeExpected(14, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(subjectId, study.getId(), study.getId());

        String sql = digester.getQuery("findBySubjectIdAndStudy");

        ArrayList<HashMap<String, Object>> results = select(sql, variables);
        StudySubjectBean answer;
        if (results.size() > 0) {
            HashMap<String, Object> row = results.get(0);
            answer = getEntityFromHashMap(row);
        } else {
        	answer = emptyBean();
        }
        return answer;
    }

    public ArrayList<StudySubjectBean> findAllByStudyId(int studyId) {
        return findAllByStudyIdAndLimit(studyId, false);
    }

    public ArrayList<StudySubjectBean> findAllByStudyIdAndLimit(int studyId, boolean isLimited) {
        this.setTypesExpected();
        this.setTypeExpected(14, TypeNames.STRING);
        // unique_identifier
        this.setTypeExpected(15, TypeNames.STRING);
        // gender
        this.setTypeExpected(16, TypeNames.STRING);
        // study.name

        HashMap<Integer, Object> variables = variables(studyId, studyId);

        String sql;
        if (isLimited) {
            sql = digester.getQuery("findAllByStudyIdAndLimit");
        } else {
            sql = digester.getQuery("findAllByStudyId");
        }
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);

        ArrayList<StudySubjectBean> answer = new ArrayList<>();
        for (HashMap<String, Object> hm : alist) {
            StudySubjectBean ssb = this.getEntityFromHashMap(hm);
            ssb.setUniqueIdentifier((String) hm.get("unique_identifier"));
            ssb.setStudyName((String) hm.get("name"));
            String gender = (String) hm.get("gender");
            if (gender != null && !gender.isEmpty()) {
                ssb.setGender(gender.charAt(0));
            } else {
                ssb.setGender(' ');
            }
            answer.add(ssb);
        }

        return answer;
    }

    public String findStudySubjectIdsByStudyIds(String studyIds) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);
        ArrayList<HashMap<String, Object>> alist = this.select("select study_subject_id from study_subject where study_id in (" + studyIds + ")");
        List<String> ids = alist.stream().map(hm -> (String) hm.get("study_subject_id")).collect(Collectors.toList());
        return String.join(",", ids);
    }

	@Override
	public StudySubjectBean emptyBean() {
		return new StudySubjectBean();
	}
	
}
