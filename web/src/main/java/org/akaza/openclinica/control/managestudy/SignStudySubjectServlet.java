/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.StudyEventAuditBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DisplayEventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateNewStudyEventServlet;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DisplayStudyEventRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

/**
 * Created by IntelliJ IDEA. User: bads Date: Jun 10, 2008 Time: 5:28:46 PM To
 * change this template use File | Settings | File Templates.
 */
public class SignStudySubjectServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4280235447806067251L;

	/**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        mayAccess();

        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");
    }

    public static ArrayList<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudyBean study, StudySubjectBean studySub, DataSource ds, UserAccountBean ub,
            StudyUserRoleBean currentRole) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);

        ArrayList<StudyEventBean> events = sedao.findAllByStudySubject(studySub);

        ArrayList<DisplayStudyEventBean> displayEvents = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean event = events.get(i);

            StudyEventDefinitionBean sed = seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            // find all active crfs in the definition
            ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = edcdao.findAllActiveByEventDefinitionId(study, sed.getId());

            ArrayList<EventCRFBean> eventCRFs = ecdao.findAllByStudyEvent(event);

            // construct info needed on view study event page
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs(getDisplayEventCRFs(study, ds, eventCRFs, ub, currentRole, event.getSubjectEventStatus()));
            ArrayList<DisplayEventDefinitionCRFBean> al = getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus());
            populateUncompletedCRFsWithCRFAndVersions(ds, al);
            de.setUncompletedCRFs(al);

            StudySubjectBean studySubject = (StudySubjectBean) ssdao.findByPK(event.getStudySubjectId());
            de.setMaximumSampleOrdinal(sedao.getMaxSampleOrdinal(sed, studySubject));

            displayEvents.add(de);
            // event.setEventCRFs(createAllEventCRFs(eventCRFs,
            // eventDefinitionCRFs));

        }

        return displayEvents;
    }

    public static boolean permitSign(StudySubjectBean studySub, DataSource ds) {
        boolean sign = true;
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudyDAO sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByPK(studySub.getStudyId());
        // DiscrepancyNoteDAO discDao = new DiscrepancyNoteDAO(ds);
        ArrayList<StudyEventBean> studyEvents = sedao.findAllByStudySubject(studySub);
        for (int l = 0; l < studyEvents.size(); l++) {
            StudyEventBean studyEvent = (StudyEventBean) studyEvents.get(l);
            ArrayList<EventCRFBean> eventCrfs = ecdao.findAllByStudyEvent(studyEvent);
            for (int i = 0; i < eventCrfs.size(); i++) {
                EventCRFBean ecrf = eventCrfs.get(i);
                // ArrayList discList =
                // discDao.findAllItemNotesByEventCRF(ecrf.getId());
                // for (int j = 0; j < discList.size(); j++) {
                // DiscrepancyNoteBean discBean = (DiscrepancyNoteBean)
                // discList.get(j);
                // if
                //(discBean.getResStatus().equals(org.akaza.openclinica.bean.core
                // .ResolutionStatus.OPEN)
                // ||
                //discBean.getResStatus().equals(org.akaza.openclinica.bean.core
                // .ResolutionStatus.UPDATED))
                // {
                // sign = false;
                // break;
                // }
                // }
                EventDefinitionCRFBean edcBean = edcdao.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(), ecrf.getCRFVersionId());
                if (ecrf.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) || ecrf.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                    && edcBean.isDoubleEntry() == true) {
                    sign = false;
                    break;
                }

            }
        }
        return sign;
    }

    public static boolean signSubjectEvents(StudySubjectBean studySub, DataSource ds, UserAccountBean ub) {
        boolean updated = true;
        StudyEventDAO sedao = new StudyEventDAO(ds);
        ArrayList<StudyEventBean> studyEvents = sedao.findAllByStudySubject(studySub);
        for (int l = 0; l < studyEvents.size(); l++) {
            try {
                StudyEventBean studyEvent = (StudyEventBean) studyEvents.get(l);
                studyEvent.setUpdater(ub);
                studyEvent.setUpdatedDate(new Date());
                studyEvent.setSubjectEventStatus(SubjectEventStatus.SIGNED);
                sedao.update(studyEvent);
            } catch (Exception ex) {
                updated = false;
            }
        }
        return updated;
    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        String action = fp.getString("action");
        int studySubId = fp.getInt("id", true);// studySubjectId

        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
            return;
        }
        StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);

        if (!permitSign(studySub, sm.getDataSource())) {
            addPageMessage(respage.getString("subject_event_cannot_signed"));
            // forwardPage(Page.SUBMIT_DATA_SERVLET);
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            // >> changed tbh, 06/2009
            return;
        }

        if (action.equalsIgnoreCase("confirm")) {
            String username = request.getParameter("j_user");
            String password = request.getParameter("j_pass");
            SecurityManager securityManager = ((SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager"));
            // String encodedUserPass =
            // org.akaza.openclinica.core.SecurityManager
            // .getInstance().encrytPassword(password);
            UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
            if (securityManager.verifyPassword(password, getUserDetails()) && ub.getName().equals(username)) {
                if (signSubjectEvents(studySub, sm.getDataSource(), ub)) {
                    // Making the StudySubject signed as all the events have
                    // become signed.
                    studySub.setStatus(Status.SIGNED);
                    studySub.setUpdater(ub);
                    subdao.update(studySub);
                    addPageMessage(respage.getString("subject_event_signed"));
                    // forwardPage(Page.SUBMIT_DATA_SERVLET);
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    // >> changed tbh, 06/2009
                    return;
                } else {
                    addPageMessage(respage.getString("errors_in_submission_see_below"));
                    forwardPage(Page.LIST_STUDY_SUBJECTS);
                    return;
                }
            } else {
                request.setAttribute("id", new Integer(studySubId).toString());
                addPageMessage(restext.getString("password_match"));
                forwardPage(Page.LIST_STUDY_SUBJECTS);
                return;
            }
        }

        request.setAttribute("studySub", studySub);

        int studyId = studySub.getStudyId();
        int subjectId = studySub.getSubjectId();

        SubjectBean subject = (SubjectBean) sdao.findByPK(subjectId);
        if (currentStudy.getStudyParameterConfig().getCollectDob().equals("2")) {
            Date dob = subject.getDateOfBirth();
            if (dob != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dob);
                int year = cal.get(Calendar.YEAR);
                request.setAttribute("yearOfBirth", new Integer(year));
            } else {
                request.setAttribute("yearOfBirth", "");
            }
        }

        request.setAttribute("subject", subject);


        StudyDAO studydao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean) studydao.findByPK(studyId);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        study.getStudyParameterConfig().setCollectDob(spvdao.findByHandleAndStudy(studyId, "collectDob").getValue());
        //request.setAttribute("study", study);

        if (study.getParentStudyId() > 0) {// this is a site,find parent
            StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
            request.setAttribute("parentStudy", parentStudy);
        } else {
            request.setAttribute("parentStudy", new StudyBean());
        }

        ArrayList<SubjectBean> children = sdao.findAllChildrenByPK(subjectId);

        request.setAttribute("children", children);

        // find study events
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());

        ArrayList<DisplayStudyEventBean> displayEvents = getDisplayStudyEventsForStudySubject(study, studySub, sm.getDataSource(), ub, currentRole);

        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
        // Don't filter for now; disc note beans are returned with eventCRFId
        // set
        discNoteUtil.injectParentDiscNotesIntoDisplayStudyEvents(displayEvents, new HashSet<>(), sm.getDataSource(), 0);
        // All the displaystudyevents for one subject
        request.setAttribute("displayStudyEvents", displayEvents);

        // Set up a Map for the JSP view, mapping the eventCRFId to another Map:
        // the
        // inner Map maps the resolution status name to the number of notes for
        // that
        // eventCRF id, as in New --> 2
        Map<Integer, Map<String, Integer>> discNoteByEventCRFid = discNoteUtil.createDiscNoteMapByEventCRF(displayEvents);
        request.setAttribute("discNoteByEventCRFid", discNoteByEventCRFid);

        EntityBeanTable table = fp.getEntityBeanTable();
        table.setSortingIfNotExplicitlySet(1, false);// sort by start date,
        // desc
        ArrayList<DisplayStudyEventRow> allEventRows = DisplayStudyEventRow.generateRowsFromBeans(displayEvents);

        String[] columns =
            { resword.getString("event") + " (" + resword.getString("occurrence_number") + ")", resword.getString("start_date1"),
                resword.getString("location"), resword.getString("status"), resword.getString("actions"), resword.getString("CRFs_atrib") };
        table.setColumns(new ArrayList<String>(Arrays.asList(columns)));
        table.hideColumnLink(4);
        table.hideColumnLink(5);

        if (!"removed".equalsIgnoreCase(studySub.getStatus().getName()) && !"auto-removed".equalsIgnoreCase(studySub.getStatus().getName())) {
            table.addLink(resword.getString("add_new_event"), "CreateNewStudyEvent?" + CreateNewStudyEventServlet.INPUT_STUDY_SUBJECT_ID_FROM_VIEWSUBJECT + "="
                + studySub.getId());
        }

        HashMap<String, Object> args = new HashMap<>();
        args.put("id", new Integer(studySubId).toString());
        table.setQuery("ViewStudySubject", args);
        table.setRows(allEventRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
        ArrayList<SubjectGroupMapBean> groupMaps = sgmdao.findAllByStudySubject(studySubId);
        request.setAttribute("groups", groupMaps);

        AuditEventDAO aedao = new AuditEventDAO(sm.getDataSource());
        ArrayList<AuditEventBean> logs = aedao.findEventStatusLogByStudySubject(studySubId);

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList<StudyEventAuditBean> eventLogs = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            AuditEventBean avb = (AuditEventBean) logs.get(i);
            StudyEventAuditBean sea = new StudyEventAuditBean();
            sea.setAuditEvent(avb);
            StudyEventBean se = (StudyEventBean) sedao.findByPK(avb.getEntityId());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
            sea.setDefinition(sed);
            String old = avb.getOldValue().trim();
            try {
                if (!(old == null || old.trim().isEmpty())) {
                    SubjectEventStatus oldStatus = SubjectEventStatus.get(new Integer(old).intValue());
                    sea.setOldSubjectEventStatus(oldStatus);
                }
                String newValue = avb.getNewValue().trim();
                if (!(newValue == null || newValue.trim().isEmpty())) {
                    SubjectEventStatus newStatus = SubjectEventStatus.get(new Integer(newValue).intValue());
                    sea.setNewSubjectEventStatus(newStatus);
                }
            } catch (NumberFormatException e) {
                logger.error("Subject event status is not able to be fetched properly: ", e);
            }
            UserAccountBean updater = (UserAccountBean) udao.findByPK(avb.getUserId());
            sea.setUpdater(updater);
            eventLogs.add(sea);

        }
        request.setAttribute("eventLogs", eventLogs);

        forwardPage(Page.SIGN_STUDY_SUBJECT);

    }

    /**
     * Each of the event CRFs with its corresponding CRFBean. Then generates a
     * list of DisplayEventCRFBeans, one for each event CRF.
     *
     * @param eventCRFs
     *            The list of event CRFs for this study event.
     * @return The list of DisplayEventCRFBeans for this study event.
     */
    public static ArrayList<DisplayEventCRFBean> getDisplayEventCRFs(StudyBean study, DataSource ds, ArrayList<EventCRFBean> eventCRFs, UserAccountBean ub, StudyUserRoleBean currentRole,
            SubjectEventStatus status) {
        ArrayList<DisplayEventCRFBean> answer = new ArrayList<>();

        // HashMap definitionsById = new HashMap();
        int i;
        /*
         * for (i = 0; i < eventDefinitionCRFs.size(); i++) {
         * EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
         * eventDefinitionCRFs.get(i); definitionsById.put(new
         * Integer(edc.getStudyEventDefinitionId()), edc); }
         */
        StudyEventDAO sedao = new StudyEventDAO(ds);
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemDataDAO iddao = new ItemDataDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int crfVersionId = ecb.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            ecb.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            ecb.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = ecb.getStudyEventId();
            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);

            // EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
            // definitionsById.get(new Integer(
            // studyEventDefinitionId));
            // fix problem of the above code(commented out), find the correct
            // edc, note that on definitionId can be related to multiple
            // eventdefinitioncrfBeans
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());
            // below added 092007 tbh
            // rules updated 112007 tbh
            if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED) || status.equals(SubjectEventStatus.STOPPED)) {
                ecb.setStage(DataEntryStage.LOCKED);

                // we need to set a SED-wide flag here, because other edcs
                // in this event can be filled in and change the status, tbh
            } else if (status.equals(SubjectEventStatus.INVALID)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!cb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!cvb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            }
            // above added 092007-102007 tbh
            // TODO need to refactor since this is similar to other code, tbh
            if (edc != null) {
                // System.out.println("edc is not null, need to set flags");
                DisplayEventCRFBean dec = new DisplayEventCRFBean();
                // System.out.println("edc.isDoubleEntry()" +
                // edc.isDoubleEntry() + ecb.getId());
                dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());
//                if (dec.isLocked()) {
//                    System.out.println("*** found a locked DEC: " + edc.getCrfName());
//                }
                ArrayList<ItemDataBean> idata = iddao.findAllByEventCRFId(ecb.getId());
                if (!idata.isEmpty()) {
                    // consider an event crf started only if item data get
                    // created
                    answer.add(dec);
                }
            }
        }

        return answer;
    }

    /**
     * Finds all the event definitions for which no event CRF exists - which is
     * the list of event definitions with uncompleted event CRFs.
     *
     * @param eventDefinitionCRFs
     *            All of the event definition CRFs for this study event.
     * @param eventCRFs
     *            All of the event CRFs for this study event.
     * @return The list of event definitions for which no event CRF exists.
     */
    public static ArrayList<DisplayEventDefinitionCRFBean> getUncompletedCRFs(DataSource ds, ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs, ArrayList<EventCRFBean> eventCRFs, SubjectEventStatus status) {
        int i;
        HashMap<Integer, Boolean> completed = new HashMap<>();
        HashMap<Integer, EventCRFBean> startedButIncompleted = new HashMap<>();
        ArrayList<DisplayEventDefinitionCRFBean> answer = new ArrayList<>();

        /**
         * A somewhat non-standard algorithm is used here: let answer = empty;
         * foreach event definition ED, set isCompleted(ED) = false foreach
         * event crf EC, set isCompleted(EC.getEventDefinition()) = true foreach
         * event definition ED, if (!isCompleted(ED)) { answer += ED; } return
         * answer; This algorithm is guaranteed to find all the event
         * definitions for which no event CRF exists.
         *
         * The motivation for using this algorithm is reducing the number of
         * database hits.
         *
         * -jun-we have to add more CRFs here: the event CRF which dones't have
         * item data yet
         */

        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcrf = eventDefinitionCRFs.get(i);
            completed.put(new Integer(edcrf.getCrfId()), Boolean.FALSE);
            startedButIncompleted.put(new Integer(edcrf.getCrfId()), new EventCRFBean());
        }

        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemDataDAO iddao = new ItemDataDAO(ds);
        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecrf = eventCRFs.get(i);
            int crfId = cvdao.getCRFIdFromCRFVersionId(ecrf.getCRFVersionId());
            ArrayList<ItemDataBean> idata = iddao.findAllByEventCRFId(ecrf.getId());
            if (!idata.isEmpty()) {// this crf has data already
                completed.put(new Integer(crfId), Boolean.TRUE);
            } else {// event crf got created, but no data entered
                startedButIncompleted.put(new Integer(crfId), ecrf);
            }
        }

        // TODO possible relation to 1689 here, tbh
        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            EventDefinitionCRFBean edcrf = eventDefinitionCRFs.get(i);

            // System.out.println("created dedc with edcrf
            // "+edcrf.getCrfName()+" default version "+
            // edcrf.getDefaultVersionName()+", id
            // "+edcrf.getDefaultVersionId());

            dedc.setEdc(edcrf);
            // below added tbh, 112007 to fix bug 1943
            if (status.equals(SubjectEventStatus.LOCKED)) {
                dedc.setStatus(Status.LOCKED);
            }
            Boolean b = (Boolean) completed.get(new Integer(edcrf.getCrfId()));
            EventCRFBean ev = (EventCRFBean) startedButIncompleted.get(new Integer(edcrf.getCrfId()));
            if (b == null || !b.booleanValue()) {

                // System.out.println("entered boolean loop with ev
                // "+ev.getId()+" crf version id "+
                // ev.getCRFVersionId());

                dedc.setEventCRF(ev);
                answer.add(dedc);

                // System.out.println("just added dedc to answer");
                // removed, tbh, since this is proving nothing, 11-2007

                /*
                 * if (dedc.getEdc().getDefaultVersionId() !=
                 * dedc.getEventCRF().getId()) { System.out.println("ID
                 * MISMATCH: edc name "+dedc.getEdc().getName()+ ", default
                 * version id "+dedc.getEdc().getDefaultVersionId()+ " event crf
                 * id "+dedc.getEventCRF().getId()); }
                 */
            }
        }

        return answer;
    }

    public static void populateUncompletedCRFsWithCRFAndVersions(DataSource ds, ArrayList<DisplayEventDefinitionCRFBean> uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);

        for(DisplayEventDefinitionCRFBean dedcrf : uncompletedEventDefinitionCRFs) {
            CRFBean cb = cdao.findByPK(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setCrf(cb);

            ArrayList<CRFVersionBean> versions = cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setVersions(versions);
        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    public void mayAccess() throws InsufficientPermissionException {
        FormProcessor fp = new FormProcessor(request);
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        int studySubId = fp.getInt("id", true);

        if (studySubId > 0) {
            if (!entityIncluded(studySubId, ub.getName(), subdao, sm.getDataSource())) {
                addPageMessage(respage.getString("required_study_subject_not_belong"));
                throw new InsufficientPermissionException(Page.MENU, resexception.getString("entity_not_belong_studies"), "1");
            }
        }
    }

}
