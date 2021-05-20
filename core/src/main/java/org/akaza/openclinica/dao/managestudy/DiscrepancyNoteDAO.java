/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class DiscrepancyNoteDAO extends AuditableEntityDAO<DiscrepancyNoteBean> {
    // if true, we fetch the mapping along with the bean
    // only applies to functions which return a single bean
    private boolean fetchMapping = false;

    /**
     * @return Returns the fetchMapping.
     */
    public boolean isFetchMapping() {
        return fetchMapping;
    }

    /**
     * @param fetchMapping
     *            The fetchMapping to set.
     */
    public void setFetchMapping(boolean fetchMapping) {
        this.fetchMapping = fetchMapping;
    }

    private void setQueryNames() {
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public DiscrepancyNoteDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public DiscrepancyNoteDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_DISCREPANCY_NOTE;
    }

    @Override
    public void setTypesExpected() {
        // discrepancy_note_id serial NOT NULL,
        // description varchar(255),
        // discrepancy_note_type_id numeric,
        // resolution_status_id numeric,

        // detailed_notes varchar(1000),
        // date_created date,
        // owner_id numeric,
        // parent_dn_id numeric,
        // adding study id
        // adding assigned user id, tbh 02/2009
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);

        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.TIMESTAMP);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);
    }

    public void setMapTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public DiscrepancyNoteBean getEntityFromHashMap(HashMap<String, Object> hm) {
        DiscrepancyNoteBean eb = new DiscrepancyNoteBean();
        Date dateCreated = (Date) hm.get("date_created");
        Integer ownerId = (Integer) hm.get("owner_id");
        eb.setCreatedDate(dateCreated);
        UserAccountBean owner = (UserAccountBean) getUserAccountDAO().findByPK(ownerId);
        eb.setOwner(owner);

        // discrepancy_note_id serial NOT NULL,
        // description varchar(255),
        // discrepancy_note_type_id numeric,
        // resolution_status_id numeric,

        // detailed_notes varchar(1000),
        // date_created date,
        // owner_id numeric,
        // parent_dn_id numeric,
        eb.setId(selectInt(hm, "discrepancy_note_id"));
        eb.setDescription((String) hm.get("description"));
        eb.setDiscrepancyNoteTypeId(((Integer) hm.get("discrepancy_note_type_id")).intValue());
        eb.setResolutionStatusId(((Integer) hm.get("resolution_status_id")).intValue());
        eb.setParentDnId(((Integer) hm.get("parent_dn_id")).intValue());
        eb.setDetailedNotes((String) hm.get("detailed_notes"));
        eb.setEntityType((String) hm.get("entity_type"));
        eb.setDisType(DiscrepancyNoteType.get(eb.getDiscrepancyNoteTypeId()));
        eb.setResStatus(ResolutionStatus.get(eb.getResolutionStatusId()));
        eb.setStudyId(selectInt(hm, "study_id"));
        eb.setAssignedUserId(selectInt(hm, "assigned_user_id"));
        if (eb.getAssignedUserId() > 0) {
            UserAccountDAO userAccountDAO = new UserAccountDAO(ds);
            UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(eb.getAssignedUserId());
            eb.setAssignedUser(assignedUser);
        }
        eb.setAge(selectInt(hm, "age"));
        eb.setDays(selectInt(hm, "days"));
        return eb;
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAll() {
        return this.executeFindAllQuery("findAll");
    }

    public ArrayList<DiscrepancyNoteBean> findAllParentsByStudy(StudyBean study) {
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());
        ArrayList<DiscrepancyNoteBean> notes = executeFindAllQuery("findAllParentsByStudy", variables);

        if (fetchMapping) {
            for (int i = 0; i < notes.size(); i++) {
                DiscrepancyNoteBean dnb = notes.get(i);
                dnb = findSingleMapping(dnb);
                notes.set(i, dnb);
            }
        }

        return notes;
    }

    public ArrayList<DiscrepancyNoteBean> findAllByStudyAndParent(StudyBean study, int parentId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(parentId));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(study.getId()));
        return this.executeFindAllQuery("findAllByStudyAndParent", variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllItemNotesByEventCRF(int eventCRFId) {
    	String queryName = "findAllItemNotesByEventCRF";
        HashMap<Integer, Object> variables = variables(eventCRFId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRF(int eventCRFId) {
    	String queryName = "findAllParentItemNotesByEventCRF";
        HashMap<Integer, Object> variables = variables(eventCRFId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRFWithConstraints(int eventCRFId, StringBuffer constraints) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(eventCRFId);
        String sql = digester.getQuery("findAllParentItemNotesByEventCRF");
        String[] s = sql.split("order by");
        sql = s[0] + " " + constraints.toString() + " order by " + s[1];
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public Integer getSubjectDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        HashMap<Integer, Object> variables = variables(currentStudyId, currentStudyId);
        String query = digester.getQuery("getSubjectDNCountWithFilter");
        query += filter.execute("", variables);        
        return getCountByQuery(query, variables);
    }

    public Integer getStudySubjectDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        HashMap<Integer, Object> variables = variables(currentStudyId, currentStudyId);
        String query = digester.getQuery("getStudySubjectDNCountWithFilter");
        query += filter.execute("", variables);
        return getCountByQuery(query, variables);
    }

    public Integer getStudyEventDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        HashMap<Integer, Object> variables = variables(currentStudyId, currentStudyId);
        String query = digester.getQuery("getStudyEventDNCountWithFilter");
        query += filter.execute("", variables);
        return getCountByQuery(query, variables);
    }

    public Integer getEventCrfDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        HashMap<Integer, Object> variables = variables(currentStudyId, currentStudyId);
        String query = digester.getQuery("getEventCrfDNCountWithFilter");
        query += filter.execute("", variables);
        return getCountByQuery(query, variables);
    }

    public Integer getItemDataDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        HashMap<Integer, Object> variables = variables(currentStudyId, currentStudyId);
        String query = digester.getQuery("getItemDataDNCountWithFilter");
        query += filter.execute("", variables);
        return getCountByQuery(query, variables);
    }

    public ArrayList<DiscrepancyNoteBean> getWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort, int rowStart, int rowEnd) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();

        HashMap<Integer, Object> variables = variables(currentStudy.getId(), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql += filter.execute("", variables);

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " AND rownum <= " + rowEnd + " and rownum >" + rowStart;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = select(sql, variables);
        for(HashMap<String, Object> row : rows) {
            DiscrepancyNoteBean discBean = this.getEntityFromHashMap(row);
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;

    }

    public Integer getViewNotesCountWithFilter(ListNotesFilter filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        
        String query = "select count(all_dn.discrepancy_note_id) as COUNT from (";
        query += digester.getQuery("findAllSubjectDNByStudy");
        query += filter.execute("", variables);
        query += " UNION ";
        query += digester.getQuery("findAllStudySubjectDNByStudy");
        query += filter.execute("", variables);
        query += " UNION ";
        query += digester.getQuery("findAllStudyEventDNByStudy");
        query += filter.execute("", variables);
        query += " UNION ";
        query += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            query += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        query += filter.execute("", variables);
        query += " UNION ";
        query += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            query += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        query += filter.execute("", variables);
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            query += " ) all_dn";
        } else {
            query += " ) as all_dn";
        }
        return getCountByQuery(query, variables);
    }

    public Integer getViewNotesCountWithFilter(String filter, StudyBean currentStudy) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String query = "select count(all_dn.discrepancy_note_id) as COUNT from (";
        query += digester.getQuery("findAllSubjectDNByStudy");
        query += filter;
        query += " UNION ";
        query += digester.getQuery("findAllStudySubjectDNByStudy");
        query += filter;
        query += " UNION ";
        query += digester.getQuery("findAllStudyEventDNByStudy");
        query += filter;
        query += " UNION ";
        query += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            query += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        query += filter;
        query += " UNION ";
        query += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            query += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        query += filter;
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            query += " ) all_dn";
        } else {
            query += " ) as all_dn";
        }

        return getCountByQuery(query, variables);
    }

    /*
     * public ArrayList<DiscrepancyNoteBean> getViewNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter
     * filter, ListNotesSort sort, int rowStart,
     * int rowEnd) {
     * ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
     * setTypesExpected();
     * this.setTypeExpected(12, TypeNames.STRING);
     * this.setTypeExpected(13, TypeNames.INT);
     * this.setTypeExpected(14, TypeNames.INT);
     * this.setTypeExpected(15, TypeNames.INT);
     * 
     * HashMap variables = new HashMap();
     * variables.put(Integer.valueOf(1), currentStudy.getId());
     * variables.put(Integer.valueOf(2), currentStudy.getId());
     * variables.put(Integer.valueOf(3), currentStudy.getId());
     * variables.put(Integer.valueOf(4), currentStudy.getId());
     * variables.put(Integer.valueOf(5), currentStudy.getId());
     * variables.put(Integer.valueOf(6), currentStudy.getId());
     * variables.put(Integer.valueOf(7), currentStudy.getId());
     * variables.put(Integer.valueOf(8), currentStudy.getId());
     * variables.put(Integer.valueOf(9), currentStudy.getId());
     * variables.put(Integer.valueOf(10), currentStudy.getId());
     * 
     * String sql = "";
     * if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
     * sql = sql + "SELECT * FROM ( SELECT x.*, ROWNUM as rnum FROM (";
     * }
     * sql = sql + digester.getQuery("findAllSubjectDNByStudy");
     * sql = sql + filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllStudySubjectDNByStudy");
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllStudyEventDNByStudy");
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllEventCrfDNByStudy");
     * if (currentStudy.isSite(currentStudy.getParentStudyId())) {
     * sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
     * }
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllItemDataDNByStudy");
     * if (currentStudy.isSite(currentStudy.getParentStudyId())) {
     * sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
     * }
     * sql += filter.execute("");
     * 
     * if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
     * sql += ") x )  WHERE rnum BETWEEN " + (rowStart + 1) + " and " + rowEnd;
     * sql += sort.execute("");
     * } else {
     * sql += sort.execute("");
     * sql += " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
     * }
     * // System.out.println(sql);
     * ArrayList rows = select(sql, variables);
     * 
     * Iterator it = rows.iterator();
     * while (it.hasNext()) {
     * DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
     * discBean = findSingleMapping(discBean);
     * discNotes.add(discBean);
     * }
     * return discNotes;
     * }
     */

    public ArrayList<DiscrepancyNoteBean> getViewNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);
        this.setTypeExpected(15, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());

        String sql = "";
        // if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
        // sql = sql + "SELECT * FROM ( SELECT x.*, ROWNUM as rnum FROM (";
        // }
        sql = sql + digester.getQuery("findAllSubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += sort.execute("");

        ArrayList<HashMap<String, Object>> rows = select(sql, variables);

        for(HashMap<String, Object> hm: rows) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public ArrayList<DiscrepancyNoteBean> findAllDiscrepancyNotesDataByStudy(StudyBean currentStudy) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = digester.getQuery("findAllSubjectDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }

        ArrayList<HashMap<String, Object>> rows = select(sql, variables);
        for(HashMap<String, Object> row : rows) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap(row);
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public ArrayList<DiscrepancyNoteBean> getNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = digester.getQuery("findAllSubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " order by label";

        ArrayList<HashMap<String, Object>> rows = select(sql, variables);
        for(HashMap<String, Object> row : rows) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap(row);
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public ArrayList<DiscrepancyNoteBean> findAllByEntityAndColumn(String entityName, int entityId, String column) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        ArrayList<HashMap<String, Object>> alist;
        HashMap<Integer, Object> variables = variables(entityId, column);
        if ("subject".equalsIgnoreCase(entityName)) {
            alist = this.select(digester.getQuery("findAllBySubjectAndColumn"), variables);
        } else if ("studySub".equalsIgnoreCase(entityName)) {
            alist = this.select(digester.getQuery("findAllByStudySubjectAndColumn"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            alist = this.select(digester.getQuery("findAllByEventCRFAndColumn"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            alist = this.select(digester.getQuery("findAllByStudyEventAndColumn"), variables);
        } else if ("itemData".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// item_name
            alist = this.select(digester.getQuery("findAllByItemDataAndColumn"), variables);
        } else {
        	 alist = new ArrayList<>();
        }

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            if ("eventCrf".equalsIgnoreCase(entityName) || "itemData".equalsIgnoreCase(entityName)) {
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setEntityName((String) hm.get("item_name"));

            } else if ("studyEvent".equalsIgnoreCase(entityName)) {
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
            }
            if (fetchMapping) {
                eb = findSingleMapping(eb);
            }

            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllEntityByPK(String entityName, int noteId) {
        this.setTypesExpected();
        ArrayList<HashMap<String, Object>> alist;
        this.setTypeExpected(12, TypeNames.STRING);// ss.label

        HashMap<Integer, Object> variables = variables(noteId, noteId);
        if ("subject".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllSubjectByPK"), variables);
        } else if ("studySub".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllStudySubjectByPK"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllEventCRFByPK"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllStudyEventByPK"), variables);
        } else if ("itemData".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// item_name
            this.setTypeExpected(17, TypeNames.STRING);// value
            // YW <<
            this.setTypeExpected(18, TypeNames.INT);// item_data_id
            this.setTypeExpected(19, TypeNames.INT);// item_id
            // YW >>
            alist = this.select(digester.getQuery("findAllItemDataByPK"), variables);
        } else {
        	alist = new ArrayList<>();
        }

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = this.getEntityFromHashMap(hm);
            if ("subject".equalsIgnoreCase(entityName) || "studySub".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setColumn((String) hm.get("column_name"));
            } else if ("eventCrf".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setColumn((String) hm.get("column_name"));
            } else if ("itemData".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setEntityName((String) hm.get("item_name"));
                eb.setEntityValue((String) hm.get("value"));
                // YW <<
                eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
                eb.setItemId(((Integer) hm.get("item_id")).intValue());
                // YW >>

            } else if ("studyEvent".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setColumn((String) hm.get("column_name"));
            }
            if (fetchMapping) {
                eb = findSingleMapping(eb);
            }
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudy(StudyBean study) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllSubjectByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudyAndId(StudyBean study, int subjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(subjectId));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllSubjectByStudyAndId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudy(StudyBean study) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudySubjectByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudyAndId(StudyBean study, int studySubjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(studySubjectId));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudySubjectByStudyAndId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudiesAndStudySubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(studySubjectId));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudySubjectByStudiesAndStudySubjectId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudiesAndSubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(5), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(6), Integer.valueOf(studySubjectId));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllSubjectByStudiesAndSubjectId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudyEventByStudy(StudyBean study) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudyEventByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    /**
     * Find all DiscrepancyNoteBeans associated with a certain Study Subject and Study.
     *
     * @param study
     *            A StudyBean, whose id property is checked.
     * @param studySubjectId
     *            The id of a Study Subject.
     * @return An ArrayList of DiscrepancyNoteBeans.
     */
    public ArrayList<DiscrepancyNoteBean> findAllStudyEventByStudyAndId(StudyBean study, int studySubjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(studySubjectId));
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudyEventByStudyAndId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudyEventByStudiesAndSubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(studySubjectId));
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllStudyEventByStudiesAndSubjectId"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllEventCRFByStudy(StudyBean study) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// column_name
        this.setTypeExpected(17, TypeNames.INT);// event_crf_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllEventCRFByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("event_crf_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllEventCRFByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// column_name
        this.setTypeExpected(17, TypeNames.INT);// event_crf_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(parent.getId()));

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllEventCRFByStudyAndParent"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("event_crf_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = variables(eventCRFBean.getId());
        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findItemDataDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;
    }

    public ArrayList<DiscrepancyNoteBean> findParentItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        this.setTypesExpected();

        HashMap<Integer, Object> variables = variables(eventCRFBean.getId());
        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findParentItemDataDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(eventCRFBean.getId());
        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findEventCRFDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesToolTips(EventCRFBean eventCRFBean) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(5), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(6), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(7), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(10), Integer.valueOf(eventCRFBean.getId()));

        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findEventCRFDNotesForToolTips"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;
    }

    public ArrayList<DiscrepancyNoteBean> findAllDNotesByItemNameAndEventCRF(EventCRFBean eventCRFBean, String itemName) {
        this.setTypesExpected();
        
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(2), itemName);

        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findAllDNotesByItemNameAndEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findAllItemDataByStudy(StudyBean study) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// item_name
        this.setTypeExpected(17, TypeNames.STRING);// value
        this.setTypeExpected(18, TypeNames.INT);// item_data_id
        this.setTypeExpected(19, TypeNames.INT);// item_id

        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllItemDataByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setEntityName((String) hm.get("item_name"));
            eb.setEntityValue((String) hm.get("value"));
            // YW << change EntityId from item_id to item_data_id.
            eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
            eb.setItemId(((Integer) hm.get("item_id")).intValue());
            // YW >>
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllItemDataByStudy(StudyBean study, Set<String> hiddenCrfNames) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.INT);// sed_id
        this.setTypeExpected(15, TypeNames.STRING);// sed_name
        this.setTypeExpected(16, TypeNames.STRING);// crf_name
        this.setTypeExpected(17, TypeNames.STRING);// item_name
        this.setTypeExpected(18, TypeNames.STRING);// value
        this.setTypeExpected(19, TypeNames.INT);// item_data_id
        this.setTypeExpected(20, TypeNames.INT);// item_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllItemDataByStudy"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        if (hiddenCrfNames.size() > 0) {
        	for(HashMap<String, Object> hm : alist) {
                Integer sedId = (Integer) hm.get("sed_id");
                String crfName = (String) hm.get("crf_name");
                if (!hiddenCrfNames.contains(sedId + "_" + crfName)) {
                    DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
                    eb.setEventName((String) hm.get("sed_name"));
                    eb.setEventStart((Date) hm.get("date_start"));
                    eb.setCrfName(crfName);
                    eb.setSubjectName((String) hm.get("label"));
                    eb.setEntityName((String) hm.get("item_name"));
                    eb.setEntityValue((String) hm.get("value"));
                    eb.setEntityId((Integer) hm.get("item_data_id"));
                    eb.setItemId((Integer) hm.get("item_id"));
                    al.add(eb);
                }
            }
        } else {
        	for(HashMap<String, Object> hm : alist) {
                DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setSubjectName((String) hm.get("label"));
                eb.setEntityName((String) hm.get("item_name"));
                eb.setEntityValue((String) hm.get("value"));
                eb.setEntityId((Integer) hm.get("item_data_id"));
                eb.setItemId((Integer) hm.get("item_id"));
                al.add(eb);
            }
        }
        return al;
    }

    public Integer countAllItemDataByStudyAndUser(StudyBean study, UserAccountBean user) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(user.getId()));

        String query = digester.getQuery("countAllItemDataByStudyAndUser");
        return getCountByQuery(query, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllItemDataByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.TIMESTAMP);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// item_name
        this.setTypeExpected(17, TypeNames.STRING);// value
        this.setTypeExpected(18, TypeNames.INT);// item_data_id
        this.setTypeExpected(19, TypeNames.INT);// item_id

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(parent.getId()));
        
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllItemDataByStudyAndParent"), variables);

        ArrayList<DiscrepancyNoteBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setEntityName((String) hm.get("item_name"));
            eb.setEntityValue((String) hm.get("value"));
            // YW << change EntityId from item_id to item_data_id.
            eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
            eb.setItemId(((Integer) hm.get("item_id")).intValue());
            // YW >>
            al.add(eb);
        }
        return al;
    }

     /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<DiscrepancyNoteBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public EntityBean findByPK(int ID) {
        DiscrepancyNoteBean eb = new DiscrepancyNoteBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = variables(ID);

        String sql = digester.getQuery("findByPK");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);

        if (alist != null && alist.size() > 0) {
            eb = this.getEntityFromHashMap(alist.get(0));
        }

        if (fetchMapping) {
            eb = findSingleMapping(eb);
        }

        return eb;
    }

    /**
     * Creates a new discrepancy note
     */
    @Override
    public DiscrepancyNoteBean create(DiscrepancyNoteBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        HashMap<Integer, Integer> nullVars = new HashMap<>();
        // INSERT INTO discrepancy_note
        // (description, discrepancy_note_type_id ,
        // resolution_status_id , detailed_notes , date_created,
        // owner_id, parent_dn_id)
        // VALUES (?,?,?,?,now(),?,?)
        variables.put(Integer.valueOf(1), sb.getDescription());
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getDiscrepancyNoteTypeId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(sb.getResolutionStatusId()));
        variables.put(Integer.valueOf(4), sb.getDetailedNotes());

        variables.put(Integer.valueOf(5), Integer.valueOf(sb.getOwner().getId()));
        if (sb.getParentDnId() == 0) {
            nullVars.put(Integer.valueOf(6), Integer.valueOf(Types.INTEGER));
            variables.put(Integer.valueOf(6), null);
        } else {
            variables.put(Integer.valueOf(6), Integer.valueOf(sb.getParentDnId()));
        }
        variables.put(Integer.valueOf(7), sb.getEntityType());
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getStudyId()));
        if (sb.getAssignedUserId() == 0) {
            nullVars.put(Integer.valueOf(9), Integer.valueOf(Types.INTEGER));
            variables.put(Integer.valueOf(9), null);
        } else {
            variables.put(Integer.valueOf(9), Integer.valueOf(sb.getAssignedUserId()));
        }
        // variables.put(Integer.valueOf(9), Integer.valueOf(sb.getAssignedUserId()));

        this.executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        return sb;
    }

    /**
     * Creates a new discrepancy note map
     */
    public void createMapping(DiscrepancyNoteBean eb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(eb.getEntityId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(eb.getId()));
        variables.put(Integer.valueOf(3), eb.getColumn());
        String entityType = eb.getEntityType();

        if ("subject".equalsIgnoreCase(entityType)) {
            this.executeUpdate(digester.getQuery("createSubjectMap"), variables);
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            this.executeUpdate(digester.getQuery("createStudySubjectMap"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            this.executeUpdate(digester.getQuery("createEventCRFMap"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            this.executeUpdate(digester.getQuery("createStudyEventMap"), variables);
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            variables.put(Integer.valueOf(4), eb.isActivated());
            this.executeUpdate(digester.getQuery("createItemDataMap"), variables);
        }

    }

    /**
     * Updates a Study event
     */
    @Override
    public DiscrepancyNoteBean update(DiscrepancyNoteBean dnb) {
        // update discrepancy_note set
        // description =?,
        // discrepancy_note_type_id =? ,
        // resolution_status_id =? ,
        // detailed_notes =?
        // where discrepancy_note_id=?
        dnb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), dnb.getDescription());
        variables.put(Integer.valueOf(2), Integer.valueOf(dnb.getDiscrepancyNoteTypeId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(dnb.getResolutionStatusId()));
        variables.put(Integer.valueOf(4), dnb.getDetailedNotes());
        variables.put(Integer.valueOf(5), Integer.valueOf(dnb.getId()));
        this.executeUpdate(digester.getQuery("update"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateAssignedUser(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = ?
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), dnb.getAssignedUserId());
        variables.put(Integer.valueOf(2), Integer.valueOf(dnb.getId()));
        this.executeUpdate(digester.getQuery("updateAssignedUser"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateDnMapActivation(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = ?
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), dnb.isActivated());
        variables.put(Integer.valueOf(2), dnb.getEntityId());
        this.executeUpdate(digester.getQuery("updateDnMapActivation"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateAssignedUserToNull(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = null
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(dnb.getId()));
        this.executeUpdate(digester.getQuery("updateAssignedUserToNull"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public void deleteNotes(int id) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(id));
        this.executeUpdate(digester.getQuery("deleteNotes"), variables);
        return;

    }

     /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<DiscrepancyNoteBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

     /**
     * NOT IMPLEMENTED
     */
    @Override
    public ArrayList<DiscrepancyNoteBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getCurrentPK() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        int pk = 0;
        ArrayList<HashMap<String, Object>> al = select(digester.getQuery("getCurrentPrimaryKey"));

        if (al.size() > 0) {
            HashMap<String, Object> h = al.get(0);
            pk = ((Integer) h.get("key")).intValue();
        }

        return pk;
    }

    public ArrayList<DiscrepancyNoteBean> findAllByParent(DiscrepancyNoteBean parent) {
        HashMap<Integer, Object> variables = variables(parent.getId());
        return this.executeFindAllQuery("findAllByParent", variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllByStudyEvent(StudyEventBean studyEvent) {
        HashMap<Integer, Object> variables = variables(studyEvent.getId());
        return this.executeFindAllQuery("findByStudyEvent", variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllByStudyEventWithConstraints(StudyEventBean studyEvent, StringBuffer constraints) {
        this.setTypesExpected();
        HashMap<Integer, Object> variables = variables(studyEvent.getId());
        String sql = digester.getQuery("findByStudyEvent");
        sql += constraints.toString();
        ArrayList<HashMap<String, Object>> al = this.select(sql, variables);
        ArrayList<DiscrepancyNoteBean> answer = new ArrayList<>();
        for(HashMap<String, Object> hm : al) {
            answer.add(this.getEntityFromHashMap(hm));
        }
        return answer;
    }

    public HashMap<ResolutionStatus, Integer> findAllByStudyEventWithConstraints(StudyEventBean studyEvent, StringBuffer constraints, boolean isSite) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        HashMap<Integer, Object> variables = variables(studyEvent.getId());
        String sql = digester.getQuery("findByStudyEvent");
        sql += constraints.toString();
        if (isSite) {
            if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                sql += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                        + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                        + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1" + " AND edc.event_definition_crf_id not in ("
                        + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
            } else {
                sql += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                        + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                        + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                        + " AND edc.event_definition_crf_id not in ("
                        + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

            }
        }
        sql += " group By  dn.resolution_status_id ";
        ArrayList<HashMap<String, Object>> al = this.select(sql, variables);
        HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
        for(HashMap<String, Object> h : al) {
            Integer resolutionStatusId = (Integer) h.get("resolution_status_id");
            Integer count = (Integer) h.get("count");
            discCounts.put(ResolutionStatus.get(resolutionStatusId), count);
        }
        return discCounts;
    }

    public HashMap<ResolutionStatus, Integer> countByEntityTypeAndStudyEventWithConstraints(String entityType, StudyEventBean studyEvent,
            StringBuffer constraints, boolean isSite) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.LONG);

        HashMap<Integer, Object> variables = variables(studyEvent.getId());
        String sql = "";
        String temp = "";
        if ("itemData".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findByStudyEvent");
            temp = " and (dn.entity_type='itemData' or dn.entity_type='ItemData') ";
            if (isSite) {
                if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
                } else {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

                }
            }
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByEventCrfTypeAndStudyEvent");
            temp = " and dn.entity_type='eventCrf' ";
            if (isSite) {
                if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
                } else {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

                }
            }
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByStudyEventTypeAndStudyEvent");
            temp = " and dn.entity_type='studyEvent' ";
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByStudySubjectTypeAndStudyEvent");
            temp = " and dn.entity_type='studySub' ";
        } else if ("subject".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countBySubjectTypeAndStudyEvent");
            temp = " and dn.entity_type='subject' ";
        }
        sql += temp;
        sql += constraints.toString();
        sql += " group By  dn.resolution_status_id ";
        ArrayList<HashMap<String, Object>> al = this.select(sql, variables);
        HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
        for(HashMap<String, Object> h : al) {
            Integer resolutionStatusId = (Integer) h.get("resolution_status_id");
            Integer count = ((Long) h.get("count")).intValue();
            discCounts.put(ResolutionStatus.get(resolutionStatusId), count);
        }
        return discCounts;
    }

    private DiscrepancyNoteBean findSingleMapping(DiscrepancyNoteBean note) {
        HashMap<Integer, Object> variables = variables(note.getId());

        setMapTypesExpected();
        String entityType = note.getEntityType();
        String sql = "";
        if ("subject".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findSubjectMapByDNId");
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findStudySubjectMapByDNId");
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findEventCRFMapByDNId");
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findStudyEventMapByDNId");
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findItemDataMapByDNId");
            this.unsetTypeExpected();
            this.setTypeExpected(1, TypeNames.INT);
            this.setTypeExpected(2, TypeNames.INT);
            this.setTypeExpected(3, TypeNames.STRING);
            this.setTypeExpected(4, TypeNames.INT);
            this.setTypeExpected(5, TypeNames.BOOL);
        }

        ArrayList<HashMap<String, Object>> hms = select(sql, variables);

        if (hms.size() > 0) {
            HashMap<String, Object> hm = hms.get(0);
            note = getMappingFromHashMap(hm, note);
        }

        return note;
    }

    private DiscrepancyNoteBean getMappingFromHashMap(HashMap<String, Object> hm, DiscrepancyNoteBean note) {
        String entityType = note.getEntityType();
        String entityIDColumn = getEntityIDColumn(entityType);

        if (!entityIDColumn.equals("")) {
            note.setEntityId(selectInt(hm, entityIDColumn));
        }
        note.setColumn(selectString(hm, "column_name"));
        return note;
    }

    public static String getEntityIDColumn(String entityType) {
        String entityIDColumn = "";
        if ("subject".equalsIgnoreCase(entityType)) {
            entityIDColumn = "subject_id";
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            entityIDColumn = "study_subject_id";
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            entityIDColumn = "event_crf_id";
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            entityIDColumn = "study_event_id";
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            entityIDColumn = "item_data_id";
        }
        return entityIDColumn;
    }

    public AuditableEntityBean findEntity(DiscrepancyNoteBean note) {
        AuditableEntityDAO<?> aedao = getAEDAO(note, ds);

        try {
            if (aedao != null) {
                AuditableEntityBean aeb = (AuditableEntityBean) aedao.findByPK(note.getEntityId());
                return aeb;
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static AuditableEntityDAO<?> getAEDAO(DiscrepancyNoteBean note, DataSource ds) {
        String entityType = note.getEntityType();
        if ("subject".equalsIgnoreCase(entityType)) {
            return new SubjectDAO(ds);
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            return new StudySubjectDAO(ds);
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            return new EventCRFDAO(ds);
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            return new StudyEventDAO(ds);
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            return new ItemDataDAO(ds);
        }

        return null;
    }

    public int findNumExistingNotesForItem(int itemDataId) {
        HashMap<Integer, Object> variables = variables(itemDataId);
        String query = digester.getQuery("findNumExistingNotesForItem");
        return getCountByQuery(query, variables, "num");
    }

    public int findNumOfActiveExistingNotesForItemData(int itemDataId) {
        HashMap<Integer, Object> variables = variables(itemDataId);
        String query = digester.getQuery("findNumOfActiveExistingNotesForItemData");
        return getCountByQuery(query, variables, "num");
    }

    public ArrayList<DiscrepancyNoteBean> findExistingNotesForItemData(int itemDataId) {
    	String queryName = "findExistingNotesForItemData";
        HashMap<Integer, Object> variables = variables(itemDataId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findExistingNotesForToolTip(int itemDataId) {
        String queryName = "findExistingNotesForToolTip";
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(2), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(3), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(4), Integer.valueOf(itemDataId));
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findParentNotesForToolTip(int itemDataId) {
    	String queryName = "findParentNotesForToolTip";
        HashMap<Integer, Object> variables = variables(itemDataId, itemDataId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findParentNotesOnlyByItemData(int itemDataId) {
    	String queryName = "findParentNotesOnlyByItemData";
        HashMap<Integer, Object> variables = variables(itemDataId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllTopNotesByEventCRF(int eventCRFId) {
    	String queryName = "findAllTopNotesByEventCRF";
    	HashMap<Integer, Object> variables = variables(eventCRFId);
    	return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<DiscrepancyNoteBean> findOnlyParentEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(eventCRFBean.getId());
        ArrayList<HashMap<String, Object>> dNotelist = this.select(digester.getQuery("findOnlyParentEventCRFDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        for(HashMap<String, Object> hm : dNotelist) {
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;
    }

    public String findSiteHiddenEventCrfIdsString(StudyBean site) {
        String sql = "";
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql = "select ec.event_crf_id from event_crf ec, study_event se, crf_version cv, " + "(select edc.study_event_definition_id, edc.crf_id, crf.name "
                    + "from event_definition_crf edc, crf, study s " + "where s.study_id=" + site.getId()
                    + " and (edc.study_id = s.study_id or edc.study_id = s.parent_study_id)" + "    and edc.event_definition_crf_id not in ( "
                    + "        select parent_id from event_definition_crf where study_id=s.study_id) "
                    + "            and edc.status_id=1 and edc.hide_crf = 1 and edc.crf_id = crf.crf_id) sedc " + "where ec.study_event_id = se.study_event_id "
                    + "and se.study_event_definition_id = sedc.study_event_definition_id "
                    + "and ec.crf_version_id = cv.crf_version_id and cv.crf_id = sedc.crf_id";
        } else {
            sql = "select ec.event_crf_id from event_crf ec, study_event se, crf_version cv, " + "(select edc.study_event_definition_id, edc.crf_id, crf.name "
                    + "from event_definition_crf edc, crf, study s " + "where s.study_id=" + site.getId()
                    + " and (edc.study_id = s.study_id or edc.study_id = s.parent_study_id)" + "    and edc.event_definition_crf_id not in ( "
                    + "        select parent_id from event_definition_crf where study_id=s.study_id) "
                    + "            and edc.status_id=1 and edc.hide_crf = 'true' and edc.crf_id = crf.crf_id) as sedc "
                    + "where ec.study_event_id = se.study_event_id " + "and se.study_event_definition_id = sedc.study_event_definition_id "
                    + "and ec.crf_version_id = cv.crf_version_id and cv.crf_id = sedc.crf_id";
        }
        return sql;
    }

    public EntityBean findLatestChildByParent(int parentId) {
    	String queryName = "findLatestChildByParent";
        HashMap<Integer, Object> variables = variables(parentId, parentId);
        return executeFindByPKQuery(queryName, variables);
    }

    public int getResolutionStatusIdForSubjectDNFlag(int subjectId, String column) {
        int id = 0;
        unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));
        variables.put(Integer.valueOf(2), new String(column));

        String sql = digester.getQuery("getResolutionStatusIdForSubjectDNFlag");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for(HashMap<String, Object> hm : alist) {
            try {
                id = ((Integer) hm.get("resolution_status_id")).intValue();
            } catch (Exception e) {
            }
        }
        return id;
    }

    // Yufang code, addded by Jamuna
    public Integer getViewNotesCountWithFilter(Integer assignedUserId, Integer studyId) {
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(Integer.valueOf(1), assignedUserId);
        variables.put(Integer.valueOf(2), studyId);
        variables.put(Integer.valueOf(3), studyId);
        String sql = digester.getQuery("countViewNotesForAssignedUserInStudy");
        return getCountByQuery(sql, variables);
    }

	@Override
	public DiscrepancyNoteBean emptyBean() {
		return new DiscrepancyNoteBean();
	}
}