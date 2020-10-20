<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>

<%-- Standard Header for second level Views (e.g. WEB-INF/jsp/admin/*.jsp) that are handled by Spring Controller --%>
<jsp:include page="../include/managestudy_top_pages_new.jsp"/>

<!-- Alert and messages -->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- Instructions -->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../../images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../../images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

    </td>
</tr>

<!-- Other info -->
<jsp:include page="../include/sideInfo.jsp"/>

<!-- View main content -->
<div style="width: 450px">

    <script type="text/javascript" src="../../includes/jmesa/jquery.min.js"></script>
    <script type="text/javascript" src="../../includes/jmesa/jquery.blockUI.js"></script>
    <script type="text/javascript" src="../../includes/jmesa/jquery-migrate-1.1.1.js"></script>

    <table style="border-collapse: separate; border-spacing: 10px;">
        <tr>
            <td>
                <!--  View title -->
                <h1><span class="title_manage"><fmt:message key="listLdapUsers.header.title" bundle="${resword}"/></span></h1>
                <span>
                    <!-- LDAP lookup form -->
                    <form action="" method="get">
                        <span class="aka_font_general">
                            <label for="filter"><fmt:message key="listLdapUsers.filter.label" bundle="${resword}"/></label>
                        </span>
                        <input type="text" id="filter" name="filter" value="<c:out value="${param.filter}"/>"/>
                        <input type="submit" class="button_search" value="<fmt:message key="listLdapUsers.filter.submit" bundle="${resword}"/>"/>
                    </form>
                </span>

                <!-- When filter parameter is set, username lookup was already executed on LDAP and table of results should be shown -->
                <c:if test="${not empty param.filter }">
                    <table style="border-collapse: collapse; border-spacing: 0;">
                        <tr>
                            <td>
                                <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
                                    <div class="tablebox_center">
                                        <table style="border-collapse: collapse; border-spacing: 0;">
                                            <thead>
                                            <tr>
                                                <td class="table_header_row_left">
                                                    <fmt:message key="listLdapUsers.list.table.header.username.label" bundle="${resword}"/>&nbsp;</td>
                                                <td class="table_header_row">
                                                    <fmt:message key="listLdapUsers.list.table.header.firstName.label" bundle="${resword}"/>&nbsp;</td>
                                                <td class="table_header_row">
                                                    <fmt:message key="listLdapUsers.list.table.header.lastName.label" bundle="${resword}"/>&nbsp;</td>
                                                <td class="table_header_row">
                                                    <fmt:message key="listLdapUsers.list.table.header.email.label" bundle="${resword}"/>&nbsp;</td>
                                                <td class="table_header_row">
                                                    <fmt:message key="listLdapUsers.list.table.header.actions.label" bundle="${resword}"/>&nbsp;</td>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${memberList}" var="m">
                                                <tr>
                                                    <td class="table_cell_left"><c:out value="${m.username}"/>&nbsp;</td>
                                                    <td class="table_cell"><c:out value="${m.firstName}"/>&nbsp;</td>
                                                    <td class="table_cell"><c:out value="${m.lastName}"/>&nbsp;</td>
                                                    <td class="table_cell"><c:out value="${m.email}"/>&nbsp;</td>
                                                    <!-- Select the LDAP user (distinguished name) -->
                                                    <td class="table_cell">
                                                        <a href="<c:url value="/pages/admin/selectLdapUser"><c:param name="dn" value="${m.distinguishedName}"/></c:url>" target="_parent">
                                                            <img src="../../images/create_new.gif"
                                                                 alt="<fmt:message key="listLdapUsers.list.table.selectUser.tooltip" bundle="${resword}"/>"
                                                                 title="<fmt:message key="listLdapUsers.list.table.selectUser.tooltip" bundle="${resword}"/>"
                                                                 style="border-width: 0"/>
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty memberList}">
                                                <tr>
                                                    <td colspan="3"><fmt:message key="listLdapUsers.list.table.emptyResults.label" bundle="${resword}"/></td>
                                                </tr>
                                            </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div></div></div></div></div></div></div></div>
                            </td>
                        </tr>
                    </table>
                </c:if>

                <!-- Get request to selectLdapUser without dn parameter is to just close the view -->
                <form action="<c:url value="/pages/admin/selectLdapUser"/>" method="get" target="_parent">
                    <input type="submit" class="button_medium" value="<fmt:message key="listLdapUsers.close.label" bundle="${resword}"/>">
                </form>
            </td>
        </tr>
    </table>

</div>

<jsp:include page="../include/footer.jsp"/>
