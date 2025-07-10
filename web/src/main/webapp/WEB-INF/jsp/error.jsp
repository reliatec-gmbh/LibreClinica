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

<c:choose>
	<c:when test="${userBean != null && userRole != null && userRole.role.name != 'invalid' && passwordExpired == 'no'}">
		<!-- homeheader.jsp BEGIN -->
		<jsp:include page="include/home-header.jsp"/>
		<!-- homeheader.jsp END -->
		<jsp:include page="include/sidebar.jsp"/>
	</c:when>
	<c:otherwise>
		<jsp:include page="login-include/login-header.jsp"/>
		<table>
			<tr><td class="sidebar" valign="top">
				<br /><b><a href="${pageContext.request.contextPath}/j_spring_security_logout"><fmt:message key="logout" bundle="${restext}"/></a></b>
				<br /><b><a href="${pageContext.request.contextPath}/MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a></b>
			</td>
			<td class="content">
	</c:otherwise>
</c:choose>

<!-- start of error.jsp -->
<h1><span class="title_manage"><fmt:message key="an_error_has_ocurred" bundle="${resword}"/></span></h1>

<font class="bodytext">
<c:set var="referer" value="MainMenu"/>
<c:forEach var="refererValue" items="${pageContext.request.headerNames}">
	<c:if test="${refererValue eq 'referer'}">
		<!-- found it! -->
		<c:set var="referer" value="${header[refererValue]}"/>
	</c:if>
</c:forEach>
<fmt:message key="error_page" bundle="${resword}">
	<%--<fmt:param><%=request.getHeader("Referer")%></fmt:param>--%>
	<%-- tbh 02/2010 remove HTML/XML from the referer name --%>
	<fmt:param><c:out value="${referer}"/></fmt:param>
	<fmt:param><%=SQLInitServlet.getField("mail.errormsg")%></fmt:param>
</fmt:message>

</font>
</td></tr></table>

<c:choose>
	<c:when test="${userBean != null && userRole != null && userRole.role.name != 'invalid' && passwordExpired == 'no'}">
		<jsp:include page="include/footer.jsp"/>
	</c:when>
	<c:otherwise>
		<jsp:include page="login-include/login-footer.jsp"/>
	</c:otherwise>
</c:choose>
<!-- end of error.jsp -->
