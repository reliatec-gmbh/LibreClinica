/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import htmlflow.HtmlFlow;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.xmlet.htmlapifaster.Tr;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Servlet for creating a table.
 *
 * @author Krikor Krumlian
 */
public class AuditUserActivityServlet extends SecureController {

    private static final long serialVersionUID = 1L;

    // ── URL parameter names for the HtmlFlow rendering path ──────────────────
    static final String PARAM_PAGE          = "page";
    static final String PARAM_MAX_ROWS      = "maxRows";
    static final String PARAM_SORT_PROP     = "sortProp";
    static final String PARAM_SORT_DIR      = "sortDir";
    static final String PARAM_FILTER_PREFIX = "filter.";

    private AuditUserLoginDao auditUserLoginDao;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET,
                    resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }
    }

    @Override
    protected void processRequest() throws Exception {
        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        // Use JMesa rendering only when LC_TABLE_RENDERING is explicitly set to "jmesa"
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            // Explicit JMesa rendering path
            AuditUserLoginTableFactory factory = new AuditUserLoginTableFactory();
            factory.setAuditUserLoginDao(getAuditUserLoginDao());
            String auditUserLoginHtml = factory.createTable(request, response).render();
            request.setAttribute("auditUserLoginHtml", auditUserLoginHtml);
            request.setAttribute("tableRenderingMode", "jmesa");
        } else {
            // HtmlFlow rendering path (default)
            int    page     = intParam(request, PARAM_PAGE,     1);
            int    maxRows  = intParam(request, PARAM_MAX_ROWS, 10);
            String sortProp = strParam(request, PARAM_SORT_PROP, "loginAttemptDate");
            String sortDir  = strParam(request, PARAM_SORT_DIR,  "desc");
            Map<String, String> filters = readFilters(request);
            String auditUserLoginHtml = renderAuditUserLoginTableHtml(
                    page, maxRows, sortProp, sortDir, filters);
            request.setAttribute("auditUserLoginHtml", auditUserLoginHtml);
            request.setAttribute("tableRenderingMode", "htmlflow");
        }
        forwardPage(Page.AUDIT_USER_ACTIVITY);
    }

    /**
     * Generic typed table renderer. Each column declares its header label and
     * a cell-renderer closure that writes directly into the HtmlFlow row element.
     *
     * @param columns column definitions (header + cell renderer)
     * @param data    rows to render
     * @param <T>     row bean type
     * @return rendered HTML string
     */
    private static <T> String renderTableHtml(List<ColumnDef<T>> columns, List<T> data) {
        StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrClass("jmesa")
                .table().attrClass("table")
                    .attrStyle("border-collapse:collapse")
                    .thead()
                        .tr().attrClass("header")
                        .of(tr -> columns.forEach(col -> tr.td().text(col.getHeader()).__()))
                        .__() // tr
                    .__() // thead
                    .tbody().attrClass("tbody")
                    .of(tbody -> {
                        int rowIndex = 1;
                        for (T item : data) {
                            String rowClass = (rowIndex % 2 == 1) ? "odd" : "even";
                            tbody.tr().attrClass(rowClass)
                                .of(tr -> columns.forEach(col -> col.getCellRenderer().accept(tr, item)))
                                .__(); // tr
                            rowIndex++;
                        }
                    })
                    .__() // tbody
                .__() // table
            .__(); // div
        return sw.toString();
    }

    /**
     * Renders the AuditUserLogin table using HtmlFlow.
     * Fetches a page of {@link AuditUserLoginBean} records from the DAO and
     * renders them with the generic typed table renderer.
     */
    private String renderAuditUserLoginTableHtml(
            final int page, final int maxRows,
            final String sortProp, final String sortDir,
            final Map<String, String> filters) {

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
                getAuditUserLoginDao().getWithFilterAndSort(filter, sort, rowStart, rowStart + maxRows);

        // Column definitions
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ColumnDef<AuditUserLoginBean>> columns = Arrays.asList(
            new ColumnDef<>("User Name",
                (tr, b) -> tr.td().text(nullSafe(b.getUserName())).__()),
            new ColumnDef<>("Attempt Date",
                (tr, b) -> tr.td().text(b.getLoginAttemptDate() != null
                        ? dateFmt.format(b.getLoginAttemptDate()) : "").__()),
            new ColumnDef<>("Status",
                (tr, b) -> tr.td().text(b.getLoginStatus() != null
                        ? b.getLoginStatus().toString() : "").__()),
            new ColumnDef<>("Details",
                (tr, b) -> tr.td().text(nullSafe(b.getDetails())).__()),
            new ColumnDef<>("Actions",
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

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    // ── Request-parameter helpers ─────────────────────────────────────────────

    private static int intParam(HttpServletRequest req, String name, int defaultValue) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String strParam(HttpServletRequest req, String name, String defaultValue) {
        String v = req.getParameter(name);
        return (v == null || v.trim().isEmpty()) ? defaultValue : v.trim();
    }

    /** Reads all request parameters whose name starts with {@value #PARAM_FILTER_PREFIX}. */
    @SuppressWarnings("unchecked")
    private static Map<String, String> readFilters(HttpServletRequest req) {
        final Map<String, String> filters = new LinkedHashMap<String, String>();
        // getParameterMap() returns raw Map in older servlet APIs — cast is safe
        final Map<String, String[]> params = (Map<String, String[]>) req.getParameterMap();
        for (Map.Entry<String, String[]> e : params.entrySet()) {
            if (e.getKey().startsWith(PARAM_FILTER_PREFIX)
                    && e.getValue().length > 0
                    && !e.getValue()[0].trim().isEmpty()) {
                filters.put(
                    e.getKey().substring(PARAM_FILTER_PREFIX.length()),
                    e.getValue()[0].trim());
            }
        }
        return filters;
    }

    // ── Infrastructure ────────────────────────────────────────────────────────

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        auditUserLoginDao = auditUserLoginDao != null
            ? auditUserLoginDao
            : (AuditUserLoginDao) SpringServletAccess.getApplicationContext(context)
                    .getBean("auditUserLoginDao");
        return auditUserLoginDao;
    }

    // ── Generic table-rendering support ──────────────────────────────────────
    // TODO: extract to a shared utility class once more tables are migrated.

    /**
     * Describes one column of a typed table: a header label and a closure that
     * writes the cell content for a given row bean into the HtmlFlow row element.
     *
     * @param <T> row bean type
     */
    private static class ColumnDef<T> {
        private final String header;
        private final BiConsumer<Tr<?>, T> cellRenderer;

        ColumnDef(String header, BiConsumer<Tr<?>, T> cellRenderer) {
            this.header = header;
            this.cellRenderer = cellRenderer;
        }

        String getHeader() { return header; }
        BiConsumer<Tr<?>, T> getCellRenderer() { return cellRenderer; }
    }
}
