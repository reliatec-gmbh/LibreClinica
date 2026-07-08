/**
 * Session-expiry redirect guard for HTMX requests.
 *
 * When the HTTP session expires, the next HTMX pagination/sort request is
 * intercepted by Spring Security, which responds with a 302 to the login page.
 * Browsers transparently follow 302 redirects inside XHR, so HTMX receives
 * a 200 OK containing the full login-page HTML and would normally inject it
 * into the table's swap target.
 *
 * This listener detects that the XHR was redirected to the login page
 * (xhr.responseURL contains the known login path) and, instead of letting
 * HTMX swap the login HTML into the panel div, cancels the swap and performs
 * a full-page navigation to the login URL. The user sees a proper login
 * screen and, after re-login, Spring Security redirects to /MainMenu
 * (the HtmxAwareHttpSessionRequestCache ensures the HTMX partial request
 * is never saved, so no broken-page replay can occur on the way back).
 */
document.body.addEventListener('htmx:beforeSwap', function (evt) {
    var responseUrl = evt.detail.xhr && evt.detail.xhr.responseURL;
    if (responseUrl && responseUrl.indexOf('/pages/login/login') !== -1) {
        evt.detail.shouldSwap = false;
        window.location.href = responseUrl;
    }
});
