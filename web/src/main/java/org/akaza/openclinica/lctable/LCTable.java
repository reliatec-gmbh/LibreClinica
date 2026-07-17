/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableParams.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;

import htmlflow.HtmlFlow;
import org.xmlet.htmlapifaster.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

public class LCTable<T>  {

    final String tableName;     // name of the table (used for generating unique IDs and classes)
    final String panelId;       // ID of the panel element to target with HTMX requests (e.g. "books-panel")
    final List<LCTableColumnDef<T>> columns;
    final Function<LCTableParams, LCTableData<T>> fetchData;

    // constants to control table behaviour
    static final boolean HIDE_PAGINATION_TOOLS_FOR_SINGLE_PAGE_TABLE = false;


    public LCTable(String tableName, List<LCTableColumnDef<T>> columns, Function<LCTableParams, LCTableData<T>> fetchData) {
        this.tableName    = tableName;
        this.panelId      = tableName + "-panel";   // Generate panel ID based on table name
        this.columns      = columns;
        this.fetchData    = fetchData;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnNames() {
        return columns.stream().map(col -> col.columnName).collect(Collectors.toList());
    }

    // -- HTMX attribute names (use constants to avoid repeating string literals)
    public static final String HX_GET = "hx-get";
    public static final String HX_TARGET = "hx-target";
    public static final String HX_SWAP = "hx-swap";
    public static final String HX_PUSH_URL = "hx-push-url";
    public static final String HX_TRIGGER = "hx-trigger";
    public static final String HX_INCLUDE = "hx-include";

    // -- Generic typed table renderer -----------------------------------------

    private void renderColumnNames(Tr<?> tr, LCTableContext<T> ctx) {
        columns.forEach(col -> renderColumnName(tr, col, ctx));
    }

    /**
     * Render a clickable header cell that toggles sorting state for the column.
     * Clicking cycles through: no sort → ascending → descending → no sort.
     */
    private void renderColumnName(Tr<?> tr, LCTableColumnDef<T> col, LCTableContext<T> ctx) {
        final String widthStyle = "width: " + col.columnWidth + "rem";
        if (!col.isSortable()) {
            // Non-sortable column: just render the header text without a link
            tr.th().attrStyle(widthStyle).text(col.columnDisplayName).__();
        } else {
            boolean isSorted = col.columnName.equals(ctx.sortProp);
            final String currentSortDir = isSorted ? ctx.sortDir : null;
            final String nextSortDir = currentSortDir == null ? "asc" : (currentSortDir.equals("asc") ? "desc" : null);
            final String href = urlForSort(ctx.entityPath, ctx, col.columnName, nextSortDir);
            // Render the header cell with a link that triggers sorting via HTMX
            tr.th().attrStyle(widthStyle)
                .a().attrClass("sort-header-link")
                .attrHref(href).of(hxGetAttrs(href, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
                .of(a -> {
                    a.span().attrClass("sort-header-text").text(col.columnDisplayName).__();
                    // Show sort indicator if sorted
                    if (isSorted && currentSortDir != null) {
                        String imgSrc = ctx.resourcePath + (currentSortDir.equals("asc") ? "/images/table/sortAsc.gif" : "/images/table/sortDesc.gif");
                        a.img().attrClass("sort-indicator").addAttr("src", imgSrc).attrAlt(currentSortDir).__();
                    }
                }).__();
        }
    }

    private void renderToolbar(Tr<?> tr, LCTableContext<T> ctx) {
        tr.td().attrClass("toolbar").attrColspan(columns.size())
            .div().attrStyle("display:flex;justify-content:space-between;align-items:center")
            .of(container -> {
                // Left: page navigation
                container.nav().of(nav -> buildPageNavigation(nav, ctx)).__();
                // Right: page-size selector
                container.div().of(div -> buildMaxRowsSelector(div, ctx)).__();
            }).__();
    }

    private void renderFilters(Tr<?> tr, LCTableContext<T> ctx) {
        // Render a filter input for each column
        columns.forEach(col -> {
            if (col.isFilterable()) {
                col.filterDef.renderFilter(tr, ctx, col, this);
            } else {
                tr.td().__();   // insert empty <td> to fill column cell when there is no filter
            }
        });
    }

    private void renderTableHeader(Thead<?> thead, LCTableContext<T> ctx) {
        thead.tr().attrClass("header").of(tr -> renderToolbar(tr, ctx)).__();
        thead.tr().attrClass("header").of(tr -> renderColumnNames(tr, ctx)).__();
        thead.tr().attrClass("filter").of(tr -> renderFilters(tr, ctx)).__();
    }

    private void renderTableBody(Tbody<?> tbody, List<T> data) {
        tbody.attrClass("tbody");
        IntStream.range(0, data.size()).forEach(i -> {
            T item = data.get(i);
            String rowClass = ((i+1) % 2 == 0) ? "even" : "odd";    // use (i+1) to start from 1 for class assignment
            Tr<?> tr = tbody.tr().attrClass(rowClass);
            columns.forEach(col -> col.cellRenderer.accept(tr, item));
            tr.__();
        });
    }

    /**
     * Generic typed table renderer. Each column declares its header label and
     * a cell-renderer closure that writes directly into the HtmlFlow row element.
     *
     * @param ctx the table context containing all pagination/sorting state and the data for the current page
     * @return rendered HTML string
     */
    private String renderTableHtml(LCTableContext<T> ctx) {
        final StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrId(panelId).attrClass("lctable")
            .addAttr("hx-ext", "morph")         // use 'idiomorph' extension for morphing the table content instead of replacing it
            .form().attrId(panelId + "-form")
            // Hidden inputs for filter submission. Page is reset to 1 when filtering (like search box).
            // Pagination buttons use their own URLs with all parameters, so this page value
            // doesn't affect them.
            .input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_PAGE).attrValue("1").__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_MAX_ROWS).attrValue(String.valueOf(ctx.maxRows)).__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_SORT_PROP).attrValue(ctx.sortProp).__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_SORT_DIR).attrValue(ctx.sortDir).__()

            .table().attrId(panelId + "-table").attrClass("table").attrStyle("border-collapse:collapse")
            .thead().attrId(panelId + "-thead").of(thead -> renderTableHeader(thead, ctx)).__() // thead
            .tbody().attrId(panelId + "-tbody").attrClass("tbody").of(tbody -> renderTableBody(tbody, ctx.data.pageItems)).__() // tbody
            .tfoot().attrId(panelId + "-tfoot").of(tfoot -> renderTableFooter(tfoot, ctx)).__()
            .__() // table
            .__() // form
            .__(); // div
        return sw.toString();
    }

    /**
     * Main entry point for rendering the table.
     * Fetches the data for the current page using the provided fetchData function and renders the HTML table.
     *
     * @param params the parameters for fetching data (page number, page size, sorting, filters, etc.)
     * @return the rendered HTML string for the table
     */
    public String render(String entityPath, LCTableParams params, String resourcePath) {
        final LCTableContext<T> ctx = new LCTableContext<>(entityPath, params, fetchData, resourcePath);
        return renderTableHtml(ctx);
    }

    // -- Controls (pagination etc.) -------------------------------------------

    public void renderTableFooter(Tfoot<?> tfoot, LCTableContext<T> ctx) {
        long    from     = (long) ctx.page * ctx.maxRows + 1;
        long    to       = (long) ctx.page * ctx.maxRows + ctx.data.pageItems.size();

        Tfoot<?> footer = tfoot.attrClass("statusBar");
        Td<?> td = footer.tr().td().attrColspan(columns.size());
        final int count = ctx.data.totalCountWithFilter;
        td.text(count == 0 ? "No results." : format("Results %d-%d of %d.", from, to, count)).__();
        footer.__(); // div.table-footer
    }

    private void buildPageNavigation(Nav<?> nav, LCTableContext<T> ctx) {
        if (HIDE_PAGINATION_TOOLS_FOR_SINGLE_PAGE_TABLE && ctx.totalPages <= 1) return;

        final int page = ctx.page;
        final int total = ctx.totalPages;
        final int size = ctx.maxRows;
        final String sort = ctx.sortProp;
        final String dir = ctx.sortDir;

        nav.attrClass("toolbar");

        // « first
        pageBtn(nav, "«", url(ctx.entityPath, 0, size, sort, dir, ctx.filters), panelId, page == 0);
        // ‹ previous
        pageBtn(nav, "‹", url(ctx.entityPath, max(0, page - 1), size, sort, dir, ctx.filters), panelId, page == 0);
        // numbered slots / ellipsis
        for (LCTablePageSlot slot : ctx.slots) {
            if (slot.ellipsis()) {
                nav.span().attrClass("page-ellipsis").text("…").__();
            } else {
                String slotHref = url(ctx.entityPath, slot.page(), size, sort, dir, ctx.filters);
                nav.a().attrClass("page-btn" + (slot.current() ? " current" : ""))
                    .attrHref(slotHref).of(hxGetAttrs(slotHref, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
                    .text(String.valueOf(slot.page() + 1))
                    .__(); // a
            }
        }
        // › next
        pageBtn(nav, "›", url(ctx.entityPath, min(total - 1, page + 1), size, sort, dir, ctx.filters), panelId, page >= total - 1);
        // » last
        pageBtn(nav, "»", url(ctx.entityPath, total - 1, size, sort, dir, ctx.filters), panelId, page >= total - 1);

        nav.__(); // nav.pagination
    }

    /** Writes a single pagination button into the given {@code nav} element. */
    private void pageBtn(Nav<?> nav, String text, String href, String panelId, boolean disabled) {
        nav.a().attrClass("page-btn" + (disabled ? " disabled" : ""))
            .attrHref(href).of(hxGetAttrs(href, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
            .text(text)
            .__(); // a
    }

    /** Builds the page-size selector (maxRows) and appends it into the provided div. */
    private void buildMaxRowsSelector(Div<?> div, LCTableContext<T> ctx) {
        if (HIDE_PAGINATION_TOOLS_FOR_SINGLE_PAGE_TABLE && ctx.totalPages <= 1) return;
        div.attrClass("page-size");
        div.label().text("Rows: ").__();
        div.select()
            .attrName(PARAM_MAX_ROWS)
            .of(hxGetAttrs(ctx.entityPath, "#" + panelId + " input, #" + panelId + " select", "#" + panelId, "change"))
            .of(select -> {
                for (int s : new int[]{15, 25, 50}) {
                    if (s == ctx.maxRows) {
                        select.option().attrValue(String.valueOf(s)).addAttr("selected", "selected").text(String.valueOf(s)).__();
                    } else {
                        select.option().attrValue(String.valueOf(s)).text(String.valueOf(s)).__();
                    }
                }
            }).__();
    }

    /**
     * Build a URL with sort parameters. If sortDir is null, omit sortProp and sortDir from the URL
     * (effectively removing the sort). Otherwise, include both sortProp and sortDir.
     */
    private String urlForSort(String path, LCTableContext<T> ctx, String columnName, String sortDir) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
            .queryParam(PARAM_PAGE, 1)  // reset to page 1 when sorting changes
            .queryParam(PARAM_MAX_ROWS, ctx.maxRows);
        if (sortDir != null) {
            builder.queryParam(PARAM_SORT_PROP, columnName);
            builder.queryParam(PARAM_SORT_DIR, sortDir);
        }
        if (ctx.filters != null) {
            ctx.filters.forEach((key, val) -> {
                if (val != null && !val.isEmpty()) builder.queryParam(PARAM_FILTER_PREFIX + key, val);
            });
        }
        return builder.encode().toUriString();
    }

    /**
     * Build an application URL with all table-state parameters.
     * Filter parameters are URL-encoded and appended as <PARAM_FILTER_PREFIX>.<columnName>=value.
     */
    private String url(String path, int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
            .queryParam(PARAM_PAGE, page + 1)
            .queryParam(PARAM_MAX_ROWS, maxRows)
            .queryParam(PARAM_SORT_PROP, sortProp == null ? "" : sortProp)
            .queryParam(PARAM_SORT_DIR, sortDir == null ? "asc" : sortDir);
        if (filters != null) {
            filters.forEach((key, val) -> {
                if (val != null && !val.isEmpty()) builder.queryParam(PARAM_FILTER_PREFIX + key, val);
            });
        }
        return builder.encode().toUriString();
    }

}
