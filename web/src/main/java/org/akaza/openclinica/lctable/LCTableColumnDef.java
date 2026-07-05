package org.akaza.openclinica.lctable;

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

    // synonyms of true, false and null for better readability in column definitions
    // for use in 'sortable' field of column definitions
    public static final boolean SORTABLE = true;
    public static final boolean NOT_SORTABLE = false;
    // for use in 'filterDef' field of column definitions
    public static final LCTableFilterDef NO_FILTER = null;

    /** Internal column name used by controllers / query params. */
    public final String columnName;

    /** Human-facing header text shown in the table. */
    public final String columnDisplayName;

    /** Width of the column (in rem) */
    public final double columnWidth;

    /** Flag to indicate whether column data is sortable or not */
    public final boolean isSortable;

    /** Optional filter definition for this column. */
    public final LCTableFilterDef filterDef;                // null if no filter is foreseen for this column

    /** Closure that writes the cell content for a given row bean into the HtmlFlow row element. */
    public final BiConsumer<Tr<?>, R> cellRenderer;

    // General constructor
    public LCTableColumnDef(String columnName, String columnDisplayName, double columnWidth, boolean isSortable, LCTableFilterDef filterDef, BiConsumer<Tr<?>, R> cellRenderer) {
        this.columnName = columnName;
        this.columnDisplayName = columnDisplayName;
        this.columnWidth = columnWidth;
        this.isSortable = isSortable;
        this.filterDef = filterDef;
        this.cellRenderer = cellRenderer;
    }

    // Basic factory method
    public static <R> LCTableColumnDef<R> columnDef(String columnName, String displayName, double columnWidth, boolean sortable, LCTableFilterDef filterDef, BiConsumer<Tr<?>, R> cellRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, columnWidth, sortable, filterDef, cellRenderer);
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
            Td<?> td = tr.td();
            pipeline.apply(Optional.ofNullable(row)).ifPresentOrElse(
                val -> finish.accept(td, val),
                () -> td.text("—")
            );
            td.__();
        };
    }

    // --- 2. Null-safe text renderers ---

    public static <R> BiConsumer<Tr<?>, R> nullSafeColText(Function<R, String> renderer, double width) {
        return nullSafeCell(row -> row.map(renderer), (td, s) -> td.attrStyle("width: " + width + "rem").text(s));
    }

    public static <R, F> BiConsumer<Tr<?>, R> nullSafeColText(Function<R, F> extractor, Function<F, String> renderer, double width) {
        return nullSafeCell(row -> row.map(extractor).map(renderer), (td, s) -> td.attrStyle("width: " + width + "rem").text(s));
    }

    // --- 3. Column factory methods for text columns ---

    public static <R> LCTableColumnDef<R> textCol(String columnName, String displayName, double width,  Function<R, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, new LCTableFilterDef.Text(), nullSafeColText(renderer, width));
    }

    public static <R> LCTableColumnDef<R> textCol(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<R, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, filterDef, nullSafeColText(renderer, width));
    }

    public static <R, F> LCTableColumnDef<R> textCol(String columnName, String displayName, double width, Function<R, F> extractor, Function<F, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, new LCTableFilterDef.Text(), nullSafeColText(extractor, renderer, width));
    }

    public static <R, F> LCTableColumnDef<R> textCol(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<R, F> extractor, Function<F, String> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, filterDef, nullSafeColText(extractor, renderer, width));
    }

    // --- 4. Column factory methods for enum-like types (similar to textCol, but with list of allowed value and 'select' filter ---

    public static <R, V> LCTableColumnDef<R> enumCol(String columnName, String displayName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<R, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), nullSafeColText(colRenderer, width));
    }

    public static <R, V> LCTableColumnDef<R> enumColNotSortable(String columnName, String displayName,  double width,List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<R, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, false, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), nullSafeColText(colRenderer, width));
    }

    public static <R, F, V> LCTableColumnDef<R> enumCol(String columnName, String displayName,  double width,List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<R, F> extractor, Function<F, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, true, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), nullSafeColText(extractor, colRenderer, width));
    }

    public static <R, F, V> LCTableColumnDef<R> enumColNotSortable(String columnName, String displayName,  double width,List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<R, F> extractor, Function<F, String> colRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, false, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), nullSafeColText(extractor, colRenderer, width));
    }

    // --- 5. Column factory methods for custom 'td' rendering ---
    public static <R> LCTableColumnDef<R> customTdCol(String columnName, String displayName, double width, boolean sortable, LCTableFilterDef filterDef, BiConsumer<Td<?>, R> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, sortable, filterDef, nullSafeCell(Function.identity(), renderer));
    }

    public static <R, F> LCTableColumnDef<R> customTdCol(String columnName, String displayName, double width, boolean sortable, LCTableFilterDef filterDef, Function<R, F> extractor, BiConsumer<Td<?>, F> renderer) {
        return new LCTableColumnDef<>(columnName, displayName, width, sortable, filterDef, nullSafeCell(row -> row.map(extractor), renderer));
    }

}
