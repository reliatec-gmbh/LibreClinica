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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;


public final class LCTableParams {

    // -- URL parameter names --------------------------------------------------
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_MAX_ROWS = "maxRows";
    public static final String PARAM_SORT_PROP = "sortProp";
    public static final String PARAM_SORT_DIR = "sortDir";
    public static final String PARAM_FILTER_PREFIX = "q.";

    // --- URL parameters -------------------------------------------------------
    public final int page;
    public final int maxRows;
    public final String sortProp;
    public final String sortDir;
    public final Map<String, String> filters;

    // --- it needs to be package-private for unit-testing, should not be called by regular users of the library ---
    LCTableParams(int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters) {
        this.page = page;
        this.maxRows = maxRows;
        this.sortProp = sortProp;
        this.sortDir = sortDir;
        this.filters = filters;
    }

    /**
     * Construct a LcTableParams object by reading request parameters from a Spring MultiValueMap
     * (as provided by a controller with {@code @RequestParam MultiValueMap<String,String> allParams}).
     *
     * <p>The helper methods intParam, strParam and readFilters are used to parse and
     * normalise the parameter values.
     */
    public LCTableParams(MultiValueMap<String, String> params, LCTable<?> table) {
        this.page = Math.max(intParam(params, PARAM_PAGE, 1) - 1, 0);
        final int maxRowsParam = intParam(params, PARAM_MAX_ROWS, 15);
        this.maxRows  = maxRowsParam > 0 ? maxRowsParam : 15;
        this.sortProp = strParam(params, PARAM_SORT_PROP, "");
        this.sortDir  = strParam(params, PARAM_SORT_DIR, "asc");
        this.filters  = readFilters(params, table.getColumnNames());
    }

    /**
     * Construct a LcTableParams object by reading request parameters from a legacy servlet query string
     * as returned by {@code HttpServletRequest.getQueryString()}
     * @param queryString the query string from the request
     * @param table the LCTable instance to get the allowed filter keys
     */
    public LCTableParams(String queryString, LCTable<?> table) {
        this(UriComponentsBuilder.fromUriString("?" + (queryString == null ? "" : queryString)).build().getQueryParams(), table);
    }

    /**
     * Reads all filter request parameters starting with {@value #PARAM_FILTER_PREFIX}.
     */
    public static Map<String, String> readFilters(MultiValueMap<String, String> params, List<String> allowedKeys) {
        Map<String, String> filters = new LinkedHashMap<>();
        if (params != null) {
            params.forEach((key, vals) -> {
                if (key.startsWith(PARAM_FILTER_PREFIX) && vals != null && !vals.isEmpty()) {
                    String columnName = key.substring(PARAM_FILTER_PREFIX.length());
                    if (allowedKeys.contains(columnName)) {
                        final String firstVal = vals.get(0);
                        if (firstVal != null && !firstVal.isEmpty()) {
                            filters.put(columnName, UriUtils.decode(firstVal, StandardCharsets.UTF_8));
                        }
                    }
                }
            });
        }
        return filters;
    }

    /**
     * Reads an integer request parameter, returning a default value if the parameter is missing or invalid.
     */
    public static int intParam(MultiValueMap<String, String> params, String name, int defaultValue) {
        String v = params == null ? null : params.getFirst(name);
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Reads a string request parameter, returning a default value if the parameter is missing.
     */
    public static String strParam(MultiValueMap<String, String> params, String name, String defaultValue) {
        String v = params == null ? null : params.getFirst(name);
        return v == null ? defaultValue : v;
    }


    public String toString() {
        return "LcTableParams[page=" + page
            + ", maxRows=" + maxRows
            + ", sortProp=" + sortProp
            + ", sortDir=" + sortDir
            + ", filters=" + filters + "]";
    }

}
