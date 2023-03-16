/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class DatabaseChangeLogDao {

    private SessionFactory sessionFactory;

    public String getDomainClassName() {
        return domainClass().getName();
    }

    public Class<DatabaseChangeLogBean> domainClass() {
        return DatabaseChangeLogBean.class;
    }

    public ArrayList<DatabaseChangeLogBean> findAll() {
        String query = "from " + getDomainClassName() + " dcl order by dcl.id desc ";
        Query<DatabaseChangeLogBean> q = getCurrentSession().createQuery(query, DatabaseChangeLogBean.class);
        return new ArrayList<>(q.list());
    }

    // TODO update to CriteriaQuery 
    @SuppressWarnings("deprecation")
    public DatabaseChangeLogBean findById(String id, String author, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.id = :id and do.author = :author and do.fileName = :fileName ";
        Query<DatabaseChangeLogBean> q = getCurrentSession().createQuery(query, DatabaseChangeLogBean.class);
        q.setString("id", id);
        q.setString("author", author);
        q.setString("fileName", fileName);
        return q.uniqueResult();
    }

    public Long count() {
        return (Long) getCurrentSession().createQuery("select count(*) from " + domainClass().getName()).uniqueResult();
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory
     *            the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return Session Object
     */
    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
