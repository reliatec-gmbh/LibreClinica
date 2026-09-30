/* Carry LCTable URL state through SDV actions; pending checkbox selections are not retained. */
(function () {
    'use strict';

    function tableForm(tableName) {
        return document.getElementById(tableName + '-panel-form');
    }

    window.captureSdvTableState = function (workflowForm, tableName) {
        var table = tableForm(tableName);
        if (!table || !workflowForm || !workflowForm.elements.sdvTableState) return;
        var state = new URLSearchParams(new FormData(table));
        var currentUrl = new URL(window.location.href);
        var expectedPath = tableName === 'sdv' ? '/pages/viewAllSubjectSDVtmp' : '/pages/viewSubjectAggregate';
        if (currentUrl.pathname.endsWith(expectedPath) && currentUrl.searchParams.has('page')) {
            state.set('page', currentUrl.searchParams.get('page'));
        }
        state.delete('studyId'); // The workflow form already has the only studyId submitted to the action.
        workflowForm.elements.sdvTableState.value = state.toString();
    };

    window.initSdvTableState = function (tableName) {
        var workflowForm = document.getElementById('sdvForm');
        var panel = document.getElementById(tableName + '-panel');
        if (!workflowForm || !panel) return;
        document.addEventListener('click', function (event) {
            var action = event.target.closest('button[form="sdvForm"]');
            if (action && panel.contains(action)) window.captureSdvTableState(workflowForm, tableName);
        }, true);
        workflowForm.addEventListener('submit', function () {
            window.captureSdvTableState(workflowForm, tableName);
        });
        document.body.addEventListener('htmx:afterSwap', function (event) {
            if (event.target.id === tableName + '-panel') {
                // Morphing can reuse existing inputs, so clear even those it preserved.
                document.getElementById(tableName + '-panel').querySelectorAll('input.sdvCheck').forEach(function (box) {
                    box.checked = false;
                });
                var selectAll = document.getElementById('sdvSelectAllOnPage');
                if (selectAll) selectAll.checked = false;
            }
        });
    };
}());
