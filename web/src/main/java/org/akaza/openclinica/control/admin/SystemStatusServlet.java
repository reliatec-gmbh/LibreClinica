/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import java.io.PrintWriter;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.web.InsufficientPermissionException;

// allows both deletion and restoration of a study user role

public class SystemStatusServlet extends SecureController {

    private static final long serialVersionUID = 1722670001851393612L;
    private DatabaseChangeLogDao databaseChangeLogDao;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }

    @Override
    protected void processRequest() throws Exception {

        Long databaseChangelLogCount = getDatabaseChangeLogDao().count();
        String applicationStatus = "OK";
        if (session.getAttribute("ome")!=null) {
            applicationStatus = "OutOfMemory.";
        }
//        request.setAttribute("databaseChangeLogCount", String.valueOf(databaseChangelLogCount));
//        request.setAttribute("applicationStatus", applicationStatus);
//        forwardPage(Page.SYSTEM_STATUS);

        PrintWriter out = response.getWriter();
        out.println(applicationStatus);
        out.println(String.valueOf(databaseChangelLogCount));
    }

    public DatabaseChangeLogDao getDatabaseChangeLogDao() {
        databaseChangeLogDao =
            this.databaseChangeLogDao != null ? databaseChangeLogDao : (DatabaseChangeLogDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "databaseChangeLogDao");
        return databaseChangeLogDao;
    }

    public void setDatabaseChangeLogDao(DatabaseChangeLogDao databaseChangeLogDao) {
        this.databaseChangeLogDao = databaseChangeLogDao;
    }
}
