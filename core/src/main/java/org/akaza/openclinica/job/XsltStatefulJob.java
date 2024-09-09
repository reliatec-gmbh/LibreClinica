/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.job;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XsltStatefulJob extends XsltTransformJob implements InterruptableJob {

    private static final Logger LOG = LoggerFactory.getLogger(XsltStatefulJob.class);

    protected JobTerminationMonitor jobTerminationMonitor;

    public XsltStatefulJob() {
        super();
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        jobTerminationMonitor = JobTerminationMonitor.createInstance(context.getJobDetail().getDescription());
        super.executeInternal(context);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (jobTerminationMonitor != null) {
            LOG.info("Received cancellation request for job " + jobTerminationMonitor.getJobName());
            jobTerminationMonitor.terminate();
        }
    }
}
