/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.managestudy;

import java.util.Date;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DeleteStudyEventServlet extends SecureController{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2156394172493768917L;


	@Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));
        
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studyEventId = fp.getInt("id");// studyEventId
        int studySubId = fp.getInt("studySubId");// studySubjectId

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());

        if (studyEventId == 0) {
            addPageMessage(respage.getString("please_choose_a_SE_to_remove"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyBean study = (StudyBean) studydao.findByPK(studySub.getStudyId());

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                // find all crfs in the definition
                // construct info needed on view study event page
                DisplayStudyEventBean de = new DisplayStudyEventBean();
                de.setStudyEvent(event);

                request.setAttribute("displayEvent", de);
//                request.setAttribute("crfs", eventDefinitionCRFs);

                forwardPage(Page.DELETE_STUDY_EVENT);
            } else {
                logger.info("submit to delete the event from study");
                // delete event from study

                event.setSubjectEventStatus(SubjectEventStatus.NOT_SCHEDULED);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                sedao.update(event);
                String emailBody =
                    respage.getString("the_event") + " " + event.getStudyEventDefinition().getName() + " "
                        + respage.getString("has_been_removed_from_the_subject_record_for") + " " + studySub.getLabel() + " "
                        + respage.getString("in_the_study") + " " + study.getName() + ".";

                addPageMessage(emailBody);
//                sendEmail(emailBody);
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }
    
}
