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

import org.xmlet.htmlapifaster.Td;
import org.xmlet.htmlapifaster.TextGroup;
import org.xmlet.htmlapifaster.Tr;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a table column: internal name, header display name, a cell-rendering closure and various other properties (width, visibility, sortability, filter definition).
 *
 * @param <T> record (row) type
 */
public class LCTableColumnDef<T> {

    public enum Visibility {
        VISIBLE,
        HIDDEN;
        public boolean isVisible() { return this == VISIBLE; }
        public boolean isHidden() { return this == HIDDEN; }
    }
    public static final Visibility VISIBLE = Visibility.VISIBLE;
    public static final Visibility HIDDEN = Visibility.HIDDEN;

    public enum Sortability {
        SORTABLE,
        NOT_SORTABLE;
        public boolean isSortable() { return this == SORTABLE; }
    }
    public static final Sortability SORTABLE = Sortability.SORTABLE;
    public static final Sortability NOT_SORTABLE = Sortability.NOT_SORTABLE;

    // for use in 'filterDef' field of column definitions
    public static final LCTableFilterDef NO_FILTER = null;

    /** Fallback text rendered by {@link #nullSafeCell} (and usable by callers needing the same fallback for a
     *  sub-part of a cell, e.g. an "actions" column omitting an action link because the id it needs is null). */
    public static final String NULL_PLACEHOLDER = "—";

    /** Internal column name used by controllers / query params. */
    public final String columnName;

    /** Human-facing header text shown in the table. */
    public final String columnDisplayName;

    /** Width of the column (in rem) */
    public final double columnWidth;

    /** Visibility of the column (default: visible) */
    public final Visibility visibility;

    /** Flag to indicate whether column data is sortable or not */
    public final Sortability sortability;

    /** Optional filter definition for this column. */
    public final LCTableFilterDef filterDef;                // null if no filter is foreseen for this column

    /** Closure that writes the cell content for a given row bean into the HtmlFlow row element. */
    public BiConsumer<Tr<?>, T> cellRenderer;

    private Function<T, Map<String, String>> testAttributes = row -> Collections.emptyMap();

    // General constructor
    public LCTableColumnDef(String columnName, String columnDisplayName, double columnWidth, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, BiConsumer<Tr<?>, T> cellRenderer) {
        this.columnName = columnName;
        this.columnDisplayName = columnDisplayName;
        this.columnWidth = columnWidth;
        this.visibility = visibility;
        this.sortability = sortability;
        this.filterDef = filterDef;
        this.cellRenderer = cellRenderer;
    }

    public boolean isVisible() { return this.visibility == Visibility.VISIBLE; }

    public boolean isSortable() {
        return this.sortability.isSortable();
    }
    public boolean isFilterable() { return this.filterDef != null; }

    public LCTableColumnDef<T> setTestAttributes(Function<T, Map<String, String>> testAttributes) {
        this.testAttributes = Objects.requireNonNull(testAttributes, "testAttributes");
        return this;
    }

    // Basic factory method
    public static <T> LCTableColumnDef<T> columnDef(String columnName, String displayName, double columnWidth, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, BiConsumer<Tr<?>, T> cellRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, columnWidth, visibility, sortability, filterDef, cellRenderer);
    }

    // --- 1. Null-safe cell generator ('Optional'-based) ---

    /**
     * Evaluates the Optional pipeline, safely rendering the value or a fallback '—'.
     */
    private static <T, V> BiConsumer<Tr<?>, T> nullSafeCell(
        LCTableColumnDef<T> col,
        Function<Optional<T>, Optional<V>> pipeline,
        BiConsumer<Td<?>, V> finish
    ) {
        return (tr, row) -> {
            Td<?> td = tr.td();
            if (col.columnName != null) td.attrClass("lc-col-" + col.columnName);
            Optional<T> rowOpt = Optional.ofNullable(row);
            rowOpt.ifPresent(value -> td.of(LCTableUtil.testAttrs(col.testAttributes.apply(value))));
            pipeline.apply(rowOpt).ifPresentOrElse(
                val -> finish.accept(td, val),
                () -> td.text(NULL_PLACEHOLDER)
            );
            td.__();
        };
    }

    // --- 2. Null-safe text renderers ---

    private static <T, V> LCTableColumnDef<T> nullSafeColumn(String columnName, String displayName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef,
            Function<Optional<T>, Optional<V>> pipeline, BiConsumer<Td<?>, V> finish) {
        LCTableColumnDef<T> col = new LCTableColumnDef<>(columnName, displayName, width, visibility, sortability, filterDef, null);
        col.cellRenderer = nullSafeCell(col, pipeline, finish);
        return col;
    }

    private static <T> LCTableColumnDef<T> nullSafeTextColumn(String columnName, String displayName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeColumn(columnName, displayName, width, visibility, sortability, filterDef,
            row -> row.map(renderer), TextGroup::text);
    }

    private static <T, F> LCTableColumnDef<T> nullSafeTextColumn(String columnName, String displayName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef,
            Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeColumn(columnName, displayName, width, visibility, sortability, filterDef,
            row -> row.map(extractor).map(renderer), TextGroup::text);
    }

    // --- 3. Column factory methods for text columns ---

    public static <T> LCTableColumnDef<T> textCol(String columnName, String displayName, double width, Function<T, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Text(), renderer);
    }

    public static <T> LCTableColumnDef<T> textColHidden(String columnName, String displayName, double width, Function<T, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Text(), renderer);

    }
    public static <T> LCTableColumnDef<T> textCol(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, filterDef, renderer);
    }

    public static <T> LCTableColumnDef<T> textColHidden(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, filterDef, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String columnName, String displayName, double width, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Text(), extractor, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textColHidden(String columnName, String displayName, double width, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Text(), extractor, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, filterDef, extractor, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textColHidden(String columnName, String displayName, double width, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, filterDef, extractor, renderer);
    }

    // --- 4. Column factory methods for enum-like types (similar to textCol, but with list of allowed value and 'select' filter ---

    public static <T, V> LCTableColumnDef<T> enumCol(String columnName, String displayName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHidden(String columnName, String displayName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColNotSortable(String columnName, String displayName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, NOT_SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHiddenNotSortable(String columnName, String displayName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, NOT_SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumCol(String columnName, String displayName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHidden(String columnName, String displayName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(columnName, displayName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColNotSortable(String columnName, String displayName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(columnName, displayName, width, VISIBLE, NOT_SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    // --- 5. Column factory methods for custom 'td' rendering ---

    public static <T> LCTableColumnDef<T> customTdCol(String columnName, String displayName, double width, Sortability sortability, LCTableFilterDef filterDef, BiConsumer<Td<?>, T> renderer) {
        return nullSafeColumn(columnName, displayName, width, VISIBLE, sortability, filterDef, Function.identity(), renderer);
    }

    /**
     * Same as {@link #customTdCol(String, String, double, Sortability, LCTableFilterDef, BiConsumer)}, but skips
     * rendering (falling back to {@link #NULL_PLACEHOLDER}) when {@code presenceKey.apply(row)} is null -- for
     * columns whose content depends on a nullable, row-specific value (typically a foreign-key id) that may
     * legitimately be absent. {@code renderer} still receives the full row, so it can access whatever fields it needs.
     *
     * @param presenceKey extracts the nullable value whose presence is required to render this cell's content
     * @param renderer    renders the cell's content for a given row (only called if {@code presenceKey.apply(row) != null})
     */
    public static <T, F> LCTableColumnDef<T> customTdCol(String columnName, String displayName, double width, Sortability sortability, LCTableFilterDef filterDef, Function<T, F> presenceKey, BiConsumer<Td<?>, T> renderer) {
        return nullSafeColumn(columnName, displayName, width, VISIBLE, sortability, filterDef,
            row -> row.filter(r -> presenceKey.apply(r) != null), renderer);
    }

}
