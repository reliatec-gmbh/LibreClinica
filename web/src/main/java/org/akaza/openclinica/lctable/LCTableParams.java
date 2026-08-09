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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * An {@code LCTableParams} object is an immutable object holding the URL parameters of a request
 * to an {@link LCTable} (pagination, sorting, filtering, etc.).
 */
public final class LCTableParams {

    // -- URL parameter names --------------------------------------------------
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_MAX_ROWS = "maxRows";
    public static final String PARAM_SORT_PROP = "sortProp";
    public static final String PARAM_SORT_DIR = "sortDir";
    public static final String PARAM_SHOW_HIDDEN_COLS = "showHiddenCols";
    public static final String PARAM_FILTER_PREFIX = "q.";

    // --- URL parameters -------------------------------------------------------
    public final int page;
    public final int maxRows;
    public final String sortProp;
    public final String sortDir;
    public final Map<String, String> filters;
    public final boolean showHiddenCols;

    /** Explicitly whitelisted "sticky" parameters from incoming request (see {@link LCTable#stickyParamNames}). */
    public final Map<String, String> stickyParams;

    // --- it needs to be package-private for unit-testing, should not be called by regular users of the library ---
    LCTableParams(int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters) {
        this(page, maxRows, sortProp, sortDir, filters, false, Collections.emptyMap());
    }

    // --- it needs to be package-private for unit-testing, should not be called by regular users of the library ---
    LCTableParams(int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters, boolean showHiddenCols) {
        this(page, maxRows, sortProp, sortDir, filters, showHiddenCols, Collections.emptyMap());
    }

    // --- it needs to be package-private for unit-testing, should not be called by regular users of the library ---
    LCTableParams(int page, int maxRows, String sortProp, String sortDir, Map<String, String> filters, boolean showHiddenCols,
            Map<String, String> stickyParams) {
        this.page = page;
        this.maxRows = maxRows;
        this.sortProp = sortProp;
        this.sortDir = sortDir;
        this.filters = filters;
        this.showHiddenCols = showHiddenCols;
        this.stickyParams = stickyParams == null ? Collections.emptyMap() : stickyParams;
    }

    /**
     * Constructs an {@link LCTableParams} object from a Spring {@link org.springframework.util.MultiValueMap}. Expects values to already be URL-decoded.
     */
    public LCTableParams(MultiValueMap<String, String> params, LCTable<?> table) {
        this.page = Math.max(intParam(params, PARAM_PAGE, 1) - 1, 0);
        final int maxRowsParam = intParam(params, PARAM_MAX_ROWS, 15);
        this.maxRows  = maxRowsParam > 0 ? maxRowsParam : 15;
        this.sortProp = strParam(params, PARAM_SORT_PROP, "");
        this.sortDir  = strParam(params, PARAM_SORT_DIR, "asc");
        this.filters  = readFilters(params, table.getColumnNames());
        this.showHiddenCols = boolParam(params, PARAM_SHOW_HIDDEN_COLS);
        this.stickyParams = readStickyParams(params, table.getStickyParamNames());
    }

    /**
     * Constructs an {@link LCTableParams} object <b>from a raw, URL-encoded query string</b> {@code queryString}, decoding values explicitly before mapping.<br><br>
     * Typically, {@code queryString} is the value returned by {@link javax.servlet.http.HttpServletRequest#getQueryString()},
     * which is <b>not</b> decoded by the servlet container.<br><br>Passing an already-decoded string to this constructor could cause
     * a "double decoding" issue, possibly resulting in an {@code IllegalArgumentException} (e.g. for values containing a literal {@code %}).
     *
     * @param queryString the raw query string from the request
     * @param table the {@link LCTable} instance
     */
    public LCTableParams(String queryString, LCTable<?> table) {
        this(decodeQueryParams(UriComponentsBuilder.fromUriString("?" + (queryString == null ? "" : queryString))
            .build().getQueryParams()), table);
    }

    private static MultiValueMap<String, String> decodeQueryParams(MultiValueMap<String, String> raw) {
        MultiValueMap<String, String> decoded = new LinkedMultiValueMap<>();
        raw.forEach((key, vals) -> vals.forEach(v ->
            decoded.add(key, v == null ? null : UriUtils.decode(v, StandardCharsets.UTF_8))));
        return decoded;
    }

    /**
     * Extracts from {@code params} all filter parameters, i.e. those starting with {@value #PARAM_FILTER_PREFIX}.<br><br>
     * Values are copied as-is and are <b>not</b> URL-decoded by this method.
     * Therefore, they must already be URL-decoded by the caller.
     */
    public static Map<String, String> readFilters(MultiValueMap<String, String> params, List<String> allowedKeys) {
        Map<String, String> filters = new LinkedHashMap<>();
        if (params != null) {
            params.forEach((key, vals) -> {
                if (key.startsWith(PARAM_FILTER_PREFIX) && vals != null && !vals.isEmpty()) {
                    final String columnName = key.substring(PARAM_FILTER_PREFIX.length());
                    if (allowedKeys.contains(columnName)) {
                        final String firstVal = vals.get(0);
                        if (firstVal != null && !firstVal.isEmpty()) filters.put(columnName, firstVal);
                    }
                }
            });
        }
        return filters;
    }

    /**
     * Extracts from {@code params} the explicitly whitelisted "sticky" parameters (see {@link LCTable#stickyParamNames}),
     * omitting any such parameters that are absent or blank.<br><br>
     * Values are copied as-is and are <b>not</b> URL-decoded by this method.
     * Therefore, they must already be URL-decoded by the caller.
     */
    public static Map<String, String> readStickyParams(MultiValueMap<String, String> params, List<String> stickyParamNames) {
        Map<String, String> stickyParams = new LinkedHashMap<>();
        if (params != null && stickyParamNames != null) {
            for (String name : stickyParamNames) {
                String value = params.getFirst(name);
                if (value != null && !value.isEmpty()) {
                    stickyParams.put(name, value);
                }
            }
        }
        return stickyParams;
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

    /**
     * Reads a boolean request parameter, returning true if the parameter is present and
     * equals "true" (case-insensitive), or false otherwise.
     */
    public static boolean boolParam(MultiValueMap<String, String> params, String name) {
        String v = params == null ? null : params.getFirst(name);
        return v != null && v.trim().equalsIgnoreCase("true");
    }


    public String toString() {
        return "LcTableParams[page=" + page
            + ", maxRows=" + maxRows
            + ", sortProp=" + sortProp
            + ", sortDir=" + sortDir
            + ", filters=" + filters
            + ", showHiddenCols=" + showHiddenCols
            + ", stickyParams=" + stickyParams + "]";
    }

}
