/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * @author Krikor Krumlian
 */
public class ListEventsForSubjectsServlet extends SecureController {

    // Shaoyu Su
    private static final long serialVersionUID = 1L;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private SubjectDAO subjectDAO;
    private StudySubjectDAO studySubjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private StudyGroupDAO studyGroupDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private CRFDAO crfDAO;
    Locale locale;
    private boolean showMoreLink;
    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
        // set up auto study subject id
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            //int nextLabel = getStudySubjectDAO().findTheGreatestLabel() + 1;
            //request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
        }

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int definitionId = fp.getInt("defId");
        if (definitionId <= 0) {
            addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
            return;
        }

        StudyEventDefinitionBean selectedStudyEventDefinition = getStudyEventDefinitionDao().findByPK(definitionId);

        // For event definitions and group class list in the add subject popup (imported addNewSubjectExpressNew.jsp).
        // Needed unconditionally in both rendering paths -- unrelated to jmesa/LCTable.
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        request.setAttribute("defId", definitionId);

        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        // Use JMesa rendering only when LC_TABLE_RENDERING is explicitly set to "jmesa"
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            // Legacy JMesa rendering path: unchanged behaviour (render and forward)
            request.setAttribute("tableRenderingMode", "jmesa");
            ListEventsForSubjectTableFactory factory = new ListEventsForSubjectTableFactory(showMoreLink);
            factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
            factory.setSubjectDAO(getSubjectDAO());
            factory.setStudySubjectDAO(getStudySubjectDAO());
            factory.setStudyEventDAO(getStudyEventDAO());
            factory.setStudyBean(currentStudy);
            factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
            factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
            factory.setStudyDAO(getStudyDAO());
            factory.setStudyGroupDAO(getStudyGroupDAO());
            factory.setCurrentRole(currentRole);
            factory.setCurrentUser(ub);
            factory.setEventCRFDAO(getEventCRFDAO());
            factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
            factory.setCrfDAO(getCrfDAO());
            factory.setCrfVersionDAO(getCRFVersionDAO());
            factory.setSelectedStudyEventDefinition(selectedStudyEventDefinition);
            String listEventsForSubjectsHtml = factory.createTable(request, response).render();
            request.setAttribute("listEventsForSubjectsHtml", listEventsForSubjectsHtml);
            forwardPage(Page.LIST_EVENTS_FOR_SUBJECTS);
        } else {
            // HtmlFlow rendering path: supports HTMX partials (panel vs full page)
            request.setAttribute("tableRenderingMode", "htmlflow");
            ListEventsForSubjectTable table = new ListEventsForSubjectTable(getStudySubjectDAO(), getSubjectDAO(), getStudyEventDAO(),
                getStudyEventDefinitionDao(), getStudyGroupClassDAO(), getSubjectGroupMapDAO(), getStudyGroupDAO(), getStudyDAO(), getEventCRFDAO(),
                getEventDefinitionCRFDAO(), getCrfDAO(), getCRFVersionDAO(), currentStudy, currentRole, ub, selectedStudyEventDefinition, locale,
                request.getContextPath());
            String listEventsForSubjectsHtml = table.render(request);
            // HTMX partial handling
            response.addHeader("Vary", "HX-Request");
            String hxReq = request.getHeader("HX-Request");
            if (hxReq != null) {
                // HTMX request: only return the table HTML fragment
                try {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write(listEventsForSubjectsHtml);
                    response.getWriter().flush();
                } catch (java.io.IOException e) {
                    logger.error("Error writing listEventsForSubjects HTMX partial response: ", e);
                }
            } else {
                // Non-HTMX request: embed into JSP and forward
                request.setAttribute("listEventsForSubjectsHtml", listEventsForSubjectsHtml);
                forwardPage(Page.LIST_EVENTS_FOR_SUBJECTS);
            }
        }

    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDAO = studyEventDefinitionDAO == null ? new StudyEventDefinitionDAO(sm.getDataSource()) : studyEventDefinitionDAO;
        return studyEventDefinitionDAO;
    }

    public SubjectDAO getSubjectDAO() {
        subjectDAO = this.subjectDAO == null ? new SubjectDAO(sm.getDataSource()) : subjectDAO;
        return subjectDAO;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        studySubjectDAO = this.studySubjectDAO == null ? new StudySubjectDAO(sm.getDataSource()) : studySubjectDAO;
        return studySubjectDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? new StudyGroupClassDAO(sm.getDataSource()) : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? new SubjectGroupMapDAO(sm.getDataSource()) : subjectGroupMapDAO;
        return subjectGroupMapDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        studyEventDAO = this.studyEventDAO == null ? new StudyEventDAO(sm.getDataSource()) : studyEventDAO;
        return studyEventDAO;
    }

    public StudyDAO getStudyDAO() {
        studyDAO = this.studyDAO == null ? new StudyDAO(sm.getDataSource()) : studyDAO;
        return studyDAO;
    }

    public EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO == null ? new EventCRFDAO(sm.getDataSource()) : eventCRFDAO;
        return eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? new EventDefinitionCRFDAO(sm.getDataSource()) : eventDefintionCRFDAO;
        return eventDefintionCRFDAO;
    }

    public CRFDAO getCrfDAO() {
        crfDAO = this.crfDAO == null ? new CRFDAO(sm.getDataSource()) : crfDAO;
        return crfDAO;
    }

    public CRFVersionDAO getCRFVersionDAO(){
    	CRFVersionDAO	crfVersionDAO =new CRFVersionDAO(sm.getDataSource());
    	return crfVersionDAO;
    	}
    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? new StudyGroupDAO(sm.getDataSource()) : studyGroupDAO;
        return studyGroupDAO;
    }

}
