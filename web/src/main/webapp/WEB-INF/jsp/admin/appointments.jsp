<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>


<c:choose>
    <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin}">
        <c:import url="../include/admin-header.jsp"/>

    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${userRole.manageStudy}">
                <c:import url="../include/managestudy-header.jsp"/>
            </c:when>
            <c:otherwise>
                <c:import url="../include/home-header.jsp"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>


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

<jsp:useBean scope='request' id='studyToView' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>

<h1><div class="title_manage">Details of subject</div></h1>

<table class="summaryTable">
    <tbody>
        <tr>
            <th>Subject id</th>
            <td>${subject.label}</td>
        </tr>
        <tr>
            <th>PID</th>
            <td>${subject.secondaryLabel}</td>
        </tr>
        <tr>
            <th>Regimen</th>
            <td>${subject.secondaryLabel}</td>
        </tr>
        <tr>
            <th>gender</th>
            <td>${subject.gender}</td>
        </tr>
        <tr>
            <th>Enrollment date</th>
            <td>${subject.enrollmentDate}</td>
        </tr>
    </tbody>
</table>

<c:if test="${!appointments.isEmpty()}">
<h1><div class="title_manage">Details of schedule</div></h1>

<table class="summaryTable" align="center">
    <thead>
    <tr>
        <th>App no</th>
        <th>Date</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${appointments}" var="appointment">
        <tr>
            <td>${appointment.key}</td>
            <td>${appointment.value}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</c:if>
<jsp:include page="../include/footer.jsp"/>


