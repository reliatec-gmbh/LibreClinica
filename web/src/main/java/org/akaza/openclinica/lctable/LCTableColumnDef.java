package org.akaza.openclinica.lctable;

import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.Tr;

import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.TextGroup;
import org.xmlet.htmlapifaster.Tr;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes one column of a typed table: an internal name (used in URLs/filters),
 * a display name (rendered in the header) and a closure that writes the cell
 * content for a given row bean into the HtmlFlow row element.
 *
 * @param <R> record (row) type
 */
public class LCTableColumnDef<R> {

    /** Internal column name used by controllers / query params. */
    public final String columnName;

    /** Human-facing header text shown in the table. */
    public final String columnDisplayName;

    /** Optional filter definition for this column. */
    public final LCTableFilterDef filterDef;                // null if no filter is foreseen for this column

    /** Closure that writes the cell content for a given row bean into the HtmlFlow row element. */
    public final BiConsumer<Tr<?>, R> cellRenderer;

    // General constructor
    public LCTableColumnDef(String columnName, String columnDisplayName, LCTableFilterDef filterDef, BiConsumer<Tr<?>, R> cellRenderer) {
        this.columnName = columnName;
        this.columnDisplayName = columnDisplayName;
        this.filterDef = filterDef;
        this.cellRenderer = cellRenderer;
    }

    // Basic factory method
    public static <R> LCTableColumnDef<R> columnDef(String columnName, String displayName, LCTableFilterDef filterDef, BiConsumer<Tr<?>, R> cellRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, filterDef, cellRenderer);
    }

    // --- 1. Null-safe cell generator ('Optional'-based) ---

    /**
     * Core helper: Opens a Td, evaluates the Optional pipeline, renders the value or a fallback, and safely closes the Td.
     */
    private static <R, V> BiConsumer<Tr<?>, R> nullSafeCell(
        Function<Optional<R>, Optional<V>> pipeline,
        BiConsumer<Td<?>, V> finish
    ) {
        return (tr, row) -> {
            Td<?> td = tr.td();   // 1. Open the 'td' element
            pipeline.apply(Optional.ofNullable(row)).ifPresentOrElse(
                val -> finish.accept(td, val),   // 2a. Final rendering of actual content
                () -> td.text("—")                  // 2b. Render fallback (null or empty case)
            );
            td.__();              // 3. Close the 'td' element
        };
    }

    // --- 2. Null-safe text renderers ---

    public static <R> BiConsumer<Tr<?>, R> nullSafeColText(Function<R, String> renderer) {
        return nullSafeCell(row -> row.map(renderer), TextGroup::text);
    }

    public static <R, F> BiConsumer<Tr<?>, R> nullSafeColText(Function<R, F> extractor, Function<F, String> renderer) {
        return nullSafeCell(row -> row.map(extractor).map(renderer), TextGroup::text);
    }

    // --- 3. Column factory methods for text columns ---

    public static <R> LCTableColumnDef<R> textCol(String columnName, String displayName, Function<R, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, new LCTableFilterDef.Text(), nullSafeColText(renderer));
    }

    public static <R, F> LCTableColumnDef<R> textCol(String columnName, String displayName, Function<R, F> extractor, Function<F, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, new LCTableFilterDef.Text(), nullSafeColText(extractor, renderer));
    }

    // --- 4. Column factory methods for enum-like types (similar to textCol, but with list of allowed value and 'select' filter ---

    public static <R, V> LCTableColumnDef<R> enumCol(String columnName, String displayName, List<V> filterValues, Function<V, String> filterRenderer, Function<R, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, new LCTableFilterDef.Select<>(filterValues, filterRenderer), nullSafeColText(colRenderer));
    }

    public static <R, F, V> LCTableColumnDef<R> enumCol(String columnName, String displayName, List<V> filterValues, Function<V, String> filterRenderer, Function<R, F> extractor, Function<F, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, new LCTableFilterDef.Select<>(filterValues, filterRenderer), nullSafeColText(extractor, colRenderer));
    }

    // --- 5. Column factory methods for custom 'td' rendering ---
    public static <R> LCTableColumnDef<R> customTdCol(String columnName, String displayName, LCTableFilterDef filterDef, BiConsumer<Td<?>, R> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, filterDef, nullSafeCell(Function.identity(), renderer));
    }

    public static <R, F> LCTableColumnDef<R> customTdCol(String columnName, String displayName, LCTableFilterDef filterDef, Function<R, F> extractor, BiConsumer<Td<?>, F> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, filterDef, nullSafeCell(row -> row.map(extractor), renderer));
    }

}
