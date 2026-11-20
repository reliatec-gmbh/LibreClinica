/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.web.table.scheduledjobs;

import org.akaza.openclinica.lctable.LCTable;
import org.akaza.openclinica.lctable.LCTableColumnDef;
import org.akaza.openclinica.lctable.LCTableData;
import org.akaza.openclinica.lctable.LCTableParams;
import org.akaza.openclinica.lctable.SafeUrl;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.util.MultiValueMap;
import org.xmlet.htmlapifaster.Td;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.akaza.openclinica.lctable.LCTableColumnDef.NOT_SORTABLE;
import static org.akaza.openclinica.lctable.LCTableColumnDef.customTdCol;
import static org.akaza.openclinica.lctable.LCTableColumnDef.textCol;
import static org.akaza.openclinica.lctable.LCTableFilterDef.clearFilter;
import static org.akaza.openclinica.lctable.LCTableText.key;
import static org.akaza.openclinica.lctable.SafeUrl.url;

/** LCTable-based scheduled export-job list served by the Spring MVC scheduled-job controller. */
public class ScheduledJobTable {

    private static final String TABLE_NAME = "scheduledJobs";

    private final List<ScheduledJobs> jobs;
    private final Locale locale;
    private final String contextPath;
    private final ResourceBundle resword;
    private final LCTable<ScheduledJobs> table;

    public ScheduledJobTable(List<ScheduledJobs> jobs, Locale locale, String contextPath) {
        this.jobs = new ArrayList<>(jobs);
        this.locale = locale;
        this.contextPath = contextPath;
        this.resword = ResourceBundleProvider.getWordsBundle(locale);
        this.table = new LCTable<>(TABLE_NAME, buildColumns(), this::fetchData)
            .setRowTestAttributes(row -> Map.of("job", row.getJobName()));
    }

    private List<LCTableColumnDef<ScheduledJobs>> buildColumns() {
        return Arrays.asList(
            textCol("datasetId", key("scheduled_job_dataset_name"), "dataset-name", 0, ScheduledJobs::getDatasetId),
            textCol("fireTime", key("scheduled_job_fire_time"), "fire-time", 0, ScheduledJobs::getFireTime),
            textCol("exportFileName", key("scheduled_job_export_file"), "export-file", 0, ScheduledJobs::getExportFileName),
            textCol("jobStatus", key("scheduled_job_status"), "job-status", 0, ScheduledJobs::getJobStatus),
            customTdCol("action", key("actions"), "actions", 0, NOT_SORTABLE, clearFilter(), this::renderActionCell)
        );
    }

    private LCTableData<ScheduledJobs> fetchData(LCTableParams params) {
        List<ScheduledJobs> filtered = jobs.stream()
            .filter(row -> matches(row, params.filters))
            .collect(Collectors.toList());

        comparator(params.sortProp).ifPresent(comparator -> {
            Comparator<ScheduledJobs> effective = "desc".equalsIgnoreCase(params.sortDir)
                ? comparator.reversed()
                : comparator;
            filtered.sort(effective);
        });

        int from = Math.min(params.page * params.maxRows, filtered.size());
        int to = Math.min(from + params.maxRows, filtered.size());
        return new LCTableData<>(new ArrayList<>(filtered.subList(from, to)), filtered.size());
    }

    private boolean matches(ScheduledJobs row, Map<String, String> filters) {
        return contains(row.getDatasetId(), filters.get("datasetId"))
            && contains(row.getFireTime(), filters.get("fireTime"))
            && contains(row.getExportFileName(), filters.get("exportFileName"))
            && contains(row.getJobStatus(), filters.get("jobStatus"));
    }

    private boolean contains(String value, String filter) {
        return filter == null || filter.isEmpty()
            || (value != null && value.toLowerCase(locale).contains(filter.toLowerCase(locale)));
    }

    private java.util.Optional<Comparator<ScheduledJobs>> comparator(String property) {
        Function<ScheduledJobs, String> extractor;
        if ("datasetId".equals(property)) {
            extractor = ScheduledJobs::getDatasetId;
        } else if ("fireTime".equals(property)) {
            extractor = ScheduledJobs::getFireTime;
        } else if ("exportFileName".equals(property)) {
            extractor = ScheduledJobs::getExportFileName;
        } else if ("jobStatus".equals(property)) {
            extractor = ScheduledJobs::getJobStatus;
        } else {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(Comparator.comparing(
            extractor, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));
    }

    private void renderActionCell(Td<?> td, ScheduledJobs row) {
        if (!row.isCancellable()) {
            return;
        }
        SafeUrl href = url(contextPath + "/pages/cancelScheduledJob")
            .param("theJobName", row.getJobName())
            .param("theJobGroupName", row.getJobGroupName())
            .param("theTriggerName", row.getTriggerName())
            .param("theTriggerGroupName", row.getTriggerGroupName())
            .param("redirection", "listCurrentScheduledJobs");
        td.a().attrClass("button")
            .attrHref(href.toUriString())
            .addAttr("data-test-action", "cancel")
            .text(resword.getString("cancel_job"))
            .__();
    }

    public String render(MultiValueMap<String, String> requestParams, String requestUri) {
        LCTableParams params = new LCTableParams(requestParams, table);
        return table.render(requestUri, params, contextPath, locale);
    }
}