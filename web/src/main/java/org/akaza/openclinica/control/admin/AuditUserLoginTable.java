package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.LCTable.renderTableHtml;


public class AuditUserLoginTable {

    private AuditUserLoginDao auditUserLoginDao;

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    /**
     * Renders the AuditUserLogin table using LCTable.
     * Fetches a page of {@link AuditUserLoginBean} records from the DAO and
     * renders them with the generic typed table renderer.
     */
    public String render(HttpServletRequest request) {
        MultiValueMap<String, String> params =
            UriComponentsBuilder.fromUriString("?" + request.getQueryString())
                .build().getQueryParams();

        final int    page     = intParam(params, PARAM_PAGE,     1);
        final int    maxRows  = intParam(params, PARAM_MAX_ROWS, 10);
        final String sortProp = strParam(params, PARAM_SORT_PROP, "loginAttemptDate");
        final String sortDir  = strParam(params, PARAM_SORT_DIR,  "desc");
        final Map<String, String> filters = readFilters(params);

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
            LCTableColumnDef.textCol(
                "User Name",
                b -> nullSafe(b.getUserName())
            ),
            LCTableColumnDef.textCol(
                "Attempt Date",
                b -> b.getLoginAttemptDate() != null ? dateFmt.format(b.getLoginAttemptDate()) : ""
            ),
            LCTableColumnDef.textCol(
                "Status",
                b -> b.getLoginStatus() != null ? b.getLoginStatus().toString() : ""
            ),
            LCTableColumnDef.textCol(
                "Details",
                b -> nullSafe(b.getDetails())
            ),
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

