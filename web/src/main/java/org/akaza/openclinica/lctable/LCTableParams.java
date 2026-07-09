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

import org.springframework.util.LinkedMultiValueMap;
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
     * The helper methods intParam, strParam and readFilters are used to parse and normalise the parameter values.
     *
     * <p><b>Important:</b> the values in {@code params} are expected to be already URL-decoded, as
     * they normally are when the map is populated by Spring MVC from an incoming request
     * (e.g. via {@code @RequestParam MultiValueMap<String, String>}). Neither this constructor nor
     * {@link #readFilters(MultiValueMap, List)} performs any decoding of parameter values.
     * Decoding an already-decoded value again would corrupt any value containing a literal {@code %}
     * would throw an {@code IllegalArgumentException} on a second decode attempt.
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
     *
     * <p><b>Precondition:</b> {@code queryString} must be the raw, still URL-encoded query string exactly
     * as it appears on the wire, e.g. the value returned by {@link javax.servlet.http.HttpServletRequest#getQueryString()},
     * which is <b>not</b> decoded by the servlet container. This constructor:<br>
     * 1. parses the {@code queryString} with {@link UriComponentsBuilder#fromUriString(String)},
     * which does not decode query parameter values,<br>
     * 2. then explicitly URL-decodes all parameter values before delegating to {@link #LCTableParams(MultiValueMap, LCTable)},
     * which in turn expects to receive already-decoded values (see its Javadoc).
     * <p>Passing an already-decoded string here would cause the decoding step to run against decoded input, which
     * throws an {@code IllegalArgumentException} for any value containing a literal {@code %}.
     *
     * @param queryString the query string from the request
     * @param table the {@link LCTable} instance (used in {@link LCTableParams} to get the list of allowed filter keys)
     */
    public LCTableParams(String queryString, LCTable<?> table) {
        // .build().encode() would double-encode; instead decode explicitly here,
        // at the one place that's actually receiving a raw/encoded map.
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
     * Reads all filter request parameters starting with {@value #PARAM_FILTER_PREFIX}.
     *
     * <p><b>Important:</b> values are copied as-is and are <b>not</b> URL-decoded by this method.
     * Callers must ensure {@code params} already contains decoded values — this is the case for a
     * {@code MultiValueMap} populated by Spring MVC from an incoming request, but not for one built
     * directly from a raw query string. Decoding here would risk double-decoding values that were
     * already decoded upstream, which throws {@code IllegalArgumentException} for any value
     * containing a literal {@code %} (e.g. {@code "100%"}).
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
