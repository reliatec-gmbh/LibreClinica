/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.core;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import oracle.jdbc.pool.OracleDataSource;

/**
 * Utility which handles connection and login, as prompted by LibreClinica control servlets.
 * Updated August 2004 to better handle connection pooling.
 * Updated again in August 2004 to support SQL Statements in XML.
 * Will require a change to all control servlets; new SessionManagers will have to be stored in session,
 * since we don't want to take in all this information every time a user clicks on a new servlet.
 *
 * @author Tom Hickerson
 * @author Jun Xu
 */
public class SessionManager {

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private UserAccountBean ub;

    //TODO: We no longer support oracle, it should be removed in a future
    private OracleDataSource ods;

    private DataSource ds;

    //TODO: this is a hack needs to be refactored
    private static DataSource staticDataSource;

    /**
     * Constructor of SessionManager
     *
     * @param userFromSession user from session
     * @param userName username
     * @throws SQLException SQL exception
     */
    public SessionManager(UserAccountBean userFromSession, String userName) throws SQLException {
        setupDataSource();
        setupUser(userFromSession, userName);
    }

    /**
     * Constructor of SessionManager
     *
     * @param userFromSession user from session
     * @param userName username
     * @param applicationContext application context
     * @throws SQLException SQL exception
     */
    public SessionManager(UserAccountBean userFromSession, String userName, ApplicationContext applicationContext) throws SQLException {
        this.ds = (DataSource) applicationContext.getBean("dataSource");
        staticDataSource = ds;
        setupUser(userFromSession, userName);
    }

    public SessionManager( ApplicationContext applicationContext) {
        this.ds = (DataSource) applicationContext.getBean("dataSource");
        staticDataSource = ds;
    }

    public void setupUser(UserAccountBean userFromSession, String userName) {
        if (userFromSession == null ||
            userFromSession.getName() == null ||
            userFromSession.getName().trim().isEmpty()) {

            // Load a new user account bean from database
            UserAccountDAO userDao = new UserAccountDAO(ds);
            if (userName == null) {
                userName = "";
            }
            ub = userDao.findByUserName(userName);
            logger.debug("User  : {} , email address : {} Logged In ", ub.getName(), ub.getEmail());
        } else {
            ub = userFromSession;
        }
    }
    
    public void setupDataSource() {
        try {
            Context ctx = new InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            String dbName = CoreResources.getField("dataBase");
            if ("oracle".equals(dbName)) {
                // logger.debug("looking up oracle...");
                ds = (DataSource) env.lookup("SQLOracle");
            } else if ("postgres".equals(dbName)) {
                // logger.debug("looking up postgres...");
                ds = (DataSource) env.lookup("SQLPostgres");
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            logger.warn("This is :" + ne.getMessage() + " when we tried to get the connection");
        }
    }

    public SessionManager() {
        setupDataSource();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public UserAccountBean getUserBean() {
        return ub;
    }

    public void setUserBean(UserAccountBean user) {
        this.ub = user;
    }

    public DataSource getDataSource() {
        return ds;
    }

    // added 08-04-2004 by tbh, supporting Oracle 10g
    // TODO: we no longer support Oracle, it should be removed in a future
    public OracleDataSource getOracleDataSource() {
        return ods;
    }

    public static DataSource getStaticDataSource() {
        return staticDataSource;
    }

}