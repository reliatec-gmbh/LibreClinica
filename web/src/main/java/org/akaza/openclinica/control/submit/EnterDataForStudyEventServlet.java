/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2025 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
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
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.managestudy.ViewStudySubjectServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.crfdata.HideCRFManager;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Enter or Validate Data for StudyEvent CRFs
 * @author ssachs
 */
public class EnterDataForStudyEventServlet extends SecureController {
    
	private static final long serialVersionUID = -3152159894339737423L;

	private Locale locale;

    public static final String INPUT_EVENT_ID = "eventId";

    public static final String BEAN_STUDY_EVENT = "studyEvent";

    public static final String BEAN_STUDY_SUBJECT = "studySubject";

    public static final String BEAN_UNCOMPLETED_EVENTDEFINITIONCRFS = "uncompletedEventDefinitionCRFs";

    public static final String BEAN_DISPLAY_EVENT_CRFS = "displayEventCRFs";

    // The study event has an existing discrepancy note related to attribute 'Location'
    // value will be saved as a request attribute
    public final static String HAS_LOCATION_NOTE = "hasLocationNote";
    // The study event has an existing discrepancy note related to attribute 'Start Date'
    // value will be saved as a request attribute
    public final static String HAS_START_DATE_NOTE = "hasStartDateNote";
    // The study event has an existing discrepancy note related to attribute 'End Date'
    // value will be saved as a request attribute
    public final static String HAS_END_DATE_NOTE = "hasEndDateNote";
    // Status of the discrepancy note for attribute 'Location'
    // the value will be saved as a request attribute
    public final static String STATUS_DN_LOCATION ="statusDnLocation";
    // Status of the discrepancy note for attribute 'Start Date'
    // the value will be saved as a request attribute
    public final static String STATUS_DN_START_DATE ="statusDnStartDate";
    // Status of the discrepancy note for attribute 'End Date'
    // the value will be saved as a request attribute
    public final static String STATUS_DN_END_DATE ="statusDnEndDate";
    
    private StudyEventBean getStudyEvent(int eventId) throws Exception {
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());

        StudyBean studyWithSED = currentStudy;
        if (currentStudy.getParentStudyId() > 0) {
            studyWithSED = new StudyBean();
            studyWithSED.setId(currentStudy.getParentStudyId());
        }

        AuditableEntityBean aeb = sedao.findByPKAndStudy(eventId, studyWithSED);

        if (!aeb.isActive()) {
            addPageMessage(respage.getString("study_event_to_enter_data_not_belong_study"));
            throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("study_event_not_belong_study"), "1");
        }

        StudyEventBean seb = (StudyEventBean) aeb;

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sedb = seddao.findByPK(seb.getStudyEventDefinitionId());
        seb.setStudyEventDefinition(sedb);
        // A. Hamid mantis issue 5048
        if (!(currentRole.isDirector() || currentRole.isCoordinator()) && seb.getSubjectEventStatus().isLocked()){
            seb.setEditable(false);
        }
        return seb;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
       // removeLockedCRF(ub.getId());
        getCrfLocker().unlockAllForUser(ub.getId());
        FormProcessor fp = new FormProcessor(request);

        int eventId = fp.getInt(INPUT_EVENT_ID, true);
        request.setAttribute("eventId", eventId + "");

        // so we can display the event for which we're entering data
        StudyEventBean seb = getStudyEvent(eventId);

        // so we can display the subject's label
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean studySubjectBean = ssdao.findByPK(seb.getStudySubjectId());
        int studyId = studySubjectBean.getStudyId();

        StudyDAO studydao = new StudyDAO(sm.getDataSource());
        StudyBean study = studydao.findByPK(studyId);
        // If the study subject derives from a site, and is being viewed from a parent study,
        // then the study IDs will be different. However, since each note is saved with the specific
        // study ID, then its study ID may be different from the study subject's ID.
        boolean subjectStudyIsCurrentStudy = studyId == currentStudy.getId();
        boolean isParentStudy = study.getParentStudyId() < 1;

        // Get any disc notes for this study event
        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());
        ArrayList<DiscrepancyNoteBean> allNotesForSubjectAndEvent;

        // These methods return only parent disc notes
        if (subjectStudyIsCurrentStudy && isParentStudy) {
            allNotesForSubjectAndEvent = discrepancyNoteDAO.findAllStudyEventByStudyAndId(currentStudy, studySubjectBean.getId());
        } else { // findAllStudyEventByStudiesAndSubjectId
            if (!isParentStudy) {
                StudyBean stParent = studydao.findByPK(study.getParentStudyId());
                allNotesForSubjectAndEvent = discrepancyNoteDAO.findAllStudyEventByStudiesAndSubjectId(stParent, study, studySubjectBean.getId());
            } else {
                allNotesForSubjectAndEvent = discrepancyNoteDAO.findAllStudyEventByStudiesAndSubjectId(currentStudy, study, studySubjectBean.getId());
            }
        }

        if (!allNotesForSubjectAndEvent.isEmpty()) {
            setRequestAttributesForNotes(allNotesForSubjectAndEvent, eventId);
        }

        // prepare to figure out what the display should look like
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        ArrayList<EventCRFBean> eventCRFs = ecdao.findAllByStudyEvent(seb);
        ArrayList<Boolean> doRuleSetsExist = new ArrayList<>();

        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = edcdao.findAllActiveByEventDefinitionId(study, seb.getStudyEventDefinitionId());

        // get the event definition CRFs for which no event CRF exists
        // the event definition CRFs must be populated with versions so we can
        // let the user choose which version he will enter data for
        // However, this method seems to be returning DisplayEventDefinitionCRFs
        // that contain valid eventCRFs??
        ArrayList<DisplayEventDefinitionCRFBean> uncompletedEventDefinitionCRFs = getUncompletedCRFs(eventDefinitionCRFs, eventCRFs);
        populateUncompletedCRFsWithCRFAndVersions(uncompletedEventDefinitionCRFs);

        // BWP 2816 << Attempt to provide the DisplayEventDefinitionCRF with a
        // valid owner
        // only if its container eventCRf has a valid id
        populateUncompletedCRFsWithAnOwner(uncompletedEventDefinitionCRFs);
        // >>BWP

        // for the event definition CRFs for which event CRFs exist, get
        // DisplayEventCRFBeans, which the JSP will use to determine what
        // the user will see for each event CRF

        // removing the below row in exchange for the ViewStudySubjectServlet version, for two reasons:
        // 1. concentrate all business logic in one place
        // 2. VSSS seems to handle the javascript creation correctly
        // ArrayList displayEventCRFs = getDisplayEventCRFs(eventCRFs, eventDefinitionCRFs, seb.getSubjectEventStatus());
        ArrayList<DisplayEventCRFBean> displayEventCRFs = ViewStudySubjectServlet.getDisplayEventCRFs(
                sm.getDataSource(),
                eventCRFs,
                eventDefinitionCRFs,
                ub, currentRole,
                seb.getSubjectEventStatus(),
                study
        );

        // Issue 3212 BWP << hide certain CRFs at the site level
        if (currentStudy.getParentStudyId() > 0) {
            HideCRFManager hideCRFManager = HideCRFManager.createHideCRFManager();
            uncompletedEventDefinitionCRFs = hideCRFManager.removeHiddenEventDefinitionCRFBeans(uncompletedEventDefinitionCRFs);
            displayEventCRFs = hideCRFManager.removeHiddenEventCRFBeans(displayEventCRFs);
        }
        // >>

        request.setAttribute(BEAN_STUDY_EVENT, seb);
        request.setAttribute("doRuleSetsExist", doRuleSetsExist);
        request.setAttribute(BEAN_STUDY_SUBJECT, studySubjectBean);
        request.setAttribute(BEAN_UNCOMPLETED_EVENTDEFINITIONCRFS, uncompletedEventDefinitionCRFs);
        request.setAttribute(BEAN_DISPLAY_EVENT_CRFS, displayEventCRFs);

        // @pgawade 31-Aug-2012 fix for issue #15315: Reverting to set the request variable "beans" back
        // this is for generating side info panel
        ArrayList<DisplayStudyEventBean> beans = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(studySubjectBean, sm.getDataSource(), ub, currentRole);
        request.setAttribute("beans", beans);
        EventCRFBean ecb = new EventCRFBean();
        ecb.setStudyEventId(eventId);
        request.setAttribute("eventCRF", ecb);
        // Make available the study
        request.setAttribute("study", currentStudy);

        forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);

        String exceptionName = resexception.getString("no_permission_to_submit_data");
        String noAccessMessage = respage.getString("may_not_enter_data_for_this_study");

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, exceptionName, "1");
    }

    /**
     * Finds all the event definitions for which no event CRF exists - which is
     * the list of event definitions with uncompleted event CRFs.
     *
     * @param eventDefinitionCRFs All the event definition CRFs for this study event.
     * @param eventCRFs All the event CRFs for this study event.
     * @return The list of event definitions for which no event CRF exists.
     */
    private ArrayList<DisplayEventDefinitionCRFBean> getUncompletedCRFs(ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs, ArrayList<EventCRFBean> eventCRFs) {
        HashMap<Integer, Boolean> completed = new HashMap<>();
        HashMap<Integer, EventCRFBean> startedButIncompleted = new HashMap<>();
        ArrayList<DisplayEventDefinitionCRFBean> answer = new ArrayList<>();

        /*
         * A somewhat non-standard algorithm is used here: let answer = empty;
         * foreach event definition ED, set isCompleted(ED) = false foreach
         * event crf EC, set isCompleted(EC.getEventDefinition()) = true foreach
         * event definition ED, if (!isCompleted(ED)) { answer += ED; } return
         * answer; This algorithm is guaranteed to find all the event
         * definitions for which no event CRF exists.
         *
         * The motivation for using this algorithm is reducing the number of database hits.
         *
         * -jun-we have to add more CRFs here: the event CRF which does not have item data yet
         */

        for (EventDefinitionCRFBean edcrf : eventDefinitionCRFs) {
            completed.put(new Integer(edcrf.getCrfId()), Boolean.FALSE);
            startedButIncompleted.put(new Integer(edcrf.getCrfId()), new EventCRFBean());
        }

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
        for (EventCRFBean ecrf : eventCRFs) {
            int crfId = cvdao.getCRFIdFromCRFVersionId(ecrf.getCRFVersionId());
            ArrayList<ItemDataBean> idata = iddao.findAllByEventCRFId(ecrf.getId());
            if (!idata.isEmpty()) { // this crf has data already
                completed.put(new Integer(crfId), Boolean.TRUE);
            } else { // event crf got created, but no data entered
                startedButIncompleted.put(new Integer(crfId), ecrf);
            }
        }

        for (EventDefinitionCRFBean edcrf : eventDefinitionCRFs) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            dedc.setEdc(edcrf);
            Boolean b = completed.get(new Integer(edcrf.getCrfId()));
            EventCRFBean ev = startedButIncompleted.get(new Integer(edcrf.getCrfId()));
            if (b == null || !b.booleanValue()) {
                dedc.setEventCRF(ev);
                answer.add(dedc);
            }
        }

        return answer;
    }

    private void populateUncompletedCRFsWithAnOwner(List<DisplayEventDefinitionCRFBean> displayEventDefinitionCRFBeans) {
        if (displayEventDefinitionCRFBeans == null || displayEventDefinitionCRFBeans.isEmpty()) {
            return;
        }
        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
        UserAccountBean userAccountBean;
        EventCRFBean eventCRFBean;

        for (DisplayEventDefinitionCRFBean dedcBean : displayEventDefinitionCRFBeans) {

            eventCRFBean = dedcBean.getEventCRF();
            if (eventCRFBean != null && eventCRFBean.getOwner() == null && eventCRFBean.getOwnerId() > 0) {
                userAccountBean = userAccountDAO.findByPK(eventCRFBean.getOwnerId());

                eventCRFBean.setOwner(userAccountBean);
            }

            // Failing the above, obtain the owner from the EventDefinitionCRFBean
            if (eventCRFBean != null && eventCRFBean.getOwner() == null) {
                int ownerId = dedcBean.getEdc().getOwnerId();
                if (ownerId > 0) {
                    userAccountBean = userAccountDAO.findByPK(ownerId);

                    eventCRFBean.setOwner(userAccountBean);
                }
            }

        }

    }

    private void populateUncompletedCRFsWithCRFAndVersions(ArrayList<DisplayEventDefinitionCRFBean> uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

        for (int i = 0; i < uncompletedEventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedcrf = uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = cdao.findByPK(dedcrf.getEdc().getCrfId());
            // note that we do not check status in the above query, so let's
            // check it here, tbh 102007
            if (cb.getStatus().equals(Status.AVAILABLE)) {
                // the above does not allow us to show the CRF as a thing with
                // status of 'invalid' so we have to
                // go to the JSP for this one, I think
                dedcrf.getEdc().setCrf(cb);

                ArrayList<CRFVersionBean> theVersions = cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
                ArrayList<CRFVersionBean> versions = new ArrayList<>();
                HashMap<String, CRFVersionBean> crfVersionIds = new HashMap<>();

                for (CRFVersionBean crfVersion : theVersions) {
                    crfVersionIds.put(String.valueOf(crfVersion.getId()), crfVersion);
                }

                if (!dedcrf.getEdc().getSelectedVersionIds().isEmpty()) {
                    String[] kk = dedcrf.getEdc().getSelectedVersionIds().split(",");
                    for (String string : kk) {
                        if (crfVersionIds.get(string) != null) {
                            versions.add(crfVersionIds.get(string));
                        }
                    }
                } else {
                    versions = theVersions;
                }

                dedcrf.getEdc().setVersions(versions);
                // added tbh 092007, fix for 1461
                if (!versions.isEmpty()) {
                    boolean isLocked = false;
                    for (CRFVersionBean crfvb : versions) {
                        logger.debug("...checking versions...{}", crfvb.getName());
                        if (!crfvb.getStatus().equals(Status.AVAILABLE)) {
                            logger.debug("found a non active crf version");
                            isLocked = true;
                        }
                    }
                    logger.debug("re-set event def, line 240: {}", isLocked);
                    if (isLocked) {
                        dedcrf.setStatus(Status.LOCKED);
                        dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    }
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } else { // above added 092007, tbh
                    dedcrf.setStatus(Status.LOCKED);
                    dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } // added 102007, tbh
            } else {
                dedcrf.getEdc().setCrf(cb);
                logger.debug("_found a non active crf _");
                dedcrf.setStatus(Status.LOCKED);
                dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                dedcrf.getEdc().getCrf().setStatus(Status.LOCKED);
                uncompletedEventDefinitionCRFs.set(i, dedcrf);
            } // enclosing if statement added 102007, tbh
        }
    }

    /**
     * Generate a list of DisplayEventCRFBean objects for a study event. Some of
     * the DisplayEventCRFBeans will represent uncompleted Event CRFs; others
     * will represent Event CRFs which are in initial data entry, have completed
     * initial data entry, are in double data entry, or have completed double data entry.
     * The list is sorted using the DisplayEventCRFBean's compareTo method (that
     * is, using the event definition crf bean's ordinal value.) Also, the
     * setFlags method of each DisplayEventCRFBean object will have been called once.
     *
     * @param studyEvent The study event for which we want the Event CRFs.
     * @param ecdao An EventCRFDAO from which to grab the study event's Event CRFs.
     * @param edcdao An EventDefinitionCRFDAO from which to grab the Event CRF Definitions which apply to the study event.
     * @return A list of DisplayEventCRFBean objects related to the study
     *         event, ordered by the EventDefinitionCRF ordinal property, and
     *         with flags already set.
     */
    public static ArrayList<DisplayEventCRFBean> getDisplayEventCRFs(
        StudyEventBean studyEvent,
        EventCRFDAO ecdao,
        EventDefinitionCRFDAO edcdao,
        CRFVersionDAO crfvdao,
        UserAccountBean user,
        StudyUserRoleBean surb) {
        
        ArrayList<DisplayEventCRFBean> answer = new ArrayList<>();
        HashMap<Integer, Integer> indexByCRFId = new HashMap<>();

        ArrayList<EventCRFBean> eventCRFs = ecdao.findAllByStudyEvent(studyEvent);
        ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = edcdao.findAllByEventDefinitionId(studyEvent.getStudyEventDefinitionId());

        // TODO: map this out to another function
        ArrayList<CRFVersionBean> crfVersions = crfvdao.findAll();
        HashMap<Integer, Integer> crfIdByCRFVersionId = new HashMap<>();
        for (CRFVersionBean cvb : crfVersions) {
            crfIdByCRFVersionId.put(new Integer(cvb.getId()), new Integer(cvb.getCrfId()));
        }

        // put the event definition crfs inside DisplayEventCRFs
        for (EventDefinitionCRFBean edcb : eventDefinitionCRFs) {
            DisplayEventCRFBean decb = new DisplayEventCRFBean();
            decb.setEventDefinitionCRF(edcb);

            answer.add(decb);
            indexByCRFId.put(new Integer(edcb.getCrfId()), new Integer(answer.size() - 1));
        }

        // attach EventCRFs to the DisplayEventCRFs
        for (EventCRFBean ecb : eventCRFs) {
            Integer crfVersionId = new Integer(ecb.getCRFVersionId());
            if (crfIdByCRFVersionId.containsKey(crfVersionId)) {
                Integer crfId = crfIdByCRFVersionId.get(crfVersionId);

                if (crfId != null && indexByCRFId.containsKey(crfId)) {
                    Integer indexObj = indexByCRFId.get(crfId);

                    if (indexObj != null) {
                        int index = indexObj.intValue();
                        if (index > 0 && index < answer.size()) {
                            DisplayEventCRFBean decb = answer.get(index);
                            decb.setEventCRF(ecb);
                            answer.set(index, decb);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < answer.size(); i++) {
            DisplayEventCRFBean decb = answer.get(i);
            decb.setFlags(decb.getEventCRF(), user, surb, decb.getEventDefinitionCRF().isDoubleEntry());
            answer.set(i, decb);
        }

        // TODO: attach crf versions to the DisplayEventCRFs

        return answer;
    }

    /**
     * If DiscrepancyNoteBeans have a certain column value, then set flags that
     * a JSP will check in the request attribute. This is a convenience method
     * called by the processRequest() method.
     *
     * @param discBeans A List of DiscrepancyNoteBeans
     * @param currentEventId ID of current event to only display relevant notes
     */
    private void setRequestAttributesForNotes(List<DiscrepancyNoteBean> discBeans, int currentEventId) {
        for (DiscrepancyNoteBean discrepancyNoteBean : discBeans) {
        	if (discrepancyNoteBean.getEntityId() == currentEventId) {
        		if ("location".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
        			request.setAttribute(STATUS_DN_LOCATION, discrepancyNoteBean.getResolutionStatusId());
            		request.setAttribute(HAS_LOCATION_NOTE, "yes");
        		} else if ("start_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
        			request.setAttribute(STATUS_DN_START_DATE, discrepancyNoteBean.getResolutionStatusId());
        			request.setAttribute(HAS_START_DATE_NOTE, "yes");
        		} else if ("end_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
        			request.setAttribute(STATUS_DN_END_DATE, discrepancyNoteBean.getResolutionStatusId());
        			request.setAttribute(HAS_END_DATE_NOTE, "yes");
        		}
        	}	
        }
    }
}
