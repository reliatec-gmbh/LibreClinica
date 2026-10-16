/*
* LibreClinica is distributed under the
* GNU Lesser General Public License (GNU LGPL).

* For details see: https://libreclinica.org/license
* copyright (C) 2026 LibreClinica
*/
package org.akaza.openclinica.lctable;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** HTTP response support for endpoints offering an HTML fragment and a full page. */
public final class HtmxResponseSupport {

    private HtmxResponseSupport() {
    }

    /**
     * Adds the response variation header and writes HTML when HX-Request is present.
     * Call only after excluding downloads, print views and other non-fragment routes.
     * The caller retains ownership of flushing and closing the response. Normal requests
     * return false without obtaining a writer or changing the response content type.
     *
     * @return true when the fragment was written; the caller must stop page handling
     * @throws IOException if obtaining the response writer fails; no page fallback is safe
     */
    public static boolean writeFragmentIfRequested(HttpServletRequest request, HttpServletResponse response, String html) throws IOException {
        response.addHeader("Vary", "HX-Request");
        if (request.getHeader("HX-Request") == null) {
            return false;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
        return true;
    }
}