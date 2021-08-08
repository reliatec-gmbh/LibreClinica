<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />

<!-- start of menu.jsp -->

<jsp:include page="include/home-header.jsp"/>

<jsp:include page="include/sideAlert.jsp"/>

<script type="text/JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" src="includes/jmesa/jquery.blockUI.js"></script>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">

<!-- warning is study is frozen or locked -->
<div id="box" class="dialog">
	<span id="mbm">
	    <br><fmt:message key="study_frozen_locked_note" bundle="${restext}"/>
	</span>
	<br>
    <div>
        <button onclick="hm('box');">OK</button>
    </div>
</div>

<!-- then instructions-->
	<tr id="sidebar_Instructions_open" style="display: all">
        <td class="sidebar_tab">
	        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
    	    <img src="images/sidebar_collapse.gif" class="sidebar_collapse_expand"></a>
			<b><fmt:message key="instructions" bundle="${resword}"/></b>
			<div class="sidebar_tab_content"><fmt:message key="may_change_request_access" bundle="${restext}"/></div>
        </td>
	</tr>
    <tr id="sidebar_Instructions_closed" style="display: none">
        <td class="sidebar_tab">
	        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
	        <img src="images/sidebar_expand.gif" class="sidebar_collapse_expand"></a>
	        <b><fmt:message key="instructions" bundle="${resword}"/></b>
        </td>
	</tr>

<jsp:include page="include/sideInfo.jsp"/>

<h1><span class="title_manage">
	<fmt:message key="welcome_to" bundle="${restext}"/>
	<c:choose>
	    <c:when test='${study.parentStudyId > 0}'>
	        <c:out value='${study.parentStudyName}'/>
	    </c:when>
	    <c:otherwise>
	        <c:out value='${study.name}'/>
	    </c:otherwise>
	</c:choose>
</span></h1>

<c:set var="roleName" value=""/>
<c:if test="${userRole != null && !userRole.invalid}">
	<c:set var="roleName" value="${userRole.role.name}"/>
	<c:set var="studyidentifier">
   		<span class="alert"><c:out value="${study.identifier}"/></span>
	</c:set>
</c:if>

<span class="table_title_Admin" >
	<a href="ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=<c:out value='${userBean.name}' />">
	<fmt:message key="notes_assigned_to_me" bundle="${restext}"/><span>${assignedDiscrepancies}</span>&nbsp;</a><br /><br />
</span>

<c:if test="${userRole.investigator || userRole.researchAssistant || userRole.researchAssistant2}"> <!-- if investigator, research assistant or ra2 -->
	<div id="findSubjectsDiv">
    <script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('findSubjects') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/MainMenu?'+ parameterString;
    }
    jQuery(document).ready(function() {
        jQuery('#addSubject').click(function() {
            jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('#cancel').click(function() {
            jQuery.unblockUI();
            return false;
        });
    });
    </script>
    <form  action="${pageContext.request.contextPath}/ListStudySubjects">
        <input type="hidden" name="module" value="admin">
        ${findSubjectsHtml}
    </form>
</div>
    <div id="addSubjectForm" style="display:none;">
         <c:import url="addSubjectMonitor.jsp"/>
    </div>
</c:if>

<c:if test="${userRole.coordinator || userRole.director}">										<!-- datamanager / study director -->
    <script type="text/javascript">
	    function onInvokeAction(id,action) {
	        if(id.indexOf('studySiteStatistics') == -1)  {
	            setExportToLimit(id, '');
	        }
	        if(id.indexOf('subjectEventStatusStatistics') == -1)  {
	            setExportToLimit(id, '');
	        }
	        if(id.indexOf('studySubjectStatusStatistics') == -1)  {
	            setExportToLimit(id, '');
	        }
	        createHiddenInputFieldsForLimitAndSubmit(id);
	    }
    </script>

	<table>
		<tr>
		    <td class="statistics_td">
		    <form  action="${pageContext.request.contextPath}/MainMenu">
		        ${studySiteStatistics}
		    </form>
		    </td>
		    <td class="statistics_td">
		    <form  action="${pageContext.request.contextPath}/MainMenu">
		        ${studyStatistics}
		    </form>
		    </td>
		</tr>
		<tr>
    		<td class="statistics_td">
    			<form  action="${pageContext.request.contextPath}/MainMenu">
        			${subjectEventStatusStatistics}
    			</form>
    		</td>
			<td class="statistics_td">
				<form  action="${pageContext.request.contextPath}/MainMenu">
					${studySubjectStatusStatistics}
				</form>
			</td>
		</tr>
	</table>
</c:if>

<c:if test="${userRole.monitor}">												<!-- monitor -->
	<script type="text/javascript">
	    function onInvokeAction(id,action) {
	        setExportToLimit(id, '');
	        createHiddenInputFieldsForLimitAndSubmit(id);
	    }
	    function onInvokeExportAction(id) {
	        var parameterString = createParameterStringForLimit(id);
	    }
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

	<div id="searchFilterSDV">
	    <table>
	        <tr>
	            <td valign="bottom" id="Tab1'">
	                <div id="Tab1NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
	                    <a class="tabtext" title="<fmt:message key="view_by_event_CRF" bundle="${resword}"/>" href='pages/viewAllSubjectSDVtmp?studyId=${studyId}' onclick="javascript:HighlightTab(1);"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></a></div></div></div></div>
	               <div id="Tab1Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_event_CRF" bundle="${resword}"/></span></div></div></div></div></td>
	             
	           <td valign="bottom" id="Tab2'">
	               <div id="Tab2Selected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
	               <a class="tabtext" title="<fmt:message key="view_by_studysubjectID" bundle="${resword}"/>" href='pages/viewSubjectAggregate?studyId=${studyId}' onclick="javascript:HighlightTab(2);"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></a></div></div></div></div>
	               <div id="Tab2NotSelected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_studysubjectID" bundle="${resword}"/></span></div></div></div></div>
	           </td>
	       </tr>
	   </table>
	   <script type="text/javascript"> HighlightTab(1);</script>
	</div>
	<div id="subjectSDV">
	    <form name='sdvForm' action="${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp">
	        <input type="hidden" name="studyId" value="${study.id}">
	        <input type="hidden" name=imagePathPrefix value="">
	        <%--This value will be set by an onclick handler associated with an SDV button --%>
	        <input type="hidden" name="crfId" value="0">
	        <%-- the destination JSP page after removal or adding SDV for an eventCRF --%>
	        <input type="hidden" name="redirection" value="viewAllSubjectSDVtmp">
	        ${sdvMatrix}
	        <br />
	        <c:if test="${!(study.status.locked)}">        
	             <input type="submit" name="sdvAllFormSubmit" class="button_medium" value="<fmt:message key="submit" bundle="${resword}"/>" onclick="this.form.method='POST';this.form.action='${pageContext.request.contextPath}/pages/handleSDVPost';this.form.submit();"/>
	             <input type="submit" name="sdvAllFormCancel" class="button_medium" value="<fmt:message key="cancel" bundle="${resword}"/>" onclick="this.form.action='${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp';this.form.submit();"/>
	       </c:if>
		</form>
	</div>
</c:if>

<!-- end of menu.jsp -->
<jsp:include page="include/footer.jsp"/>
