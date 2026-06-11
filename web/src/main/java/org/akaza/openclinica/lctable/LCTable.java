package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableUtil.*;
import static org.akaza.openclinica.lctable.LCTableUtil.HX_PUSH_URL;

import htmlflow.HtmlFlow;
import org.xmlet.htmlapifaster.*;

import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

public class LCTable<T>  {
    private final String entityPath;    // base path for pagination/sorting URLs (e.g. "/books")
    private final String panelId;       // ID of the panel element to target with htmx requests (e.g. "books-panel")
    private final List<LCTableColumnDef<T>> columns;
    private final Function<LCTableParams, LCTableData<T>> fetchData;

    public LCTable(String entityPath, String panelId, List<LCTableColumnDef<T>> columns, Function<LCTableParams, LCTableData<T>> fetchData) {
        this.entityPath = entityPath;
        this.panelId    = panelId;
        this.columns    = columns;
        this.fetchData  = fetchData;
    }

    // -- Generic typed table renderer -----------------------------------------

    private void renderColumnNames(Tr<?> tr) {
        columns.forEach(col -> tr.th().text(col.columnName).__());
        // should actually use th instead of td in the table header:
        // however, use td like jmesa for the moment (otherwise styling with jmesa CSS does not work)
    }

    private void renderTableHeader(Thead<?> thead, LCTableContext<T> ctx) {
        Tr<?> tr1 = thead.tr().attrClass("header");
        tr1.td().attrClass("toolbar").attrColspan(columns.size())
            .nav().of(nav -> buildPagination(nav, ctx)).__()
            .__();
        Tr<?> tr2 = thead.tr().attrClass("header");
        renderColumnNames(tr2);
        tr2.__();
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
        StringWriter sw = new StringWriter();
        HtmlFlow.doc(sw)
            .div().attrId(panelId).attrClass("jmesa")
            .table().attrClass("table").attrStyle("border-collapse:collapse")
            .thead().of(thead -> renderTableHeader(thead, ctx)).__() // thead
            .tbody().attrClass("tbody").of(tbody -> renderTableBody(tbody, ctx.data.pageItems)).__() // tbody
            .tfoot().of(tfoot -> renderTableFooter(tfoot, ctx)).__()
            .__() // table
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
        LCTableContext<T> ctx = new LCTableContext<>(params, fetchData);
        return renderTableHtml(ctx);
    }

    // -- Controls (pagination etc.) -------------------------------------------

    public void renderTableFooter(Tfoot<?> tfoot, LCTableContext<T> ctx) {
        long    from     = (long) ctx.page * ctx.maxRows + 1;
        long    to       = (long) ctx.page * ctx.maxRows + ctx.data.pageItems.size();

        Tfoot<?> footer = tfoot.attrClass("statusBar");
        Td<?> td = footer.tr().td().attrColspan(columns.size());
        td.text(format("Results %d-%d of %d.", from, to, ctx.data.totalCountWithFilter)).__();
        footer.__(); // div.table-footer
    }

    private void buildPagination(Nav<?> nav, LCTableContext<T> ctx) {
        if (ctx.totalPages <= 1) return;

        final int page = ctx.page;
        final long total = ctx.totalPages;
        final int size = ctx.maxRows;
        final String sort = ctx.sortProp;
        final String dir = ctx.sortDir;

        nav.attrClass("toolbar");

        // « first
        pageBtn(nav, "«", url(entityPath, 0, size, sort, dir), panelId, page == 0);

        // ‹ previous
        pageBtn(nav, "‹", url(entityPath, max(0, page - 1), size, sort, dir), panelId, page == 0);

        // numbered slots / ellipsis
        for (LCTablePageSlot slot : ctx.slots) {
            if (slot.ellipsis()) {
                nav.span().attrClass("page-ellipsis").text("…").__();
            } else {
                String slotHref = url(entityPath, slot.page(), size, sort, dir);
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
        pageBtn(nav, "›", url(entityPath, min(total - 1, page + 1), size, sort, dir), panelId, page >= total - 1);

        // » last
        pageBtn(nav, "»", url(entityPath, total - 1, size, sort, dir), panelId, page >= total - 1);

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
     * Only {@code q} is URL-encoded; the other params are always safe.
     */
    private String url(String path, long page, int maxRows, String sortProp, String sortDir) {
        // TODO: is it safe to just use string concatenation like this?
        long pageParam = page + 1; // external URLs are 1-based
        return path
            + "?page=" + pageParam
            + "&maxRows=" + maxRows
            + "&sortProp=" + (sortProp == null ? "" : sortProp)
            + "&sortDir="  + (sortDir  == null ? "asc" : sortDir);
    }

}
