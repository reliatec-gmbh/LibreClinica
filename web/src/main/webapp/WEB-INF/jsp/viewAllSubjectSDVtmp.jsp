<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>

<jsp:include page="include/managestudy_top_pages.jsp"/>

<!-- start of viewAllSubjectSDVtmp.jsp -->

<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
        <img src="../images/sidebar_collapse.gif"  class="sidebar_collapse_expand"></a>
        <b><fmt:message key="instructions" bundle="${restext}"/></b>
        <div class="sidebar_tab_content"><fmt:message key="design_implement_sdv" bundle="${restext}"/></div>
    </td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
        <img src="../images/sidebar_expand.gif" class="sidebar_collapse_expand"></a>
        <b><fmt:message key="instructions" bundle="${restext}"/></b>
    </td>
</tr>

<jsp:include page="include/sideInfo.jsp"/>

<link rel="stylesheet" href="${pageContext.request.contextPath}/includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery.jmesa.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/includes/jmesa/jquery-migrate-1.1.1.js"></script>

<%-- view all subjects starts here --%>
<script type="text/javascript">
    function onInvokeAction(id,action) {
        setExportToLimit(id, '');
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
    }
</script>

<h1><span class="title_manage"><fmt:message key="sdv_sdv_for" bundle="${resword}"/> <c:out value="${study.name}"/></span></h1>

<jsp:useBean scope='session' id='sSdvRestore' class='java.lang.String' />
<c:set var="restore" value="true"/>
<c:if test="${sSdvRestore=='false'}"><c:set var="restore" value="false"/></c:if>

<div id="searchFilterSDV">
    <table>
        <tr>
            <td valign="bottom" id="Tab1'">
                <div id="Tab1NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <a class="tabtext" title="<fmt:message key="view_by_event_CRF" bundle="${resword}"/>" href='viewAllSubjectSDVtmp?studyId=${studyId}' onclick="javascript:HighlightTab(1);"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></a></div></div></div></div>
                <div id="Tab1Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></span></div></div></div></div></td>
              
            <td valign="bottom" id="Tab2'">
				<div id="Tab2Selected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <a class="tabtext" title="<fmt:message key="view_by_studysubjectID" bundle="${resword}"/>" href='viewSubjectAggregate?s_sdv_restore=${restore}&studyId=${studyId}' onclick="javascript:HighlightTab(2);"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></a></div></div></div></div>
                <div id="Tab2NotSelected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></span></div></div></div></div></td>

        </tr>
    </table>
    <script type="text/JavaScript">HighlightTab(1);</script>
</div>


<script type="text/javascript">
    function prompt(formObj,crfId){
        var bool = confirm(
                "<fmt:message key="uncheck_sdv" bundle="${resmessages}"/>");
        if(bool){
            formObj.action='${pageContext.request.contextPath}/pages/handleSDVRemove';
            formObj.crfId.value=crfId;
            formObj.submit();
        }
    }
</script>
<div id="subjectSDV">
    <form name='sdvForm' action="${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp">
        <input type="hidden" name="studyId" value="${param.studyId}">
        <input type="hidden" name=imagePathPrefix value="../">
        <%--This value will be set by an onclick handler associated with an SDV button --%>
        <input type="hidden" name="crfId" value="0">
        <%-- the destination JSP page after removal or adding SDV for an eventCRF --%>
        <input type="hidden" name="redirection" value="viewAllSubjectSDVtmp">
        <%--<input type="hidden" name="decorator" value="mydecorator">--%>
        ${sdvTableAttribute}
        <br />
            <c:if test="${!(study.status.locked)}">
               <input type="submit" name="sdvAllFormSubmit" class="button_medium" value="<fmt:message key="sdv_all_checked" bundle="${resword}"/>" onclick="this.form.method='POST';this.form.action='${pageContext.request.contextPath}/pages/handleSDVPost';this.form.submit();"/>
           </c:if>        
        
    </form>
</div>

<!-- end of viewAllSubjectSDVtmp.jsp -->
<jsp:include page="include/footer.jsp"/>
