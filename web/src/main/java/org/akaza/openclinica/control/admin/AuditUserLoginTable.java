package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.lctable.LCTableColumnDef;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.akaza.openclinica.lctable.LCTable.PARAM_MAX_ROWS;
import static org.akaza.openclinica.lctable.LCTable.PARAM_PAGE;
import static org.akaza.openclinica.lctable.LCTable.PARAM_SORT_DIR;
import static org.akaza.openclinica.lctable.LCTable.PARAM_SORT_PROP;
import static org.akaza.openclinica.lctable.LCTable.intParam;
import static org.akaza.openclinica.lctable.LCTable.nullSafe;
import static org.akaza.openclinica.lctable.LCTable.readFilters;
import static org.akaza.openclinica.lctable.LCTable.renderTableHtml;
import static org.akaza.openclinica.lctable.LCTable.strParam;

public class AuditUserLoginTable {

    private AuditUserLoginDao auditUserLoginDao;

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    /**
     * Renders the AuditUserLogin table using HtmlFlow.
     * Fetches a page of {@link AuditUserLoginBean} records from the DAO and
     * renders them with the generic typed table renderer.
     */
    public String render(HttpServletRequest request) {
        final int    page     = intParam(request, PARAM_PAGE,     1);
        final int    maxRows  = intParam(request, PARAM_MAX_ROWS, 10);
        final String sortProp = strParam(request, PARAM_SORT_PROP, "loginAttemptDate");
        final String sortDir  = strParam(request, PARAM_SORT_DIR,  "desc");
        final Map<String, String> filters = readFilters(request);

        // Build filter
        AuditUserLoginFilter filter = new AuditUserLoginFilter();
        for (Map.Entry<String, String> e : filters.entrySet()) {
            filter.addFilter(e.getKey(), e.getValue());
        }

        // Build sort
        AuditUserLoginSort sort = new AuditUserLoginSort();
        sort.addSort(sortProp, sortDir);

        // Fetch page
        int rowStart = (page - 1) * maxRows;
        List<AuditUserLoginBean> data =
                auditUserLoginDao.getWithFilterAndSort(filter, sort, rowStart, rowStart + maxRows);

        // Column definitions
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<LCTableColumnDef<AuditUserLoginBean>> columns = Arrays.asList(
            new LCTableColumnDef<>("User Name",
                (tr, b) -> tr.td().text(nullSafe(b.getUserName())).__()),
            new LCTableColumnDef<>("Attempt Date",
                (tr, b) -> tr.td().text(b.getLoginAttemptDate() != null
                        ? dateFmt.format(b.getLoginAttemptDate()) : "").__()),
            new LCTableColumnDef<>("Status",
                (tr, b) -> tr.td().text(b.getLoginStatus() != null
                        ? b.getLoginStatus().toString() : "").__()),
            new LCTableColumnDef<>("Details",
                (tr, b) -> tr.td().text(nullSafe(b.getDetails())).__()),
            new LCTableColumnDef<>("Actions",
                (tr, b) -> {
                    if (b.getUserAccountId() != null) {
                        tr.td()
                          .a().attrHref("ViewUserAccount?userId=" + b.getUserAccountId() + "&viewFull=yes")
                              .img().attrSrc("images/bt_View.gif").attrAlt("View").attrTitle("View").__()
                          .__()  // a
                        .__();   // td
                    } else {
                        tr.td().__();
                    }
                })
        );

        return renderTableHtml(columns, data);
    }

}

