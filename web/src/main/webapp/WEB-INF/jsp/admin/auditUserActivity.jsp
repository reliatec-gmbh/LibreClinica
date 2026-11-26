<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>


<%-- JMesa scripts are only needed when the JMesa rendering path is active.
     Loading them unconditionally causes prototype.js to wire event observers
     to non-existent table structures, which triggers
     "element.dispatchEvent is not a function" errors (especially after
     session-expiry redirects via the login page). --%>
<c:choose>
    <c:when test="${tableRenderingMode == 'jmesa'}">
        <link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
    </c:when>
    <c:otherwise>
        <link rel="stylesheet" href="includes/lctable/lctable.css" type="text/css">
    </c:otherwise>
</c:choose>
<c:if test="${tableRenderingMode == 'jmesa'}">
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-migrate-3.4.1.min.js"></script>

    <script type="text/javascript">
        function onInvokeAction(id, action) {
            if (id.indexOf('userLogins') == -1) {
                setExportToLimit(id, '');
            }
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '${pageContext.request.contextPath}/AuditUserActivity?' + parameterString;
        }
    </script>
</c:if>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1><span class="title_Manage"><fmt:message key="audit_user_activity" bundle="${resword}"/></span></h1>

<jsp:useBean id="now" class="java.util.Date" />
<P><I><fmt:message key="server_time_info" bundle="${resword}"/> <fmt:formatDate value="${now}" pattern="yyyy-MM-dd HH:mm"/>.</I></P>
<div id="auditUserLoginDiv">
    <%-- IMPORTANT: the LCTable (HtmlFlow) rendering path renders its own <form> around the whole
         table (see LCTable.renderTableHtml()) -- it must NOT also be wrapped in a <form> here, or
         the browser will treat the two nested <form>s as a single merged form (nested <form>s are
         invalid HTML; the inner start tag is simply dropped by the parser), causing any hidden
         input declared in *this* JSP (e.g. hardcoded "module"/"crfId") to collide with LCTable's
         own same-named hidden inputs (e.g. a sticky parameter of the same name) once both are
         serialized together by HTMX's "closest form" -- see LCTable's class-level javadoc for
         details, and see the analogous fix in managestudy/findSubjects.jsp.
         Only the legacy JMesa rendering path (native form-based pagination/sort/filter resubmission,
         via createHiddenInputFieldsForLimitAndSubmit()) actually needs a surrounding <form>. --%>
    <c:choose>
        <c:when test="${tableRenderingMode == 'jmesa'}">
            <form  action="${pageContext.request.contextPath}/AuditUserActivity">
                <input type="hidden" name="module" value="admin">
                <input type="hidden" name="crfId" value="${crf.id}">
                ${auditUserLoginHtml}
            </form>
        </c:when>
        <c:otherwise>
            ${auditUserLoginHtml}
        </c:otherwise>
    </c:choose>
</div>


<br>
<input type="button" onclick="confirmExit('ListUserAccounts');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>

<!-- Include everything that is needed for proper use of HTMX with LCTable -->
<jsp:include page="../include/useLCTable.jsp"/>

<jsp:include page="../include/footer.jsp"/>