<%@ page contentType="text/html; charset=UTF-8" %>

<!-- This JSP fragment is used to include in a JSP page everything that is needed for HTMX to work properly
      (especially for the implementation of / in connection with the LCTable library)
-->

<!-- Load HTMX, which is provided by the webjar declared in web/pom.xml -->
<script src="${pageContext.request.contextPath}/webjars/htmx.org/2.0.9/dist/htmx.min.js"></script>

<!-- Load the 'idiomorph' HTMX extension, which is provided by the webjar declared in web/pom.xml -->
<script src="${pageContext.request.contextPath}/webjars/idiomorph/0.7.4/dist/idiomorph-ext.js"></script>

<!-- The following is needed to avoid problems with browser back/forward navigation buttons. See https://htmx.org/docs/#history -->
<script>
if (!document.querySelector('meta[name="htmx-config"]'))
    document.head.insertAdjacentHTML('beforeend', '<meta name="htmx-config" content=\'{"historyRestoreAsHxRequest": false}\'>');
</script>

<!-- Session-expiry redirect guard for HTMX-based LCTable requests -->
<script src="${pageContext.request.contextPath}/js/htmx-session-expiry-guard.js"></script>
