<%
    String self_url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString();
    String oauth_client_id = CoreResources.getField("oauth.clientId");
	String oauth_account_management_url = CoreResources.getField("oauth.url") + "/login";

    String oauth_redirect_uri = self_url + CoreResources.getField("oauth.redirectUri") /* "/oauth"*/;
    String oauth_authorize_url = CoreResources.getField("oauth.authorizeUrl") +
            "?response_type=code&client_id="+oauth_client_id+"&"+
            "redirect_uri="+oauth_redirect_uri+"&scope=openid%20profile";
    String oauth_as_first_page = CoreResources.getField("oauth.as_login_page");
    if("true".compareTo(oauth_as_first_page)==0)
        response.sendRedirect(oauth_authorize_url);
%>

<%@ include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.akaza.openclinica.service.otp.TwoFactorService" %>
<%@ page import="org.springframework.web.servlet.support.ServletUriComponentsBuilder" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="org.akaza.openclinica.control.core.SecureController" %>
<%@ page import="org.akaza.openclinica.dao.core.CoreResources" %>


<!-- For Mantis Issue 6099 -->
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<c:if test="${userBean.name!=''}">
	<c:redirect url="/MainMenu"/>
</c:if>
<!-- End of 6099-->

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><fmt:message key="openclinica" bundle="${resword}"/></title>	
	<meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
	<meta http-equiv="X-UA-Compatible" content="IE=8" />
    
    <link rel="shortcut icon" type="image/x-icon" href="<c:url value='/images/favicon.ico'/>">
	<link rel="stylesheet" href="<c:url value='/includes/styles.css'/>" type="text/css"/>
	
	<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.min.js'/>"></script>
	<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-migrate-1.1.1.js'/>"></script>
	<script type="text/javascript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.blockUI.js'/>"></script>
	<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript.js'/>"></script>
	<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/ua-parser.min.js'/>"></script>
</head>

<%
ApplicationContext appContext = RequestContextUtils.findWebApplicationContext(request);
TwoFactorService factorService = (TwoFactorService) appContext.getBean("factorService");

request.setAttribute("factorService", factorService);
session.setAttribute("factorService", factorService);
%>

<body>
<!-- start of login/login.jsp -->
<!-- login_BG -->
    <div class="login-welcome">

    <!-- LibreClinica logo -->
	<%String ua = request.getHeader( "User-Agent" );
	String temp = "";
	String iev = "";
	if( ua != null && ua.indexOf( "MSIE" ) != -1 ) {
		temp = ua.substring(ua.indexOf( "MSIE" ),ua.length());
		iev = temp.substring(4, temp.indexOf(";"));
		iev = iev.trim();
	}
	if(iev.length() > 1 && Double.valueOf(iev)<7) {%>
	<div ID="logoIE6">&nbsp;</div>
	<%} else {%>
    <div ID="logo">&nbsp;</div>
  	<%}%>
    <!-- end LibreClinica logo -->




        <table>

    <script type="text/javascript">
        var parser = new UAParser();
        var showMessage = false;

        if (parser.getBrowser().name != 'Firefox' && parser.getBrowser().name !='Chrome') {
            showMessage = true;
        }

        if (showMessage) {
            document.write(
                "<tr><td align='center'><h4>" +
                " <fmt:message key="choose_browser" bundle="${restext}"/>" +
                "</h4></td></tr>"
            );
        }
    </script>
            </table>


		<div class="login-options">
			<a href="login/login" class="local-login">Libreclinica login</a>
			<a href="<%= oauth_account_management_url %>" class="oauth-login">Oauth Account Management</a>
			<a href="<%= oauth_authorize_url %>" class="oauth-login">Oauth login</a>
		</div>
</div>

<!-- end of login/login.jsp -->
<!--
<jsp:include page="./login-include/login-footer.jsp"/>
