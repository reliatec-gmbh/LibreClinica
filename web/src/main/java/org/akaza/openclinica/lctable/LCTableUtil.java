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

import static org.akaza.openclinica.lctable.LCTable.*;

import org.springframework.web.util.HtmlUtils;
import org.xmlet.htmlapifaster.CustomAttributeGroup;
import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.FlowContent;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Consumer;

public class LCTableUtil {
    private LCTableUtil() {}    // Private constructor to prevent instantiation of this utility class

    // --- Timestamp filter regex for HTML5 validation and thread-safe timestamp-to-string conversion ---
    public static final String TIMESTAMP_FILTER_FOR_HTML_VALIDATION =
        "(?:(?:(?:00|20)(?:00|0[48]|[2468][048]|[13579][26])|(?:0[1-9]|1\\d)(?:0[48]|[2468][048]|[13579][26]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-9]))?))?|(?:(?:00|20)(?:0[1-35-79]|[13579][01345789]|[2468][1-35-79])|(?:0[1-9]|1\\d)(?:00|0[1-35-79]|[13579][01345789]|[2468][1-35-79]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-8]))?))?)(?: (?:[01]\\d|2[0-3])(?::[0-5]\\d)?)?";

    public static final String TIMESTAMP_FILTER_MESSAGE =
        "Please enter a valid format: yyyy, yyyy-MM, yyyy-MM-dd, yyyy-MM-dd hh, or yyyy-MM-dd hh:mm (years up to 2099)";

    public static String timestampToString(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // --- HTMX helpers ---
    public static final String NO_HX_INCLUDE = null;          // just for better readability in method calls
    public static final String NO_HX_TRIGGER = null;          // just for better readability in method calls

    public static <T extends CustomAttributeGroup<T, ?>> Consumer<T> hxGetAttrs(
        String hxGet, String hxInclude, String hxTarget, String hxTrigger, boolean ignoreActiveValue
    ) {
        return el -> {
            el.addAttr(HX_GET, hxGet);
            if (hxInclude != null) el.addAttr(HX_INCLUDE, hxInclude);
            el.addAttr(HX_TARGET, hxTarget);
            el.addAttr("hx-select", hxTarget);
            if (ignoreActiveValue) {
                // For incremental input filters and the like, where the user may continue typing while the request
                // is in flight: 'ignoreActiveValue:true' prevents replacing the input with the value from the response
                el.addAttr(HX_SWAP, "morph:{morphStyle:'outerHTML',ignoreActiveValue:true}");
            } else {
                // Other cases: replace the entire target element with the response (outerHTML)
                el.addAttr(HX_SWAP, "outerHTML");
            }
            if (hxTrigger != null) el.addAttr(HX_TRIGGER, hxTrigger);
            el.addAttr(HX_PUSH_URL, "true");
        };
    }

    public static <T extends CustomAttributeGroup<T, ?>> Consumer<T> hxGetAttrs(
        String hxGet, String hxInclude, String hxTarget, String hxTrigger
    ) {
        return hxGetAttrs(hxGet, hxInclude, hxTarget, hxTrigger, false);
    }

    public static <T extends CustomAttributeGroup<T, ?>> Consumer<T> hxGetAttrsIgnoreActiveValue(
        String hxGet, String hxInclude, String hxTarget, String hxTrigger
    ) {
        return hxGetAttrs(hxGet, hxInclude, hxTarget, hxTrigger, true);
    }

    /**
     * Escapes '%' and '_' (SQL LIKE wildcards) so that certain existing filters (such as AuditUserLoginFilter),
     * which use the "%" + value + "%" pattern, match them literally instead of as wildcards.
     * Relies on PostgreSQL's LIKE operator treating '\' as the default escape
     * character even without an explicit ESCAPE clause.
     */
    public static String escapeSqlLikeWildcards(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    /** Applies non-empty values as {@code data-test-*} attributes to an HtmlFlow element. */
    public static <T extends CustomAttributeGroup<T, ?>> Consumer<T> testAttrs(Map<String, String> attributes) {
        return element -> {
            if (attributes != null && !attributes.isEmpty()) {
                attributes.forEach((key, value) -> {
                    if (value != null && !value.isEmpty()) {
                        String safeValue = HtmlUtils.htmlEscape(value);
                        element.addAttr("data-test-" + key, safeValue);
                    }
                });
            }
        };
    }

    // --- generate HTML for various common elements ---
    /**
     * Constructs an action link, optionally with a text label if {@code includeText} is true. If {@code includeText} is false, uses
     * a custom {@code data-tooltip} (implemented in {@code lctable.js} and {@code lctable.css}) instead of the native {@code title} attribute
     * (which can be flaky) to display the {@code altText}.
     *
     * @param id          unique HTML element id or null if none
     * @param altText     accessible name / tooltip text
     * @param href        href for the anchor
     * @param imgSrc      src (relative to "images/") for the inner img
     * @param includeText if true, renders {@code altText} as a visible label; if false, icon-only with a tooltip
     * @param testAction  stable, locale-independent action name used by automated tests
     */
    public static <T extends Element<T, Z> & FlowContent<T, Z>, Z extends Element> Consumer<T> actionLink(
            String id, String altText, SafeUrl href, String imgSrc, boolean includeText, String testAction) {
        return actionLink(id, altText, href.toUriString(), imgSrc, includeText, testAction);
    }

    /** Icon-only variant of {@link #actionLink(String, String, SafeUrl, String, boolean, String)} (no visible text label). */
    public static <T extends Element<T, Z> & FlowContent<T, Z>, Z extends Element> Consumer<T> actionLink(
            String id, String altText, SafeUrl href, String imgSrc, String testAction) {
        return actionLink(id, altText, href, imgSrc, false, testAction);
    }

    /**
    * Same as {@link #actionLink(String, String, SafeUrl, String, boolean, String)}, but for the rare cases (e.g. the REST
     * "print" links) where the href is already a fully-built, pre-encoded string rather than a {@link SafeUrl}
     * (re-encoding it via {@code SafeUrl} would corrupt already-percent-encoded path segments).
     */
    public static <T extends Element<T, Z> & FlowContent<T, Z>, Z extends Element> Consumer<T> actionLink(
            String id, String altText, String href, String imgSrc, boolean includeText, String testAction) {
        return container -> {
            var anchor = container.a().attrClass("action-link").attrHref(href).addAttr("aria-label", altText);
            if (id != null) {
                anchor = anchor.attrId(id);
            }
            if (testAction != null) {
                anchor = anchor.addAttr("data-testid", "action-link")
                    .addAttr("data-test-action", testAction);
            }
            if (!includeText) {
                anchor = anchor.addAttr("data-tooltip", altText);
            }
            var a = anchor.img().attrSrc("images/" + imgSrc).attrAlt(altText).__();
            if (includeText) {
                a.text(" " + altText);
            }
            a.__();  // close a()
        };
    }

    /** Icon-only variant of {@link #actionLink(String, String, String, String, boolean, String)} (no visible text label). */
    public static <T extends Element<T, Z> & FlowContent<T, Z>, Z extends Element> Consumer<T> actionLink(
            String id, String altText, String href, String imgSrc, String testAction) {
        return actionLink(id, altText, href, imgSrc, false, testAction);
    }

}
