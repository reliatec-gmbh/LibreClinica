package org.akaza.openclinica.lctable;

import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LCTableUtil {

    private LCTableUtil() {
        // Private constructor to prevent instantiation of this utility class
    }

    // -- URL parameter names --------------------------------------------------
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_MAX_ROWS = "maxRows";
    public static final String PARAM_SORT_PROP = "sortProp";
    public static final String PARAM_SORT_DIR = "sortDir";
    public static final String PARAM_FILTER_PREFIX = "q.";

    // -- HTMX attribute names (use constants to avoid repeating string literals)
    public static final String HX_GET = "hx-get";
    public static final String HX_TARGET = "hx-target";
    public static final String HX_SWAP = "hx-swap";
    public static final String HX_PUSH_URL = "hx-push-url";
    public static final String HX_TRIGGER = "hx-trigger";
    public static final String HX_INCLUDE = "hx-include";
    // -- Generic static request-parameter helpers -----------------------------

    // Make nullSafe static so it can be statically imported and used from templates/helpers
    public static String nullSafe(String s) {
        return s != null ? s : "";
    }

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

    public static String strParam(MultiValueMap<String, String> params, String name, String defaultValue) {
        String v = params == null ? null : params.getFirst(name);
        return v == null ? defaultValue : v;
    }

    /**
     * Reads all request parameters whose name starts with {@value #PARAM_FILTER_PREFIX}.
     */
    public static Map<String, String> readFilters(MultiValueMap<String, String> params, List<String> allowedKeys) {
        Map<String, String> filters = new LinkedHashMap<>();
        if (params != null) {
            params.forEach((key, vals) -> {
                if (key.startsWith(PARAM_FILTER_PREFIX) && vals != null && !vals.isEmpty() && allowedKeys.contains(key.substring(PARAM_FILTER_PREFIX.length()))) {
                    String firstVal = vals.get(0);
                    if (firstVal != null && !firstVal.isEmpty()) {
                        filters.put(key.substring(PARAM_FILTER_PREFIX.length()), firstVal);
                    }
                }
            });
        }
        return filters;
    }

}