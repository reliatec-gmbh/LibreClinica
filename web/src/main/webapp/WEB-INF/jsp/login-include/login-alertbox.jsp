<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- start of login/login-alertbox.jsp, which is used when you enter a wrong account and/or pass word-->

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 

<% 
    String action = request.getParameter("action");
    if (action!=null) {
       if (action.equals("errorLogin")) { 
%>
    <div class="alertbox_center"><fmt:message key="password_failed" bundle="${restext}"/></div>
<%
       }  
    }
%>

<% 
    if (action!=null) {
       if (action.equals("errorLocked")) { 
%>
    <div class="alertbox_center"><fmt:message key="account_locked" bundle="${restext}"/></div>
<%
       } 
    }

%>

<jsp:include page="../include/showPageMessages.jsp" />
<!-- end of login/login-alertbox.jsp -->
