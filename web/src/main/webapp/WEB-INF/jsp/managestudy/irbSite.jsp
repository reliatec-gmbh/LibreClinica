<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>



<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

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
<jsp:useBean id="siteId" scope="request" type="java.lang.Integer"/>

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


<h1><span class="title_manage">IRB Site Definition</span></h1>
<div class="form-standard">
    <style type="text/css" scoped>
        table tbody tr td:nth-child(2) {
        text-align: left;
        }
        table tfoot tr td:nth-child(2) {
        text-align: left;
        }
    </style>
    <form action="${pageContext.request.contextPath}/IrbSite" method="post">
        <input type="hidden" name="siteId" value="${siteId}"/>
        <table>
            <tr>
                <td align="right"><label>Version number</label></td>
                <td>
                    <input name="version_number" id="version_number" value="${presetValues['version_number']}"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="version_number"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <input name="site_relies_on_cdc_irb" type="checkbox" value="1"
                           ${preset_values['site_relies_on_cdc_irb']?'checked':''}/>
                </td>
                <td><label>Sites relies on CDC IRB</label></td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="site_relies_on_cdc_irb"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <input name="is_1572" type="checkbox" value="1"
                    ${preset_values['is_1572']?'checked':''} />
                </td>
                <td><label>1572</label></td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="is_1572"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>CDC IRB protocol version date</label>
                </td>
                <td>
                    <input name="cdc_irb_protocol_version_date" id="cdc_irb_protocol_version_date"
                           value="${presetValues['cdc_irb_protocol_version_date']}"/>

                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="cdc_irb_protocol_version_date-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "cdc_irb_protocol_version_date",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "cdc_irb_protocol_version_date-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="cdc_irb_protocol_version_date"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>Local IRB approved protocol</label>
                </td>
                <td>
                    <input name="local_irb_approved_protocol" id="local_irb_approved_protocol"
                           value="${presetValues['local_irb_approved_protocol']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="local_irb_approved_protocol-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "local_irb_approved_protocol",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "local_irb_approved_protocol-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="local_irb_approved_protocol"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>CDC received local documents</label>
                </td>
                <td>
                    <input name="cdc_received_local_documents"
                           id="cdc_received_local_documents"
                           value="${presetValues['cdc_received_local_documents']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="cdc_received_local_documents-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "cdc_received_local_documents",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "cdc_received_local_documents-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="cdc_received_local_documents"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>Site consent package send to CDC IRB</label>
                </td>
                <td>
                    <input name="site_consent_package_send_to_cdc_irb"
                           id="site_consent_package_send_to_cdc_irb"
                           value="${presetValues['site_consent_package_send_to_cdc_irb']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="site_consent_package_send_to_cdc_irb-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "site_consent_package_send_to_cdc_irb",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "site_consent_package_send_to_cdc_irb-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="site_consent_package_send_to_cdc_irb"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>Initial CDC IRB approval</label>
                </td>
                <td>
                    <input name="initial_cdc_irb_approval"
                           id="initial_cdc_irb_approval"
                           value="${presetValues['initial_cdc_irb_approval']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="initial_cdc_irb_approval-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "initial_cdc_irb_approval",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "initial_cdc_irb_approval-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="initial_cdc_irb_approval"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>CRB approval to enroll</label>
                </td>
                <td>
                    <input name="crb_approval_to_enroll"
                           id="crb_approval_to_enroll"
                           value="${presetValues['crb_approval_to_enroll']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="crb_approval_to_enroll-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "crb_approval_to_enroll",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "crb_approval_to_enroll-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="crb_approval_to_enroll"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>IRB approval</label>
                </td>
                <td>
                    <input name="irb_approval" id="irb_approval" value="${presetValues['irb_approval']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="irb_approval-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "irb_approval",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "irb_approval-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="irb_approval"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>Expiration date</label>
                </td>
                <td>
                    <input name="expiration_date" id="expiration_date"
                           value="${presetValues['expiration_date']}"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="expiration_date-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "expiration_date",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "expiration_date-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="expiration_date"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <input name="active" type="checkbox" value="1" ${preset_values['active']?'checked':''} />
                </td>
                <td>
                    <label>Active</label>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="active"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td align="right">
                    <label>Comments</label>
                </td>
                <td>
                    <textarea name="comments" id="comments">
                        ${presetValues['comments']}
                    </textarea>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="comments"/></jsp:include>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td><button class="button" type="submit">Submit</button></td>
            </tr>
        </table>
    </form>
    <jsp:include page="./irbProtocolActions.jsp"/>


<jsp:include page="../include/footer.jsp"/>


