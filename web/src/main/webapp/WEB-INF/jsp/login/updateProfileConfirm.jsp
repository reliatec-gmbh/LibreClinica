<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/home-header.jsp"/>

<!-- start of login/updateProfileConfirm.jsp -->
<!-- alerts -->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
	<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">
			<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_collapse.gif" class="sidebar_collapse_expand"></a>
			<b><fmt:message key="instructions" bundle="${resword}"/></b>
			<div class="sidebar_tab_content"></div>
		</td>	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">
			<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_expand.gif" class="sidebar_collapse_expand"></a>
			<b><fmt:message key="instructions" bundle="${resword}"/></b>
		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="session" id="userBean11" class="org.akaza.openclinica.bean.login.UserAccountBean"/>
<h1><span class="title_manage"><fmt:message key="confirm_user_profile_updates" bundle="${resword}"/></span></h1>

<form action="UpdateProfile?action=submit" method="post">
<div class="textbox_center">
	<table class="contenttable">
		<tr><td class="table_header_column"><fmt:message key="first_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.firstName}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.lastName}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.email}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.institutionalAffiliation}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="default_active_study" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${newActiveStudy.name}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.passwdChallengeQuestion}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.passwdChallengeAnswer}"/></td></tr>
		<tr><td class="table_header_column"><fmt:message key="phone" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.phone}"/></td></tr>
		<c:if test="${factorService.twoFactorActivated}">
			<tr><td class="table_header_column"><fmt:message key="auth_type" bundle="${resword}"/>:</td><td class="table_cell"><fmt:message key="auth_type.${userBean1.authtype}" bundle="${resword}"/></td></tr>
		</c:if>
	</table>
</div>

<input type="submit" name="Submit" value="<fmt:message key="update_profile" bundle="${resword}"/>" class="button_long">
<input type="button" onclick="confirmCancel('MainMenu');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
	
</form>
<jsp:include page="../include/footer.jsp"/>
