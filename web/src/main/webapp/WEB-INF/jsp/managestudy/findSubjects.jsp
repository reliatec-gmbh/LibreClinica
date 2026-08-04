<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/submit-header.jsp"/>
<!-- start of managestudy/findSubjects.jsp -->

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<%-- JMesa scripts are only needed when the JMesa rendering path is active. --%>
<c:choose>
    <c:when test="${tableRenderingMode == 'jmesa'}">
        <link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
    </c:when>
    <c:otherwise>
        <link rel="stylesheet" href="includes/lctable/lctable.css" type="text/css">
    </c:otherwise>
</c:choose>

<!-- jquery.min.js and jquery.blockUI.js are needed unconditionally: jQuery/blockUI power the
     "Add New Subject" modal overlay (see the jQuery(document).ready(...) block below), unrelated to jmesa -->
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>

<c:if test="${tableRenderingMode == 'jmesa'}">
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-3.4.1.min.js"></script>

    <script type="text/javascript">
        function onInvokeAction(id,action) {
            if(id.indexOf('findSubjects') == -1)  {
            setExportToLimit(id, '');
            }
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '${pageContext.request.contextPath}/ListStudySubjects?'+ parameterString;
        }
    </script>
</c:if>

<script type="text/javascript">
    $.noConflict();			// to avoid conflicts with prototype.js
	jQuery(document).ready(function() {
		// add a listener to the add subject link
		jQuery('#addSubject').click(function() {
			// this prepares the overlay that is displayed when clickking add new subject; use 'css: etc' for overridin the default appearance of the div
			jQuery.blockUI({message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px", width:"", padding:"1em", cursor:"default"}});
			// defaults can be found inline in includes/jmesa/jquery.blockUI.js
		});
		// add a listerner to the cancel button in submit/addNewSubjectExpressNew.jsp 
		jQuery('#cancel').click(function() {
			jQuery.unblockUI();
			return false;
		});
		// show the overlay with the add-new-subject-dialog when there were errors with the submission
		<c:if test="${showOverlay}">
        	jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px", cursor:"default"}});
   		</c:if>
	});
</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" class="sidebar_collapse_expand"></a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		<div class="sidebar_tab_content"></div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" class="sidebar_collapse_expand"></a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
	</td>
</tr>

<!-- include study/site info -->
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1><span class="title_manage"><fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/></span></h1>

<%-- The "Select an Event"/"Add New Subject" toolbar controls used to be rendered directly here (for the
     htmlflow rendering path only -- the legacy jmesa rendering path always rendered its own equivalent
     controls as part of the jmesa table itself, via ListStudySubjectTableToolbar). They are now rendered
     as custom toolbar controls of the LCTable itself for the htmlflow path too -- see LCTable's
     addCustomToolbarControl / ListStudySubjectTable -- so no separate markup is needed here any more. --%>

<div id="findSubjectsDiv">
	<%-- IMPORTANT: the LCTable (HtmlFlow) rendering path renders its own <form> around the whole
	     table (see LCTable.renderTableHtml()) -- it must NOT also be wrapped in a <form> here, or
	     the browser will treat the two nested <form>s as a single merged form (nested <form>s are
	     invalid HTML; the inner start tag is simply dropped by the parser), causing any hidden
	     input declared in *this* JSP (e.g. a hardcoded "module") to collide with LCTable's own
	     same-named hidden inputs (e.g. a "module" sticky parameter) once both are serialized
	     together by HTMX's "closest form" -- see LCTable's class-level javadoc for details.
	     Only the legacy JMesa rendering path (native form-based pagination/sort/filter resubmission,
	     via createHiddenInputFieldsForLimitAndSubmit()) actually needs a surrounding <form>. --%>
	<c:choose>
		<c:when test="${tableRenderingMode == 'jmesa'}">
			<form action="${pageContext.request.contextPath}/ListStudySubjects">
				<input type="hidden" name="module" value="admin">
				${findSubjectsHtml}
			</form>
		</c:when>
		<c:otherwise>
			${findSubjectsHtml}
		</c:otherwise>
	</c:choose>
</div>

<!-- compose the overlay to add new subject, but don't show it-->
<div id="addSubjectForm" style="display:none;">
	<c:import url="../submit/addNewSubjectExpressNew.jsp"></c:import>
</div>

<br />

<!-- Include everything that is needed for proper use of HTMX with LCTable -->
<jsp:include page="../include/useLCTable.jsp"/>

<jsp:include page="../include/footer.jsp"/>