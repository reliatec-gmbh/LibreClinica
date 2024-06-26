/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.job;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.SimpleTrigger;

public class XalanTriggerService {
    public XalanTriggerService() {
        
    }

    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String XSL_FILE_PATH = "xslFilePath";
    public static final String XML_FILE_PATH = "xmlFilePath";
    public static final String SQL_FILE_PATH = "sqlFilePath";
    
    public static String TRIGGER_GROUP_NAME = "XalanTriggers";
    
    public SimpleTrigger generateXalanTrigger(String xslFile, String xmlFile, String sqlFile, int datasetId) {
        Date startDateTime = new Date(System.currentTimeMillis());
        String jobName = xmlFile + datasetId;
        
        SimpleTrigger simpleTrigger = (SimpleTrigger) newTrigger()
                .forJob(jobName, TRIGGER_GROUP_NAME)
                .startAt(startDateTime)
                .withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(100)
                .withMisfireHandlingInstructionNextWithExistingCount());
        
        // set job data map

        // simpleTrigger.getJobDataMap().put(EMAIL, email);
        // simpleTrigger.getJobDataMap().put(USER_ID, userAccount.getId());
        simpleTrigger.getJobDataMap().put(XSL_FILE_PATH, xslFile);
        simpleTrigger.getJobDataMap().put(XML_FILE_PATH, xmlFile);
        simpleTrigger.getJobDataMap().put(SQL_FILE_PATH, sqlFile);
        // simpleTrigger.getJobDataMap().put(DIRECTORY, directory);
        // simpleTrigger.getJobDataMap().put(ExampleSpringJob.LOCALE, locale);
        
        return simpleTrigger;
    }
}
