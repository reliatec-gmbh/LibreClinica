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

import org.xmlet.htmlapifaster.CustomAttributeGroup;
import org.xmlet.htmlapifaster.Element;
import org.xmlet.htmlapifaster.Td;

import java.util.function.Consumer;

public class LCTableUtil {
    public static final String TIMESTAMP_FILTER_FOR_HTML_VALIDATION =
        "(?:(?:(?:00|20)(?:00|0[48]|[2468][048]|[13579][26])|(?:0[1-9]|1\\d)(?:0[48]|[2468][048]|[13579][26]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-9]))?))?|(?:(?:00|20)(?:0[1-35-79]|[13579][01345789]|[2468][1-35-79])|(?:0[1-9]|1\\d)(?:00|0[1-35-79]|[13579][01345789]|[2468][1-35-79]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-8]))?))?)(?: (?:[01]\\d|2[0-3])(?::[0-5]\\d)?)?";

    public static final String TIMESTAMP_FILTER_MESSAGE =
        "Please enter a valid format: yyyy, yyyy-mm, yyyy-mm-dd, yyyy-mm-dd hh, or yyyy-mm-dd hh:mm (years up to 2099)";

    private LCTableUtil() {
        // Private constructor to prevent instantiation of this utility class
    }

    public static final String NO_HX_INCLUDE = null;          // just for better readability in method calls
    public static final String NO_HX_TRIGGER = null;          // just for better readability in method calls

    public static <T extends CustomAttributeGroup<T, ?>> Consumer<T> hxGetAttrs(
        String hxGet, String hxInclude, String hxTarget, String hxTrigger
    ) {
        return el -> {
            el.addAttr(HX_GET, hxGet);
            if (hxInclude != null) el.addAttr(HX_INCLUDE, hxInclude);
            el.addAttr(HX_TARGET, hxTarget);
            el.addAttr(HX_SWAP, "outerHTML");
            if (hxTrigger != null) el.addAttr(HX_TRIGGER, hxTrigger);
            el.addAttr(HX_PUSH_URL, "true");
        };
    }

    /**
     * Convenience helper to construct an icon with a link.
     * Use like: td.of(LCTableUtil.iconLink("View", href, "images/bt_View.gif", "View"));
     *
     * @param altTitle  title/alt text to put on the anchor (and used as title on the anchor)
     * @param href      href for the anchor
     * @param imgSrc    src for the inner img
     * @param imgAlt    alt text for the inner img
     */
    public static <T extends Element<?, ?>> Consumer<Td<T>> linkIcon(String altTitle, String href, String imgSrc, String imgAlt) {
        return td -> td
            .a().attrHref(href).attrTitle(altTitle)
                .img().attrSrc(imgSrc).attrAlt(imgAlt).__()
            .__();  // close a()
    }

}
