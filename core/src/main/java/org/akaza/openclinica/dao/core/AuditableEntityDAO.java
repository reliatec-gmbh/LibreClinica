/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;

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
	
	private UserAccountDAO uadao;

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
    
    public UserAccountBean getUserById(int id) {
		UserAccountBean result = getUserAccountDAO().findByPK(id, false);
        if(result == null) {
        	String msg = String.format("No UserAccountBean found with id '%d'", id);
        	logger.debug(msg);
        	throw new RuntimeException(msg);
        }
        return result;
    }
    
    public UserAccountDAO getUserAccountDAO() {
    	if(uadao == null) {
    		uadao = new UserAccountDAO(ds);
    	}
    	return uadao;
    }
}