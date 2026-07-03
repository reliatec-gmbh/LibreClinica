/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

/**
 * A {@link HttpSessionRequestCache} that refuses to save HTMX partial-page
 * requests.
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
 * By simply not saving HTMX requests, the cache stays empty after a
 * session-expiry redirect triggered by an HTMX call.  After re-login, Spring
 * Security falls back to the configured {@code defaultTargetUrl} ({@code /MainMenu})
 * rather than trying to replay the stale partial request.
 */
public class HtmxAwareHttpSessionRequestCache extends HttpSessionRequestCache {

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            // HTMX partial requests must not be saved: replaying them after
            // re-login would cause the RequestCacheAwareFilter to wrap the
            // browser GET with a stale HX-Request header, making the servlet
            // serve a bare HTML fragment instead of the full page.
            return;
        }
        super.saveRequest(request, response);
    }
}
