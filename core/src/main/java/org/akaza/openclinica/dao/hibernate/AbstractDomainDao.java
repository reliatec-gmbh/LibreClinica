/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.DomainObject;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractDomainDao<T extends DomainObject> {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private HibernateTemplate hibernateTemplate;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findById(Integer id) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.id = :id";
        Query<T> q = getCurrentSession().createQuery(query);
        q.setParameter("id", id);
        return q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<T> findAll() {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do";
        Query<T> q = getCurrentSession().createQuery(query);
        return new ArrayList<T>(q.list());
    }
    
    @SuppressWarnings("unchecked")
	public T findByOcOID(String OCOID){
    	 getSessionFactory().getStatistics().logSummary();
         String query = "from " + getDomainClassName() + " do  where do.oc_oid = :oc_oid";
         Query<T> q = getCurrentSession().createQuery(query);
         q.setParameter("oc_oid", OCOID);
         return q.uniqueResult();
    }

    @Transactional
    public T saveOrUpdate(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        getCurrentSession().saveOrUpdate(domainObject);
        return domainObject;
    }

    @Transactional
    public Serializable save(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        return getCurrentSession().save(domainObject);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findByColumnName(Object id, String key) {
        String query = "from " + getDomainClassName() + " do where do." + key + " = :key_value";
        Query<T> q = getCurrentSession().createQuery(query);
        q.setParameter("key_value", id);
        return q.uniqueResult();
    }    
    
    public Long count() {
        return (Long) getCurrentSession().createQuery("select count(*) from " + domainClass().getName()).uniqueResult();
    }

    public SessionFactory getSessionFactory() {
        return hibernateTemplate.getSessionFactory();
    }

    /**
     * @return Session Object
     */
    public Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public Session getCurrentSession(String schema) {
        Session session = getSessionFactory().getCurrentSession();

        if (StringUtils.isNotEmpty(schema)) {
            SessionImpl sessionImpl = (SessionImpl) session;
            try {
                String currentSchema = sessionImpl.connection().getSchema();
                if (!schema.equals(currentSchema)) {
                    sessionImpl.connection().setSchema(schema);
                    CoreResources.tenantSchema.set(schema);
                    //CoreResources.setSchema(sessionImpl.connection());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return session;
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

}
