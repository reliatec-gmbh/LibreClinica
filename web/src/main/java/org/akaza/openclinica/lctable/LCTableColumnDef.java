package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Tr;

import java.util.function.BiConsumer;

/**
 * Describes one column of a typed table: a header label and a closure that
 * writes the cell content for a given row bean into the HtmlFlow row element.
 *
 * @param <T> row bean type
 */
public class LCTableColumnDef<T> {
    private final String header;
    private final BiConsumer<Tr<?>, T> cellRenderer;

    public LCTableColumnDef(String header, BiConsumer<Tr<?>, T> cellRenderer) {
        this.header = header;
        this.cellRenderer = cellRenderer;
    }

    String getHeader() {
        return header;
    }

    BiConsumer<Tr<?>, T> getCellRenderer() {
        return cellRenderer;
    }
}
