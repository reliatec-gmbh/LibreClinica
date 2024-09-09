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
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author jxu
 */
public class StudyGroupDAO extends AuditableEntityDAO<StudyGroupBean> {
    // private DAODigester digester;

    protected void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
    }

    public StudyGroupDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyGroupDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYGROUP;
    }

    @Override
    public void setTypesExpected() {
        // study_group_id int4 ,
        // name varchar(255),
        // description varchar(1000),
        // study_group_class_id numeric,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.INT);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public StudyGroupBean getEntityFromHashMap(HashMap<String, Object> hm) {
        StudyGroupBean eb = new StudyGroupBean();
        eb.setId(((Integer) hm.get("study_group_id")).intValue());
        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setStudyGroupClassId(((Integer) hm.get("study_group_class_id")).intValue());

        return eb;
    }

    public ArrayList<StudyGroupBean> findAll() {
    	String queryName = "";
        return executeFindAllQuery(queryName);
    }

    public ArrayList<StudyGroupBean> findAllByGroupClass(StudyGroupClassBean group) {
    	String queryName = "findAllByGroupClass";
        HashMap<Integer, Object> variables = variables(group.getId());
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<StudyGroupBean> getGroupByStudySubject(int studySubjectId,int studyId,int parentStudyId) {
    	String queryName = "getGroupByStudySubject";
        HashMap<Integer, Object> variables = variables(studySubjectId, studyId, parentStudyId);
        return executeFindAllQuery(queryName, variables);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
       throw new RuntimeException("Not implemented");
    }

    public StudyGroupBean findByPK(int id) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindByPKQuery(queryName, variables);
    }

    public StudyGroupBean findByStudyId(int studyId) {
    	String queryName = "findByStudyId";
        HashMap<Integer, Object> variables = variables(studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    /*
     * created for Extract Bean, we give the method a study subject bean and it
     * returns for us a hash map of class ids pointing to study group beans, tbh
     * July 2007
     */
    public HashMap<Integer, StudyGroupBean> findByStudySubject(StudySubjectBean studySubject) {
    	String queryName = "findByStudySubject";
        HashMap<Integer, Object> variables = variables(studySubject.getId());
        ArrayList<StudyGroupBean> beans = executeFindAllQuery(queryName, variables);
        
        return new HashMap<>(beans.stream().collect(Collectors.toMap(StudyGroupBean::getStudyGroupClassId, b -> b)));
    }

    public HashMap<Integer, ArrayList<HashMap<Integer, StudyGroupBean>>> findSubjectGroupMaps(int studyId) {
        logger.info("testing with variable: " + studyId);
        HashMap<Integer, ArrayList<HashMap<Integer, StudyGroupBean>>> subjectGroupMaps = new HashMap<>();
        ArrayList<HashMap<Integer, StudyGroupBean>> groupMaps = new ArrayList<>();
        HashMap<Integer, StudyGroupBean> subjectGroupMap = new HashMap<>();

        this.setTypesExpected();
        this.setTypeExpected(5, TypeNames.INT);

        HashMap<Integer, Object> variables = variables(studyId);

        String sql = digester.getQuery("findSubjectGroupMaps");
        ArrayList<HashMap<String, Object>> alist = this.select(sql, variables);
        for(HashMap<String, Object> answers : alist) {
            logger.info("iteration over answers...");
            subjectGroupMap = new HashMap<>();
            Integer studySubjectId = (Integer) answers.get("study_subject_id");
            logger.info("iteration over answers..." + studySubjectId.intValue());
            if (subjectGroupMaps.containsKey(studySubjectId)) {
                groupMaps = subjectGroupMaps.get(studySubjectId);
            } else {
                groupMaps = new ArrayList<>();
            }
            StudyGroupBean sgbean = this.getEntityFromHashMap(answers);

            subjectGroupMap.put(new Integer(sgbean.getStudyGroupClassId()), sgbean);
            groupMaps.add(subjectGroupMap);
            logger.info("subjectgroupmaps: just put in " + sgbean.getStudyGroupClassId());
            subjectGroupMaps.put(studySubjectId, groupMaps);
        }
        return subjectGroupMaps;
    }

    /**
     * Creates a new StudyGroup
     */
    @Override
    public StudyGroupBean create(StudyGroupBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();

        variables.put(new Integer(1), sb.getName());
        variables.put(new Integer(2), sb.getDescription());
        variables.put(new Integer(3), new Integer(sb.getStudyGroupClassId()));

        this.executeUpdate(digester.getQuery("create"), variables);

        return sb;
    }

    /**
     * Updates a StudyGroup
     */
    public StudyGroupBean update(StudyGroupBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();

        // UPDATE study_group SET study_group_class_id=?, name=?,
        // description=?
        // WHERE study_group_id=?
        variables.put(new Integer(1), new Integer(sb.getStudyGroupClassId()));
        variables.put(new Integer(2), sb.getName());
        variables.put(new Integer(3), sb.getDescription());
        variables.put(new Integer(4), new Integer(sb.getId()));

        String sql = digester.getQuery("update");
        this.executeUpdate(sql, variables);

        return sb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<StudyGroupBean> findAllByPermission(Object objCurrentUser, int intActionType) {
        throw new RuntimeException("Not implemented");
    }

    public StudyGroupBean findByNameAndGroupClassID(String name, int studyGroupClassId) {
    	String queryName = "findByNameAndGroupClassId";
        HashMap<Integer, Object> variables = variables(name, studyGroupClassId);
        return executeFindByPKQuery(queryName, variables);
    }
    
 
    public StudyGroupBean findSubjectStudyGroup(int subjectId, String groupClassName) {
    	String queryName = "findSubjectStudyGroup";
        HashMap<Integer, Object> variables = variables(subjectId, groupClassName);
        return executeFindByPKQuery(queryName, variables);
    }

	@Override
	public StudyGroupBean emptyBean() {
		return new StudyGroupBean();
	}
 
    
}