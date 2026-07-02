package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableParams.*;

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

public class LCTable<T1>  {
    final String entityPath;    // base path for pagination/sorting URLs
    final String resourcePath;
    final String tableName;     // name of the table (used for generating unique IDs and classes)
    final String panelId;       // ID of the panel element to target with htmx requests (e.g. "books-panel")
    final List<LCTableColumnDef<T1>> columns;
    final Function<LCTableParams, LCTableData<T1>> fetchData;

    public LCTable(String entityPath, String resourcePath, String tableName, List<LCTableColumnDef<T1>> columns, Function<LCTableParams, LCTableData<T1>> fetchData) {
        this.entityPath   = entityPath;
        this.resourcePath = resourcePath;
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

    private void renderColumnNames(Tr<?> tr, LCTableContext<T1> ctx) {
        columns.forEach(col -> renderSortableHeader(tr, col, ctx));
    }

    /**
     * Render a clickable header cell that toggles sorting state for the column.
     * Clicking cycles through: no sort → ascending → descending → no sort.
     */
    private void renderSortableHeader(Tr<?> tr, LCTableColumnDef<T1> col, LCTableContext<T1> ctx) {
        // Determine if this column is currently sorted
        boolean isSorted = col.columnName.equals(ctx.sortProp);
        String currentDir = isSorted ? ctx.sortDir : null;

        // Determine next sort direction when clicked: none → asc → desc → none
        String nextDir = currentDir == null ? "asc" : currentDir.equals("asc") ? "desc" : null;

        // Build the URL for this sort state
        String href = urlForSort(entityPath, ctx, col.columnName, nextDir);

        tr.th()
            .a().attrClass("sort-header-link")
            .attrHref(href)
            .addAttr(HX_GET, href)
            .addAttr(HX_TARGET, "#" + panelId)
            .addAttr(HX_SWAP, "outerHTML")
            .addAttr(HX_PUSH_URL, "true")
            .of(a -> {
                a.span().attrClass("sort-header-text").text(col.columnDisplayName).__();
                // Show sort indicator if sorted
                if (isSorted && currentDir != null) {
                    String imgSrc = resourcePath + (currentDir.equals("asc") ? "/images/table/sortAsc.gif" : "/images/table/sortDesc.gif");
                    a.img().attrClass("sort-indicator").addAttr("src", imgSrc).attrAlt(currentDir).__();
                }
            }).__();
    }

    private void renderToolbar(Tr<?> tr, LCTableContext<T1> ctx) {
        tr.td().attrClass("toolbar").attrColspan(columns.size())
            .div().attrStyle("display:flex;justify-content:space-between;align-items:center")
            .of(container -> {
                // Left: page navigation
                container.nav().of(nav -> buildPageNavigation(nav, ctx)).__();
                // Right: page-size selector
                container.div().of(div -> buildMaxRowsSelector(div, ctx)).__();
            }).__();
    }

    private void renderFilters(Tr<?> tr, LCTableContext<T1> ctx) {
        // Render a filter input for each column
        columns.forEach(col -> {
            if (col.filterDef != null) {
                col.filterDef.renderFilter(tr, ctx, col, this);
            } else {
                tr.td().__();   // insert empty <td> to fill column cell when there is no filter
            }
        });
    }

    private void renderTableHeader(Thead<?> thead, LCTableContext<T1> ctx) {
        thead.tr().attrClass("header").of(tr -> renderToolbar(tr, ctx)).__();
        thead.tr().attrClass("header").of(tr -> renderColumnNames(tr, ctx)).__();
        thead.tr().attrClass("filter").of(tr -> renderFilters(tr, ctx)).__();
    }

    private void renderTableBody(Tbody<?> tbody, List<T1> data) {
        tbody.attrClass("tbody");
        IntStream.range(0, data.size()).forEach(i -> {
            T1 item = data.get(i);
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
    private String renderTableHtml(LCTableContext<T1> ctx) {
        final StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrId(panelId).attrClass("lctable")
            .form()
            // Hidden inputs for filter submission. Page is reset to 1 when filtering (like search box).
            // Pagination buttons use their own URLs with all parameters, so this page value
            // doesn't affect them.
            .input().attrType(EnumTypeInputType.HIDDEN).attrName("page").attrValue("1").__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName("maxRows").attrValue(String.valueOf(ctx.maxRows)).__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName("sortProp").attrValue(ctx.sortProp).__()
            .input().attrType(EnumTypeInputType.HIDDEN).attrName("sortDir").attrValue(ctx.sortDir).__()

            .table().attrClass("table").attrStyle("border-collapse:collapse")
            .thead().of(thead -> renderTableHeader(thead, ctx)).__() // thead
            .tbody().attrClass("tbody").of(tbody -> renderTableBody(tbody, ctx.data.pageItems)).__() // tbody
            .tfoot().of(tfoot -> renderTableFooter(tfoot, ctx)).__()
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
    public String render(LCTableParams params) {
        final LCTableContext<T1> ctx = new LCTableContext<>(params, fetchData);
        return renderTableHtml(ctx);
    }

    // -- Controls (pagination etc.) -------------------------------------------

    public void renderTableFooter(Tfoot<?> tfoot, LCTableContext<T1> ctx) {
        long    from     = (long) ctx.page * ctx.maxRows + 1;
        long    to       = (long) ctx.page * ctx.maxRows + ctx.data.pageItems.size();

        Tfoot<?> footer = tfoot.attrClass("statusBar");
        Td<?> td = footer.tr().td().attrColspan(columns.size());
        final int count = ctx.data.totalCountWithFilter;
        td.text(count == 0 ? "No results." : format("Results %d-%d of %d.", from, to, count)).__();
        footer.__(); // div.table-footer
    }

    private void buildPageNavigation(Nav<?> nav, LCTableContext<T1> ctx) {
        if (ctx.totalPages <= 1) return;

        final int page = ctx.page;
        final int total = ctx.totalPages;
        final int size = ctx.maxRows;
        final String sort = ctx.sortProp;
        final String dir = ctx.sortDir;

        nav.attrClass("toolbar");

        // « first
        pageBtn(nav, "«", url(entityPath, 0, size, sort, dir, ctx.filters), panelId, page == 0);

        // ‹ previous
        pageBtn(nav, "‹", url(entityPath, max(0, page - 1), size, sort, dir, ctx.filters), panelId, page == 0);

        // numbered slots / ellipsis
        for (LCTablePageSlot slot : ctx.slots) {
            if (slot.ellipsis()) {
                nav.span().attrClass("page-ellipsis").text("…").__();
            } else {
                String slotHref = url(entityPath, slot.page(), size, sort, dir, ctx.filters);
                nav.a().attrClass("page-btn" + (slot.current() ? " current" : ""))
                    .attrHref(slotHref)
                    .addAttr(HX_GET, slotHref)
                    .addAttr(HX_TARGET, "#" + panelId)
                    .addAttr(HX_SWAP, "outerHTML")
                    .addAttr(HX_PUSH_URL, "true")
                    .text(String.valueOf(slot.page() + 1))
                    .__(); // a
            }
        }

        // › next
        pageBtn(nav, "›", url(entityPath, min(total - 1, page + 1), size, sort, dir, ctx.filters), panelId, page >= total - 1);

        // » last
        pageBtn(nav, "»", url(entityPath, total - 1, size, sort, dir, ctx.filters), panelId, page >= total - 1);

        nav.__(); // nav.pagination
    }

    /** Writes a single pagination button into the given {@code nav} element. */
    private void pageBtn(Nav<?> nav, String text, String href, String panelId, boolean disabled) {
        nav.a().attrClass("page-btn" + (disabled ? " disabled" : ""))
            .attrHref(href)
            .addAttr(HX_GET,      href)
            .addAttr(HX_TARGET,   "#" + panelId)
            .addAttr(HX_SWAP,     "outerHTML")
            .addAttr(HX_PUSH_URL, "true")
            .text(text)
            .__(); // a
    }

    /** Builds the page-size selector (maxRows) and appends it into the provided div. */
    private void buildMaxRowsSelector(Div<?> div, LCTableContext<T1> ctx) {
        div.attrClass("page-size");
        div.label().text("Rows: ").__();
        div.select()
            .attrName("maxRows")
            .addAttr(HX_GET, entityPath)
            .addAttr(HX_INCLUDE, "#" + panelId + " input, #" + panelId + " select")
            .addAttr(HX_TRIGGER, "change")
            .addAttr(HX_TARGET, "#" + panelId)
            .addAttr(HX_SWAP, "outerHTML")
            .addAttr(HX_PUSH_URL, "true")
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
    private String urlForSort(String path, LCTableContext<T1> ctx, String columnName, String sortDir) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
            .queryParam("page", 1)  // reset to page 1 when sorting changes
            .queryParam("maxRows", ctx.maxRows);

        if (sortDir != null) {
            builder.queryParam("sortProp", columnName);
            builder.queryParam("sortDir", sortDir);
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
            .queryParam("page", page + 1)
            .queryParam("maxRows", maxRows)
            .queryParam("sortProp", sortProp == null ? "" : sortProp)
            .queryParam("sortDir", sortDir == null ? "asc" : sortDir);
        if (filters != null) {
            filters.forEach((key, val) -> {
                if (val != null && !val.isEmpty()) builder.queryParam(PARAM_FILTER_PREFIX + key, val);
            });
        }
        return builder.encode().toUriString();
    }

}
