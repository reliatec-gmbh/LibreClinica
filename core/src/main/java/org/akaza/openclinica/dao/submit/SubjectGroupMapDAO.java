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
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author jxu
 * 
 */
public class SubjectGroupMapDAO extends AuditableEntityDAO<SubjectGroupMapBean> {

    private void setQueryNames() {
        this.getCurrentPKName = "getCurrentPK";
    }

    public SubjectGroupMapDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public SubjectGroupMapDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SubjectGroupMapDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECTGROUPMAP;
    }

    @Override
    public void setTypesExpected() {
        // subject_group_map_id serial NOT NULL,
        // study_group_class_id numeric,
        // study_subject_id numeric,
        // study_group_id numeric,

        // status_id numeric,
        // owner_id numeric,
        // date_created date,
        // date_updated date,

        // update_id numeric,
        // notes varchar(255),
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);

        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.DATE);

        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.STRING);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public SubjectGroupMapBean getEntityFromHashMap(HashMap<String, Object> hm) {
        SubjectGroupMapBean eb = new SubjectGroupMapBean();
        super.setEntityAuditInformation(eb, hm);
        // subject_group_map_id serial NOT NULL,
        // study_group_class_id numeric,
        // study_subject_id numeric,
        // study_group_id numeric,
        // status_id numeric,
        // owner_id numeric,
        // date_created date,
        // date_updated date,
        // update_id numeric,
        // notes varchar(255),
        eb.setId(((Integer) hm.get("subject_group_map_id")).intValue());
        eb.setStudyGroupId(((Integer) hm.get("study_group_id")).intValue());
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setStudyGroupClassId(((Integer) hm.get("study_group_class_id")).intValue());
        eb.setNotes((String) hm.get("notes"));

        return eb;
    }

    public ArrayList<SubjectGroupMapBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    public ArrayList<SubjectGroupMapBean> findAllByStudySubject(int studySubjectId) {
        setTypesExpected();
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);
        
        HashMap<Integer, Object> variables = variables(studySubjectId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllByStudySubject"), variables);
        ArrayList<SubjectGroupMapBean> al = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            SubjectGroupMapBean eb = (SubjectGroupMapBean) this.getEntityFromHashMap(hm);
            eb.setStudyGroupName(((String) hm.get("group_name")));
            eb.setGroupClassName(((String) hm.get("class_name")));
            al.add(eb);
        }
        return al;
    }

    public SubjectGroupMapBean findByStudySubjectAndStudyGroupClass(int studySubjectId, int studyGroupClassId) {
        setTypesExpected();
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);
        HashMap<Integer, Object> variables = variables(studySubjectId, studyGroupClassId);
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findByStudySubjectAndStudyGroupClass"), variables);
        SubjectGroupMapBean eb = null;
        if(alist != null && alist.size() > 0) {
            HashMap<String, Object> hm = alist.get(0);
            eb = (SubjectGroupMapBean) this.getEntityFromHashMap(hm);
            eb.setStudyGroupName(((String) hm.get("group_name")));
            eb.setGroupClassName(((String) hm.get("class_name")));
        }
        return eb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectGroupMapBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public SubjectGroupMapBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Creates a new subject
     */
    public SubjectGroupMapBean create(SubjectGroupMapBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        // INSERT INTO SUBJECT_GROUP_MAP (study_group_class_id,
        // study_subject_id, study_group_id,
        // status_id, owner_id,date_created,
        // notes) VALUES (?,?,?,?,?,NOW(),?)
        variables.put(new Integer(1), new Integer(sb.getStudyGroupClassId()));
        variables.put(new Integer(2), new Integer(sb.getStudySubjectId()));
        variables.put(new Integer(3), new Integer(sb.getStudyGroupId()));
        variables.put(new Integer(4), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(5), new Integer(sb.getOwner().getId()));
        variables.put(new Integer(6), sb.getNotes());
        // DATE_CREATED is now()

        this.executeUpdate(digester.getQuery("create"), variables);

        return sb;
    }

    /**
     * <b>update </b>, the method that returns an updated subject bean after it
     * updates the database.
     * 
     * @return sb, an updated study bean.
     */
    public SubjectGroupMapBean update(SubjectGroupMapBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        // UPDATE SUBJECT_GROUP_MAP SET STUDY_GROUP_CLASS_ID=?,
        // STUDY_SUBJECT_ID=?,STUDY_GROUP_ID=?,
        // STATUS_ID=?,DATE_UPDATED=?, UPDATE_ID=? , notes = ?
        // WHERE SUBJECT_GROUP_MAP_ID=?
        variables.put(new Integer(1), new Integer(sb.getStudyGroupClassId()));
        variables.put(new Integer(2), new Integer(sb.getStudySubjectId()));
        variables.put(new Integer(3), new Integer(sb.getStudyGroupId()));
        variables.put(new Integer(4), new Integer(sb.getStatus().getId()));

        variables.put(new Integer(5), new java.util.Date());
        variables.put(new Integer(6), new Integer(sb.getUpdater().getId()));
        variables.put(new Integer(8), new Integer(sb.getId()));
        variables.put(new Integer(7), sb.getNotes());

        String sql = digester.getQuery("update");
        this.executeUpdate(sql, variables);

        return sb;
    }

    public ArrayList<SubjectGroupMapBean> findAllByStudyGroupClassAndGroup(int studyGroupClassId, int studyGroupId) {
        setTypesExpected();
        this.setTypeExpected(11, TypeNames.STRING);
        HashMap<Integer, Object> variables = new HashMap<>();
        variables.put(new Integer(1), new Integer(studyGroupClassId));
        variables.put(new Integer(2), new Integer(studyGroupId));
        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllByStudyGroupClassAndGroup"), variables);
        ArrayList<SubjectGroupMapBean> al = new ArrayList<>();
        Iterator<HashMap<String, Object>> it = alist.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> hm = it.next();
            SubjectGroupMapBean eb = (SubjectGroupMapBean) this.getEntityFromHashMap(hm);
            eb.setSubjectLabel(((String) hm.get("label")));
            al.add(eb);
        }
        return al;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectGroupMapBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectGroupMapBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public ArrayList<SubjectGroupMapBean> findAllByStudyGroupId(int studyGroupId) {
    	String queryName = "findAllByStudyGroupId";
        HashMap<Integer, Object> variables = variables(studyGroupId);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<SubjectGroupMapBean> findAllByStudyGroupClassId(int studyGroupClassId) {
    	String queryName = "findAllByStudyGroupClassId";
        HashMap<Integer, Object> variables = variables(studyGroupClassId);
        return executeFindAllQuery(queryName, variables);
    }

    public void deleteTestGroupMap(int id) {
        HashMap<Integer, Object> variables = variables(id);
        this.executeUpdate(digester.getQuery("deleteTestGroupMap"), variables);
    }

	@Override
	public SubjectGroupMapBean emptyBean() {
		return new SubjectGroupMapBean();
	}

}