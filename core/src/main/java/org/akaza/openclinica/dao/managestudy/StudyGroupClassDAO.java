/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.GroupClassType;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author jxu
 *
 * The data access object that users will access the database for study group
 * class objects
 */
public class StudyGroupClassDAO extends AuditableEntityDAO<StudyGroupClassBean> {
    protected void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
        getNextPKName = "getNextPK";
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public StudyGroupClassDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyGroupClassDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYGROUPCLASS;
    }

    @Override
    public void setTypesExpected() {
        // study_group_class_id int4,
        // name varchar(30),
        // study_id numeric,
        // owner_id numeric,
        // date_created date,
        // group_class_type_id numeric,
        // status_id numeric,
        // date_updated date,
        // update_id numeric,
        // subject_assignment varchar(30),
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);

        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.DATE);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.STRING);

    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public StudyGroupClassBean getEntityFromHashMap(HashMap<String, Object> hm) {
        StudyGroupClassBean eb = new StudyGroupClassBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_GROUP_ID NAME STUDY_ID OWNER_ID DATE_CREATED
        // GROUP_TYPE_ID STATUS_ID DATE_UPDATED UPDATE_ID
        eb.setId(((Integer) hm.get("study_group_class_id")).intValue());
        eb.setName((String) hm.get("name"));
        eb.setStudyId(((Integer) hm.get("study_id")).intValue());
        eb.setGroupClassTypeId(((Integer) hm.get("group_class_type_id")).intValue());
        String classTypeName = GroupClassType.get(((Integer) hm.get("group_class_type_id")).intValue()).getName();
        eb.setGroupClassTypeName(classTypeName);
        eb.setSubjectAssignment((String) hm.get("subject_assignment"));
        return eb;
    }

    public ArrayList<StudyGroupClassBean> findAll() {
    	String queryName = "findAll";
        return executeFindAllQuery(queryName);
    }

    @Override
    public ArrayList<StudyGroupClassBean> findAllByStudy(StudyBean study) {
        ArrayList<StudyGroupClassBean> answer = new ArrayList<>();

        this.setTypesExpected();
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllByStudy"), variables);

        for(HashMap<String, Object> hm : alist) {
            StudyGroupClassBean group = (StudyGroupClassBean) this.getEntityFromHashMap(hm);
            group.setStudyName((String) hm.get("study_name"));
            logger.info("study Name" + group.getStudyName());
            group.setGroupClassTypeName((String) hm.get("type_name"));
            answer.add(group);
        }

        return answer;
    }

    @Override
    public ArrayList<StudyGroupClassBean> findAllActiveByStudy(StudyBean study) {
        ArrayList<StudyGroupClassBean> answer = new ArrayList<>();

        this.setTypesExpected();
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);

        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());

        ArrayList<HashMap<String, Object>> alist = this.select(digester.getQuery("findAllActiveByStudy"), variables);

        for(HashMap<String, Object> hm : alist) {
            StudyGroupClassBean group = (StudyGroupClassBean) this.getEntityFromHashMap(hm);
            group.setStudyName((String) hm.get("study_name"));
            // logger.info("study Name " + group.getStudyName());
            group.setGroupClassTypeName((String) hm.get("type_name"));
            group.setSelected(false);
            answer.add(group);
        }

        return answer;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupClassBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    public StudyGroupClassBean findByPK(int id) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyGroupClassBean findByStudyId(int studyId) {
    	String queryName = "findByStudyId";
        HashMap<Integer, Object> variables = variables(studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Creates a new StudyGroup
     */
    @Override
    public StudyGroupClassBean create(StudyGroupClassBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
        int id = getNextPK();
        // INSERT INTO study_group_class
        // (NAME,STUDY_ID,OWNER_ID,DATE_CREATED, GROUP_CLASS_TYPE_ID,
        // STATUS_ID,subject_assignment)
        // VALUES (?,?,?,NOW(),?,?,?)
        variables.put(new Integer(1), new Integer(id));
        variables.put(new Integer(2), sb.getName());
        variables.put(new Integer(3), new Integer(sb.getStudyId()));
        variables.put(new Integer(4), new Integer(sb.getOwner().getId()));
        variables.put(new Integer(5), new Integer(sb.getGroupClassTypeId()));
        // Date_created is now()
        variables.put(new Integer(6), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(7), sb.getSubjectAssignment());
        this.executeUpdate(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            sb.setId(id);
        }

        return sb;
    }

    /**
     * Updates a StudyGroupClass
     */
    public StudyGroupClassBean update(StudyGroupClassBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();

        // UPDATE study_group_class SET NAME=?,STUDY_ID=?,
        // GROUP_class_TYPE_ID=?,
        // STATUS_ID=?, DATE_UPDATED=?,UPDATE_ID=?,
        // subject_assignment=? WHERE STUDY_GROUP_class_ID=?
        variables.put(new Integer(1), sb.getName());
        variables.put(new Integer(2), new Integer(sb.getStudyId()));
        variables.put(new Integer(3), new Integer(sb.getGroupClassTypeId()));

        variables.put(new Integer(4), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(5), new java.util.Date());
        variables.put(new Integer(6), new Integer(sb.getUpdater().getId()));
        variables.put(new Integer(7), sb.getSubjectAssignment());
        variables.put(new Integer(8), new Integer(sb.getId()));

        String sql = digester.getQuery("update");
        this.executeUpdate(sql, variables);

        return sb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupClassBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
    	throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupClassBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

	@Override
	public StudyGroupClassBean emptyBean() {
		return new StudyGroupClassBean();
	}

}
