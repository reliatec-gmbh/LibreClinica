/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.dao.core;

import org.quartz.JobPersistenceException;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

/**
 * Created by yogi on 2/10/17.
 */
public class MultiSchemaJobStoreTx extends LocalDataSourceJobStore {
    @Override
    protected Connection getNonManagedTXConnection() throws JobPersistenceException {
        Connection conn = this.getConnection();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            String schema = (String) requestAttributes.getRequest().getAttribute(CURRENT_TENANT_ID);
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.execute("set search_path to '" + schema + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return conn;
    }
}
