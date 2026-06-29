package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableParams.*;
import static org.akaza.openclinica.lctable.LCTableUtil.*;

import htmlflow.HtmlFlow;
import org.springframework.web.util.UriComponentsBuilder;
import org.xmlet.htmlapifaster.*;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

public class LCTable<T1>  {
    private final String entityPath;    // base path for pagination/sorting URLs (e.g. "/books")
    private final String tableName;     // name of the table (used for generating unique IDs and classes)
    private final String panelId;       // ID of the panel element to target with htmx requests (e.g. "books-panel")
    private final List<LCTableColumnDef<T1>> columns;
    private final Function<LCTableParams, LCTableData<T1>> fetchData;

    public LCTable(String entityPath, String tableName, List<LCTableColumnDef<T1>> columns, Function<LCTableParams, LCTableData<T1>> fetchData) {
        this.entityPath = entityPath;
        this.tableName  = tableName;
        this.panelId    = tableName + "-panel";   // Generate panel ID based on table name
        this.columns    = columns;
        this.fetchData  = fetchData;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnNames() {
        return columns.stream().map(col -> col.columnName).collect(Collectors.toList());
    }

    // -- Generic typed table renderer -----------------------------------------

    private void renderColumnNames(Tr<?> tr) {
        // Use the human-facing display name for the header
        columns.forEach(col -> tr.th().text(col.columnDisplayName).__());
    }

    private void renderPageNavigation(Tr<?> tr, LCTableContext<T1> ctx) {
        tr.td().attrClass("toolbar").attrColspan(columns.size())
            .nav().of(nav -> buildPageNavigation(nav, ctx)).__();
    }

    private void renderFilters(Tr<?> tr, LCTableContext<T1> ctx) {
        // Render a filter input for each column
        columns.forEach(col -> {
            if (col.filterDef != null) {
                if (col.filterDef instanceof LCTableFilterDef.Text) {
                    renderTextFilter(tr, ctx, col);
                } else if (col.filterDef instanceof LCTableFilterDef.Select) {
                    //TODO: when moving to Java 17, we can use pattern matching for instanceof to avoid the cast below
                    // 1. Cast safely using the wildcard <?> to eliminate raw type warnings
                    LCTableFilterDef.Select<?> selectFilter = (LCTableFilterDef.Select<?>) col.filterDef;
                    // 2. Pass the safely typed list down to your render method
                    renderSelectFilter(tr, ctx, col, selectFilter);
                }
            } else {
                // insert empty <td> to preserve column cell even when there is no filter
                tr.td().__();
            }
        });
    }

    // 1. This method accepts the wildcard <?> from renderFilters without warnings
    //TODO: when moving to Java 17, we can use pattern matching for instanceof to avoid the need for the helper method below
    private <R extends Element<?, ?>> void renderSelectFilter(Tr<R> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col, LCTableFilterDef.Select<?> selectFilter) {
        // Forward to the private helper method.
        // Java automatically captures the '?' and assigns it to 'S' safely.
        renderSelectFilterHelper(tr, ctx, col, selectFilter);
    }

    // 2. The helper method enforces strict type discipline using <S>
    private <T2, R extends Element<?, ?>> void renderSelectFilterHelper(Tr<R> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col, LCTableFilterDef.Select<T2> selectFilter) {
        String filterName = PARAM_FILTER_PREFIX + col.columnName;
        String rawSelected = ctx.filters.getOrDefault(col.columnName, "");

        // ... (your validation logic stays exactly the same) ...
        boolean isValidOption = rawSelected.isEmpty() || selectFilter.values.stream()
            .map(option -> selectFilter.label(Optional.ofNullable(option)))
            .anyMatch(label -> label.equals(rawSelected));

        String currentSelected = isValidOption ? rawSelected : "";

        Select<Td<Tr<R>>> select = tr.td().select()
            .attrName(filterName)
            .attrId(panelId + "-filter-" + col.columnName)
            .attrClass("filter-select")
            .addAttr(HX_GET, entityPath)
            .addAttr(HX_TARGET, "#" + panelId)
            .addAttr(HX_SWAP, "outerHTML")
            .addAttr(HX_PUSH_URL, "true")
            .addAttr(HX_TRIGGER, "change")
            .addAttr(HX_INCLUDE, "closest form");

        select.option()
            .attrValue("")
            .attrSelected(currentSelected.isEmpty())
            .text(selectFilter.emptyLabel)
            .__();

        // Now 'option' is strictly of type 'T2', and selectFilter expects 'Optional<T2>'
        // Perfect type discipline is maintained!
        selectFilter.values.forEach(option -> {
            String labelText = selectFilter.label(Optional.ofNullable(option));
            boolean isSelected = labelText.equals(currentSelected);
            select.option()
                .attrValue(labelText)
                .attrSelected(isSelected)
                .text(labelText)
                .__();
        });

        select.__().__();
    }

    private void renderTextFilter(Tr<?> tr, LCTableContext<T1> ctx, LCTableColumnDef<T1> col) {
        String filterName = PARAM_FILTER_PREFIX + col.columnName;
        String filterValue = ctx.filters.getOrDefault(col.columnName, "");

        tr.td().input()
            .attrType(EnumTypeInputType.TEXT)
            .attrName(filterName)
            .attrValue(filterValue)
            .attrClass("filter-input")
            .attrId(panelId + "-filter-" + col.columnName)
            .addAttr(HX_GET, entityPath)
            .addAttr(HX_TARGET, "#" + panelId)
            .addAttr(HX_SWAP, "outerHTML")
            .addAttr(HX_PUSH_URL, "true")
            .addAttr(HX_TRIGGER, "input delay:400ms")
            .addAttr(HX_INCLUDE, "closest form")
            .__().__();
    }

    private void renderTableHeader(Thead<?> thead, LCTableContext<T1> ctx) {
        thead.tr().attrClass("header").of(tr -> renderPageNavigation(tr, ctx)).__();
        thead.tr().attrClass("header").of(this::renderColumnNames).__();
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
        StringWriter sw = new StringWriter();
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
        LCTableContext<T1> ctx = new LCTableContext<>(params, fetchData);
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

    // -- URL builder -----------------------------------------------------------

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
