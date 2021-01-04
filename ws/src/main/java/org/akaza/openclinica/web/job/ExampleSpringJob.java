/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

// TODO duplicate of the version in the web module?
public class ExampleSpringJob extends QuartzJobBean {
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        
    }



}
