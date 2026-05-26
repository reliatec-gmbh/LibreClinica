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
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Renders a generic two-dimensional table using HtmlFlow.
     *
     * @param columnNames header labels
     * @param rows        each inner list is one row; values must match column order
     * @return rendered HTML string
     */
    private static String renderTableHtml(List<String> columnNames, List<List<String>> rows) {
        StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrClass("jmesa")
                .table().attrClass("table").attrId("debugParams")
                    .attrStyle("border-collapse:collapse")
                    .thead()
                        .tr().attrClass("header")
                        .of(tr -> columnNames.forEach(col -> tr.td().text(col).__()))
                        .__() // tr
                    .__() // thead
                    .tbody().attrClass("tbody")
                    .of(tbody -> {
                        final int[] i = {0};
                        for (List<String> row : rows) {
                            i[0]++;
                            final int rowIndex = i[0];
                            tbody
                                .tr().attrClass(rowIndex % 2 == 1 ? "odd" : "even")
                                .of(tr -> row.forEach(cell -> tr.td().text(cell).__()))
                                .__(); // tr
                        }
                    })
                    .__() // tbody
                .__() // table
            .__(); // div
        return sw.toString();
    }

    /**
     * Renders the AuditUserLogin table using HtmlFlow.
     * <p>
     * Currently produces a debug table showing the parsed URL parameters.
     * Real data rendering will replace this in a subsequent step.
     */
    private String renderAuditUserLoginTableHtml(
            final int page, final int maxRows,
            final String sortProp, final String sortDir,
            final Map<String, String> filters) {

        // Collect all parameters for display
        final Map<String, String> allParams = new LinkedHashMap<String, String>();
        allParams.put(PARAM_PAGE,      String.valueOf(page));
        allParams.put(PARAM_MAX_ROWS,  String.valueOf(maxRows));
        allParams.put(PARAM_SORT_PROP, sortProp);
        allParams.put(PARAM_SORT_DIR,  sortDir);
        for (Map.Entry<String, String> e : filters.entrySet()) {
            allParams.put(PARAM_FILTER_PREFIX + e.getKey(), e.getValue());
        }

        List<List<String>> rows = allParams.entrySet().stream()
                .map(e -> Arrays.asList(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return renderTableHtml(Arrays.asList("Parameter", "Value"), rows);   // TODO: localization
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
}
