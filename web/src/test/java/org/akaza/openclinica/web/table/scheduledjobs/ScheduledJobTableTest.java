/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 *
 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */
package org.akaza.openclinica.web.table.scheduledjobs;

import junit.framework.TestCase;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Locale;

public class ScheduledJobTableTest extends TestCase {

    @Override
    protected void setUp() {
        ResourceBundleProvider.updateLocale(Locale.ENGLISH);
    }

    public void testRendersLegacyColumnsAndTypedGetCancellationAction() {
        ScheduledJobs scheduled = job(
            "Data Set A", "Thu Jan 01 00:00:00 UTC 1970", "export a.xml", "Scheduled",
            "job one", "job group", true);
        ScheduledJobs executing = job(
            "Data Set B", "Fri Jan 02 00:00:00 UTC 1970", "export-b.xml", "Currently Executing",
            "job-two", "job-group", false);

        String html = table(scheduled, executing).render(new LinkedMultiValueMap<>(),
            "/LibreClinica/pages/listCurrentScheduledJobs");

        assertColumn(html, "dataset-name");
        assertColumn(html, "fire-time");
        assertColumn(html, "export-file");
        assertColumn(html, "job-status");
        assertColumn(html, "actions");
        assertTrue(html.contains("DataSet Name"));
        assertTrue(html.contains("Thu Jan 01 00:00:00 UTC 1970"));
        assertTrue(html.contains("Currently Executing"));
        assertFalse(html.contains("scheduledFireTime"));
        assertTrue(html.contains("data-test-action=\"cancel\""));
        assertTrue(html.contains("/LibreClinica/pages/cancelScheduledJob"));
        assertTrue(html.contains("theJobName=job%20one"));
        assertTrue(html.contains("theJobGroupName=job%20group"));
        assertTrue(html.contains("theTriggerName=job%20one"));
        assertTrue(html.contains("theTriggerGroupName=job%20group"));
        assertTrue(html.contains("redirection=listCurrentScheduledJobs"));
        assertEquals(1, occurrences(html, "data-test-action=\"cancel\""));
    }

    public void testFiltersSortsAndPaginatesFormattedLegacyValues() {
        ScheduledJobs beta = job(
            "Beta", "Mon Jan 03 00:00:00 UTC 2022", "beta.xml", "Scheduled",
            "beta-job", "group", true);
        ScheduledJobs alpha = job(
            "Alpha", "Tue Jan 04 00:00:00 UTC 2022", "alpha.xml", "Scheduled",
            "alpha-job", "group", true);
        ScheduledJobs executing = job(
            "Gamma", "Wed Jan 05 00:00:00 UTC 2022", "gamma.xml", "Currently Executing",
            "gamma-job", "group", false);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("q.jobStatus", "scheduled");
        params.add("sortProp", "datasetId");
        params.add("sortDir", "desc");
        params.add("maxRows", "1");
        params.add("page", "2");

        String html = table(beta, alpha, executing).render(params,
            "/LibreClinica/pages/listCurrentScheduledJobs");

        assertTrue(html.contains("data-test-job=\"alpha-job\""));
        assertFalse(html.contains("data-test-job=\"beta-job\""));
        assertFalse(html.contains("data-test-job=\"gamma-job\""));
        assertTrue(html.contains("1–1 of 2") || html.contains("1-1 of 2") || html.contains("2–2 of 2") || html.contains("2-2 of 2"));
        assertTrue(html.contains("name=\"q.jobStatus\" value=\"scheduled\""));
        assertTrue(html.contains("data-test-sort=\"desc\""));
    }

    private static ScheduledJobTable table(ScheduledJobs... jobs) {
        return new ScheduledJobTable(Arrays.asList(jobs), Locale.ENGLISH, "/LibreClinica");
    }

    private static ScheduledJobs job(String dataset, String fireTime, String exportFile, String status,
            String jobName, String groupName, boolean cancellable) {
        ScheduledJobs job = new ScheduledJobs();
        job.setDatasetId(dataset);
        job.setFireTime(fireTime);
        job.setScheduledFireTime("hidden-next-fire-time");
        job.setExportFileName(exportFile);
        job.setJobStatus(status);
        job.setJobName(jobName);
        job.setJobGroupName(groupName);
        job.setTriggerName(jobName);
        job.setTriggerGroupName(groupName);
        job.setCancellable(cancellable);
        return job;
    }

    private static void assertColumn(String html, String columnName) {
        assertTrue("Missing data-test-column=" + columnName,
            html.contains("data-test-column=\"" + columnName + "\""));
    }

    private static int occurrences(String value, String token) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(token, offset)) >= 0) {
            count++;
            offset += token.length();
        }
        return count;
    }
}
