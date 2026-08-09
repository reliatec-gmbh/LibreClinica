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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;

/** Immutable view-context holding pagination/sorting/filtering state and current page data. */
public final class LCTableContext<T> {

    // parameters (from the request URL)
    public final int page;
    public final int maxRows;
    public final String sortProp;
    public final String sortDir;
    public final Map<String, String> filters;
    public final boolean showHiddenCols;

    /** Current values of this table's "sticky" parameters -- see {@link LCTable#stickyParamNames}. */
    public final Map<String, String> stickyParams;

    // paths (derived from the request URL)
    public final String entityPath;
    public final String resourcePath;

    // fetched data for the current page (page, sorting and filtering according to the parameters)
    public final LCTableData<T> data;       // data items (rows) in current page

    // derived pagination state (computed from the parameters and the fetched data)
    public final int totalPages;
    public final int rowsCountInPage;
    public final List<LCTablePageSlot> slots;

    // -- Constructor -----------------------------------------------------------

    public LCTableContext(String entityPath, LCTableParams params, Function<LCTableParams, LCTableData<T>> fetchData, String resourcePath) {
        this.page = params.page; // 0-based, converted from 1-based URL in LCTableParams
        this.maxRows = params.maxRows;
        this.sortProp = params.sortProp;
        this.sortDir = params.sortDir;
        this.filters = params.filters;
        this.showHiddenCols = params.showHiddenCols;
        this.stickyParams = params.stickyParams;
        this.data = fetchData.apply(params);

        this.entityPath = entityPath;
        this.resourcePath = resourcePath;

        final int pageSize = this.maxRows;
        final int totalCountWithFilter = data.totalCountWithFilter;
        this.rowsCountInPage = data.pageItems.size();
        this.totalPages = pageSize <= 0 ? 1 : (totalCountWithFilter == 0 ? 0 : ((totalCountWithFilter - 1) / pageSize + 1));
        this.slots = buildSlots(this.page, this.totalPages);
    }

    /**
     * Build the pagination slots.
     *
     * @since 9
     */
    private List<LCTablePageSlot> buildSlots(int p, int N) {
        if (N <= 0) return Collections.emptyList();

        if (N <= 7) {
            return IntStream.range(0, N)
                .mapToObj(i -> LCTablePageSlot.forPage(i, i == p))
                .collect(Collectors.toList());
        }

        if (p <= 3) {
            return Arrays.asList(
                LCTablePageSlot.forPage(0, p == 0), LCTablePageSlot.forPage(1, p == 1),
                LCTablePageSlot.forPage(2, p == 2), LCTablePageSlot.forPage(3, p == 3),
                LCTablePageSlot.forPage(4, p == 4), LCTablePageSlot.forEllipsis(),
                LCTablePageSlot.forPage(N - 1, p == N - 1));
        }

        if (p >= N - 4) {
            return Arrays.asList(
                LCTablePageSlot.forPage(0, p == 0), LCTablePageSlot.forEllipsis(),
                LCTablePageSlot.forPage(N - 5, p == N - 5), LCTablePageSlot.forPage(N - 4, p == N - 4),
                LCTablePageSlot.forPage(N - 3, p == N - 3), LCTablePageSlot.forPage(N - 2, p == N - 2),
                LCTablePageSlot.forPage(N - 1, p == N - 1));
        }

        return Arrays.asList(
            LCTablePageSlot.forPage(0, false),     LCTablePageSlot.forEllipsis(),
            LCTablePageSlot.forPage(p - 1, false), LCTablePageSlot.forPage(p, true),
            LCTablePageSlot.forPage(p + 1, false), LCTablePageSlot.forEllipsis(),
            LCTablePageSlot.forPage(N - 1, false));
    }

}
