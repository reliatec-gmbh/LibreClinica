/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.submit;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author jxu
 */
public class SubjectDAO extends AuditableEntityDAO<SubjectBean> {
    // private DataSource ds;
    // private DAODigester digester;
    // protected String

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public SubjectDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public SubjectDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SubjectDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECT;
    }

    @Override
    public void setTypesExpected() {
        // SERIAL, NUMERIC, NUMERIC, NUMERIC,
        // DATE, CHAR(1), VARCHAR(255),DATE,
        // NUMERIC, DATE, NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
        this.setTypeExpected(8, TypeNames.TIMESTAMP);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.TIMESTAMP);
        this.setTypeExpected(11, TypeNames.INT);
        this.setTypeExpected(12, TypeNames.BOOL);
        this.setTypeExpected(13, TypeNames.STRING);

    }

    /**
     * findAllSubjectsAndStudies()
     *
     * For every subject find all studies that subject belongs to.
     *
     * smw
     *
     */
    public ArrayList<SubjectBean> findAllSubjectsAndStudies() {

        this.setTypesExpected();
        this.setTypeExpected(13, TypeNames.STRING); // label from study_subject table
        this.setTypeExpected(14, TypeNames.STRING); // unique_identifier from study table

        String sql = digester.getQuery("findAllSubjectsAndStudies");

        ArrayList<HashMap<String, Object>> alist = this.select(sql);
        ArrayList<SubjectBean> answer = new ArrayList<>();
        for(HashMap<String, Object> hm : alist) {
            SubjectBean sb = (SubjectBean) this.getEntityFromHashMap(hm);
            sb.setLabel((String) hm.get("label"));
            sb.setStudyIdentifier((String) hm.get("study_unique_identifier"));

            answer.add(sb);
        }

        return answer;
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList<SubjectBean> findAllByGender(char gender) {
    	ArrayList<SubjectBean> beans;
    	switch(gender) {
    		case 'm':
    			beans = findAllMales();
    			break;
    		case 'f':
    			beans = findAllFemales();
    			break;
			default:
				beans = new ArrayList<>();
    	}
    	return beans;
    }

    public ArrayList<SubjectBean> findAllFemales() {
        return executeFindAllQuery("findAllFemales");
    }

    public ArrayList<SubjectBean> findAllMales() {
        return executeFindAllQuery("findAllMales");
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females, not include himself.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList<SubjectBean> findAllByGenderNotSelf(char gender, int id) {
    	ArrayList<SubjectBean> beans;
    	switch(gender) {
    		case 'm':
    			beans = findAllMalesNotSelf(id);
    			break;
    		case 'f':
    			beans = findAllFemalesNotSelf(id);
    			break;
			default:
				beans = new ArrayList<>();
    	}
    	return beans;
    }

    public ArrayList<SubjectBean> findAllFemalesNotSelf(int id) {
    	String queryName = "findAllFemalesNotSelf";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindAllQuery(queryName, variables);
    }

    public ArrayList<SubjectBean> findAllMalesNotSelf(int id) {
    	String queryName = "findAllMalesNotSelf";
        HashMap<Integer, Object> variables = variables(id);
        return executeFindAllQuery(queryName, variables);
    }

    // TODO remove unused parameter 'currentStudy'
    public ArrayList<SubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListSubjectFilter filter, ListSubjectSort sort, int rowStart, int rowEnd) {
        setTypesExpected();

        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");

        if (CoreResources.getDBName().equals("oracle")) {
            sql += " )x)where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList<HashMap<String, Object>> rows = this.select(sql);
        ArrayList<SubjectBean> subjects = new ArrayList<SubjectBean>();
        for(HashMap<String, Object> hm : rows) {
            SubjectBean subjectBean = (SubjectBean) this.getEntityFromHashMap(hm);
            subjects.add(subjectBean);
        }
        return subjects;
    }

    public Integer getCountWithFilter(ListSubjectFilter filter, StudyBean currentStudy) {
        String query = digester.getQuery("getCountWithFilter");
        query += filter.execute("");
        return getCountByQuery(query, new HashMap<Integer, Object>());
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public SubjectBean getEntityFromHashMap(HashMap<String, Object> hm) {
        SubjectBean eb = new SubjectBean();
        super.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("subject_id")).intValue());
        Date birthday = (Date) hm.get("date_of_birth");
        eb.setDateOfBirth(birthday);
        try {
            String gender = (String) hm.get("gender");
            char[] genderarr = gender.toCharArray();
            eb.setGender(genderarr[0]);
        } catch (ClassCastException ce) {
            eb.setGender(' ');
        }
        eb.setUniqueIdentifier((String) hm.get("unique_identifier"));
        eb.setDobCollected(((Boolean) hm.get("dob_collected")).booleanValue());

        return eb;
    }

    public ArrayList<SubjectBean> findAll() {

        return findAllByLimit(false);
    }

    
    public ArrayList<SubjectBean> findAllByLimit(boolean hasLimit) {
    	String queryName;
        if (hasLimit) {
            queryName = "findAllByLimit";
        } else {
            queryName = "findAll";
        }
        return executeFindAllQuery(queryName);
    }

    public SubjectBean findAnotherByIdentifier(String name, int subjectId) {
    	String queryName = "findAnotherByIdentifier";
        HashMap<Integer, Object> variables = variables(name, subjectId);
        return executeFindByPKQuery(queryName, variables);
    }

    public ArrayList<SubjectBean> findAllChildrenByPK(int subjectId) {
    	String queryName = "findAllChildrenByPK";
        HashMap<Integer, Object> variables = variables(subjectId, subjectId);
        return executeFindAllQuery(queryName, variables);
    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */
    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
    	String queryName = "findByUniqueIdentifierAndAnyStudy";
        HashMap<Integer, Object> variables = variables(uniqueIdentifier, studyId, studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    
    public SubjectBean findByUniqueIdentifierAndStudy(String uniqueIdentifier, int studyId) {
    	String queryName = "findByUniqueIdentifierAndStudy";
        HashMap<Integer, Object> variables = variables(uniqueIdentifier, studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */
    public SubjectBean findByUniqueIdentifierAndParentStudy(String uniqueIdentifier, int studyId) {
    	String queryName = "findByUniqueIdentifierAndParentStudy";
        HashMap<Integer, Object> variables = variables(uniqueIdentifier, studyId);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectBean> findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    public SubjectBean findByPK(int ID) {
    	String queryName = "findByPK";
        HashMap<Integer, Object> variables = variables(ID);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * Create a subject.
     *
     * @param sb
     *            The subject to create. <code>true</code> if the father and
     *            mother id have been properly set; primarily for use with
     *            genetic studies. <code>false</code> if the father and mother
     *            id have not been properly set; primarily for use with
     *            non-genetic studies.
     * @return
     */
    @Override
    public SubjectBean create(SubjectBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();
        logger.debug("Logged in subject DAO.create");
        
        int ind = 1;
        variables.put(ind++, sb.getStatus().getId());

        if (sb.getDateOfBirth() == null) {
            nullVars.put(ind, Types.DATE);
            variables.put(ind, null);
            ind++;
        } else {
            variables.put(ind++, sb.getDateOfBirth());
        }

        switch(sb.getGender()) {
        case 'm':
        case 'f':
            variables.put(ind++, String.valueOf(sb.getGender()));
            break;
        default:
            nullVars.put(ind, Types.CHAR);
            variables.put(ind, null);
            ind++;          
        }
        variables.put(ind++, sb.getUniqueIdentifier());
        variables.put(ind++, sb.getOwnerId());
        variables.put(ind++, sb.isDobCollected());

        executeUpdateWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }
        return sb;
    }

    public SubjectBean findByUniqueIdentifier(String uniqueIdentifier) {
    	String queryName = "findByUniqueIdentifier";
        HashMap<Integer, Object> variables = variables(uniqueIdentifier);
        return executeFindByPKQuery(queryName, variables);
    }

    /**
     * <b>update </b>, the method that returns an updated subject bean after it
     * updates the database.
     *
     * @return sb, an updated study bean.
     */
    public SubjectBean update(SubjectBean sb) {
        HashMap<Integer, Object> variables = new HashMap<>();
		HashMap<Integer, Integer> nullVars = new HashMap<>();

        // UPDATE subject SET FATHER_ID=?,MOTHER_ID=?, STATUS_ID=?,
        // DATE_OF_BIRTH=?,GENDER=?,UNIQUE_IDENTIFIER=?, DATE_UPDATED=?,
        // UPDATE_ID=? DOB_COLLECTED=? WHERE SUBJECT_ID=?
        // YW <<
        int ind = 1;
        variables.put(ind++, sb.getStatus().getId());
        if (sb.getDateOfBirth() != null) {
            variables.put(ind++, sb.getDateOfBirth());
        } else {
            nullVars.put(ind, Types.DATE);
            variables.put(ind, null);
            ind++;
        }

        switch(sb.getGender()) {
        case 'm':
        case 'f':
            variables.put(ind++, String.valueOf(sb.getGender()));
            break;
        default:
            nullVars.put(ind, Types.CHAR);
            variables.put(ind, null);
            ind++;          
        }
        variables.put(ind++, sb.getUniqueIdentifier());
        // date_updated is set to now()
        //    variables.put(new Integer(ind++), new java.util.Date());
        variables.put(ind++, sb.getUpdater().getId());
        variables.put(ind++, sb.isDobCollected());
        variables.put(ind++, sb.getId());

        String sql = digester.getQuery("update");
        this.executeUpdate(sql, variables, nullVars);

        return sb;
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectBean> findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * NOT IMPLEMENTED
     */
    public ArrayList<SubjectBean> findAllByPermission(Object objCurrentUser, int intActionType) {
    	throw new RuntimeException("Not implemented");
    }

    public void deleteTestSubject(String uniqueIdentifier) {
        HashMap<Integer, Object> variables = variables(uniqueIdentifier);
        this.executeUpdate(digester.getQuery("deleteTestSubject"), variables);
    }

	@Override
	public SubjectBean emptyBean() {
		return new SubjectBean();
	}
}
