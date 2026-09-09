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

    /** Human-facing column name shown in the table header, retained as a key or literal until rendering. */
    public final LCTableText columnDisplayName;

    /** Optional test name for the column, used for testing purposes as value of the @code data-test-column attributes */
    public final String columnTestName;

    /** Width of the column (in rem) */
    public final double columnWidth;

    /** Visibility of the column (default: visible) */
    public final Visibility visibility;

    /** Flag to indicate whether column data is sortable or not */
    public final Sortability sortability;

    /** Optional filter definition for this column. */
    public final LCTableFilterDef filterDef;                // null if no filter is foreseen for this column

    /** Closure that writes the cell content for a given row bean into the HtmlFlow row element. */
    public CellRenderer<T> cellRenderer;

    @FunctionalInterface
    public interface CellRenderer<T> {
        void render(Tr<?> rowElement, T row, LCTableContext<T> context);
    }

    @FunctionalInterface
    public interface ContextCellRenderer<T> {
        void render(Td<?> cell, T row, LCTableContext<T> context);
    }

    private Function<T, Map<String, String>> testAttributes = row -> Collections.emptyMap();

    // General constructor
    public LCTableColumnDef(String columnName, LCTableText columnDisplayName, String columnTestName, double columnWidth, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, CellRenderer<T> cellRenderer) {
        this.columnName = Objects.requireNonNull(columnName, "columnName");
        this.columnDisplayName = Objects.requireNonNull(columnDisplayName, "columnDisplayName");
        this.columnTestName = columnTestName;
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

    private void applyBodyCellAttributes(Td<?> td, T row) {
        if (columnTestName != null) td.addAttr("data-test-column", columnTestName);
        if (row == null) return;
        Map<String, String> attributes = testAttributes.apply(row);
        if (attributes.containsKey("column")) {
            throw new IllegalArgumentException("Error in '" + columnName + "' column definition: 'testAttributes' must not contain 'column' attribute, specify the 'data-test-column' attribute via 'columnTestName' instead");
        }
        td.of(LCTableUtil.testAttrs(attributes));
    }

    // Basic factory method
    public static <T> LCTableColumnDef<T> columnDef(String columnName, LCTableText displayName, String columnTestName, double columnWidth, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, CellRenderer<T> cellRenderer) {
        return new LCTableColumnDef<>(columnName, displayName, columnTestName, columnWidth, visibility, sortability, filterDef, cellRenderer);
    }

    // --- 1. Null-safe cell generator ('Optional'-based) ---

    /**
     * Evaluates the Optional pipeline, safely rendering the value or a fallback '—'.
     */
    private static <T, V> CellRenderer<T> nullSafeCell(
        LCTableColumnDef<T> col,
        Function<Optional<T>, Optional<V>> pipeline,
        BiConsumer<Td<?>, V> finish
    ) {
        return (tr, row, context) -> {
            Td<?> td = tr.td();
            if (col.columnName != null) td.attrClass("lc-col-" + col.columnName);
            col.applyBodyCellAttributes(td, row);
            Optional<T> rowOpt = Optional.ofNullable(row);
            pipeline.apply(rowOpt).ifPresentOrElse(
                val -> finish.accept(td, val),
                () -> td.text(NULL_PLACEHOLDER)
            );
            td.__();
        };
    }

    // --- 2. Null-safe text renderers ---

    private static <T, V> LCTableColumnDef<T> nullSafeColumn(String columnName, LCTableText displayName, String columnTestName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef,
            Function<Optional<T>, Optional<V>> pipeline, BiConsumer<Td<?>, V> finish) {
        LCTableColumnDef<T> col = new LCTableColumnDef<>(columnName, displayName, columnTestName, width, visibility, sortability, filterDef, null);
        col.cellRenderer = nullSafeCell(col, pipeline, finish);
        return col;
    }

    private static <T> LCTableColumnDef<T> nullSafeTextColumn(String columnName, LCTableText displayName, String columnTestName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeColumn(columnName, displayName, columnTestName, width, visibility, sortability, filterDef,
            row -> row.map(renderer), TextGroup::text);
    }

    private static <T, F> LCTableColumnDef<T> nullSafeTextColumn(String columnName, LCTableText displayName, String columnTestName, double width,
            Visibility visibility, Sortability sortability, LCTableFilterDef filterDef,
            Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeColumn(columnName, displayName, columnTestName, width, visibility, sortability, filterDef,
            row -> row.map(extractor).map(renderer), TextGroup::text);
    }

    // --- 3. Column factory methods for text columns ---

    public static <T> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, visibility, sortability, filterDef, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Visibility visibility, Sortability sortability, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, visibility, sortability, filterDef, extractor, renderer);
    }

    public static <T> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Visibility visibility, Sortability sortability, Function<T, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, visibility, sortability, new LCTableFilterDef.Text(), renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Visibility visibility, Sortability sortability, Function<T, F> extractor, Function <F, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, visibility, sortability, new LCTableFilterDef.Text(), extractor, renderer);
    }

    public static <T> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Function<T, String> renderer) {
        return textCol(name, displayName, testName, width, VISIBLE, SORTABLE, renderer);
    }

    public static <T> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, Function<T, String> renderer) {
        return textCol(name, displayName, testName, width, HIDDEN, SORTABLE, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, Function<T, F> extractor, Function<F, String> renderer) {
        return textCol(name, displayName, testName, width, VISIBLE, SORTABLE, extractor, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, Function<T, F> extractor, Function<F, String> renderer) {
        return textCol(name, displayName, testName, width, HIDDEN, SORTABLE, extractor, renderer);
    }

    public static <T> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, SORTABLE, filterDef, renderer);
    }

    public static <T> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, SORTABLE, filterDef, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textCol(String name, LCTableText displayName, String testName, double width, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, SORTABLE, filterDef, extractor, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, SORTABLE, filterDef, extractor, renderer);
    }

    public static <T> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, Sortability sortability, LCTableFilterDef filterDef, Function<T, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, sortability, filterDef, renderer);
    }

    public static <T, F> LCTableColumnDef<T> textColHidden(String name, LCTableText displayName, String testName, double width, Sortability sortability, LCTableFilterDef filterDef, Function<T, F> extractor, Function<F, String> renderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, sortability, filterDef, extractor, renderer);
    }


    // --- 4. Column factory methods for enum-like types (similar to textCol, but with list of allowed value and 'select' filter ---

    public static <T, V> LCTableColumnDef<T> enumCol(String name, LCTableText displayName, String testName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHidden(String name, LCTableText displayName, String testName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColNotSortable(String name, LCTableText displayName, String testName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, NOT_SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHiddenNotSortable(String name, LCTableText displayName, String testName, double width, List<V> filterValues, Function<V, String> filterRenderer, Function<V, String> urlParamConverter, Function<T, String> colRenderer) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, NOT_SORTABLE, new LCTableFilterDef.Select<>(filterValues, filterRenderer, urlParamConverter), colRenderer);
    }

    public static <T, V> LCTableColumnDef<T> enumCol(String name, LCTableText displayName, String testName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColHidden(String name, LCTableText displayName, String testName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(name, displayName, testName, width, HIDDEN, SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    public static <T, V> LCTableColumnDef<T> enumColNotSortable(String name, LCTableText displayName, String testName, double width, Function<T, V> extractor, List<V> enumValues, Function<V, String> renderer, Function<V, String> urlParamConverter) {
        return nullSafeTextColumn(name, displayName, testName, width, VISIBLE, NOT_SORTABLE, new LCTableFilterDef.Select<>(enumValues, renderer, urlParamConverter), extractor, renderer);
    }

    // --- 5. Column factory methods for custom 'td' rendering ---

    public static <T> LCTableColumnDef<T> customTdCol(String name, LCTableText displayName, String testName, double width, Sortability sortability, LCTableFilterDef filterDef, BiConsumer<Td<?>, T> renderer) {
        return nullSafeColumn(name, displayName, testName, width, VISIBLE, sortability, filterDef, Function.identity(), renderer);
    }

    /** Custom cell renderer that can resolve request-scoped table text through its context. */
    public static <T, F> LCTableColumnDef<T> customTdColWithContext(String name, LCTableText displayName, String testName, double width,
            Sortability sortability, LCTableFilterDef filterDef, Function<T, F> presenceKey, ContextCellRenderer<T> renderer) {
        LCTableColumnDef<T> col = new LCTableColumnDef<>(name, displayName, testName, width, VISIBLE, sortability, filterDef, null);
        col.cellRenderer = (tr, row, context) -> {
            Td<?> td = tr.td();
            td.attrClass("lc-col-" + col.columnName);
            col.applyBodyCellAttributes(td, row);
            if (row != null && presenceKey.apply(row) != null) {
                renderer.render(td, row, context);
            } else {
                td.text(NULL_PLACEHOLDER);
            }
            td.__();
        };
        return col;
    }

    /**
    * Same as {@link #customTdCol(String, LCTableText, String, double, Sortability, LCTableFilterDef, BiConsumer)}, but skips
     * rendering (falling back to {@link #NULL_PLACEHOLDER}) when {@code presenceKey.apply(row)} is null -- for
     * columns whose content depends on a nullable, row-specific value (typically a foreign-key id) that may
     * legitimately be absent. {@code renderer} still receives the full row, so it can access whatever fields it needs.
     *
     * @param presenceKey extracts the nullable value whose presence is required to render this cell's content
     * @param renderer    renders the cell's content for a given row (only called if {@code presenceKey.apply(row) != null})
     */
    public static <T, F> LCTableColumnDef<T> customTdCol(String name, LCTableText displayName, String testName, double width, Sortability sortability, LCTableFilterDef filterDef, Function<T, F> presenceKey, BiConsumer<Td<?>, T> renderer) {
        return nullSafeColumn(name, displayName, testName, width, VISIBLE, sortability, filterDef,
            row -> row.filter(r -> presenceKey.apply(r) != null), renderer);
    }

}
