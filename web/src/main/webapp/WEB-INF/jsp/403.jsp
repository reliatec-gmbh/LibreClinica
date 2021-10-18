<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.akaza.openclinica.web.SQLInitServlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope="session" id="passwordExpired" class="java.lang.String"/>

<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<c:choose>
	<c:when test="${userBean != null && userRole != null && userRole.role.name != 'invalid' && passwordExpired == 'no'}">
		<!-- homeheader.jsp BEGIN -->
		<jsp:include page="include/home-header.jsp"/>
		<!-- homeheader.jsp END -->
		<jsp:include page="include/sidebar.jsp"/>
	</c:when>
	<c:otherwise>
		<jsp:include page="login-include/login-header.jsp"/>
		<jsp:include page="include/userbox-inactive.jsp"/>
		<table>
			<tr><td class="sidebar" valign="top">
				<br /><b><a href="${pageContext.request.contextPath}/j_spring_security_logout"><fmt:message key="logout" bundle="${restext}"/></a></b>
				<br /><b><a href="${pageContext.request.contextPath}/MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a></b>
			</td>
			<td class="content" valign="top">
	</c:otherwise>
</c:choose>

<!-- start of 403.jsp -->
<h1><span class="title_manage"><fmt:message key="403_error_msg_header" bundle="${resword}"/></span></h1> 
<c:if test="${! empty formMessages}">
    <div id="errorMessagesContainer" class="aka_err_message">
        <ul>
            <c:forEach var="formMsg" items="${formMessages}">
                <li class='form_msg'><label for="<c:out value="${formMsg.key}" />"><c:out value="${formMsg.value}" /></label></li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<font class="bodytext">
	<fmt:message key="403_error_msg_body" bundle="${resword}"/>
</font>
</td></tr></table>

<!-- end of 403.jsp -->
<c:choose>
	<c:when test="${userBean != null && userRole != null && userRole.role.name != 'invalid' && passwordExpired == 'no'}">
		<jsp:include page="include/footer.jsp"/>
	</c:when>
	<c:otherwise>
		<jsp:include page="login-include/login-footer.jsp"/>
	</c:otherwise>
</c:choose>
