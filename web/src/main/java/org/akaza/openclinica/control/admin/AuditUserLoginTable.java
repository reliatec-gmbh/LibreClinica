/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.lctable.*;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableFilterDef.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


public class AuditUserLoginTable {

    private final AuditUserLoginDao auditUserLoginDao;
    private final LCTable<AuditUserLoginBean> table;

    // constructor (takes DAO as parameter and initializes the LCTable with column definitions and fetchData method)
    public AuditUserLoginTable(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
        this.table = new LCTable<>("userLogins", COLUMNS, this::fetchData);
    }

    // defines the configuration of columns for the AuditUserLogin table
    private static final List<LCTableColumnDef<AuditUserLoginBean>> COLUMNS = Arrays.asList(
        textCol("userName", "User Name", 5, AuditUserLoginBean::getUserName),
        textCol("loginAttemptDate", "Attempt Date", 7,
            textFilter(TIMESTAMP_FILTER_FOR_HTML_VALIDATION, TIMESTAMP_FILTER_MESSAGE),
            AuditUserLoginBean::getLoginAttemptDate, LCTableUtil::utcTimestampToString
        ),
        enumCol("loginStatus", "Status", 7,
            AuditUserLoginBean::getLoginStatus, Arrays.asList(LoginStatus.values()), LoginStatus::toString, LoginStatus::name
        ),
        textCol("details", "Details", 3, AuditUserLoginBean::getDetails),
        customTdCol("actions", "Actions", 4, NOT_SORTABLE, clearFilter(),
            AuditUserLoginBean::getUserAccountId,
            (td, userAccountId) ->
                td.of(linkIcon("View", "ViewUserAccount?userId=" + userAccountId + "&viewFull=yes", "images/bt_View.gif", "View"))
        )
    );

    // fetches a page of AuditUserLoginBean records from the DAO based on the provided LCTableParams and returns them as LCTableData
    private LCTableData<AuditUserLoginBean> fetchData(LCTableParams p) {
        // Build filter
        AuditUserLoginFilter filter = new AuditUserLoginFilter();
        p.filters.forEach(filter::addFilter);

        // Build sort: default to loginAttemptDate desc if no sort provided
        boolean noSort = p.sortProp == null || p.sortProp.isEmpty();
        final var sortProp = noSort ? "loginAttemptDate" : p.sortProp;
        final var sortDir = noSort ? "desc" : p.sortDir;
        AuditUserLoginSort sort = new AuditUserLoginSort();
        sort.addSort(sortProp, sortDir);

        // Fetch the page of data from the DAO
        int rowStart = p.page * p.maxRows;
        int rowEnd = rowStart + p.maxRows;
        final var pageItems = auditUserLoginDao.getWithFilterAndSort(filter, sort, rowStart, rowEnd);
        int total = auditUserLoginDao.getCountWithFilter(filter);

        return new LCTableData<>(pageItems, total);
    }

    // rendering method putting all pieces together
    public String render(HttpServletRequest request) {
        final LCTableParams params = new LCTableParams(request.getQueryString(), this.table);
        return this.table.render(request.getRequestURI(), params, request.getContextPath());
    }

}
