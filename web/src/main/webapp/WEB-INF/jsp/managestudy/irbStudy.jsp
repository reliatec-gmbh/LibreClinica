<jsp:useBean id="openEditorOnStartup" scope="request" type="java.lang.Boolean"/>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="label" class="java.lang.String"/>

<%--<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.IRBStudyBean" />--%>
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />

<c:set var="cdc_irb_protocol_number" value="" />
<c:set var="version1_protocol_date" value="" />
<c:set var="protocol_officer" value="" />
<c:set var="submitted_cdc_irb" value="" />
<c:set var="approval_by_cdc_irb" value="" />
<c:set var="cdc_irb_expiration_date" value="" />

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "cdc_irb_protocol_number"}'>
        <c:set var="cdc_irb_protocol_number" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "version1_protocol_date"}'>
        <c:set var="version1_protocol_date" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "protocol_officer"}'>
        <c:set var="protocol_officer" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "submitted_cdc_irb"}'>
        <c:set var="submitted_cdc_irb" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "approval_by_cdc_irb"}'>
        <c:set var="approval_by_cdc_irb" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "cdc_irb_expiration_date"}'>
        <c:set var="cdc_irb_expiration_date" value="${presetValue.value}" />
    </c:if>
</c:forEach>


<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>

<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>

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


<h1><fmt:message key="irb_study" bundle="${resword}"/></h1>
<div class="form-standard">
    <style type="text/css" scoped>
        /*table tbody tr td:nth-child(2) {
            text-align: left;
        }
        table tfoot tr td:nth-child(2) {
            text-align: left;
        }*/
    </style>
    <form name="irbStudyForm" action="${pageContext.request.contextPath}/IrbStudy" method="post">
    <jsp:include page="../include/showSubmitted.jsp" />
    <table>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_protocol_number" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <%--<input name="cdc_irb_protocol_number" class="formfieldM" value="${irbStudyBean.cdcIrbProtocolNumber}"/>--%>
                                <input name="cdc_irb_protocol_number" class="formfieldM" value="${presetValues['cdc_irb_protocol_number']}"/>
                            </div>
                        </td>
                        <td>*</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="cdc_irb_protocol_number"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_version1_protocol_date" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <%--<input name="version1_protocol_date" value="${irbStudyBean.version1ProtocolDate}"/>--%>
                                <input onfocus="this.select()" type="text" name="version1_protocol_date"
                                       value="${presetValues['version1_protocol_date']}"
                                    class="formfieldM" id="version1ProtocolDateField" />
                                <a href="#">
                                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                                         title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="version1ProtocolDateTrigger" />
                                    <script type="text/javascript">
                                        Calendar.setup({inputField: "version1ProtocolDateField",
                                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                                            button: "version1ProtocolDateTrigger", customPX: 300, customPY: 10 });
                                    </script>
                                </a> *
                            </div>
                        </td>
                        <td align="left">

                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="version1_protocol_date"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_protocol_officer" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <%--<input name="protocol_officer" value="${irbStudyBean.protocol_officer}"/>--%>
                                <input name="protocol_officer" class="formfieldM" value="${presetValues['protocol_officer']}"/>
                            </div>
                        </td>
                        <td>*</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocol_officer"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_submitted_cdc_irb" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <input onfocus="this.select()" type="text" name="submitted_cdc_irb" value="${presetValues['submitted_cdc_irb']}"
                                       class="formfieldM" id="submittedCdcIrbField" />
                                <a href="#">
                                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="submittedCdcIrbTrigger" />
                                    <script type="text/javascript">Calendar.setup({inputField: "submittedCdcIrbField", ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button: "submittedCdcIrbTrigger", customPX: 300, customPY: 10 }); </script>
                                </a> *
                            </div>
                        </td>
                        <td align="left">

                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="submitted_cdc_irb"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_approval_by_cdc_irb" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <input onfocus="this.select()" type="text" name="approval_by_cdc_irb" value="${presetValues['approval_by_cdc_irb']}"
                                       class="formfieldM" id="approvalByCdcIrbField" />
                                <a href="#">
                                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="approvalByCdcIrbTrigger" />
                                    <script type="text/javascript">Calendar.setup({inputField: "approvalByCdcIrbField", ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button: "approvalByCdcIrbTrigger", customPX: 300, customPY: 10 }); </script>
                                </a> *
                            </div>
                        </td>
                        <td align="left">

                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="approval_by_cdc_irb"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="formlabel">
                <fmt:message key="irb_study_cdc_irb_expiration_date" bundle="${resword}"/>:
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div class="formfieldXL_BG">
                                <input onfocus="this.select()" type="text" name="cdc_irb_expiration_date" value="${presetValues['cdc_irb_expiration_date']}"
                                       class="formfieldM" id="cdcIrbExpirationDateField" />
                                <a href="#">
                                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="cdcIrbExpirationDateTrigger" />
                                    <script type="text/javascript">Calendar.setup({inputField: "cdcIrbExpirationDateField", ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button: "cdcIrbExpirationDateTrigger", customPX: 300, customPY: 10 }); </script>
                                </a> *
                            </div>
                        </td>
                        <td>

                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="cdc_irb_expiration_date"/></jsp:include>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input type="submit" id="" name="createIrbStudy" value="<fmt:message key="submit" bundle="${resword}"/>" class="button" />
                &nbsp;
                <input type="button" onclick="confirmCancel('pages/studymodule');" id="cancel" name="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button"/>
            </td>
        </tr>
    </table>
</form>
</div>
<jsp:include page="./irbStudyActionHistory.jsp"/>

<%-- Work here --%>



<jsp:include page="../include/footer.jsp"/>


