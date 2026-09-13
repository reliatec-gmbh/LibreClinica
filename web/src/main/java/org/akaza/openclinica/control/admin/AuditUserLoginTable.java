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
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.lctable.*;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;
import static org.akaza.openclinica.lctable.LCTableFilterDef.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.SafeUrl.url;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.akaza.openclinica.lctable.LCTableText.key;


public class AuditUserLoginTable {

    private final AuditUserLoginDao auditUserLoginDao;
    private final LCTable<AuditUserLoginBean> table;

    // constructor (takes DAO as parameter and initializes the LCTable with column definitions and fetchData method)
    public AuditUserLoginTable(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
        this.table = new LCTable<>("userLogins", COLUMNS, this::fetchData)
            .setRowTestAttributes(row -> java.util.Map.of("login", String.valueOf(row.getId())));
    }

    /**
     * Builds the id of an action link for a given row of this table.
     * {@link AuditUserLoginBean#getId()} (inherited from {@code AbstractMutableDomainObject}) is the audit-log
     * entry's own database-generated primary key -- distinct from {@link AuditUserLoginBean#getUserAccountId()},
     * which is a foreign key to the *viewed* user account. It is a stable, unique-per-row identifier, and thus a
     * much better fit for element ids than a row-number/index (as the legacy JSP-based table used), which shifts
     * whenever the table is paginated/sorted/filtered.
     */
    private static String actionId(String action, AuditUserLoginBean row) {
        if (row == null || row.getId() == null) {
            throw new IllegalArgumentException("row and row.getId() must not be null");
        }
        return "userLogins-" + action + "-" + row.getId();
    }

    // defines the configuration of columns for the AuditUserLogin table
    private static final List<LCTableColumnDef<AuditUserLoginBean>> COLUMNS = Arrays.asList(
        textCol("userName",         key("username2"),    "user-name", 5, AuditUserLoginBean::getUserName),
        textCol("loginAttemptDate", key("attempt_date"), "login-attempt-date", 7,
            textFilter(TIMESTAMP_FILTER_FOR_HTML_VALIDATION, TIMESTAMP_FILTER_MESSAGE),
            AuditUserLoginBean::getLoginAttemptDate, LCTableUtil::timestampToString
        ),
        enumCol("loginStatus",      key("status"),       "login-status", 7,
            AuditUserLoginBean::getLoginStatus, Arrays.asList(LoginStatus.values()), LoginStatus::toString, LoginStatus::name
        ),
        textCol("details",          key("details"),      "details", 3, AuditUserLoginBean::getDetails),

        customTdColWithContext("actions", key("actions"), "actions", 4, NOT_SORTABLE, clearFilter(),
            AuditUserLoginBean::getUserAccountId,
            (td, row, context) ->
                td.of(actionLink(actionId("view", row), context.words.getString("view"),
                    url("ViewUserAccount").param("userId", row.getUserAccountId()).param("viewFull", "yes"), "bt_View.gif", "view"))
        )
    );

    // fetches a page of AuditUserLoginBean records from the DAO based on the provided LCTableParams and returns them as LCTableData
    private LCTableData<AuditUserLoginBean> fetchData(LCTableParams p) {
        AuditUserLoginFilter filter = new AuditUserLoginFilter();
        // Here we need to escape SQL LIKE wildcards as a workaround for a bug in AuditUserLoginFilter, which does
        // not do it. Without this workaround, the following would just be: 'p.filters.forEach(filter::addFilter);'
        final Set<String> freeTextColumns = Set.of("userName", "details");
        p.filters.forEach((property, value) ->
            filter.addFilter(property, freeTextColumns.contains(property) ? escapeSqlLikeWildcards(value) : value)
        );

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
        return this.table.render(request.getRequestURI(), params, request.getContextPath(), LocaleResolver.getLocale(request));
    }

}
