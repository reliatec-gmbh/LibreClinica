<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
        function onInvokeAction(id,action) {
            if(id.indexOf('databaseChangeLogs') == -1)  {
                setExportToLimit(id, '');
            }
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '${pageContext.request.contextPath}/AuditDatabase;'
        }
    </script>
</c:if>
<c:if test="${tableRenderingMode == 'htmlflow'}">
    <%-- Audit Database is the only page where the generated LCTable form picks up visible
         bottom box space. Keep this normalization page-scoped rather than changing every table. --%>
    <style type="text/css">
        #auditDatabaseDiv .lctable > form {
            display: block;
            margin: 0;
            padding: 0;
            line-height: 0;
        }
        #auditDatabaseDiv .lctable > form > table {
            line-height: normal;
        }
    </style>
</c:if>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

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

<h1><span class="title_manage"><fmt:message key="audit_database" bundle="${resword}"/></span></h1>

<jsp:useBean id="now" class="java.util.Date" />
<P><I><fmt:message key="server_time_info" bundle="${resword}"/> <fmt:formatDate value="${now}" pattern="yyyy-MM-dd hh:mm"/>.</I></P>
<div id="auditDatabaseDiv">
    <c:choose>
        <c:when test="${tableRenderingMode == 'jmesa'}">
            <form action="${pageContext.request.contextPath}/AuditDatabase">
                <input type="hidden" name="module" value="admin">
                ${auditDatabaseHtml}
            </form>
        </c:when>
        <c:otherwise>
            ${auditDatabaseHtml}
        </c:otherwise>
    </c:choose>
</div>


<br>
<input type="button" onclick="confirmExit('ListUserAccounts');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>

<c:choose>
    <c:when test="${userBean.sysAdmin && module=='admin'}">
        <c:import url="../include/workflow.jsp">
            <c:param name="module" value="admin"/>
        </c:import>
    </c:when>
    <c:otherwise>
        <c:import url="../include/workflow.jsp">
            <c:param name="module" value="manage"/>
        </c:import>
    </c:otherwise>
</c:choose>
<!-- Include everything that is needed for proper use of HTMX with LCTable -->
<jsp:include page="../include/useLCTable.jsp"/>

<jsp:include page="../include/footer.jsp"/>