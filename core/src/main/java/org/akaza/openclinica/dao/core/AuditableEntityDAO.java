/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;

/**
 * <P>
 * AuditableEntityDAO.java, an extension of EntityDAO.java.
 * <P>
 * A DAO Class meant to represent an object in the database which is auditable;
 * that is, carry extra information about that object in the object and the
 * database.
 *
 * @author thickerson
 *
 *
 */
public abstract class AuditableEntityDAO<T extends EntityBean> extends EntityDAO<T> {
    /**
     * Should the name of a query which refers to a SQL command of the following
     * form:
     *
     * <code>
     * 	SELECT t.*
     * 	FROM tableName t, study s
     * 	WHERE t.study_id=s.study_id
     * 	AND (s.study_id=? or s.parent_study_id=?)
     * </code>
     */
    protected String findAllByStudyName;

    /**
     * status =1
     */
    protected String findAllActiveByStudyName;

    /**
     * Should the name of a query which refers to a SQL command of the following
     * form:
     *
     * <code>
     * 	SELECT t.*
     * 	FROM tableName t, study s
     * 	WHERE t.id=?
     * 		AND t.study_id=s.study_id
     * 		AND (s.study_id=? or s.parent_study_id=?)
     * </code>
     */
    protected String findByPKAndStudyName;

    public AuditableEntityDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        // logger.info("digester name set to " + digesterName);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        // logger.info("digester null? " + (digester == null));
    }

    /*
     * public AuditableEntityBean getEntityAuditInformation(HashMap hm) {
     * AuditableEntityBean aeb = new AuditableEntityBean(); //grab the required
     * information from the table //so that we don't have to repeat this in
     * every single dao Date dateCreated = (Date) hm.get("date_created"); Date
     * dateUpdated = (Date) hm.get("date_updated"); Integer statusId = (Integer)
     * hm.get("status_id"); Integer ownerId = (Integer) hm.get("owner_id");
     * Integer updateId = (Integer) hm.get("update_id");
     *
     * aeb.setCreatedDate(dateCreated); aeb.setUpdatedDate(dateUpdated);
     * aeb.setStatus(Status.get(statusId.intValue()));
     * aeb.setOwnerId(ownerId.intValue());
     * aeb.setUpdaterId(updateId.intValue()); return aeb; }
     */

    public abstract void setTypesExpected();

    /**
     * Note: The subclass must define findAllByStudyName before calling this
     * method. Otherwise an empty array will be returned.
     *
     * @param study
     *            The study to which the entities belong.
     * @return An array containing all the entities which belong to
     *         <code>study</code>.
     */
    public ArrayList<T> findAllByStudy(StudyBean study) {
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());
        return executeFindAllQuery(findAllByStudyName, variables);
    }

    public ArrayList<T> findAllActiveByStudy(StudyBean study) {
        HashMap<Integer, Object> variables = variables(study.getId(), study.getId());
        return executeFindAllQuery(findAllActiveByStudyName, variables);
    }

    /**
     * Note: The subclass must define findByPKAndStudyName before calling this
     * method. Otherwise an inactive AuditableEntityBean will be returned.
     *
     * @param id
     *            The primary key of the AuditableEntity which is sought.
     * @param study
     *            The study to which the entity belongs.
     * @return The entity which belong to <code>study</code> and has primary
     *         key <code>id</code>.
     */
    public T findByPKAndStudy(int id, StudyBean study) {
        HashMap<Integer, Object> variables = variables(id, study.getId(), study.getId());
        ArrayList<T> rows = executeFindAllQuery(findByPKAndStudyName, variables);
        T result = null;
        if(rows.size() > 0) {
        	result = rows.get(0);
        } else {
        	result = emptyBean();
        }
        return result;
    }

    public void setEntityAuditInformation(AuditableEntityBean aeb, HashMap<String, Object> hm) {
        // grab the required information from the table
        // so that we don't have to repeat this in every single dao
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Integer statusId = (Integer) hm.get("status_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        Integer updateId = (Integer) hm.get("update_id");
        
        UserAccountBean owner = getUserById(ownerId);
        UserAccountBean updater = getUserById(updateId);

        if (aeb != null) {
            aeb.setCreatedDate(dateCreated);
            aeb.setUpdatedDate(dateUpdated);
            //This was throwing a ClassCastException : BWP altered in 4/2009
           // aeb.setStatus(Status.get(statusId.intValue()));
            aeb.setStatus(Status.getFromMap(statusId));
            if(owner != null) {
            	aeb.setOwner(owner);
            }
            if(updater != null) {
            	aeb.setUpdater(updater);
            }
        }
    }

    /**
     * This method executes a "findAll-style" query. Such a query has two
     * characteristics:
     * <ol>
     * <li> The columns SELECTed by the SQL are all of the columns in the table
     * relevant to the DAO, and only those columns. (e.g., in StudyDAO, the
     * columns SELECTed are all of the columns in the study table, and only
     * those columns.)
     * <li> It returns multiple AuditableEntityBeans.
     * </ol>
     *
     * Note that queries which join two tables may be included in the definition
     * of "findAll-style" query, as long as the first criterion is met.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @param variables
     *            The set of variables used to populate the PreparedStatement;
     *            should be empty if none are needed.
     * @return An ArrayList of AuditableEntityBeans selected by the query.
     */
    public ArrayList<T> executeFindAllQuery(String queryName, HashMap<Integer, Object> variables) {
        ArrayList<T> answer = new ArrayList<>();

        if (queryName == null || queryName.trim().isEmpty()) {
            return answer;
        }
        
        setTypesExpected();
        
        String query = digester.getQuery(queryName);
        if (query == null || query.trim().isEmpty()) {
        	// TODO for backwards compatibility here is no error thrown but this should be changed in the future
        	logger.error("No query with name '%s' not found", queryName);
            return answer;
        }
        
        ArrayList<HashMap<String, Object>> alist = this.select(query, variables);
        answer.addAll(alist.stream().map(m -> (T) this.getEntityFromHashMap(m)).collect(Collectors.toList()));
        return answer;
    }

    /**
     * This method executes a "findAll-style" query which does not accept any
     * variables.
     *
     * @param queryName
     *            The name of the query which selects the AuditableEntityBeans.
     * @return An ArrayList of AuditableEntityBeans selected by the query.
     */
    public ArrayList<T> executeFindAllQuery(String queryName) {
        return executeFindAllQuery(queryName, new HashMap<>());
    }
    
    public HashMap<Integer, Object> variables(Object... variables) {
    	HashMap<Integer, Object> result = new HashMap<>();
    	
    	if(variables == null) {
    		variables = new Object[0];
    	}
    	
    	Arrays.asList(variables);
    	for (int i = 0; i < variables.length; i++) {
    		result.put(i+1, variables[i]);
		}
    	return result;
    }
}