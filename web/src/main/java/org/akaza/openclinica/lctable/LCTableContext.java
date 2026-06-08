package org.akaza.openclinica.lctable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;

/**
 * Immutable view-context passed to every lctable entity builder.
 *
 * <p>Holds all pagination / filter state that the shared fragments
 * (controls-bar, sort-th, table-footer) and the entity-specific builders need.
 *
 * <p>Equivalent to a record; written as a plain class for Java 11 compatibility.
 *
 * <p>Note: this class makes use of Java convenience APIs such as {@code List.of(...)},
 * which require Java 9 or newer. The implementation avoids newer stream APIs to
 * remain compatible with Java 11.
 *
 * @since 9
 */
public final class LCTableContext {

    private final LCTableParams params;

    private final int recordsFiltered;
    private final int recordsTotal;
    private final List<LCTablePageSlot> slots;
    private final int totalPages;
    private final int rowsInPage;

    public LCTableContext(LCTableParams params, int recordsFiltered, int recordsTotal) {
        final int pageSize = params.maxRows();
        this.params = params;
        this.recordsFiltered = recordsFiltered;
        this.recordsTotal = recordsTotal;
        // Guard against pageSize == 0 and avoid integer-division truncation.
        // Using integer division (recordsFiltered / pageSize) would truncate down
        // (e.g. 10 / 3 == 3) which undercounts pages. The common integer-safe
        // rounding-up formula is (recordsFiltered + pageSize - 1) / pageSize. We also
        // handle the zero-records case so rowsInPage is 0 instead of pageSize.
        if (pageSize > 0) {
            // Overflow-safe integer rounding-up: when recordsFiltered > 0, (recordsFiltered - 1) / pageSize + 1 computes the same as
            // (recordsFiltered + pageSize - 1) / pageSize but avoids a potential overflow on the addition.
            this.totalPages = recordsFiltered == 0 ? 0 : (int) ((recordsFiltered - 1L) / pageSize + 1L);
            this.rowsInPage = (recordsFiltered == 0L) ? 0 : (recordsFiltered % pageSize == 0 ? pageSize : (int) (recordsFiltered % pageSize));
        } else {
            // Defensive: if pageSize is zero, fall back to a sensible default.
            this.totalPages = 1;
            this.rowsInPage = 0;
        }
        this.slots = buildSlots(params.page() - 1, this.totalPages);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int page()                        { return params.page(); }
    public int maxRows()                     { return params.maxRows(); }
    public String sortProp()                 { return params.sortProp(); }
    public String sortDir()                  { return params.sortDir(); }
    public Map<String, String> filters()     { return params.filters(); }

    public int recordsFiltered()             { return recordsFiltered; }
    public int recordsTotal()                { return recordsTotal; }
    public List<LCTablePageSlot> slots()     { return slots; }
    public int totalPages()                  { return totalPages; }
    public int rowsInPage()                  { return rowsInPage; }

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
