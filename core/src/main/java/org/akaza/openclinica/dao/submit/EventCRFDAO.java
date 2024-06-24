/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.submit;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.EventCRFSDVFilter;
import org.akaza.openclinica.dao.EventCRFSDVSort;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.apache.commons.lang.StringUtils;

/**
 * <P>
 * EventCRFDAO.java, data access object for an instance of an event being filled out on a subject. Was originally
 * individual_instrument table (in OC v.1).
 * 
 * @author thickerson
 * 
 *         TODO test create and update first thing
 */
public class EventCRFDAO extends AuditableEntityDAO<EventCRFBean> {
    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public EventCRFDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public EventCRFDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public EventCRFDAO(DataSource ds, DAODigester digester, Locale locale) {
        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_EVENTCRF;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.DATE);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);// annotations
        this.setTypeExpected(9, TypeNames.TIMESTAMP);// completed
        this.setTypeExpected(10, TypeNames.INT);// validator id
        this.setTypeExpected(11, TypeNames.DATE);// date validate
        this.setTypeExpected(12, TypeNames.TIMESTAMP);// date val. completed
        this.setTypeExpected(13, TypeNames.STRING);
        this.setTypeExpected(14, TypeNames.STRING);
        this.setTypeExpected(15, TypeNames.INT);// owner id
        this.setTypeExpected(16, TypeNames.TIMESTAMP);
        this.setTypeExpected(17, TypeNames.INT);// subject id
        this.setTypeExpected(18, TypeNames.TIMESTAMP);// date updated
        this.setTypeExpected(19, TypeNames.INT);// updater
        this.setTypeExpected(20, TypeNames.BOOL);// electronic_signature_status
        this.setTypeExpected(21, TypeNames.BOOL);// sdv_status
        this.setTypeExpected(22, TypeNames.INT);// old_status
        this.setTypeExpected(23, TypeNames.INT); // sdv_update_id
    }

    public EventCRFBean update(EventCRFBean ecb) {
        ecb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), new Integer(ecb.getStudyEventId()));
        variables.put(new Integer(2), new Integer(ecb.getCRFVersionId()));
        if (ecb.getDateInterviewed() == null) {
            nullVars.put(new Integer(3), new Integer(Types.DATE));
            variables.put(new Integer(3), null);
        } else {
            variables.put(new Integer(3), ecb.getDateInterviewed());
        }
        variables.put(new Integer(4), ecb.getInterviewerName());
        variables.put(new Integer(5), new Integer(ecb.getCompletionStatusId()));
        variables.put(new Integer(6), new Integer(ecb.getStatus().getId()));
        variables.put(new Integer(7), ecb.getAnnotations());
        if (ecb.getDateCompleted() == null) {
            nullVars.put(new Integer(8), new Integer(Types.TIMESTAMP));
            variables.put(new Integer(8), null);
        } else {
            variables.put(new Integer(8), new java.sql.Timestamp(ecb.getDateCompleted().getTime()));
        }
        // variables.put(new Integer(8),ecb.getDateCompleted());

        variables.put(new Integer(9), new Integer(ecb.getValidatorId()));

        if (ecb.getDateValidate() == null) {
            nullVars.put(new Integer(10), new Integer(Types.DATE));
            variables.put(new Integer(10), null);
        } else {
            variables.put(new Integer(10), ecb.getDateValidate());
        }
        // variables.put(new Integer(10),ecb.getDateValidate());

        if (ecb.getDateValidateCompleted() == null) {
            nullVars.put(new Integer(11), new Integer(Types.TIMESTAMP));
            variables.put(new Integer(11), null);
        } else {
            variables.put(new Integer(11), new Timestamp(ecb.getDateValidateCompleted().getTime()));
        }
        // variables.put(new Integer(11),ecb.getDateValidateCompleted());
        variables.put(new Integer(12), ecb.getValidatorAnnotations());
        variables.put(new Integer(13), ecb.getValidateString());
        variables.put(new Integer(14), new Integer(ecb.getStudySubjectId()));
        variables.put(new Integer(15), new Integer(ecb.getUpdaterId()));
        variables.put(new Integer(16), new Boolean(ecb.isElectronicSignatureStatus()));

        variables.put(new Integer(17), new Boolean(ecb.isSdvStatus()));
        if (ecb.getOldStatus() != null && ecb.getOldStatus().getId() > 0) {
            variables.put(new Integer(18), new Integer(ecb.getOldStatus().getId()));
        } else {
            variables.put(new Integer(18), new Integer(0));
        }
        // @pgawade 22-May-2011 added the sdv updater id variable
        variables.put(new Integer(19), ecb.getSdvUpdateId());
        // variables.put(new Integer(19), new Integer(ecb.getId()));
        variables.put(new Integer(20), new Integer(ecb.getId()));

        this.executeUpdate(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ecb.setActive(true);
        }

        return ecb;
    }

    public void markComplete(EventCRFBean ecb, boolean ide) {
        HashMap<Integer, Object> variables = variables(ecb.getId());

        if (ide) {
            executeUpdate(digester.getQuery("markCompleteIDE"), variables);
        } else {
            executeUpdate(digester.getQuery("markCompleteDDE"), variables);
        }
    }

    public EventCRFBean create(EventCRFBean ecb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        variables.put(new Integer(1), new Integer(ecb.getStudyEventId()));
        variables.put(new Integer(2), new Integer(ecb.getCRFVersionId()));

        Date interviewed = ecb.getDateInterviewed();
        if (interviewed != null) {
            variables.put(new Integer(3), ecb.getDateInterviewed());
        } else {
            variables.put(new Integer(3), null);
            nullVars.put(new Integer(3), new Integer(Types.DATE));
        }
        logger.debug("created: ecb.getInterviewerName()" + ecb.getInterviewerName());
        variables.put(new Integer(4), ecb.getInterviewerName());

        variables.put(new Integer(5), new Integer(ecb.getCompletionStatusId()));
        variables.put(new Integer(6), new Integer(ecb.getStatus().getId()));
        variables.put(new Integer(7), ecb.getAnnotations());
        variables.put(new Integer(8), new Integer(ecb.getOwnerId()));
        variables.put(new Integer(9), new Integer(ecb.getStudySubjectId()));
        variables.put(new Integer(10), ecb.getValidateString());
        variables.put(new Integer(11), ecb.getValidatorAnnotations());

        executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            ecb.setId(getLatestPK());
        }

        return ecb;
    }

    public EventCRFBean getEntityFromHashMap(HashMap<String, Object> hm) {
        EventCRFBean eb = new EventCRFBean();
        this.setEntityAuditInformation(eb, hm);

        eb.setId(((Integer) hm.get("event_crf_id")).intValue());
        eb.setStudyEventId(((Integer) hm.get("study_event_id")).intValue());
        eb.setCRFVersionId(((Integer) hm.get("crf_version_id")).intValue());
        eb.setDateInterviewed((Date) hm.get("date_interviewed"));
        eb.setInterviewerName((String) hm.get("interviewer_name"));
        eb.setCompletionStatusId(((Integer) hm.get("completion_status_id")).intValue());
        eb.setAnnotations((String) hm.get("annotations"));
        eb.setDateCompleted((Date) hm.get("date_completed"));
        eb.setValidatorId(((Integer) hm.get("validator_id")).intValue());
        eb.setDateValidate((Date) hm.get("date_validate"));
        eb.setDateValidateCompleted((Date) hm.get("date_validate_completed"));
        eb.setValidatorAnnotations((String) hm.get("validator_annotations"));
        eb.setValidateString((String) hm.get("validate_string"));
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setSdvStatus((Boolean) hm.get("sdv_status"));
        eb.setSdvUpdateId((Integer) hm.get("sdv_update_id"));
        Integer oldStatusId = (Integer) hm.get("old_status_id");
        eb.setOldStatus(Status.get(oldStatusId));

        // eb.setStatus(Status.get((Integer) hm.get("status_id"))
        return eb;
    }

    public ArrayList<EventCRFBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

	/**
	 * NOT IMPLEMENTED
	 */
    public ArrayList<EventCRFBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    public EventCRFBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

	/**
	 * NOT IMPLEMENTED
	 */
    public ArrayList<EventCRFBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

	/**
	 * NOT IMPLEMENTED
	 */
    public ArrayList<EventCRFBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<EventCRFBean> findAllByStudyEvent(StudyEventBean studyEvent) {
        HashMap<Integer, Object> variables = variables(studyEvent.getId());
        return executeFindAllQuery("findAllByStudyEvent", variables);
    }

    public ArrayList<EventCRFBean> findAllByStudyEventAndStatus(StudyEventBean studyEvent, Status status) {
        HashMap<Integer, Object> variables = variables(studyEvent.getId(), status.getId());
        return executeFindAllQuery("findAllByStudyEventAndStatus", variables);
    }

    public ArrayList<EventCRFBean> findAllByStudySubject(int studySubjectId) {
        HashMap<Integer, Object> variables = variables(studySubjectId);
        return executeFindAllQuery("findAllByStudySubject", variables);
    }

    public List<EventCRFBean> findAllCRFMigrationReportList(CRFVersionBean sourceCrfVersionBean , CRFVersionBean targetCrfVersionBean ,ArrayList<String> studyEventDefnlist ,ArrayList<String>  sitelist) {
        String eventStr =StringUtils.join(studyEventDefnlist, ",");
        String siteStr =StringUtils.join(sitelist, ",");
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(sourceCrfVersionBean.getId()));
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        return executeFindAllQuery("findAllCRFMigrationReportList", variables);
    }
    
    
    public ArrayList<EventCRFBean> findAllByStudyEventAndCrfOrCrfVersionOid(StudyEventBean studyEvent, String crfVersionOrCrfOID) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), crfVersionOrCrfOID);
        variables.put(new Integer(3), crfVersionOrCrfOID);

        return executeFindAllQuery("findAllByStudyEventAndCrfOrCrfVersionOid", variables);
    }

    public ArrayList<EventCRFBean> findAllByStudyEventInParticipantForm(StudyEventBean studyEvent,int sed_Id,int studyId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(sed_Id));
        variables.put(new Integer(3), new Integer(studyId));

        return executeFindAllQuery("findAllByStudyEventInParticipantForm", variables);
    }

    
    public ArrayList<EventCRFBean> findAllByCRF(int crfId) {
        HashMap<Integer, Object> variables = variables(crfId);
        return executeFindAllQuery("findAllByCRF", variables);
    }

    public ArrayList<EventCRFBean> findAllByCRFVersion(int versionId) {
        HashMap<Integer, Object> variables = variables(versionId);
        return executeFindAllQuery("findAllByCRFVersion", variables);
    }

    public ArrayList<EventCRFBean> findAllStudySubjectByCRFVersion(int versionId) {
        this.setTypesExpected();

        // ss.label, sed.name as sed_name, s.name as study_name
        this.setTypeExpected(24, TypeNames.STRING);
        this.setTypeExpected(25, TypeNames.STRING);
        this.setTypeExpected(26, TypeNames.STRING);
        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, versionId);

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudySubjectByCRFVersion"), variables);
        ArrayList<EventCRFBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            EventCRFBean eb = this.getEntityFromHashMap(hm);
            eb.setStudySubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setStudyName((String) hm.get("study_name"));
            al.add(eb);
        }
        
        return al;
    }

    public ArrayList<EventCRFBean> findUndeletedWithStudySubjectsByCRFVersion(int versionId) {
        this.setTypesExpected();
        // ss.label, sed.name as sed_name, s.name as study_name, ss.sample_ordinal as repeat_number
        // this.setTypeExpected(23, TypeNames.STRING);
        this.setTypeExpected(24, TypeNames.STRING);
        this.setTypeExpected(25, TypeNames.STRING);
        this.setTypeExpected(26, TypeNames.STRING);
        this.setTypeExpected(27, TypeNames.INT);
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(versionId));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findUndeletedWithStudySubjectsByCRFVersion"), variables);
        ArrayList<EventCRFBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            EventCRFBean eb = (EventCRFBean) this.getEntityFromHashMap(hm);
            eb.setStudySubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setStudyName((String) hm.get("study_name"));
            eb.setEventOrdinal((Integer) hm.get("repeat_number"));
            al.add(eb);
        }
        return al;
    }

    public ArrayList<EventCRFBean> findByEventSubjectVersion(StudyEventBean studyEvent, StudySubjectBean studySubject, CRFVersionBean crfVersion) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(crfVersion.getId()));
        variables.put(new Integer(3), new Integer(studySubject.getId()));

        return executeFindAllQuery("findByEventSubjectVersion", variables);
    }

    // TODO: to get rid of warning refactor executeFindAllQuery method in
    // superclass
    public EventCRFBean findByEventCrfVersion(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        EventCRFBean eventCrfBean = null;
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(crfVersion.getId()));

        ArrayList<EventCRFBean> eventCrfs = executeFindAllQuery("findByEventCrfVersion", variables);
        if (!eventCrfs.isEmpty() && eventCrfs.size() == 1) {
            eventCrfBean = eventCrfs.get(0);
        }
        return eventCrfBean;

    }

    public ArrayList<EventCRFBean> findByCrfVersion(CRFVersionBean crfVersion) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(crfVersion.getId()));

        ArrayList<EventCRFBean> eventCrfs = executeFindAllQuery("findByCrfVersion", variables);
        return eventCrfs;

    }

    public void delete(int eventCRFId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(eventCRFId));

        this.executeUpdate(digester.getQuery("delete"), variables);
        return;

    }

    public void setSDVStatus(boolean sdvStatus, int userId, int eventCRFId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), sdvStatus);
        variables.put(new Integer(2), userId);
        variables.put(new Integer(3), eventCRFId);

        this.executeUpdate(digester.getQuery("setSDVStatus"), variables);
    }

    public Integer countEventCRFsByStudy(int studyId, int parentStudyId) {
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId);
        String query = digester.getQuery("countEventCRFsByStudy");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByStudyIdentifier(String identifier) {
        HashMap<Integer, Object> variables = variables(identifier);
        String query = digester.getQuery("countEventCRFsByStudyIdentifier");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        HashMap<Integer, Object> variables = variables(studySubjectId, studyId, parentStudyId);
        String query = digester.getQuery("countEventCRFsByStudySubject");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByStudyIdentifier(int studyId, int parentStudyId, String studyIdentifier) {
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId, studyIdentifier);
        String query = digester.getQuery("countEventCRFsByStudyIdentifier");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByByStudySubjectCompleteOrLockedAndNotSDVd(int studySubjectId) {
        HashMap<Integer, Object> variables = variables(studySubjectId);
        String query = digester.getQuery("countEventCRFsByByStudySubjectCompleteOrLockedAndNotSDVd");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudySubjectCompleteOrLocked(int studySubjectId) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studySubjectId);

        return executeFindAllQuery("getEventCRFsByStudySubjectCompleteOrLocked", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudySubjectLimit(int studySubjectId, int studyId, int parentStudyId, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudySubjectLimit", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        return executeFindAllQuery("getEventCRFsByStudySubject", variables);
    }

    public ArrayList<EventCRFBean> getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        return executeFindAllQuery("getGroupByStudySubject", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudyIdentifier(int studyId, int parentStudyId, String studyIdentifier, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        variables.put(3, studyIdentifier);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudyIdentifier", variables);
    }

    public Integer getCountWithFilter(int studyId, int parentStudyId, EventCRFSDVFilter filter) {
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId);
        String query = digester.getQuery("getCountWithFilter");
        query += filter.execute("");
        return getCountByQuery(query, variables);
    }

    public ArrayList<EventCRFBean> getWithFilterAndSort(int studyId, int parentStudyId, EventCRFSDVFilter filter, EventCRFSDVSort sort, int rowStart, int rowEnd) {
        ArrayList<EventCRFBean> eventCRFs = new ArrayList<>();
        setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");
        // sql = sql + sort.execute("");
        sql = sql + " order By  ec.date_created ASC "; // major hack
        sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;

        ArrayList<HashMap<String, Object>> rows = this.select(sql, variables);
        for(HashMap<String, Object> hm : rows) {
            EventCRFBean eventCRF = this.getEntityFromHashMap(hm);
            eventCRFs.add(eventCRF);
        }
        
        return eventCRFs;
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudy(int studyId, int parentStudyId, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByStudy", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudySubjectLabelLimit(String label, int studyId, int parentStudyId, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, '%' + label + '%');
        variables.put(2, studyId);
        variables.put(3, parentStudyId);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudySubjectLabelLimit", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByEventNameLimit(String eventName, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, eventName);
        variables.put(2, limit);
        variables.put(3, offset);

        return executeFindAllQuery("getEventCRFsByEventNameLimit", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByEventDateLimit(int studyId, String eventDate, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, eventDate);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByEventDateLimit", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByStudySDV(int studyId, boolean sdvStatus, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, sdvStatus);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByStudySDV", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsByCRFStatus(int studyId, int subjectEventStatusId, int limit, int offset) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, subjectEventStatusId);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByCRFStatus", variables);
    }

    public ArrayList<EventCRFBean> getEventCRFsBySDVRequirement(int studyId, int parentStudyId, int limit, int offset, Integer... sdvCode) {

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        this.setTypesExpected();

        String sql = digester.getQuery("getEventCRFsBySDVRequirement");
        sql += " AND ( ";
        for (int i = 0; i < sdvCode.length; i++) {
            sql += i != 0 ? " OR " : "";
            sql += " source_data_verification_code = " + sdvCode[i];
        }
        sql += " ) ))  limit " + limit + " offset " + offset;

        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        ArrayList<EventCRFBean> al = new ArrayList<>();
        
        for(HashMap<String, Object> hm : alist) {
            EventCRFBean eb = this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public Integer countEventCRFsByStudySubjectLabel(String label, int studyId, int parentStudyId) {
        HashMap<Integer, Object> variables = variables(label, studyId, parentStudyId);
        String query = digester.getQuery("countEventCRFsByStudySubjectLabel");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByStudySDV(int studyId, boolean sdvStatus) {
        HashMap<Integer, Object> variables = variables(studyId, sdvStatus);
        String query = digester.getQuery("countEventCRFsByStudySDV");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByCRFStatus(int studyId, int statusId) {
        HashMap<Integer, Object> variables = variables(studyId, statusId);
        String query = digester.getQuery("countEventCRFsByCRFStatus");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByEventName(String eventName) {
        HashMap<Integer, Object> variables = variables(eventName);
        String query = digester.getQuery("countEventCRFsByEventName");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsBySDVRequirement(int studyId, int parentStudyId, Integer... sdvCode) {
        HashMap<Integer, Object> variables = variables(studyId, parentStudyId);
        String query = digester.getQuery("countEventCRFsBySDVRequirement");
        query += " AND ( ";
        for (int i = 0; i < sdvCode.length; i++) {
            query += i != 0 ? " OR " : "";
            query += " source_data_verification_code = " + sdvCode[i];
        }
        query += "))) ";
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByEventNameSubjectLabel(String eventName, String subjectLabel) {
        HashMap<Integer, Object> variables = variables(eventName, subjectLabel);
        String query = digester.getQuery("countEventCRFsByEventNameSubjectLabel");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Integer countEventCRFsByEventDate(int studyId, String eventDate) {
        HashMap<Integer, Object> variables = variables(studyId, eventDate);
        String query = digester.getQuery("countEventCRFsByEventDate");
        Integer result = getCountByQuery(query, variables);
        if(result == null) {
        	result = 0;
        }
        return result;
    }

    public Map<Integer, SortedSet<EventCRFBean>> buildEventCrfListByStudyEvent(Integer studySubjectId) {
        this.setTypesExpected(); // <== Must be called first

        Map<Integer, SortedSet<EventCRFBean>> result = new HashMap<>();

        HashMap<Integer, Object> param = new HashMap<>();
        int i = 1;
        param.put(i++, studySubjectId);

        ArrayList<HashMap<String, Object>> selectResult = select(digester.getQuery("buildEventCrfListByStudyEvent"), param);

        for(HashMap<String, Object> hm : selectResult) {
            EventCRFBean bean = (EventCRFBean) this.getEntityFromHashMap(hm);

            Integer studyEventId = bean.getStudyEventId();
            if (!result.containsKey(studyEventId)) {
                result.put(studyEventId, new TreeSet<EventCRFBean>(new Comparator<EventCRFBean>() {
                    public int compare(EventCRFBean o1, EventCRFBean o2) {
                        Integer id1 = o1.getId();
                        Integer id2 = o2.getId();
                        return id1.compareTo(id2);
                    }
                }));
            }
            result.get(studyEventId).add(bean);
        }

        return result;
    }

    public Set<Integer> buildNonEmptyEventCrfIds(Integer studySubjectId) {
        Set<Integer> result = new HashSet<Integer>();

        HashMap<Integer, Object> param = new HashMap<>();
        int i = 1;
        param.put(i++, studySubjectId);

        ArrayList<HashMap<String, Object>> selectResult = select(digester.getQuery("buildNonEmptyEventCrfIds"), param);

        for(HashMap<String, Object> hm : selectResult) {
            result.add((Integer) hm.get("event_crf_id"));
        }

        return result;
    }

    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id) {
        Connection con = null;
        updateCRFVersionID(event_crf_id, crf_version_id, user_id, con);
    }

    /* this function allows to run transactional updates for an action */

    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id, Connection con) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.BOOL);
        this.setTypeExpected(3, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(1, crf_version_id);
        variables.put(2, user_id);
        variables.put(3, user_id);
        variables.put(4, false);
        variables.put(5, event_crf_id);
        String sql = digester.getQuery("updateCRFVersionID");
        // this is the way to make the change transactional
        if (con == null) {
            this.executeUpdate(sql, variables);
        } else {
            this.executeUpdate(sql, variables, con);
        }
    }

	@Override
	public EventCRFBean emptyBean() {
		return new EventCRFBean();
	}

}
