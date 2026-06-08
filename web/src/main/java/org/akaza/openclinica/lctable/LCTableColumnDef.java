package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.Tr;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes one column of a typed table: a header label and a closure that
 * writes the cell content for a given row bean into the HtmlFlow row element.
 *
 * @param <T> row bean type
 */
public class LCTableColumnDef<T> {
    private final String header;
    private final BiConsumer<Tr<?>, T> cellRenderer;

    // General constructor for full control of generated cell (custom HTML, links, etc.)
    public LCTableColumnDef(String header, BiConsumer<Tr<?>, T> cellRenderer) {
        this.header = header;
        this.cellRenderer = cellRenderer;
    }

    // Convenience factory method for simple text columns
    public static <T> LCTableColumnDef<T> textCol(String header, Function<T, String> dataToString) {
        return new LCTableColumnDef<>(header, (tr, row) -> tr.td().text(dataToString.apply(row)).__());
    }

    // Convenience factory method for generic custom cell rendering with access to the Td element for attributes, etc.
    public static <T> LCTableColumnDef<T> customTdCol(String header, BiConsumer<Td<?>, T> renderer) {
        return new LCTableColumnDef<>(header, (tr, row) ->
            tr.td().of(td -> renderer.accept(td, row)).__()
        );
    }

    String getHeader() {
        return header;
    }

    BiConsumer<Tr<?>, T> getCellRenderer() {
        return cellRenderer;
    }
}
