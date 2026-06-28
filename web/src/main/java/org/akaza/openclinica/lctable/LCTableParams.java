package org.akaza.openclinica.lctable;

import static org.akaza.openclinica.lctable.LCTableUtil.*;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;


public final class LCTableParams {

    // -- URL parameters -------------------------------------------------------
    public final int page;
    public final int maxRows;
    public final String sortProp;
    public final String sortDir;
    public final Map<String, String> filters;

    /**
     * Construct a LcTableParams object by reading request parameters from a Spring MultiValueMap
     * (as provided by a controller with {@code @RequestParam MultiValueMap<String,String> allParams}).
     *
     * <p>The helper methods in {@link LCTableUtil} are used to parse and
     * normalise the parameter values.
     */
    public LCTableParams(MultiValueMap<String, String> params, List<String> allowedFilterKeys) {
        this.page = Math.max(intParam(params, LCTableUtil.PARAM_PAGE, 1) - 1, 0);
        final int maxRowsParam = intParam(params, LCTableUtil.PARAM_MAX_ROWS, 15);
        this.maxRows  = maxRowsParam > 0 ? maxRowsParam : 15;
        this.sortProp = strParam(params, LCTableUtil.PARAM_SORT_PROP, "");
        this.sortDir  = strParam(params, LCTableUtil.PARAM_SORT_DIR, "asc");
        this.filters  = readFilters(params, allowedFilterKeys);
    }


    public String toString() {
        return "LcTableParams[page=" + page
            + ", maxRows=" + maxRows
            + ", sortProp=" + sortProp
            + ", sortDir=" + sortDir
            + ", filters=" + filters + "]";
    }

}
