<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<h1 id="title-editor">New Protocol Action</h1>
<form method="post" onsubmit="return validateForm()"
      action="${pageContext.request.contextPath}/IrbSite">
    <style type="text/css" scoped>

        table tbody tr td:nth-child(2) {
            text-align: left;
        }
        table tfoot tr td:nth-child(2) {
            text-align: left;
        }
        div.calendar {
            z-index: 2000;
        }


        input:disabled {
            background: gray;
        }
    </style>
    <input type="hidden" name="siteId" value="${siteId}"/>
    <input type="hidden" name="h_protocol_action_history_id">
    <input type="hidden" name="action" value="saveProtocolActionEditor">
    <table class="table-history-editor">
        <tbody>
            <tr>
                <td align="right"><label>Action:</label></td>
                <td>
                    <select name="h_protocol_action_type_id" id="protocolActionHistoryParameterSelection" class="formfield">
                        <c:forEach var="c" items="${protocolActionHistoryParameter}">
                            <option value="${c.irbProtocolActionHistoryParameterId}"
                                    data-version-date="${c.cdcIrbProtocolVersionDate}"
                                    data-version="${c.version}"
                                    data-site-submitted-to-local-irb="${c.siteSubmittedToLocalIrb}"
                                    data-local-irb-approval="${c.localIrbApproval}"
                                    data-received-docs-from-sites="${c.siteSendsDocsToCrb}"
                                    data-package-sent-to-cdc-irb="${c.packageSentToCdcIrb}"
                                    data-cdc-approval-acknowledgment="${c.cdcApprovalAcknowledgment}"
                                    data-enrollment-pause-date="${c.enrollmentPauseDate}"
                                    data-enrollment-re-started-date="${c.enrollmentReStartedDate}"
                                    data-reason-for-enrollment-pause="${c.enrollmentReStartedDate}"
                            >
                                ${c.action}
                            </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_protocol_action_type_id"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>CDC IRB protocol version date:</label>
                </td>
                <td>
                    <input type="text" id="h_version_date" name="h_version_date" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_version_date-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_version_date",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_version_date-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_version_date"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Version No:</label>
                </td>
                <td>
                    <input type="text" id="h_version_number" name="h_version_number" class="formfield" />
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_version_number"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Site submitted to local IRB:</label>
                </td>
                <td>
                    <input type="text" id="h_site_submitted_to_local_irb" name="h_site_submitted_to_local_irb" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_site_submitted_to_local_irb-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_site_submitted_to_local_irb",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_site_submitted_to_local_irb-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_site_submitted_to_local_irb"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Local IRB approval:</label>
                </td>
                <td>
                    <input type="text" id="h_local_irb_approval" name="h_local_irb_approval" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_local_irb_approval-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_local_irb_approval",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_local_irb_approval-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_local_irb_approval"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Site sends docs to IRB:</label>
                </td>
                <td>
                    <input type="text" id="h_received_docs_from_sites" name="h_received_docs_from_sites" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_received_docs_from_sites-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_received_docs_from_sites",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_received_docs_from_sites-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_site_sends_docs_to_irb"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Package sent to CDC IRB:</label>
                </td>
                <td>
                    <input type="text" id="h_package_sent_to_cdc_irb" name="h_package_sent_to_cdc_irb" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_package_sent_to_cdc_irb-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_package_sent_to_cdc_irb",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_package_sent_to_cdc_irb-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_package_sent_to_cdc_irb"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>CDC Approval/Acknowledgement:</label>
                </td>
                <td>
                    <input type="text" id="h_cdc_approval" name="h_cdc_approval" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_cdc_approval-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_cdc_approval",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_cdc_approval-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_cdc_approval"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Enrollment pause date:</label>
                </td>
                <td>
                    <input type="text" id="h_enrollment_pause_date" name="h_enrollment_pause_date" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_enrollment_pause_date-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_enrollment_pause_date",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_enrollment_pause_date-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_enrollment_pause_date"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Enrollment restarted date:</label>
                </td>
                <td>
                    <input type="text" id="h_enrollment_restarted_date" name="h_enrollment_restarted_date" class="formfield"/>
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         title="<fmt:message key="show_calendar" bundle="${resword}"/>"
                         border="0" id="h_enrollment_restarted_date-trigger" /> *
                    <script type="text/javascript">
                        Calendar.setup({inputField: "h_enrollment_restarted_date",
                            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
                            button: "h_enrollment_restarted_date-trigger", customPX: 300, customPY: 10 });
                    </script>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_enrollment_restarted_date"/></jsp:include></td>
            </tr>
            <tr>
                <td align="right">
                    <label>Reason for enrollment paused:</label>
                </td>
                <td>
                    <input type="text" id="h_reason_for_enrollment_paused" name="h_reason_for_enrollment_paused" class="formfield"/>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_reason_for_enrollment_paused"/></jsp:include></td>
            </tr>
        </tbody>
        <tfoot>
            <tr>
                <td></td>
                <td>
                    <button type="submit" class="button">Submit</button>
                    <button type="button" class="button close-editor">Cancel</button>
                </td>
            </tr>
        </tfoot>
    </table>
</form>
<script>
    function enableFieldsByActionParameter() {
        const option = jQuery('#protocolActionHistoryParameterSelection option[value="' +
            jQuery("#protocolActionHistoryParameterSelection").val() + '"]'
        );

        jQuery('#protocol-action-editor .table-history-editor img').css('display', 'none');

        jQuery('#h_version_date').attr('disabled', 'disabled');
        jQuery('#h_version_date').val('');
        jQuery('#h_version_number').attr('disabled', 'disabled');
        jQuery('#h_version_number').val('');
        jQuery('#h_site_submitted_to_local_irb').attr('disabled', 'disabled');
        jQuery('#h_site_submitted_to_local_irb').val('');
        jQuery('#h_local_irb_approval').attr('disabled', 'disabled');
        jQuery('#h_local_irb_approval').val('');
        jQuery('#h_received_docs_from_sites').attr('disabled', 'disabled');
        jQuery('#h_received_docs_from_sites').val('');
        jQuery('#h_package_sent_to_cdc_irb').attr('disabled', 'disabled');
        jQuery('#h_package_sent_to_cdc_irb').val('');
        jQuery('#h_cdc_approval').attr('disabled', 'disabled');
        jQuery('#h_cdc_approval').val('');
        jQuery('#h_enrollment_pause_date').attr('disabled', 'disabled');
        jQuery('#h_enrollment_pause_date').val('');
        jQuery('#h_enrollment_restarted_date').attr('disabled', 'disabled');
        jQuery('#h_enrollment_restarted_date').val('');
        jQuery('#h_reason_for_enrollment_paused').attr('disabled', 'disabled');
        jQuery('#h_reason_for_enrollment_paused').val('');

        if (jQuery(option).data('version-date')) {
            jQuery('#h_version_date').removeAttr('disabled');
            jQuery('#h_version_date-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('version')) {
            jQuery('#h_version_number').removeAttr('disabled');
        }

        if (jQuery(option).data('site-submitted-to-local-irb')) {
            jQuery('#h_site_submitted_to_local_irb').removeAttr('disabled');
            jQuery('#h_site_submitted_to_local_irb-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('local-irb-approval')) {
            jQuery('#h_local_irb_approval').removeAttr('disabled');
            jQuery('#h_local_irb_approval-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('received-docs-from-sites')) {
            jQuery('#h_received_docs_from_sites').removeAttr('disabled');
            jQuery('#h_received_docs_from_sites-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('package-sent-to-cdc-irb')) {
            jQuery('#h_package_sent_to_cdc_irb').removeAttr('disabled');
            jQuery('#h_package_sent_to_cdc_irb-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('cdc-approval-acknowledgment')) {
            jQuery('#h_cdc_approval').removeAttr('disabled');
            jQuery('#h_cdc_approval-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('enrollment-pause-date')) {
            jQuery('#h_enrollment_pause_date').removeAttr('disabled');
            jQuery('#h_enrollment_pause_date-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('enrollment-re-started-date')) {
            jQuery('#h_enrollment_restarted_date').removeAttr('disabled');
            jQuery('#h_enrollment_restarted_date-trigger').css('display', 'initial');
        }

        if (jQuery(option).data('reason-for-enrollment-pause')) {
            jQuery('#h_reason_for_enrollment_paused').removeAttr('disabled');
        }
    }

    jQuery("#protocolActionHistoryParameterSelection").change(e => {
        enableFieldsByActionParameter();
    });
</script>