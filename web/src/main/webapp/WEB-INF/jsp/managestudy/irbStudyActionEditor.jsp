<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>



<h1 id="title-editor">New Study Action</h1>
<form method="post" onsubmit="return validateForm()"
      action="${pageContext.request.contextPath}/IrbStudy">
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
  <input type="hidden" name="studyId" value="${studyId}"/>
  <input type="hidden" name="h_study_action_history_id">
  <input type="hidden" name="action" value="saveStudyActionEditor">
  <table class="table-history-editor">
    <tbody>
    <tr>
      <td align="right"><label>Action:</label></td>
      <td>
        <select name="h_study_action_type_id" id="studyActionHistoryParameterSelection" class="formfield">
          <option value="">(Select an action)</option>
          <c:forEach var="c" items="${protocolActionHistoryParameter}">
            <option value="${c.irbStudyActionHistoryParameterId}"
                    data-effective-date="${c.effectiveDate}"
                    data-hrpo-action="${c.hrpoAction}"
                    data-version-date="${c.versionDate}"
                    data-version-number="${c.versionNumber}"
                    data-submission-to-cdc-irb="${c.submissionToCdcIrb}"
                    data-cdc-irb-approval="${c.cdcIrbApproval}"
                    data-notification-sent-to-sites="${c.notificationSentToSites}"
                    data-enrollment-pause-date="${c.enrollmentPauseDate}"
                    data-enrollment-re-started-date="${c.enrollmentReStartedDate}"
                    data-reason-for-enrollment-pause="${c.reasonForEnrollmentPause}"
            >
                ${c.action}
            </option>
          </c:forEach>
        </select>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_study_action_type_id"/></jsp:include></td>
    </tr>
    <tr>
      <td align="right">
        <label>Effective date:</label>
      </td>
      <td>
        <input type="text" id="h_effective_date" name="h_effective_date" class="formfield"/>
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
             title="<fmt:message key="show_calendar" bundle="${resword}"/>"
             border="0" id="h_effective_date-trigger" /> *
        <script type="text/javascript">
          Calendar.setup({inputField: "h_effective_date",
            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
            button: "h_effective_date-trigger", customPX: 300, customPY: 10 });
        </script>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_effective_date"/></jsp:include></td>
    </tr>
    <tr>
      <td align="right">
        <label>HRPO Action:</label>
      </td>
      <td>
        <input type="text" name="h_hrpo_action" id="h_hrpo_action" class="formfield"/>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_hrpo_action"/></jsp:include></td>
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
        <label>Version date:</label>
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
        <label>Submission to CDC IRB:</label>
      </td>
      <td>
        <input type="text" id="h_submission_to_cdc_irb" name="h_submission_to_cdc_irb" class="formfield"/>
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
             title="<fmt:message key="show_calendar" bundle="${resword}"/>"
             border="0" id="h_submission_to_cdc_irb-trigger" /> *
        <script type="text/javascript">
          Calendar.setup({inputField: "h_submission_to_cdc_irb",
            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
            button: "h_submission_to_cdc_irb-trigger", customPX: 300, customPY: 10 });
        </script>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_submission_to_cdc_irb"/></jsp:include></td>
    </tr>
    <tr>
      <td align="right">
        <label>CDC IRB Approval/memo date:</label>
      </td>
      <td>
        <input type="text" id="h_cdc_irb_approval" name="h_cdc_irb_approval" class="formfield"/>
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
             title="<fmt:message key="show_calendar" bundle="${resword}"/>"
             border="0" id="h_cdc_irb_approval-trigger" /> *
        <script type="text/javascript">
          Calendar.setup({inputField: "h_cdc_irb_approval",
            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
            button: "h_cdc_irb_approval-trigger", customPX: 300, customPY: 10 });
        </script>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_cdc_irb_approval"/></jsp:include></td>
    </tr>
    <tr>
      <td align="right">
        <label>Memo/notification sent to sites:</label>
      </td>
      <td>
        <input type="text" id="h_notification_sent_to_sites" name="h_notification_sent_to_sites" class="formfield"/>
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
             title="<fmt:message key="show_calendar" bundle="${resword}"/>"
             border="0" id="h_notification_sent_to_sites-trigger" /> *
        <script type="text/javascript">
          Calendar.setup({inputField: "h_notification_sent_to_sites",
            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
            button: "h_notification_sent_to_sites-trigger", customPX: 300, customPY: 10 });
        </script>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_notification_sent_to_sites"/></jsp:include></td>
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
        <input type="text" id="h_enrollment_re_started_date" name="h_enrollment_re_started_date" class="formfield"/>
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>"
             title="<fmt:message key="show_calendar" bundle="${resword}"/>"
             border="0" id="h_enrollment_re_started_date-trigger" /> *
        <script type="text/javascript">
          Calendar.setup({inputField: "h_enrollment_re_started_date",
            ifFormat: "<fmt:message key="date_format_calender" bundle="${resformat}"/>",
            button: "h_enrollment_re_started_date-trigger", customPX: 300, customPY: 10 });
        </script>
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_enrollment_re_started_date"/></jsp:include></td>
    </tr>
    <tr>
      <td align="right">
        <label>Reason for enrollment pause</label>
      </td>
      <td>
        <input type="text" id="h_reason_for_enrollment_pause" name="h_reason_for_enrollment_pause" class="formfield" />
      </td>
    </tr>
    <tr>
      <td></td>
      <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="h_reason_for_enrollment_pause"/></jsp:include></td>
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
    const option = jQuery('#studyActionHistoryParameterSelection option[value="' +
            jQuery("#studyActionHistoryParameterSelection").val() + '"]'
    );

    jQuery('#study-action-editor .table-history-editor img').css('display', 'none');

    jQuery('#h_effective_date').attr('disabled', 'disabled');
    jQuery('#h_effective_date').val('');
    jQuery('#h_hrpo_action').attr('disabled', 'disabled');
    jQuery('#h_hrpo_action').val('');
    jQuery('#h_version_date').attr('disabled', 'disabled');
    jQuery('#h_version_date').val('');
    jQuery('#h_version_number').attr('disabled', 'disabled');
    jQuery('#h_version_number').val('');
    jQuery('#h_submission_to_cdc_irb').attr('disabled', 'disabled');
    jQuery('#h_submission_to_cdc_irb').val('');
    jQuery('#h_cdc_irb_approval').attr('disabled', 'disabled');
    jQuery('#h_cdc_irb_approval').val('');
    jQuery('#h_notification_sent_to_sites').attr('disabled', 'disabled');
    jQuery('#h_notification_sent_to_sites').val('');
    jQuery('#h_enrollment_pause_date').attr('disabled', 'disabled');
    jQuery('#h_enrollment_pause_date').val('');
    jQuery('#h_enrollment_re_started_date').attr('disabled', 'disabled');
    jQuery('#h_enrollment_re_started_date').val('');
    jQuery('#h_reason_for_enrollment_pause').attr('disabled', 'disabled');
    jQuery('#h_reason_for_enrollment_pause').val('');

    if (jQuery(option).data('effective-date')) {
      jQuery('#h_effective_date').removeAttr('disabled');
      jQuery('#h_effective_date-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('hrpo-action')) {
      jQuery('#h_hrpo_action').removeAttr('disabled');
      jQuery('#h_hrpo_action-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('version-date')) {
      jQuery('#h_version_date').removeAttr('disabled');
      jQuery('#h_version_date-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('version-number')) {
      jQuery('#h_version_number').removeAttr('disabled');
      jQuery('#h_version_number-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('submission-to-cdc-irb')) {
      jQuery('#h_submission_to_cdc_irb').removeAttr('disabled');
      jQuery('#h_submission_to_cdc_irb-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('cdc-irb-approval')) {
      jQuery('#h_cdc_irb_approval').removeAttr('disabled');
      jQuery('#h_cdc_irb_approval-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('notification-sent-to-sites')) {
      jQuery('#h_notification_sent_to_sites').removeAttr('disabled');
      jQuery('#h_notification_sent_to_sites-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('enrollment-pause-date')) {
      jQuery('#h_enrollment_pause_date').removeAttr('disabled');
      jQuery('#h_enrollment_pause_date-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('enrollment-re-started-date')) {
      jQuery('#h_enrollment_re_started_date').removeAttr('disabled');
      jQuery('#h_enrollment_re_started_date-trigger').css('display', 'initial');
    }

    if (jQuery(option).data('reason-for-enrollment-pause')) {
      jQuery('#h_reason_for_enrollment_pause').removeAttr('disabled');
    }

  }

  jQuery("#studyActionHistoryParameterSelection").change(e => {
    enableFieldsByActionParameter();
  });
</script>