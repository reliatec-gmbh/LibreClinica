/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.controller;


import static org.akaza.openclinica.core.util.ClassCastHelper.asArrayList;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.lctable.HtmxResponseSupport;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobTable;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobTableFactory;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobs;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.jmesa.facade.TableFacade;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
/**
 *
 * @author jnyayapathi
 *  Controller for listing all the scheduled jobs. Also an interface for canceling the jobs which are running.
 */
@Controller("ScheduledJobController")
public class ScheduledJobController {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobController.class);

    public final static String SCHEDULED_TABLE_ATTRIBUTE = "scheduledTableAttribute";

    @Autowired
    @Qualifier("scheduledJobTableFactory")
    private  ScheduledJobTableFactory  scheduledJobTableFactory;
    public static final String EP_BEAN = "epBean";

    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;

    @Autowired
    private Scheduler scheduler;

    @RequestMapping("/listCurrentScheduledJobs")
    public ModelAndView listScheduledJobs(HttpServletRequest request, HttpServletResponse response,
            @RequestParam MultiValueMap<String, String> requestParams) throws Exception {
        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        ModelMap gridMap = new ModelMap();

        boolean showMoreLink = false;
        if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else{
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");


        // request.setAttribute("studySubjectId",studySubjectId);
        /*SubjectIdSDVFactory tableFactory = new SubjectIdSDVFactory();
        * @RequestParam("studySubjectId") int studySubjectId,*/
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = asArrayList(request.getAttribute("pageMessages"), String.class);
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        List<JobExecutionContext> listCurrentJobs = new ArrayList<JobExecutionContext>();
        listCurrentJobs = scheduler.getCurrentlyExecutingJobs();
        List<JobKey> currentJobList = listCurrentJobs.stream().map(job -> job.getTrigger().getJobKey()).collect(Collectors.toList());
        
        List<String> triggerGroups =  scheduler.getTriggerGroupNames();
        List<SimpleTrigger> simpleTriggers = new ArrayList<SimpleTrigger>();
        for (String triggerGroup : triggerGroups) {
            logger.debug("Group: " + triggerGroup + " contains the following triggers");
            Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup));

            for (TriggerKey triggerKey : triggerKeys) {
             TriggerState state = scheduler.getTriggerState(triggerKey);
               logger.debug("- " + triggerKey.getName());
               if (state != TriggerState.PAUSED) {
               simpleTriggers.add((SimpleTrigger) scheduler.getTrigger(triggerKey));
               }
            }
         }

       List <ScheduledJobs>jobsScheduled = new ArrayList<ScheduledJobs>();

        for (SimpleTrigger st : simpleTriggers) {
        	JobKey jobKey = st.getJobKey();
            boolean isExecuting = currentJobList.contains(jobKey);

            ScheduledJobs jobs = new ScheduledJobs();

            ExtractPropertyBean epBean = null;
            if (st.getJobDataMap() != null) {
                epBean = (ExtractPropertyBean) st.getJobDataMap().get(EP_BEAN);
            }


            if (epBean != null) {
                StringBuilder checkbox = new StringBuilder();
                checkbox.append("<input style='margin-right: 5px' type='checkbox'/>");

                StringBuilder actions = new StringBuilder("<table><tr><td>");
                if (isExecuting) {
                    actions.append("&nbsp;");
                } else {
                    String contextPath = request.getContextPath();
                    StringBuilder jsCodeString = new StringBuilder("this.form.method='GET'; this.form.action='").
                            append(contextPath).append("/pages/cancelScheduledJob").append("';").
                            append("this.form.theJobName.value='").append(jobKey.getName()).append("';").
                            append("this.form.theJobGroupName.value='").append(jobKey.getGroup()).append("';").
                            append("this.form.theTriggerName.value='").append(jobKey.getName()).append("';").
                            append("this.form.theTriggerGroupName.value='").append(jobKey.getGroup()).append("';").
                            append("this.form.submit();");

                    actions.append("<td><input type=\"submit\" class=\"button\" value=\"Cancel Job\" ").
                            append("name=\"cancelJob\" onclick=\"").append(jsCodeString.toString()).append("\" />");

                }

                actions.append("</td></tr></table>");

                jobs.setCheckbox(checkbox.toString());
                jobs.setDatasetId(epBean.getDatasetName());
                String fireTime = st.getStartTime() != null ? longFormat(locale).format(st.getStartTime()) : "";
                jobs.setFireTime(fireTime);
                if(st.getNextFireTime() != null) {
                    jobs.setScheduledFireTime(longFormat(locale).format(st.getNextFireTime()));
                }
                jobs.setExportFileName(epBean.getExportFileName()[0]);
                jobs.setAction(actions.toString());
                jobs.setJobStatus(isExecuting ? "Currently Executing" : "Scheduled");
                jobs.setJobName(jobKey.getName());
                jobs.setJobGroupName(jobKey.getGroup());
                // Preserve the legacy action parameters, which use the job key for trigger identity too.
                jobs.setTriggerName(jobKey.getName());
                jobs.setTriggerGroupName(jobKey.getGroup());
                jobs.setCancellable(!isExecuting);
                jobsScheduled.add(jobs);
            }
        }
        logger.debug("totalRows " + jobsScheduled.size());

        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            request.setAttribute("tableRenderingMode", "jmesa");
            request.setAttribute("totalJobs", jobsScheduled.size());
            request.setAttribute("jobs", jobsScheduled);
            TableFacade facade = scheduledJobTableFactory.createTable(request, response);
            gridMap.addAttribute(SCHEDULED_TABLE_ATTRIBUTE, facade.render());
            return new ModelAndView("listCurrentScheduledJobs", gridMap);
        }

        request.setAttribute("tableRenderingMode", "htmlflow");
        ScheduledJobTable table = new ScheduledJobTable(jobsScheduled, locale, request.getContextPath());
        String tableHtml = table.render(requestParams, request.getRequestURI());
        if (HtmxResponseSupport.writeFragmentIfRequested(request, response, tableHtml)) {
            response.getWriter().flush();
            return null;
        }

        gridMap.addAttribute(SCHEDULED_TABLE_ATTRIBUTE, tableHtml);
        return new ModelAndView("listCurrentScheduledJobs", gridMap);

    }

    @RequestMapping("/cancelScheduledJob")
    public String cancelScheduledJob(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("theJobName") String theJobName,
            @RequestParam("theJobGroupName") String theJobGroupName,
            @RequestParam("theTriggerName") String triggerName,
            @RequestParam("theTriggerGroupName") String triggerGroupName,
            @RequestParam("redirection") String redirection, ModelMap model) throws SchedulerException {

    	JobKey jobKey = new JobKey(theJobName, theJobGroupName);
    	TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroupName);
        scheduler.getJobDetail(jobKey);
        logger.debug("About to pause the job-->" + theJobName + "Job Group Name -->" + theJobGroupName);

        SimpleTrigger oldTrigger = (SimpleTrigger) scheduler.getTrigger(triggerKey);
        if (oldTrigger != null) {
            
            Date startTime = new Date(oldTrigger.getStartTime().getTime() + oldTrigger.getRepeatInterval());
            if (triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME)) {
                interruptQuartzJob(scheduler, theJobName, theJobGroupName);
            }

            scheduler.pauseJob(jobKey);

            SimpleTrigger newTrigger = newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .startAt(startTime)
                .withSchedule(simpleSchedule()
                    .withRepeatCount(oldTrigger.getRepeatCount())
                    .withIntervalInMilliseconds(oldTrigger.getRepeatInterval())
                    .withMisfireHandlingInstructionNextWithRemainingCount())
                .withDescription(oldTrigger.getDescription())
                .usingJobData(oldTrigger.getJobDataMap())
                .build();

            scheduler.unscheduleJob(triggerKey);// these are the jobs which are from extract data and are not not required to be rescheduled.

            ArrayList<String> pageMessages = new ArrayList<>();

            if (triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME)) {
                scheduler.rescheduleJob(triggerKey, newTrigger);
                pageMessages.add("The Job " + theJobName + " has been cancelled");
            } else if (triggerGroupName.equals(XsltTriggerService.TRIGGER_GROUP_NAME)) {
                JobDetailFactoryBean jobDetailBean = new JobDetailFactoryBean();
                jobDetailBean.setGroup(XsltTriggerService.TRIGGER_GROUP_NAME);
                jobDetailBean.setName(newTrigger.getKey().getName());
                jobDetailBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
                jobDetailBean.setJobDataMap(newTrigger.getJobDataMap());
                jobDetailBean.setDurability(true); // need durability?
                jobDetailBean.afterPropertiesSet();

                scheduler.deleteJob(jobKey);
                scheduler.scheduleJob(jobDetailBean.getObject(), newTrigger);
                pageMessages.add("The Job " + theJobName + " has been rescheduled");
            }

            request.setAttribute("pageMessages", pageMessages);

            logger.debug("jobDetails>" + scheduler.getJobDetail(jobKey));
        }
        
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        
        return null;
    }

    private void interruptQuartzJob(Scheduler scheduler, String jobName, String jobGroup) throws SchedulerException {
        scheduler.interrupt(JobKey.jobKey(jobName, jobGroup));
    }

    private String longFormatString() {
        return "EEE MMM dd HH:mm:ss zzz yyyy";
    }

    private SimpleDateFormat longFormat(Locale locale) {
        return new SimpleDateFormat(longFormatString(), locale);
    }

}
