package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableUtil.*;

import java.util.Map;

import org.springframework.util.MultiValueMap;


public final class LCTableParams {

    // -- URL parameters -------------------------------------------------------
    public final int page;
    public final int maxRows;
    public final String sortProp;
    public final String sortDir;
    public final Map<String, String> filters;

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
            // URL page is 1-based (page=1 → first page); convert to 0-based for internal use.
            // page=0 in URL is treated as page=1 (first page) for robustness.
            Math.max(intParam(params, LCTableUtil.PARAM_PAGE, 1) - 1, 0),
            intParam(params, LCTableUtil.PARAM_MAX_ROWS, 15),
            strParam(params, LCTableUtil.PARAM_SORT_PROP, ""),
            strParam(params, LCTableUtil.PARAM_SORT_DIR, "asc"),
            readFilters(params)
        );
    }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString  (mirrors record semantics)
    // -------------------------------------------------------------------------

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
        // Hide implicit public no-arg constructor - delegate to canonical constructor
        this(1, 15, "", "asc", java.util.Collections.emptyMap());
    }
}
