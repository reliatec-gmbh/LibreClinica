package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.Tr;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes one column of a typed table: an internal name (used in URLs/filters),
 * a display name (rendered in the header) and a closure that writes the cell
 * content for a given row bean into the HtmlFlow row element.
 *
 * @param <T> row bean type
 */
public class LCTableColumnDef<T> {

    /** Internal column name used by controllers / query params. */
    public final String columnName;

    /** Human-facing header text shown in the table. */
    public final String columnDisplayName;

    /** Optional filter definition for this column. */
    public final LCTableFilterDef filterDef;

    /** Closure that writes the cell content for a given row bean into the HtmlFlow row element. */
    public final BiConsumer<Tr<?>, T> cellRenderer;

    // General constructor for full control of generated cell (custom HTML, links, etc.)
    public LCTableColumnDef(String columnName, String columnDisplayName, LCTableFilterDef filterDef, BiConsumer<Tr<?>, T> cellRenderer) {
        this.columnName = columnName;
        this.columnDisplayName = columnDisplayName;
        this.filterDef = filterDef;
        this.cellRenderer = cellRenderer;
    }

    // Convenience factory method for simple text columns
    public static <T> LCTableColumnDef<T> textCol(String columnName, String displayName, Function<T, String> dataToString) {
        return new LCTableColumnDef<>(columnName, displayName, new LCTableFilterDef.Text(), (tr, row) -> tr.td().text(dataToString.apply(row)).__());
    }

    // Convenience factory method for generic custom cell rendering with access to the Td element for attributes, etc.
    public static <T> LCTableColumnDef<T> customTdCol(String columnName, String displayName, LCTableFilterDef filterDef, BiConsumer<Td<?>, T> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, filterDef, (tr, row) ->
            tr.td().of(td -> renderer.accept(td, row)).__()
        );
    }

}
