package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.lctable.*;

import static org.akaza.openclinica.lctable.LCTableColumnDef.*;

import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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
        textCol("userName", "User Name", AuditUserLoginBean::getUserName),
        textCol("loginAttemptDate", "Attempt Date", AuditUserLoginBean::getLoginAttemptDate, dateFmt::format),
        enumCol("loginStatus", "Status",
            Arrays.stream(LoginStatus.values()).map(LoginStatus::name).collect(Collectors.toList()), Function.identity(),
            AuditUserLoginBean::getLoginStatus, LoginStatus::toString
        ),
        textCol("details","Details", AuditUserLoginBean::getDetails),
        customTdCol("actions","Actions",null, AuditUserLoginBean::getUserAccountId,
            (td, userAccountId) ->
                td.a().attrHref("ViewUserAccount?userId=" + userAccountId + "&viewFull=yes")
                    .img().attrSrc("images/bt_View.gif").attrAlt("View").attrTitle("View").__()
                .__()
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
        LCTable<AuditUserLoginBean> table = new LCTable<>(entityPath, "userLogins", columns, fetchData);
        LCTableParams tableParams = new LCTableParams(request.getQueryString(), table);
        return table.render(tableParams);
    }

}

