/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 *
 * Author: Giuseppe Del Castillo
 * Development sponsored by ReliaTec GmbH
 */
package org.akaza.openclinica.lctable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Describes how one property is filtered and sorted by an
 * {@link LCTableInMemoryDataSource}.
 * <p>
 * The {@code property} must match the corresponding
 * {@link LCTableColumnDef} name because it is used to resolve filter and sort
 * parameters from an {@link LCTableParams} request. Filtering always uses the
 * descriptor's text representation, whereas sorting uses its typed comparator.
 * This separation allows a typed value, such as a date, to be displayed and
 * filtered as formatted text while retaining its natural chronological order.
 *
 * @param <T> the table row type
 */
public final class LCTableInMemoryColumn<T> {

    private final String property;
    private final Function<T, String> filterText;
    private final Comparator<T> comparator;

    private LCTableInMemoryColumn(String property, Function<T, String> filterText, Comparator<T> comparator) {
        this.property = Objects.requireNonNull(property, "property");
        this.filterText = Objects.requireNonNull(filterText, "filterText");
        this.comparator = comparator;
    }

    /**
     * Creates a descriptor that filters and sorts a string property.
     * <p>
     * A {@code null} extracted value does not match a filter and sorts before
     * non-null values in ascending order.
     *
     * @param property the request and table-column property name
     * @param extractor extracts the string value from a row
     * @param <T> the table row type
     * @return a descriptor using the extracted text for both filtering and sorting
     */
    public static <T> LCTableInMemoryColumn<T> text(String property, Function<T, String> extractor) {
        Objects.requireNonNull(extractor, "extractor");
        Comparator<T> comparator = Comparator.comparing(
            extractor, Comparator.nullsFirst(Comparator.naturalOrder()));
        return new LCTableInMemoryColumn<>(property, extractor, comparator);
    }

    /**
     * Creates a descriptor that filters a formatted value but sorts its raw,
     * comparable value.
     * <p>
     * The formatter is not called for a {@code null} extracted value. Such a
     * value does not match a filter and sorts before non-null values in ascending
     * order.
     *
     * @param property the request and table-column property name
     * @param extractor extracts the typed value from a row
     * @param formatter converts a non-null value to searchable text
     * @param <T> the table row type
     * @param <V> the comparable property type
     * @return a descriptor using formatted text for filtering and the raw value for sorting
     */
    public static <T, V extends Comparable<? super V>> LCTableInMemoryColumn<T> value(
            String property, Function<T, V> extractor, Function<V, String> formatter) {
        Objects.requireNonNull(extractor, "extractor");
        Objects.requireNonNull(formatter, "formatter");
        Comparator<T> comparator = Comparator.comparing(
            extractor, Comparator.nullsFirst(Comparator.naturalOrder()));
        return new LCTableInMemoryColumn<>(property,
            row -> {
                V value = extractor.apply(row);
                return value == null ? null : formatter.apply(value);
            }, comparator);
    }

    /**
     * Returns the property name used to resolve request filters and sorting.
     *
     * @return the registered property name
     */
    String getProperty() {
        return property;
    }

    /**
     * Returns the searchable text for a row.
     *
     * @param row the row to inspect
     * @return searchable text, or {@code null} when the property has no value
     */
    String filterText(T row) {
        return filterText.apply(row);
    }

    /**
     * Returns the comparator for this property.
     *
     * @return the typed row comparator
     */
    Comparator<T> getComparator() {
        return comparator;
    }
}
