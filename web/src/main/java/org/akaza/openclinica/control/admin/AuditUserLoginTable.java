package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableParams;
import java.util.function.Function;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.akaza.openclinica.lctable.LCTableUtil.*;


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
            UriComponentsBuilder.fromUriString("?" + (request.getQueryString() == null ? "" : request.getQueryString()))
                .build().getQueryParams();

        // Column definitions (same rendering as before)
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<LCTableColumnDef<AuditUserLoginBean>> columns = Arrays.asList(
            LCTableColumnDef.textCol("userName",
                "User Name",
                b -> nullSafe(b.getUserName())
            ),
            LCTableColumnDef.textCol("loginAttemptDate",
                "Attempt Date",
                b -> b.getLoginAttemptDate() != null ? dateFmt.format(b.getLoginAttemptDate()) : ""
            ),
            LCTableColumnDef.textCol("loginStatus",
                "Status",
                b -> b.getLoginStatus() != null ? b.getLoginStatus().toString() : ""
            ),
            LCTableColumnDef.textCol("details",
                "Details",
                b -> nullSafe(b.getDetails())
            ),
            new LCTableColumnDef<>("actions",
                "Actions",
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

        // fetchData: convert LCTableParams -> LCTableData by calling the DAO
        Function<LCTableParams, LCTableData<AuditUserLoginBean>> fetchData = p -> {
            // Build filter
            AuditUserLoginFilter filter = new AuditUserLoginFilter();
            for (Map.Entry<String, String> e : p.filters.entrySet()) {
                filter.addFilter(e.getKey(), e.getValue());
            }

            // Build sort: default to loginAttemptDate desc if no sort provided
            String sortProp = (p.sortProp == null || p.sortProp.isEmpty()) ? "loginAttemptDate" : p.sortProp;
            String sortDir  = (p.sortProp == null || p.sortProp.isEmpty()) ? "desc" : p.sortDir;
            AuditUserLoginSort sort = new AuditUserLoginSort();
            sort.addSort(sortProp, sortDir);

            int rowStart = p.page * p.maxRows;
            int rowEnd = rowStart + p.maxRows;

            List<AuditUserLoginBean> pageItems = auditUserLoginDao.getWithFilterAndSort(filter, sort, rowStart, rowEnd);
            int total = auditUserLoginDao.getCountWithFilter(filter);

            return new LCTableData<>(pageItems, total);
        };

        // Build and render the LCTable. Use the request URI as base path so links point back here.
        String entityPath = request.getRequestURI();
        String panelId = "userLogins-panel";
        LCTable<AuditUserLoginBean> table = new LCTable<>(entityPath, panelId, columns, fetchData);

        LCTableParams tableParams = new LCTableParams(params, table.getColumnNames());

        return table.render(tableParams);
    }

}

