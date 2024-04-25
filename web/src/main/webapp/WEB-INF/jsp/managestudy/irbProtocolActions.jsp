<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div>
    <style type="text/css" scoped>
        #table-history tbody tr:nth-child(odd) td {
            background: var(--gray-l40);
        }
    </style>
<script>
    function loadActionHistoryEditor(payload) {
        jQuery("#protocolActionHistoryParameterSelection").val(payload.actionType);
        jQuery('input[name="h_protocol_action_history_id"]').val(payload.id);
        jQuery('input[name="h_site_id"]').val(payload.siteId);
        jQuery('input[name="h_version_number"]').val(payload.versionNumber);
        jQuery('input[name="h_version_date"]').val(payload.versionDate);
        jQuery('input[name="h_site_submitted_to_local_irb"]').val(payload.siteSubmittedToLocalIrb);
        jQuery('input[name="h_local_irb_approval"]').val(payload.localIrbApproval);
        jQuery('input[name="h_received_docs_from_sites"]').val(payload.receivedDocsFromSites);
        jQuery('input[name="h_package_sent_to_cdc_irb"]').val(payload.packageSentToCdcIrb);
        jQuery('input[name="h_cdc_approval"]').val(payload.cdcApproval);
        jQuery('input[name="h_enrollment_pause_date"]').val(payload.enrollmentPauseDate);
        jQuery('input[name="h_enrollment_restarted_date"]').val(payload.enrollmentReStartedDate);
        jQuery('input[name="h_reason_for_enrollment_pause"]').val(payload.reasonForEnrollmentPause);
        enableFieldsByActionParameter();

        jQuery.blockUI({
            message: jQuery('#protocol-action-editor'),
            css:{left: "300px", top:"10px", width: "400px" }
        });
    }


    jQuery(document).ready(function () {


        jQuery('#protocol-action-open').click(function () {
            jQuery('input[name="h_protocol_action_history_id"]').val('');
            jQuery("#protocolActionHistoryParameterSelection").val('');
            jQuery('#protocol-action-editor input[type="text"]').val('');
            enableFieldsByActionParameter();

            jQuery.blockUI({
                message: jQuery('#protocol-action-editor'),
                css:{left: "300px", top:"10px", width: "400px" }
            });
        });


        jQuery('a.update-history').click(function (e) {
            const tr = jQuery(e.currentTarget).closest('tr');
            loadActionHistoryEditor({
                actionType: jQuery(tr).data('action-type'),
                id: jQuery(tr).data('id'),
                versionDate: jQuery(tr).find('td:nth-child(2)').text(),
                versionNumber: jQuery(tr).find('td:nth-child(3)').text(),
                siteSubmittedToLocalIrb: jQuery(tr).find('td:nth-child(4)').text(),
                localIrbApproval: jQuery(tr).find('td:nth-child(5)').text(),
                receivedDocsFromSites: jQuery(tr).find('td:nth-child(6)').text(),
                packageSentToCdcIrb: jQuery(tr).find('td:nth-child(7)').text(),
                cdcIrbApproval: jQuery(tr).find('td:nth-child(8)').text(),
                enrollmentPauseDate: jQuery(tr).find('td:nth-child(9)').text(),
                enrollmentReStartedDate: jQuery(tr).find('td:nth-child(10)').text(),
                reasonForEnrollmentPause: jQuery(tr).find('td:nth-child(11)').text(),
            });


            enableFieldsByActionParameter();

            jQuery.blockUI({
                message: jQuery('#protocol-action-editor'),
                css:{left: "300px", top:"10px", width: "400px" }
            });
            //jQuery('input[name="studyActionHistoryParameterSelection"]').val(jQuery(tr).data('id'));

        });

        jQuery('.close-editor').click(function() {
            jQuery.unblockUI();
            return false;
        });
    });



    jQuery(document).ready(function () {
        jQuery('#protocol-action-open').click(function () {
            jQuery('input[name="h_study_action_history_id"]').val('');
            jQuery("#studyActionHistoryParameterSelection").val('');
            jQuery('#study-action-editor input[type="text"]').val('');
            enableFieldsByActionParameter();

            jQuery.blockUI({
                message: jQuery('#protocol-action-editor'),
                css:{left: "300px", top:"10px", width: "400px" }
            });
        });

        jQuery('button.close-editor').click(function() {
            jQuery.unblockUI();
            return false;
        });
    });
</script>

<div id="protocol-action-editor" style="display:none;">
    <jsp:include page="./irbProtocolActionEditor.jsp"/>
</div>
<h2>Protocol action history</h2>
<table id="table-history">
    <thead>
        <tr>
            <th>Action</th>
            <th>CDC IRB Protocol Version Date</th>
            <th>Version No</th>
            <th>Site submitted to local IRB</th>
            <th>Local IRB Approval</th>
            <th>Received docs from sites</th>
            <th>Package sent to CDC IRB</th>
            <th>CDC Approval/ Acknowledgment</th>
            <th>Enrollment Pause Date</th>
            <th>Enrollment Restarted Date</th>
            <th>Reason for enrollment paused</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="c" items="${protocolActionHistory}">
            <tr data-id="${c['id']}" data-action-type="${c['actionTypeId']}">
                <td>${c.action}</td>
                <td>${c.versionDate}</td>
                <td>${c.versionNumber}</td>
                <td>${c.siteSubmittedToLocalIrb}</td>
                <td>${c.localIrbApproval}</td>
                <td>${c.receivedDocsFromSites}</td>
                <td>${c.packageSentToCdcIrb}</td>
                <td>${c.cdcApproval}</td>
                <td>${c.enrollmentPauseDate}</td>
                <td>${c.enrollmentRestartedDate}</td>
                <td>${c.reasonForEnrollmentPaused}</td>
                <td>
                    <a href="#" class="update-history">Edit</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
    <tfoot style="background: #f7f7f7;">
        <tr>
            <td colspan="10" style="padding: 5px;">
                <button type="button" class="button" id="protocol-action-open">Add</button>
            </td>
        </tr>
    </tfoot>
</table>
</div>
<script>
    <c:if test="${openEditorOnStartup}">
    jQuery(document).ready(() => {
        loadActionHistoryEditor({
            actionType: "${presetValues['h_protocol_action_type_id']}",
            id: "${presetValues['h_protocol_action_history_id']}",
            siteId: "${presetValues['h_site_id']}",
            versionNumber: "${presetValues['h_version_number']}",
            versionDate: "${presetValues['h_version_date']}",
            siteSubmittedToLocalIrb: "${presetValues['h_site_submitted_to_local_irb']}",
            localIrbApproval: "${presetValues['h_local_irb_approval']}",
            receivedDocsFromSites: "${presetValues['h_received_docs_from_sites']}",
            //TODO: Missing siteSendsDocsToIrb
            packageSentToCdcIrb: "${presetValues['h_package_sent_to_cdc_irb']}",
            cdcApproval: "${presetValues['h_cdc_approval']}",
            enrollmentPauseDate: "${presetValues['h_enrollment_pause_date']}",
            enrollmentReStartedDate: "${presetValues['h_enrollment_re_started_date']}",
            reasonForEnrollmentPause: "${presetValues['h_reason_for_enrollment_pause']}",
        });
    });
    </c:if>
</script>