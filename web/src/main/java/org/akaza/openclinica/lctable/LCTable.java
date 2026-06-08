package org.akaza.openclinica.lctable;

import htmlflow.HtmlFlow;
import org.xmlet.htmlapifaster.Tbody;
import org.xmlet.htmlapifaster.Thead;
import org.xmlet.htmlapifaster.Tr;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.IntStream;

public class LCTable {
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
