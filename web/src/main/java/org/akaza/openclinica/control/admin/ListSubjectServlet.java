/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.lctable.HtmxResponseSupport;

import java.util.Locale;

/**
 * Processes user request and generate subject list
 *
 * @author jxu
 */
public class ListSubjectServlet extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1884177064586726489L;
	Locale locale;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());

        String lcTableRendering = System.getenv("LC_TABLE_RENDERING");
        if (lcTableRendering != null && lcTableRendering.equalsIgnoreCase("jmesa")) {
            request.setAttribute("tableRenderingMode", "jmesa");
            ListSubjectTableFactory factory = new ListSubjectTableFactory();
            factory.setSubjectDao(sdao);
            factory.setStudySubjectDao(subdao);
            factory.setUserAccountDao(uadao);
            factory.setStudyDao(studyDao);
            factory.setCurrentStudy(currentStudy);

            request.setAttribute("listSubjectsHtml", factory.createTable(request, response).render());
            forwardPage(Page.SUBJECT_LIST);
        } else {
            request.setAttribute("tableRenderingMode", "htmlflow");
            ListSubjectTable table = new ListSubjectTable(
                sdao, subdao, uadao, studyDao, currentStudy, LocaleResolver.getLocale(request));
            String listSubjectsHtml = table.render(request);

            if (HtmxResponseSupport.writeFragmentIfRequested(request, response, listSubjectsHtml)) {
                response.getWriter().flush();
            } else {
                request.setAttribute("listSubjectsHtml", listSubjectsHtml);
                forwardPage(Page.SUBJECT_LIST);
            }
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

}
