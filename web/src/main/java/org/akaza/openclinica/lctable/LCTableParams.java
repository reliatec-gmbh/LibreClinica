package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableUtil.*;

import java.util.Map;

import org.springframework.util.MultiValueMap;



public final class LCTableParams {

    // -- URL parameters -------------------------------------------------------
    private final int page;
    private final int maxRows;
    private final String sortProp;
    private final String sortDir;
    private final Map<String, String> filters;

    /** Canonical constructor – normalises nullable / out-of-range values. */
    public LCTableParams(int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters) {
        this.page     = Math.max(page, 0);
        this.maxRows  = maxRows > 0 ? maxRows : 15;
        this.sortProp = sortProp != null ? sortProp : "";
        this.sortDir  = sortDir  != null ? sortDir  : "asc";
        this.filters  = filters  != null ? filters  : java.util.Collections.emptyMap();
    }

    /**
     * Construct a LcTableParams object by reading request parameters from a Spring
     * MultiValueMap (as provided by a controller with
     * {@code @RequestParam MultiValueMap<String,String> allParams}).
     *
     * <p>The helper methods in {@link LCTableParams} are used to parse and
     * normalise the parameter values.
     */
    public LCTableParams(MultiValueMap<String, String> params) {
        this(
            intParam(params, LCTableUtil.PARAM_PAGE, 0),
            intParam(params, LCTableUtil.PARAM_MAX_ROWS, 15),
            strParam(params, LCTableUtil.PARAM_SORT_PROP, ""),
            strParam(params, LCTableUtil.PARAM_SORT_DIR, "asc"),
            readFilters(params)
        );
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int page()                        { return page; }
    public int maxRows()                     { return maxRows; }
    public String sortProp()                 { return sortProp; }
    public String sortDir()                  { return sortDir; }
    public Map<String, String> filters()     { return filters; }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString  (mirrors record semantics)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LCTableParams)) return false;
        LCTableParams that = (LCTableParams) o;
        return page == that.page
            && maxRows == that.maxRows
            && sortProp.equals(that.sortProp)
            && sortDir.equals(that.sortDir)
            && filters.equals(that.filters);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(page, maxRows, sortProp, sortDir, filters);
    }

    @Override
    public String toString() {
        return "LcTableParams[page=" + page
            + ", maxRows=" + maxRows
            + ", sortProp=" + sortProp
            + ", sortDir=" + sortDir
            + ", filters=" + filters + "]";
    }

    /**
     * Private no-arg constructor to prevent accidental instantiation via a
     * default public constructor and to signal this class is intentionally
     * constructed via the explicit public constructors above.
     */
    private LCTableParams() {
        // Hide implicit public no-arg constructor — delegate to canonical ctor
        this(1, 15, "", "asc", java.util.Collections.emptyMap());
    }
}
