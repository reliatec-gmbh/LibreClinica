/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.filter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

/**
 * A {@link HttpSessionRequestCache} that saves HTMX partial-page requests with
 * all {@code HX-*} headers stripped.
 *
 * <h3>Why this is needed</h3>
 * When the HTTP session expires while the user is on the AuditUserActivity page,
 * the next click on a pagination or sort link triggers an HTMX XHR that carries
 * the {@code HX-Request: true} header.  Spring Security's default
 * {@code HttpSessionRequestCache} saves that request – including all headers –
 * and, after a successful re-login, Spring Security's
 * {@code RequestCacheAwareFilter} wraps the follow-up browser GET with the
 * cached headers, replaying {@code HX-Request: true}.
 * {@code AuditUserActivityServlet} then mistakes the ordinary browser GET for an
 * HTMX partial request and returns only the bare table HTML fragment instead of
 * the full page.
 *
 * <h3>What this class does</h3>
 * When an HTMX request is detected, the request URL and parameters are saved
 * normally (so that Spring Security can redirect back to the exact page after
 * re-login), but all {@code HX-*} headers are stripped before saving.
 * {@code DefaultSavedRequest} copies headers at construction time, so the
 * stored entry will never contain {@code HX-Request}.  After re-login,
 * {@code RequestCacheAwareFilter} replays the saved URL without any {@code HX-*}
 * headers, and the servlet serves the full page as expected.
 */
public class HtmxAwareHttpSessionRequestCache extends HttpSessionRequestCache {

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            // Save the URL (so the user lands back where they were after re-login),
            // but strip all HX-* headers so the servlet does not mistake the
            // replayed request for an HTMX partial call.
            super.saveRequest(new HxHeaderStrippingRequestWrapper(request), response);
            return;
        }
        super.saveRequest(request, response);
    }

    /**
     * Wraps an {@link HttpServletRequest} and hides all headers whose names
     * start with the {@code HX-} prefix (case-insensitive).
     * {@link org.springframework.security.web.savedrequest.DefaultSavedRequest}
     * copies headers from the request at construction time via
     * {@link HttpServletRequest#getHeaderNames()} and
     * {@link HttpServletRequest#getHeaders(String)}, so this wrapper is
     * sufficient to keep {@code HX-*} headers out of the saved request.
     */
    private static final class HxHeaderStrippingRequestWrapper extends HttpServletRequestWrapper {

        HxHeaderStrippingRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        private static boolean isHtmxHeader(String name) {
            return name != null && name.toLowerCase().startsWith("hx-");
        }

        @Override
        public String getHeader(String name) {
            return isHtmxHeader(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return isHtmxHeader(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new java.util.ArrayList<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String name = original.nextElement();
                if (!isHtmxHeader(name)) {
                    names.add(name);
                }
            }
            return Collections.enumeration(names);
        }
    }
}
