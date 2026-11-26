<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>


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
            if(id.indexOf('listEventsForSubject') == -1)  {
            setExportToLimit(id, '');
            }
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '${pageContext.request.contextPath}/ListEventsForSubjects? + module=manage&defId=' + '${defId}&' + parameterString;
        }
    </script>
</c:if>

<script type="text/javascript">
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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>


<h1><span class="title_manage">
<fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
</span></h1>

<div id="findSubjectsDiv">
    <%-- IMPORTANT: the LCTable (HtmlFlow) rendering path renders its own <form> around the whole
         table (see LCTable.renderTableHtml()) -- it must NOT also be wrapped in a <form> here, or
         the two nested <form>s would collide (nested <form>s are invalid HTML; the inner start tag
         is simply dropped by the parser), causing this JSP's own hidden "module"/"defId" inputs to
         collide with LCTable's own "defId" sticky-parameter hidden input once both are serialized
         together by HTMX's "closest form" -- see LCTable's class-level javadoc for details.
         Only the legacy JMesa rendering path (native form-based pagination/sort/filter resubmission,
         via createHiddenInputFieldsForLimitAndSubmit()) actually needs a surrounding <form>. --%>
    <c:choose>
        <c:when test="${tableRenderingMode == 'jmesa'}">
            <form  action="${pageContext.request.contextPath}/ListEventsForSubjects">
                <input type="hidden" name="module" value="submit">
                <input type="hidden" name="defId" value="${defId}">
                ${listEventsForSubjectsHtml}
            </form>
        </c:when>
        <c:otherwise>
            ${listEventsForSubjectsHtml}
        </c:otherwise>
    </c:choose>
</div>
<div id="addSubjectForm" style="display:none;">
      <c:import url="../submit/addNewSubjectExpressNew.jsp">
      </c:import>
</div>


<br>
<input type="button" onclick="confirmExit('MainMenu');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="submit"/>
</c:import>

<!-- Include everything that is needed for proper use of HTMX with LCTable (only needed for the htmlflow path,
     but harmless to include unconditionally since it's small and self-contained) -->
<jsp:include page="../include/useLCTable.jsp"/>

<jsp:include page="../include/footer.jsp"/>