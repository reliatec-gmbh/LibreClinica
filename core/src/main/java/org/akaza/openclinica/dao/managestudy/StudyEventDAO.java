/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.patterns.ocobserver.Listener;
import org.akaza.openclinica.patterns.ocobserver.Observer;
import org.akaza.openclinica.patterns.ocobserver.StudyEventBeanContainer;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.StudyEventBeanListener;

/**
 * @author jxu
 *
 *         Modified by ywang.
 *
 */
public class StudyEventDAO extends AuditableEntityDAO<StudyEventBean> implements Listener {
    
	
	private Observer observer;
	// private DAODigester digester;

	    private void setQueryNames() {
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public StudyEventDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyEventDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyEventDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYEVENT;
    }

    @Override
    public void setTypesExpected() {
        // SERIAL NUMERIC NUMERIC VARCHAR(2000)
        // NUMERIC DATE DATE NUMERIC
        // NUMERIC DATE DATE NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_start
        this.setTypeExpected(7, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_end
        this.setTypeExpected(8, TypeNames.INT);

        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.TIMESTAMP);
        this.setTypeExpected(11, TypeNames.TIMESTAMP);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        // YW 08-17-2007 <<
        this.setTypeExpected(14, TypeNames.BOOL); // start_time_flag
        this.setTypeExpected(15, TypeNames.BOOL); // end_time_flag
        // YW >>

    }

    public void setTypesExpected(boolean withSubject) {
        // SERIAL NUMERIC NUMERIC VARCHAR(2000)
        // NUMERIC DATE DATE NUMERIC
        // NUMERIC DATE DATE NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_start
        this.setTypeExpected(7, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_end
        this.setTypeExpected(8, TypeNames.INT);

        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.TIMESTAMP);
        this.setTypeExpected(11, TypeNames.TIMESTAMP);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        // YW 08-17-2007 <<
        this.setTypeExpected(14, TypeNames.BOOL); // start_time_flag
        this.setTypeExpected(15, TypeNames.BOOL); // end_time_flag
        if (withSubject) {
            this.setTypeExpected(16, TypeNames.STRING);
        }
        // YW >>
    }

    public void setCRFTypesExpected() {
        /*
         * <sql>SELECT C.CRF_ID, C.STATUS_ID, C.NAME, C.DESCRIPTION,
         * V.CRF_VERSION_ID, V.NAME, V.REVISION_NOTES FROM CRF C, CRF_VERSION V,
         * EVENT_DEFINITION_CRF EDC WHERE C.CRF_ID = V.CRF_ID AND EDC.CRF_ID =
         * C.CRF_ID AND EDC.STUDY_EVENT_DEFINITION_ID =? </sql>
         */
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public StudyEventBean getEntityFromHashMap(HashMap<String, Object> hm) {
        return getEntityFromHashMap(hm, false);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public StudyEventBean getEntityFromHashMap(HashMap<String, Object> hm, boolean withSubject) {
        StudyEventBean eb = new StudyEventBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_EVENT_ID STUDY_EVENT_DEFINITION_ID SUBJECT_ID LOCATION
        // SAMPLE_ORDINAL DATE_START DATE_END OWNER_ID
        // STATUS_ID DATE_CREATED DATE_UPDATED UPDATE_ID
        eb.setId(((Integer) hm.get("study_event_id")).intValue());
        eb.setStudyEventDefinitionId(((Integer) hm.get("study_event_definition_id")).intValue());
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setLocation((String) hm.get("location"));
        eb.setSampleOrdinal(((Integer) hm.get("sample_ordinal")).intValue());
        eb.setDateStarted((Date) hm.get("date_start"));
        eb.setDateEnded((Date) hm.get("date_end"));
        // eb.setStatus(eb.getStatus());
        int subjectEventStatuId = ((Integer) hm.get("subject_event_status_id")).intValue();
        eb.setSubjectEventStatus(SubjectEventStatus.get(subjectEventStatuId));
        // YW 08-17-2007
        eb.setStartTimeFlag((Boolean) hm.get("start_time_flag"));
        eb.setEndTimeFlag((Boolean) hm.get("end_time_flag"));
        if (withSubject) {
            eb.setStudySubjectLabel((String) hm.get("label"));
        }

        return eb;
    }

    // public HashMap getListOfStudyEvents()

    public ArrayList<StudyEventBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    public ArrayList<StudyEventBean> findAllByDefinition(int definitionId) {
    	String queryName = "findAllByDefinition";
        HashMap<Integer, Object> variables = variables(definitionId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventBean> findAllByStudyEventDefinitionAndCrfOids(String studyEventDefinitionOid, String crfOrCrfVersionOid) {        
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), studyEventDefinitionOid);
        variables.put(Integer.valueOf(2), crfOrCrfVersionOid);
        variables.put(Integer.valueOf(3), crfOrCrfVersionOid);

        String sql = digester.getQuery("findAllByStudyEventDefinitionAndCrfOids");

        ArrayList<StudyEventBean> answer = new ArrayList<>();
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        answer.addAll(alist.stream().map(m -> this.getEntityFromHashMap(m, true)).collect(Collectors.toList()));
        return answer;
    }

    public Integer getCountofEventsBasedOnEventStatus(StudyBean currentStudy, SubjectEventStatus subjectEventStatus) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId(), subjectEventStatus.getId());
        String query = digester.getQuery("getCountofEventsBasedOnEventStatus");
        return getCountByQuery(query, variables);
    }

    public Integer getCountofEvents(StudyBean currentStudy) {
        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String query = digester.getQuery("getCountofEvents");
        return getCountByQuery(query, variables);
    }

    public StudyEventBean findAllByStudyEventDefinitionAndCrfOidsAndOrdinal(String studyEventDefinitionOid, String crfOrCrfVersionOid, String ordinal,
            String studySubjectId) {
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), studyEventDefinitionOid);
        variables.put(Integer.valueOf(2), Integer.valueOf(studySubjectId));
        variables.put(Integer.valueOf(3), Integer.valueOf(ordinal));
        variables.put(Integer.valueOf(4), crfOrCrfVersionOid);
        variables.put(Integer.valueOf(5), crfOrCrfVersionOid);

        String sql = digester.getQuery("findAllByStudyEventDefinitionAndCrfOidsAndOrdinal");
        
        ArrayList<StudyEventBean> answer = new ArrayList<>();
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        answer.addAll(alist.stream().map(m -> this.getEntityFromHashMap(m, true)).collect(Collectors.toList()));
        
        if (answer.isEmpty()) {
            return null;
        } else if (answer.size() == 1) {
            return answer.get(0);
        } else {
            logger.warn("The query in findAllByStudyEventDefinitionAndCrfOidsAndOrdinal return a list of size {}. Business logic assumes only one", answer.size());
            return answer.get(0);
        }
    }

    public ArrayList<StudyEventBean> findAllWithSubjectLabelByDefinition(int definitionId) {
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = variables(definitionId);

        String sql = digester.getQuery("findAllWithSubjectLabelByDefinition");

        ArrayList<StudyEventBean> answer = new ArrayList<>();
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        answer.addAll(alist.stream().map(m -> this.getEntityFromHashMap(m, true)).collect(Collectors.toList()));
        return answer;
    }

    // YW <<
    public ArrayList<StudyEventBean> findAllWithSubjectLabelByStudySubjectAndDefinition(StudySubjectBean studySubject, int definitionId) {
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(studySubject.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(definitionId));

        String sql = digester.getQuery("findAllWithSubjectLabelByStudySubjectAndDefinition");

        ArrayList<StudyEventBean> answer = new ArrayList<>();
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        answer.addAll(alist.stream().map(m -> this.getEntityFromHashMap(m, true)).collect(Collectors.toList()));
        // set the study subject for all received beans
        answer.stream().forEach(bean -> bean.setStudySubject(studySubject));
        return answer;
    }

    public EntityBean findByStudySubjectIdAndDefinitionIdAndOrdinal(int ssbid, int sedid, int ord) {
    	String queryName = "findByStudySubjectIdAndDefinitionIdAndOrdinal";
        HashMap<Integer, Object> variables = variables(ssbid, sedid, ord);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<StudyEventBean> findAllByDefinitionAndSubject(StudyEventDefinitionBean definition, StudySubjectBean subject) {
    	String queryName = "findAllByDefinitionAndSubject";
        HashMap<Integer, Object> variables = variables(definition.getId(), subject.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventBean> findAllByDefinitionAndSubjectOrderByOrdinal(StudyEventDefinitionBean definition, StudySubjectBean subject) {
    	String queryName = "findAllByDefinitionAndSubjectOrderByOrdinal";
        HashMap<Integer, Object> variables = variables(definition.getId(), subject.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public StudyEventBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }
    
    public StudyEventBean findByPKCached(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables, true);
    }

    /**
     * Creates a new studysubject
     */
    @Override
    public StudyEventBean create(StudyEventBean eb) {
        return create(eb,false);
    }
    /**
     * Creates a new studysubject
     */
    public StudyEventBean create(StudyEventBean sb, boolean isTransaction) {
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        // INSERT INTO STUDY_EVENT
        // (STUDY_EVENT_DEFINITION_ID,SUBJECT_ID,LOCATION,SAMPLE_ORDINAL,
        // DATE_START,DATE_END,OWNER_ID,STATUS_ID,DATE_CREATED,subject_event_status_id
        // start_time_flag, end_time_flag)
        // VALUES (?,?,?,?,?,?,?,?,NOW())
        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getStudyEventDefinitionId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getStudySubjectId()));
        variables.put(Integer.valueOf(3), sb.getLocation());
        variables.put(Integer.valueOf(4), Integer.valueOf(sb.getSampleOrdinal()));
        nullVars.put(3, TypeNames.STRING);
        if (sb.getDateStarted() == null) {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            nullVars.put(Integer.valueOf(5), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(5), null);
        } else {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            variables.put(Integer.valueOf(5), new Timestamp(sb.getDateStarted().getTime()));
        }
        if (sb.getDateEnded() == null) {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            nullVars.put(Integer.valueOf(6), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(6), null);
        } else {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            variables.put(Integer.valueOf(6), new Timestamp(sb.getDateEnded().getTime()));
        }
        variables.put(Integer.valueOf(7), Integer.valueOf(sb.getOwner().getId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getStatus().getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(sb.getSubjectEventStatus().getId()));
        variables.put(Integer.valueOf(10), sb.getStartTimeFlag());
        variables.put(Integer.valueOf(11), sb.getEndTimeFlag());

        this.executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }
        
        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,true);
        changeDetails.setRunningInTransaction(isTransaction);
        StudyEventBeanContainer container = new StudyEventBeanContainer(sb,changeDetails);
        notifyObservers(container);
        return sb;
    }

    /**
     * Updates a Study event
     */
    @Override
    public StudyEventBean update(StudyEventBean eb) {
        return update(eb,false);
    }
    
    /**
     * Updates a Study event
     */
    public StudyEventBean update(StudyEventBean eb, boolean isTransaction) {
    	 Connection con = null;
    	 return update( eb, con, isTransaction);
    }
    
    public StudyEventBean update(StudyEventBean eb, Connection con) {
        return update(eb,con,false);
    }
    /* this function allows to run transactional updates for an action*/
    
    public StudyEventBean update(StudyEventBean sb, Connection con, boolean isTransaction) {
        StudyEventBean oldStudyEventBean = (StudyEventBean)findByPK(sb.getId());
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        // UPDATE study_event SET
        // STUDY_EVENT_DEFINITION_ID=?,SUBJECT_ID=?,LOCATION=?,
        // SAMPLE_ORDINAL=?, DATE_START=?,DATE_END=?,STATUS_ID=?,DATE_UPDATED=?,
        // UPDATE_ID=?, subject_event_status_id=?, end_time_flag=? WHERE
        // STUDY_EVENT_ID=?

        sb.setActive(false);

        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getStudyEventDefinitionId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getStudySubjectId()));
        variables.put(Integer.valueOf(3), sb.getLocation());
        variables.put(Integer.valueOf(4), Integer.valueOf(sb.getSampleOrdinal()));
        // YW 08-17-2007, data type changed from DATE to TIMESTAMP
        variables.put(Integer.valueOf(5), new Timestamp(sb.getDateStarted().getTime()));
        if (sb.getDateEnded() == null) {
            nullVars.put(Integer.valueOf(6), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(6), null);
        } else {
            variables.put(Integer.valueOf(6), new Timestamp(sb.getDateEnded().getTime()));
        }
        variables.put(Integer.valueOf(7), Integer.valueOf(sb.getStatus().getId()));
        // changing date_updated from java.util.Date() into postgres now() statement
       // variables.put(Integer.valueOf(8), new java.util.Date());// DATE_Updated
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getUpdater().getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(sb.getSubjectEventStatus().getId()));
        variables.put(Integer.valueOf(10), sb.getStartTimeFlag()); // YW
        // 08-17-2007,
        // start_time_flag
        variables.put(Integer.valueOf(11), sb.getEndTimeFlag()); // YW
        // 08-17-2007,
        // end_time_flag
        variables.put(Integer.valueOf(12), Integer.valueOf(sb.getId()));

        String sql = digester.getQuery("update");
        if ( con == null){
        	this.executeUpdate(sql, variables, nullVars);
        }else{
        	this.executeUpdate(sql, variables, nullVars, con);
        }
        
        if (isQuerySuccessful()) {
            sb.setActive(true);
        }

        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails();
        if (oldStudyEventBean.getDateStarted().compareTo(sb.getDateStarted()) != 0)
        	changeDetails.setStartDateChanged(true);
        if (oldStudyEventBean.getSubjectEventStatus().getId() != sb.getSubjectEventStatus().getId()) 
        	changeDetails.setStatusChanged(true);
        changeDetails.setRunningInTransaction(isTransaction);
        StudyEventBeanContainer container = new StudyEventBeanContainer(sb,changeDetails);
       notifyObservers(container);
        
        return sb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyEventBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<StudyEventBean> findAllByStudyAndStudySubjectId(StudyBean study, int studySubjectId) {
    	String queryName = "findAllByStudyAndStudySubjectId";
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId(), studySubjectId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventBean> findAllByStudyAndEventDefinitionId(StudyBean study, int eventDefinitionId) {
    	String queryName = "findAllByStudyAndEventDefinitionId";
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId(), eventDefinitionId);
        return executeFindAllQuery(queryName, variables);
    }

    /**
     * Get the maximum sample ordinal over all study events for the provided
     * StudyEventDefinition / StudySubject combination. Note that the maximum
     * may be zero but must be non-negative.
     *
     * @param sedb
     *            The study event definition whose ordinal we're looking for.
     * @param studySubject
     *            The study subject whose ordinal we're looking for.
     * @return The maximum sample ordinal over all study events for the provided
     *         combination, or 0 if no such combination exists.
     */
    public int getMaxSampleOrdinal(StudyEventDefinitionBean sedb, StudySubjectBean studySubject) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(sedb.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(studySubject.getId()));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getMaxSampleOrdinal"), variables);
        for(HashMap<String, Object> hm : alist) {
            try {
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    @Override
    public ArrayList<StudyEventBean> findAllByStudy(StudyBean study) {
    	String queryName = "findAllByStudy";
        HashMap<Integer, Object> variables = variables(study.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyEventBean> findAllBySubjectId(int subjectId) {
    	String queryName = "findAllBySubjectId";
        HashMap<Integer, Object> variables = variables(subjectId);
        return executeFindAllQuery(queryName, variables);

    }

    public ArrayList<StudyEventBean> findAllBySubjectIdOrdered(int subjectId) {
    	String queryName = "findAllBySubjectIdOrdered";
        HashMap<Integer, Object> variables = variables(subjectId);
        return executeFindAllQuery(queryName, variables);
    }

    public void setNewCRFTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);
    }

    /**
     * Using the HashMaps returned from a <code>select</code> call in
     * findCRFsByStudy, prepare a HashMap whose keys are study event definitions
     * and whose values are ArrayLists of CRF versions included in those
     * definitions.
     *
     * @param rows
     *            The HashMaps retured by the <code>select</code> call in
     *            findCRFsByStudy.
     * @return a HashMap whose keys are study event definitions and whose values
     *         are ArrayLists of CRF versions included in those definitions.
     *         Both the keys of the HashMap and the elements of the ArrayLists
     *         are actually EntitBeans.
     */
    public HashMap<EntityBean, ArrayList<EntityBean>> getEventsAndMultipleCRFVersionInformation(ArrayList<HashMap<String, Object>> rows) {
        HashMap<EntityBean, ArrayList<EntityBean>> returnMe = new HashMap<>();
        EntityBean event = new EntityBean();
        EntityBean crf = new EntityBean();
        for(HashMap<String, Object> answers : rows) {
            // removed setActive since the setId calls automatically result in
            // setActive calls
            event = new EntityBean();
            event.setName((String) answers.get("sed_name"));
            event.setId(((Integer) answers.get("study_event_definition_id")).intValue());

            crf = new EntityBean();
            crf.setName((String) answers.get("crf_name") + " " + (String) answers.get("ver_name"));
            crf.setId(((Integer) answers.get("crf_version_id")).intValue());

            ArrayList<EntityBean> crfs = new ArrayList<>();
            if (this.findDouble(returnMe, event)) {// (returnMe.containsKey(event))
                // {
                // TODO create custom checker, this does not work
                // logger.warn("putting a crf into an OLD event: " +
                // crf.getName() + " into "
                // + event.getName());
                // logger.warn("just entered the if statement");
                crfs = this.returnDouble(returnMe, event);// (ArrayList)
                // returnMe.get(event);
                // logger.warn("just got the array list from the hashmap");
                crfs.add(crf);
                // logger.warn("just added the crf to the array list");
                returnMe = this.removeDouble(returnMe, event);
                // .remove(event);
                // not sure the above will work, tbh
                returnMe.put(event, crfs);
            } else {
                crfs = new ArrayList<>();
                logger.warn("put a crf into a NEW event: " + crf.getName() + " into " + event.getName());
                crfs.add(crf);
                returnMe.put(event, crfs);// maybe combine the two crf +
                // version?
            }
        }// end of cycling through answers

        return returnMe;
    }
    
    // TODO: decide whether to use SQL below in place of other sql. they're
    // pretty
    // similar
    /*
     * ssachs - this is meant for use with
     * getEventsAndMultipleCRFVersionInformation; in particular the column names
     * "study_event_name", "crf_name" and "crf_version_name" should be
     * maintained if the SQL changes SELECT SED.name AS study_event_name ,
     * SED.study_event_definition_id , C.name AS crf_name , CV.name AS
     * crf_version_name , CV.crf_version_id FROM study_event_definition SED ,
     * event_definition_crf EDC , crf C , crf_version CV WHERE SED.study_id = ?
     * AND SED.study_event_definition_id = EDC.study_event_definition_id AND
     * C.crf_id = EDC.crf_id AND C.crf_id = CV.crf_id
     */

    public HashMap<EntityBean, ArrayList<EntityBean>> findCRFsByStudy(StudyBean sb) {
        // SELECT DISTINCT
        // C.CRF_ID
        // , C.NAME AS CRF_NAME
        // , C.DESCRIPTION
        // , V.CRF_VERSION_ID
        // , V.NAME AS VER_NAME
        // , V.REVISION_NOTES
        // , SED.STUDY_EVENT_DEFINITION_ID
        // , SED.NAME AS SED_NAME
        // FROM
        // CRF C
        // , CRF_VERSION V
        // , EVENT_DEFINITION_CRF EDC
        // , STUDY_EVENT_DEFINITION SED
        // WHERE
        // C.CRF_ID = V.CRF_ID
        // AND EDC.CRF_ID = C.CRF_ID
        // AND EDC.STUDY_EVENT_DEFINITION_ID = SED.STUDY_EVENT_DEFINITION_ID
        // AND SED.STATUS_ID = 1
        // AND SED.STUDY_ID = ?
        // ORDER BY C.CRF_ID, V.CRF_VERSION_ID

        this.setNewCRFTypesExpected();
        HashMap<Integer, Object> variables = variables(sb.getId());
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findCRFsByStudy"), variables);
        // TODO make sure this other statement for eliciting crfs works, tbh
        // switched from getEventAndCRFVersionInformation
        // to getEventsAndMultipleCRFVersionInformation
        // crfs = this.getEventAndCRFVersionInformation(alist);
        HashMap<EntityBean, ArrayList<EntityBean>> crfs = this.getEventsAndMultipleCRFVersionInformation(alist);
        return crfs;
    }

    // TODO make sure we are returning the correct boolean, tbh
    public boolean findDouble(HashMap<EntityBean, ArrayList<EntityBean>> hm, EntityBean event) {
        boolean returnMe = false;
        for(EntityBean eb : hm.keySet()) {
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                logger.warn("found OLD bean, return true");
                returnMe = true;
            }
        }
        return returnMe;
    }

    // so as not to get null pointer returns, tbh
    public ArrayList<EntityBean> returnDouble(HashMap<EntityBean, ArrayList<EntityBean>> hm, EntityBean event) {
        ArrayList<EntityBean> al = new ArrayList<>();
        for(Map.Entry<EntityBean, ArrayList<EntityBean>> me : hm.entrySet()) {
            EntityBean eb = me.getKey();
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                // logger.warn("found OLD bean, return true");
                al = me.getValue();
            }
        }
        return al;
    }

    // so as to remove the object correctly, tbh
    public HashMap<EntityBean, ArrayList<EntityBean>> removeDouble(HashMap<EntityBean, ArrayList<EntityBean>> hm, EntityBean event) {
        EntityBean removeMe = new EntityBean();
        for(Map.Entry<EntityBean, ArrayList<EntityBean>> me : hm.entrySet()) {
            EntityBean eb = me.getKey();
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                logger.warn("found OLD bean, remove it");
                removeMe = eb;
            }
        }
        hm.remove(removeMe);
        return hm;
    }

    public int getDefinitionIdFromStudyEventId(int studyEventId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(studyEventId);
        ArrayList<HashMap<String, Object>> rows = select(digester.getQuery("getDefinitionIdFromStudyEventId"), variables);

        int answer = 0;
        if (rows.size() > 0) {
            HashMap<String, Object> row = rows.get(0);
            answer = ((Integer) row.get("study_event_definition_id")).intValue();
        }

        return answer;
    }

    public EntityBean getNextScheduledEvent(String studySubjectOID) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = variables(studySubjectOID, studySubjectOID);

        String sql = digester.getQuery("getNextScheduledEvent");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        
        StudyEventBean eb = new StudyEventBean();
        if (alist != null && alist.size() > 0) {
            eb = (StudyEventBean) this.getEntityFromHashMap(alist.get(0));
        }

        return eb;
    }

    public ArrayList<StudyEventBean> findAllByStudySubject(StudySubjectBean ssb) {
        HashMap<Integer, Object> variables = variables(ssb.getId());

        return executeFindAllQuery("findAllByStudySubject", variables);
    }

    public ArrayList<StudyEventBean> findAllByStudySubjectAndDefinition(StudySubjectBean ssb, StudyEventDefinitionBean sed) {
        HashMap<Integer, Object> variables = variables(ssb.getId(), sed.getId());

        return executeFindAllQuery("findAllByStudySubjectAndDefinition", variables);
    }

    public Integer countNotRemovedEvents(Integer studyEventDefinitionId) {
        HashMap<Integer, Object> variables = variables(studyEventDefinitionId);
        String query = digester.getQuery("countNotRemovedEvents");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }
    
    public HashMap<String, Object> getStudySubjectCRFData(StudyBean sb, int studySubjectId, int eventDefId, String crfVersionOID, int eventOrdinal) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
       
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, Integer.valueOf(sb.getId()));
        variables.put(2, Integer.valueOf(eventOrdinal));
        variables.put(3, crfVersionOID);
        variables.put(4, Integer.valueOf(studySubjectId));
        variables.put(5, Integer.valueOf(eventDefId));
        
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("getStudySubjectCRFDataDetails"), variables);
        // TODO make sure this other statement for eliciting crfs works, tbh
        // switched from getEventAndCRFVersionInformation
        // to getEventsAndMultipleCRFVersionInformation
        // crfs = this.getEventAndCRFVersionInformation(alist);
        HashMap<String, Object> studySubjectCRFDataDetails = this.getStudySubjectCRFDataDetails(alist);
        return studySubjectCRFDataDetails;
    }

    private HashMap<String, Object> getStudySubjectCRFDataDetails(ArrayList<HashMap<String, Object>> rows) {
        HashMap<String, Object> returnMe = new HashMap<>();
        for(HashMap<String, Object> answers : rows) {
            returnMe.put("event_crf_id", answers.get("event_crf_id"));
            returnMe.put("event_definition_crf_id", answers.get("event_definition_crf_id"));
            returnMe.put("study_event_id", answers.get("study_event_id"));

        }// end of cycling through answers

        return returnMe;
    }

    private void notifyObservers(StudyEventBeanContainer sbc){
    if(getObserver()!=null)
    	getObserver().update(sbc);
    }
    
    @Override
	public Observer getObserver() {
    	if(this.observer == null) {
        	// TODO check why always a new observer is created
    		// this if-statement is only here to suppress the 'field not used' warning
    	}

		return new StudyEventBeanListener(this);
	}

	@Override
    public void setObserver(Observer observer) {
		this.observer = observer;
	}

	@Override
	public StudyEventBean emptyBean() {
		return new StudyEventBean();
	}

}