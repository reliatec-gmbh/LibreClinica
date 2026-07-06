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

import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;


public class AuditUserLoginTable {

    private AuditUserLoginDao auditUserLoginDao;

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    final DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Defines the columns for the AuditUserLogin table.
     */
    final List<LCTableColumnDef<AuditUserLoginBean>> columns = Arrays.asList(
        textCol("userName", "User Name", 5, AuditUserLoginBean::getUserName),
        textCol("loginAttemptDate", "Attempt Date", 7,
            textFilter(TIMESTAMP_FILTER_FOR_HTML_VALIDATION, TIMESTAMP_FILTER_MESSAGE),
            AuditUserLoginBean::getLoginAttemptDate, dateFmt::format
        ),
        enumCol("loginStatus", "Status", 7,
            Arrays.asList(LoginStatus.values()), LoginStatus::toString, LoginStatus::name,    // filter definition
            AuditUserLoginBean::getLoginStatus, LoginStatus::toString                         // column data definition
        ),
        textCol("details","Details", 3, AuditUserLoginBean::getDetails),
        customTdCol("actions", "Actions", 4, NOT_SORTABLE, clearFilter(),
            AuditUserLoginBean::getUserAccountId,
            (td, userAccountId) ->
                td.of(linkIcon("View", "ViewUserAccount?userId=" + userAccountId + "&viewFull=yes", "images/bt_View.gif", "View"))
        )
    );

    /**
     * Fetches a page of {@link AuditUserLoginBean} records from the DAO
     * based on the provided {@link LCTableParams} and returns them as {@link LCTableData}
     */
    final Function<LCTableParams, LCTableData<AuditUserLoginBean>> fetchData = p -> {
        // Build filter
        AuditUserLoginFilter filter = new AuditUserLoginFilter();
        p.filters.forEach(filter::addFilter);

        // Build sort: default to loginAttemptDate desc if no sort provided
        boolean noSort = p.sortProp == null || p.sortProp.isEmpty();
        final var sortProp = noSort ? "loginAttemptDate" : p.sortProp;
        final var sortDir  = noSort ? "desc" : p.sortDir;
        AuditUserLoginSort sort = new AuditUserLoginSort();
        sort.addSort(sortProp, sortDir);

        // Fetch the page of data from the DAO
        int rowStart = p.page * p.maxRows;
        int rowEnd = rowStart + p.maxRows;
        final var pageItems = auditUserLoginDao.getWithFilterAndSort(filter, sort, rowStart, rowEnd);
        int total = auditUserLoginDao.getCountWithFilter(filter);

        return new LCTableData<>(pageItems, total);
    };

    /**
     * Renders the AuditUserLogin table using LCTable.
     * Fetches a page of {@link AuditUserLoginBean} records from the DAO and
     * renders them with the generic typed table renderer.
     */
    public String render(HttpServletRequest request) {
        String entityPath = request.getRequestURI();
        String resourcePath = request.getContextPath();
        LCTable<AuditUserLoginBean> table = new LCTable<>(entityPath, resourcePath, "userLogins", columns, fetchData);
        LCTableParams tableParams = new LCTableParams(request.getQueryString(), table);
        return table.render(tableParams);
    }

}
