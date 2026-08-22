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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * Renders a paginated/sortable/filterable HTML table using HTMX.
 *
 * <p><b>Important: do not wrap this table's rendered output in your own {@code <form>}.</b>
 * The {@link #render} method renders its own {@code <form>}. Nesting forms is invalid HTML and
 * can lead to duplicate or conflicting query parameters being submitted.
 */
public class LCTable<T>  {

    final String tableName;     // name of the table (used for generating unique IDs and classes)
    final String panelId;       // ID of the panel element to target with HTMX requests (e.g. "books-panel")
    final List<LCTableColumnDef<T>> columns;
    final Function<LCTableParams, LCTableData<T>> fetchData;
    private Function<T, Map<String, String>> rowTestAttributes = row -> Collections.emptyMap();

    /**
     * Explicit whitelist of "sticky" request parameters (e.g. {@code defId}) that are not related
     * to the table's own state, but must be preserved across all table interactions (pagination, sorting, filtering).
     * Explicitly declaring these parameters prevents forwarding arbitrary or unvetted query parameters:
     * any parameters that are neither table-related nor in the whitelist will be dropped.
     */
    final List<String> stickyParamNames;

    /**
     * Optional extra controls (e.g., custom action links or dropdowns) rendered in the toolbar
     * after the built-in controls. Added via {@link #addCustomToolbarControl} before rendering.
     */
    private final List<CustomToolbarControl<T>> customToolbarControls = new ArrayList<>();

    /**
     * Pairs a custom toolbar control's renderer with a predicate deciding, from the context alone (i.e.
     * without actually rendering anything), whether the control has any content to show for the current
     * request. This lets {@link #renderToolbar} skip the control's leading separator {@code <div>} when the
     * control would render nothing (e.g. {@code ListNotesTable}'s "download all"/"print" links, hidden when
     * the current page has no rows) -- instead of always rendering a separator followed by an empty control.
     */
    private static final class CustomToolbarControl<T> {
        final java.util.function.Predicate<LCTableContext<T>> hasContent;
        final BiConsumer<Div<?>, LCTableContext<T>> render;

        CustomToolbarControl(java.util.function.Predicate<LCTableContext<T>> hasContent, BiConsumer<Div<?>, LCTableContext<T>> render) {
            this.hasContent = hasContent;
            this.render = render;
        }
    }

    // constants to control table behaviour
    static final boolean HIDE_PAGINATION_TOOLS_FOR_SINGLE_PAGE_TABLE = false;


    public LCTable(String tableName, List<LCTableColumnDef<T>> columns, Function<LCTableParams, LCTableData<T>> fetchData) {
        this(tableName, columns, fetchData, Collections.emptyList());
    }

    public LCTable(String tableName, List<LCTableColumnDef<T>> columns, Function<LCTableParams, LCTableData<T>> fetchData,
            List<String> stickyParamNames) {
        this.tableName    = tableName;
        this.panelId      = tableName + "-panel";   // Generate panel ID based on table name
        this.columns      = columns;
        this.fetchData    = fetchData;
        this.stickyParamNames = stickyParamNames == null ? Collections.emptyList() : List.copyOf(stickyParamNames);
    }

    public String getTableName() {
        return tableName;
    }
    public List<String> getStickyParamNames() { return stickyParamNames; }

    public LCTable<T> setRowTestAttributes(Function<T, Map<String, String>> rowTestAttributes) {
        this.rowTestAttributes = Objects.requireNonNull(rowTestAttributes, "rowTestAttributes");
        return this;
    }

    public List<String> getColumnNames() {
        return columns.stream().map(col -> col.columnName).collect(Collectors.toList());
    }

    /**
     * Appends a custom control to the toolbar, rendered after built-in controls. Must be called before rendering.
     * The control is assumed to always render some content; use {@link #addCustomToolbarControl(java.util.function.Predicate, BiConsumer)}
     * for a control that may legitimately render nothing for some requests.
     *
     * @param control closure that renders the control's markup into the toolbar's container {@code <div>}
     */
    public void addCustomToolbarControl(BiConsumer<Div<?>, LCTableContext<T>> control) {
        addCustomToolbarControl(ctx -> true, control);
    }

    /**
     * Appends a custom control to the toolbar, rendered after built-in controls, but only when {@code hasContent}
     * returns true for the current context -- in which case its leading separator is rendered too. Must be
     * called before rendering.
     *
     * @param hasContent decides, from the context alone, whether the control has anything to render this request
     * @param control    closure that renders the control's markup into the toolbar's container {@code <div>}
     */
    public void addCustomToolbarControl(java.util.function.Predicate<LCTableContext<T>> hasContent, BiConsumer<Div<?>, LCTableContext<T>> control) {
        customToolbarControls.add(new CustomToolbarControl<>(
            Objects.requireNonNull(hasContent, "hasContent"), Objects.requireNonNull(control, "control")));
    }

    /** True if this table declares at least one column with HIDDEN visibility. */
    private boolean hasHiddenColumns() {
        return columns.stream().anyMatch(col -> col.visibility == LCTableColumnDef.HIDDEN);
    }

    /**
     * A column is rendered (header, filter and value cells) if it is VISIBLE, or if it is
     * HIDDEN and the current request has opted in to show hidden columns.
     */
    private boolean shouldRenderColumn(LCTableColumnDef<T> col, LCTableContext<T> ctx) {
        return col.visibility == LCTableColumnDef.VISIBLE
            || (col.visibility == LCTableColumnDef.HIDDEN && ctx.showHiddenCols);
    }

    /** Number of columns actually rendered for the given context (used for colspan calculations). */
    private long renderedColumnCount(LCTableContext<T> ctx) {
        return columns.stream().filter(col -> shouldRenderColumn(col, ctx)).count();
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
        columns.forEach(col -> {
            if (shouldRenderColumn(col, ctx)) renderColumnName(tr, col, ctx);
        });
    }

    /**
     * Render a clickable header cell that toggles sorting state for the column.
     * Clicking cycles through: no sort → ascending → descending → no sort.
     */
    private void renderColumnName(Tr<?> tr, LCTableColumnDef<T> col, LCTableContext<T> ctx) {
        // A columnWidth of 0 means "no explicit width": let the browser's auto table layout size the
        // column dynamically to fit the widest of its header/data content, rather than forcing it to 0.
        final String widthStyle = col.columnWidth > 0 ? ("width: " + col.columnWidth + "rem") : null;
        if (!col.isSortable()) {
            // Non-sortable column: just render the header text without a link
            Th<?> th = tr.th();
            if (widthStyle != null) th.attrStyle(widthStyle);
            th.text(col.columnDisplayName).__();
        } else {
            boolean isSorted = col.columnName.equals(ctx.sortProp);
            final String currentSortDir = isSorted ? ctx.sortDir : null;
            final String nextSortDir = currentSortDir == null ? "asc" : (currentSortDir.equals("asc") ? "desc" : null);
            final String href = urlForSort(ctx.entityPath, ctx, col.columnName, nextSortDir);
            // Render the header cell with a link that triggers sorting via HTMX
            Th<?> th = tr.th();
            if (widthStyle != null) th.attrStyle(widthStyle);
            th.a().attrId(tableName + "-sortable-header-" + col.columnName).attrClass("sort-header-link")
                .attrHref(href).of(hxGetAttrs(href, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
                .of(a -> {
                    a.span().attrClass("sort-header-text").text(col.columnDisplayName).__();
                    // Always render the sort indicator's <img>, so that the header's width already
                    // accounts for it whether or not the column is currently sorted (avoids the header --
                    // and hence the whole column -- growing/shrinking when sorting is toggled on/off).
                    // When not (yet) sorted on this column/direction, it is only hidden visually (kept in
                    // the layout, via the "sort-indicator-hidden" CSS class) rather than omitted outright.
                    boolean showIndicator = isSorted && currentSortDir != null;
                    String dir = showIndicator ? currentSortDir : "asc";
                    String imgSrc = ctx.resourcePath + (dir.equals("asc") ? "/images/table/sortAsc.gif" : "/images/table/sortDesc.gif");
                    a.img().attrClass(showIndicator ? "sort-indicator" : "sort-indicator sort-indicator-hidden")
                        .addAttr("src", imgSrc).attrAlt(showIndicator ? dir : "").__();
                }).__();
        }
    }

    private void renderToolbar(Tr<?> tr, LCTableContext<T> ctx) {
        tr.td().attrClass("toolbar").attrColspan((int) renderedColumnCount(ctx))
            .div().attrClass("toolbar-container")
            .of(container -> {
                // Left: page navigation
                container.nav().of(nav -> buildPageNavigation(nav, ctx)).__();
                // Separator (CSS-based vertical line)
                container.div().attrClass("toolbar-separator").__();
                // Immediately after navigation: dropdown-list
                container.div().attrClass("dropdown-list").of(div -> buildSelectMaxRowsSelect(div, ctx)).__();
                // If the table has hidden columns, show a toggle button to reveal/hide them
                if (hasHiddenColumns()) {
                    container.div().attrClass("toolbar-separator").__();
                    final String toggleHref = urlForShowHiddenColsToggle(ctx);
                    container.a().attrClass("text-btn")
                        .attrHref(toggleHref)
                        .of(hxGetAttrs(toggleHref, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
                        .text(ctx.showHiddenCols ? "Hide" : "Show More")
                        .__();
                }
                // Custom, non-tabular controls (see addCustomToolbarControl), in the order they were added --
                // each preceded by the same separator used between the built-in controls above, but only when
                // the control actually has something to render for this request (see CustomToolbarControl).
                customToolbarControls.forEach(control -> {
                    if (control.hasContent.test(ctx)) {
                        container.div().attrClass("toolbar-separator").__();
                        control.render.accept(container, ctx);
                    }
                });
            }).__();
    }

    private void renderFilters(Tr<?> tr, LCTableContext<T> ctx) {
        // Render a filter input for each rendered column
        columns.forEach(col -> {
            if (!shouldRenderColumn(col, ctx)) return;
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

    private void renderTableBody(Tbody<?> tbody, LCTableContext<T> ctx) {
        List<T> data = ctx.data.pageItems;
        tbody.attrClass("tbody");
        IntStream.range(0, data.size()).forEach(i -> {
            T item = data.get(i);
            String rowClass = ((i+1) % 2 == 0) ? "even" : "odd";    // use (i+1) to start from 1 for class assignment
            Tr<?> tr = tbody.tr().attrClass(rowClass).of(testAttrs(rowTestAttributes.apply(item)));
            columns.forEach(col -> {
                if (shouldRenderColumn(col, ctx)) col.cellRenderer.accept(tr, item);
            });
            tr.__();
        });
    }

    /**
     * Method that performs the actual generation of the HTML for the table.
     *
     * @param ctx "context" containing all state (pagination/sorting/filtering) and data for the table
     * @return rendered HTML string
     */
    private String renderTableHtml(LCTableContext<T> ctx) {
        final StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrId(panelId).attrClass("lctable")
            .addAttr("hx-ext", "morph")         // use 'idiomorph' extension for morphing the table content instead of replacing it
            .form().attrId(panelId + "-form")
            // Render sticky parameters first, keeping them at the start of URLs and DOM (form) serialization order.
            // Omitted when absent/empty.
            .of(form -> ctx.stickyParams.forEach((name, value) -> {
                if (value != null && !value.isEmpty()) {
                    form.input().attrType(EnumTypeInputType.HIDDEN).attrName(name).attrValue(value).__();
                }
            }))
            // Hidden inputs for filter submission. Page is reset to 1 when filtering (like search box).
            // Pagination buttons use their own URLs with all parameters, so this page value
            // doesn't affect them.
            .input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_PAGE).attrValue("1").__()
            // Hidden inputs for state submission.
            // Note: `maxRows` is intentionally omitted here to make the `<select>` the single source of truth
            // and avoid duplicate submissions. `sortProp`/`sortDir` and `showHiddenCols` are only rendered
            // if they have non-default values to keep HTMX-generated query strings clean.
            .of(form -> {
                if (!ctx.sortProp.isEmpty()) {
                    form.input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_SORT_PROP).attrValue(ctx.sortProp).__();
                    form.input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_SORT_DIR).attrValue(ctx.sortDir).__();
                }
                if (ctx.showHiddenCols) {
                    form.input().attrType(EnumTypeInputType.HIDDEN).attrName(PARAM_SHOW_HIDDEN_COLS).attrValue("true").__();
                }
            })
            .table().attrId(panelId + "-table").attrClass("table").attrStyle("border-collapse:collapse")
            .thead().attrId(panelId + "-thead").of(thead -> renderTableHeader(thead, ctx)).__() // thead
            .tbody().attrId(panelId + "-tbody").attrClass("tbody").of(tbody -> renderTableBody(tbody, ctx)).__() // tbody
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
     * @param entityPath the path to the entity for which the table is being rendered
     * @param params the parameters for fetching data (page number, page size, sorting, filters, etc.)
     * @param resourcePath the path to the resources needed for rendering the table
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
        Td<?> td = footer.tr().td().attrColspan((int) renderedColumnCount(ctx));
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
        pageBtn(nav, "«", url(ctx.entityPath, 0, size, sort, dir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams), panelId, page == 0, tableName + "-nav-btn-first-page");
        // ‹ previous
        pageBtn(nav, "‹", url(ctx.entityPath, max(0, page - 1), size, sort, dir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams), panelId, page == 0, tableName + "-nav-btn-prev-page");
        // numbered slots / ellipsis
        for (LCTablePageSlot slot : ctx.slots) {
            if (slot.ellipsis()) {
                nav.span().attrClass("page-ellipsis").text("…").__();
            } else {
                String slotHref = url(ctx.entityPath, slot.page(), size, sort, dir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams);
                nav.a().attrId(tableName + "-nav-btn-page-" + (slot.page() + 1))
                    .attrClass("text-btn" + (slot.current() ? " current" : ""))
                    .attrHref(slotHref).of(hxGetAttrs(slotHref, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
                    .text(String.valueOf(slot.page() + 1))
                    .__(); // a
            }
        }
        // › next
        pageBtn(nav, "›", url(ctx.entityPath, min(total - 1, page + 1), size, sort, dir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams), panelId, page >= total - 1, tableName + "-nav-btn-next-page");
        // » last
        pageBtn(nav, "»", url(ctx.entityPath, total - 1, size, sort, dir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams), panelId, page >= total - 1, tableName + "-nav-btn-last-page");

        nav.__(); // nav.pagination
    }

    /** Writes a single pagination button into the given {@code nav} element. */
    private void pageBtn(Nav<?> nav, String text, String href, String panelId, boolean disabled, String id) {
        nav.a().attrId(id).attrClass("text-btn" + (disabled ? " disabled" : ""))
            .attrHref(href).of(hxGetAttrs(href, NO_HX_INCLUDE, "#" + panelId, NO_HX_TRIGGER))
            .text(text)
            .__(); // a
    }

    /** Builds the {@code select} element for 'maxRows' and appends it into the provided div. */
    private void buildSelectMaxRowsSelect(Div<?> div, LCTableContext<T> ctx) {
        if (HIDE_PAGINATION_TOOLS_FOR_SINGLE_PAGE_TABLE && ctx.totalPages <= 1) return;
        div.label().text("").__();    // replace "" by "Rows: " if you want to make explicit what the select element is for
        div.select()
            .attrId(tableName + "-select-max-rows")
            .attrName(PARAM_MAX_ROWS)
            // Uses "closest form" to submit maxRows along with all other table-state fields.
            .of(hxGetAttrs(ctx.entityPath, "closest form", "#" + panelId, "change"))
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
        // The page is reset to 1 (index 0) whenever sorting changes. No need to null out columnName
        // ourselves when sortDir is null: url() already treats sortProp/sortDir as an atomic pair
        // and omits both unless both are present.
        return url(path, 0, ctx.maxRows, columnName, sortDir, ctx.filters, ctx.showHiddenCols, ctx.stickyParams);
    }

    /**
     * Build the URL for the "Show More" / "Hide" toggle button that reveals or conceals HIDDEN columns.
     * Preserves all current table-state parameters (page, maxRows, sort, filters) and simply flips
     * the {@link LCTableContext#showHiddenCols} flag (omitted from the URL when turned off).
     */
    private String urlForShowHiddenColsToggle(LCTableContext<T> ctx) {
        return url(ctx.entityPath, ctx.page, ctx.maxRows, ctx.sortProp, ctx.sortDir, ctx.filters, !ctx.showHiddenCols, ctx.stickyParams);
    }

    /**
     * Builds a complete URL with all table-state parameters.
     * Omits null/empty parameters to keep URLs clean. Sticky parameters are listed first.
     * {@code page} is converted from 0-based (internal representation) to 1-based (URL representation).
     */
    private String url(String path, int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters, boolean showHiddenCols,
            Map<String, String> stickyParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (stickyParams != null) {
            stickyParams.forEach((key, val) -> addParamIfPresent(builder, key, val));
        }
        builder.queryParam(PARAM_PAGE, page + 1)
            .queryParam(PARAM_MAX_ROWS, maxRows);
        if (sortProp != null && !sortProp.isEmpty() && sortDir != null && !sortDir.isEmpty()) {
            builder.queryParam(PARAM_SORT_PROP, sortProp);
            builder.queryParam(PARAM_SORT_DIR, sortDir);
        }
        if (filters != null) {
            filters.forEach((key, val) -> addParamIfPresent(builder, PARAM_FILTER_PREFIX + key, val));
        }
        if (showHiddenCols) {
            builder.queryParam(PARAM_SHOW_HIDDEN_COLS, "true");
        }
        return builder.encode().toUriString();
    }


    /** Adds {@code key=val} to {@code builder} unless {@code val} is null or empty, to avoid emitting ugly/redundant empty query parameters. */
    private static void addParamIfPresent(UriComponentsBuilder builder, String key, String val) {
        if (val != null && !val.isEmpty()) {
            builder.queryParam(key, val);
        }
    }

}
