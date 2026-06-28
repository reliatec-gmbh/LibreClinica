package org.akaza.openclinica.lctable;

import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LCTableUtil {

    private LCTableUtil() {
        // Private constructor to prevent instantiation of this utility class
    }

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

}