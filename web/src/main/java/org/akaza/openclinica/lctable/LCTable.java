package org.akaza.openclinica.lctable;

import htmlflow.HtmlFlow;
import org.xmlet.htmlapifaster.Tbody;
import org.xmlet.htmlapifaster.Thead;
import org.xmlet.htmlapifaster.Tr;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class LCTable {

    // -- URL parameter names --------------------------------------------------

    public static final String PARAM_PAGE          = "page";
    public static final String PARAM_MAX_ROWS      = "maxRows";
    public static final String PARAM_SORT_PROP     = "sortProp";
    public static final String PARAM_SORT_DIR      = "sortDir";
    public static final String PARAM_FILTER_PREFIX = "filter.";

    // -- Generic request-parameter helpers ------------------------------------

    public static String nullSafe(String s) {
        return s != null ? s : "";
    }

    public static int intParam(HttpServletRequest req, String name, int defaultValue) {
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

    public static String strParam(HttpServletRequest req, String name, String defaultValue) {
        String v = req.getParameter(name);
        return (v == null || v.trim().isEmpty()) ? defaultValue : v.trim();
    }

    /** Reads all request parameters whose name starts with {@value #PARAM_FILTER_PREFIX}. */
    @SuppressWarnings("unchecked")
    public static Map<String, String> readFilters(HttpServletRequest req) {
        final Map<String, String> filters = new LinkedHashMap<>();
        // getParameterMap() returns raw Map in older servlet APIs — cast is safe
        final Map<String, String[]> params = req.getParameterMap();
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

    // -- Generic typed table renderer -----------------------------------------

    private static <T> void renderColumnNames(Tr<?> tr, List<LCTableColumnDef<T>> columns) {
        columns.forEach(col -> tr.td().text(col.getHeader()).__());
        // should actually use th instead of td in the table header:
        // however, use td like jmesa for the moment (otherwise styling with jmesa CSS does not work)
    }

    private static <T> void renderTableHeader(Thead<?> thead, List<LCTableColumnDef<T>> columns) {
        Tr<?> tr = thead.tr().attrClass("header");
        renderColumnNames(tr, columns);
        tr.__();
    }

    private static <T> void renderTableBody(Tbody<?> tbody, List<LCTableColumnDef<T>> columns, List<T> data) {
        tbody.attrClass("tbody");
        IntStream.range(0, data.size()).forEach(i -> {
            T item = data.get(i);
            String rowClass = ((i+1) % 2 == 0) ? "even" : "odd";    // use (i+1) to start from 1 for class assignment
            Tr<?> tr = tbody.tr().attrClass(rowClass);
            columns.forEach(col -> col.getCellRenderer().accept(tr, item));
            tr.__();
        });
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
    public static <T> String renderTableHtml(List<LCTableColumnDef<T>> columns, List<T> data) {
        StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrClass("jmesa")
            .table().attrClass("table")
            .attrStyle("border-collapse:collapse")
            .thead()
                .of(thead -> renderTableHeader(thead, columns))
            .__() // thead
            .tbody()
                .of(tbody -> renderTableBody(tbody, columns, data))
            .__() // tbody
            .__() // table
            .__(); // div
        return sw.toString();
    }

}
