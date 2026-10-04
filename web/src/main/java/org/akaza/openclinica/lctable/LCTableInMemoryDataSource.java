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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Supplies an {@link LCTable} with data from a complete collection already held in
 * memory.
 * <p>
 * A {@code LCTableInMemoryDataSource<T>} can be used as the {@code fetchData} function of an {@code LCTable<T>}.
 * <p>
 * The data source applies registered filters with AND semantics and
 * case-insensitive substring matching, optionally sorts by a registered
 * property, calculates the filtered count, and then returns the requested page.
 * Unknown filter and sort properties are ignored. Without a recognized sort,
 * rows retain their source order.
 * <p>
 * Construction takes a shallow snapshot of the supplied collection, so that any changes (e.g. due to filtering, sorting)
 * do not affect the original collection, which remains unchanged. The row objects themselves are not copied.
 * This class is intended for small result sets. Large or database-backed tables should instead perform filtering,
 * sorting, counting, and paging in a typed DAO-backed data function.
 *
 * @param <T> the table row type
 */
public final class LCTableInMemoryDataSource<T> implements Function<LCTableParams, LCTableData<T>> {

    private final List<T> rows;
    private final Map<String, LCTableInMemoryColumn<T>> columns;
    private final boolean unpaginated;

    /**
     * Creates an in-memory data source and indexes its permitted properties.
     *
     * @param rows the complete collection of available rows
     * @param columns typed descriptors for filterable or sortable properties;
     *                each property name must be unique
     * @throws NullPointerException if {@code rows}, {@code columns}, or a
     *                              descriptor property is {@code null}
     * @throws IllegalArgumentException if two descriptors use the same property name
     */
    public LCTableInMemoryDataSource(Collection<T> rows, List<LCTableInMemoryColumn<T>> columns) {
        this(rows, columns, false);
    }

    /** Unpaginated is intended only for bounded, application-owned collections. */
    private LCTableInMemoryDataSource(Collection<T> rows, List<LCTableInMemoryColumn<T>> columns, boolean unpaginated) {
        this.rows = new ArrayList<>(Objects.requireNonNull(rows, "rows"));
        this.unpaginated = unpaginated;
        Objects.requireNonNull(columns, "columns");
        this.columns = new LinkedHashMap<>();
        for (LCTableInMemoryColumn<T> column : columns) {
            LCTableInMemoryColumn<T> previous = this.columns.put(column.getProperty(), column);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate in-memory column property: " + column.getProperty());
            }
        }
    }

    public static <T> LCTableInMemoryDataSource<T> allRows(Collection<T> rows, List<LCTableInMemoryColumn<T>> columns) {
        return new LCTableInMemoryDataSource<>(rows, columns, true);
    }

    /**
     * Applies the requested filters, sort, and page to the collection snapshot.
     *
     * @param params parsed table request parameters
     * @return an {@code LCTableData<T>} object containing the requested data page and the number of rows in the filtered collection
     */
    @Override
    public LCTableData<T> apply(LCTableParams params) {
        List<T> filtered = rows.stream()
            .filter(row -> matches(row, params.filters))
            .collect(Collectors.toCollection(ArrayList::new));

        LCTableInMemoryColumn<T> sortColumn = columns.get(params.sortProp);
        if (sortColumn != null && sortColumn.getComparator() != null) {
            Comparator<T> comparator = sortColumn.getComparator();
            if ("desc".equalsIgnoreCase(params.sortDir)) {
                comparator = comparator.reversed();
            }
            filtered.sort(comparator);
        }

        if (unpaginated) return new LCTableData<>(filtered, filtered.size());
        long requestedStart = (long) params.page * params.maxRows;
        int fromIndex = (int) Math.min(requestedStart, filtered.size());
        int toIndex = Math.min(fromIndex + params.maxRows, filtered.size());
        return new LCTableData<>(new ArrayList<>(filtered.subList(fromIndex, toIndex)), filtered.size());
    }

    /**
     * Tests whether a row contains every recognized filter value. Matching is
     * case-insensitive using {@link Locale#ROOT}; a {@code null} property value
     * never matches a filter.
     *
     * @param row the row to test
     * @param filters property names mapped to requested substrings
     * @return {@code true} when the row satisfies all recognized filters
     */
    private boolean matches(T row, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            LCTableInMemoryColumn<T> column = columns.get(filter.getKey());
            if (column == null) {
                continue;
            }
            String value = column.filterText(row);
            if (value == null || !value.toLowerCase(Locale.ROOT).contains(filter.getValue().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }
}
