<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="studyActionHistory" scope="request" type="java.util.List"/>
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />
<div>
    <style type="text/css" scoped>
        #table-history tbody tr:nth-child(odd) td {
            background: var(--gray-l40);
        }
    </style>
<script>
    function loadActionHistoryEditor(payload) {
        jQuery("#studyActionHistoryParameterSelection").val(payload.actionType);
        jQuery('input[name="h_study_action_history_id"]').val(payload.id);


        jQuery('input[name="h_effective_date"]').val(payload.effectiveDate);
        jQuery('input[name="h_hrpo_action"]').val(payload.hrpoAction);
        jQuery('input[name="h_version_number"]').val(payload.versionNumber);
        jQuery('input[name="h_version_date"]').val(payload.versionDate);
        jQuery('input[name="h_submission_to_cdc_irb"]').val(payload.submissionToCdcIrb);
        jQuery('input[name="h_cdc_irb_approval"]').val(payload.cdcIrbApproval);
        jQuery('input[name="h_notification_sent_to_sites"]').val(payload.notificationSentToSites);
        jQuery('input[name="h_enrollment_pause_date"]').val(payload.enrollmentPauseDate);
        jQuery('input[name="h_enrollment_re_started_date"]').val(payload.enrollmentReStartedDate);
        jQuery('input[name="h_reason_for_enrollment_pause"]').val(payload.reasonForEnrollmentPause);
        enableFieldsByActionParameter();

        jQuery.blockUI({
            message: jQuery('#study-action-editor'),
            css:{left: "300px", top:"10px", width: "400px" }
        });
    }

    jQuery(document).ready(function () {


        jQuery('#study-action-open').click(function () {
            jQuery('input[name="h_study_action_history_id"]').val('');
            jQuery("#studyActionHistoryParameterSelection").val('');
            jQuery('#study-action-editor input[type="text"]').val('');
            enableFieldsByActionParameter();

            jQuery.blockUI({
                message: jQuery('#study-action-editor'),
                css:{left: "300px", top:"10px", width: "400px" }
            });
        });


        jQuery('a.update-history').click(function (e) {
            const tr = jQuery(e.currentTarget).closest('tr');
            loadActionHistoryEditor({
                actionType: jQuery(tr).data('action-type'),
                id: jQuery(tr).data('id'),
                effectiveDate: jQuery(tr).find('td:nth-child(2)').text(),
                hrpoAction: jQuery(tr).find('td:nth-child(3)').text(),
                versionNumber: jQuery(tr).find('td:nth-child(4)').text(),
                versionDate: jQuery(tr).find('td:nth-child(5)').text(),
                submissionToCdcIrb: jQuery(tr).find('td:nth-child(6)').text(),
                cdcIrbApproval: jQuery(tr).find('td:nth-child(7)').text(),
                notificationSentToSites: jQuery(tr).find('td:nth-child(8)').text(),
                enrollmentPauseDate: jQuery(tr).find('td:nth-child(9)').text(),
                enrollmentReStartedDate: jQuery(tr).find('td:nth-child(10)').text(),
                reasonForEnrollmentPause: jQuery(tr).find('td:nth-child(11)').text(),
            });


            enableFieldsByActionParameter();

            jQuery.blockUI({
                message: jQuery('#study-action-editor'),
                css:{left: "300px", top:"10px", width: "400px" }
            });
            //jQuery('input[name="studyActionHistoryParameterSelection"]').val(jQuery(tr).data('id'));

        });

        jQuery('.close-editor').click(function() {
            jQuery.unblockUI();
            return false;
        });
    });
</script>

<div id="study-action-editor" style="display:none;">
    <jsp:include page="./irbStudyActionEditor.jsp"/>
</div>
<h2>Study action history</h2>
<table id="table-history">
    <thead>
    <tr>
        <th>Action</th>
        <th>Efective Date</th>
        <th>HRPO Action #</th>
        <th>Version No</th>
        <th>Version Date</th>
        <th>Package sent to CDC IRB</th>
        <th>CDC IRB Approval/Memo</th>
        <th>Memo notification sent to sites</th>
        <th>Enrollment pause date</th>
        <th>Enrollment restarted date</th>
        <th>Reason for enrollment pause</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>

        <c:forEach var="h" items="${studyActionHistory}">
            <tr data-id="${h['id']}" data-action-type="${h['actionTypeId']}">
                <td>${h['actionLabel']}</td>
                <td>${h['effectiveDate']}</td>
                <td>${h['hrpoAction']}</td>
                <td>${h['versionNumber']}</td>
                <td>${h['versionDate']}</td>
                <td>${h['submissionToCdcIrb']}</td>
                <td>${h['cdcIrbApproval']}</td>
                <td>${h['notificationSentToSites']}</td>
                <td>${h['enrollmentPauseDate']}</td>
                <td>${h['enrollmentReStartedDate']}</td>
                <td>${h['reasonForEnrollmentPause']}</td>
                <td>
                    <a href="#" class="update-history">Edit</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
    <tfoot style="background: #f7f7f7;">
    <tr>
        <td colspan="10" style="padding: 5px;">
            <button type="button" class="button" id="study-action-open">Add</button>
        </td>
    </tr>
    </tfoot>
</table>
</div>
<script>
<c:if test="${openEditorOnStartup}">
    jQuery(document).ready(() => {
        loadActionHistoryEditor({
            actionType: "${presetValues['h_study_action_type_id']}",
            id: "${presetValues['h_study_action_history_id']}",
            effectiveDate: "${presetValues['h_effective_date']}",
            hrpoAction: "${presetValues['h_hrpo_action']}",
            versionNumber: "${presetValues['h_version_number']}",
            versionDate: "${presetValues['h_version_date']}",
            submissionToCdcIrb: "${presetValues['h_submission_to_cdc_irb']}",
            cdcIrbApproval: "${presetValues['h_cdc_irb_approval']}",
            notificationSentToSites: "${presetValues['h_notification_sent_to_sites']}",
            enrollmentPauseDate: "${presetValues['h_enrollment_pause_date']}",
            enrollmentReStartedDate: "${presetValues['h_enrollment_re_started_date']}",
            reasonForEnrollmentPause: "${presetValues['h_reason_for_enrollment_pause']}",
        });
    });
</c:if>
</script>