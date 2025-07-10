/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AdminSystemServlet extends SecureController {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4586499211713524350L;
	Locale locale;

    // < ResourceBundleresword,resexception;
    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        // find last 5 modifed studies
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList<StudyBean> studies = sdao.findAllByLimit(true);
        request.setAttribute("studies", studies);
        ArrayList<StudyBean> allStudies = sdao.findAll();
        request.setAttribute("allStudyNumber", new Integer(allStudies.size()));

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList<UserAccountBean> users = udao.findAllByLimit(true);
        request.setAttribute("users", users);
        ArrayList<UserAccountBean> allUsers = udao.findAll();
        request.setAttribute("allUserNumber", new Integer(allUsers.size()));

        SubjectDAO subdao = new SubjectDAO(sm.getDataSource());
        ArrayList<SubjectBean> subjects = subdao.findAllByLimit(true);
        request.setAttribute("subjects", subjects);
        ArrayList<SubjectBean> allSubjects = subdao.findAll();
        request.setAttribute("allSubjectNumber", new Integer(allSubjects.size()));

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        ArrayList<CRFBean> crfs = cdao.findAllByLimit(true);
        request.setAttribute("crfs", crfs);
        ArrayList<CRFBean> allCrfs = cdao.findAll();
        request.setAttribute("allCrfNumber", new Integer(allCrfs.size()));

        resetPanel();
        panel.setOrderedData(true);
        setToPanel(resword.getString("in_the_application"), "");
        if (allSubjects.size() > 0) {
            setToPanel(resword.getString("subjects"), new Integer(allSubjects.size()).toString());
        }
        if (allUsers.size() > 0) {
            setToPanel(resword.getString("users"), new Integer(allUsers.size()).toString());
        }
        if (allStudies.size() > 0) {
            setToPanel(resword.getString("studies"), new Integer(allStudies.size()).toString());
        }
        if (allCrfs.size() > 0) {
            setToPanel(resword.getString("CRFs"), new Integer(allCrfs.size()).toString());
        }

        panel.setStudyInfoShown(false);
        forwardPage(Page.ADMIN_SYSTEM);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (!ub.isSysAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, "You may not perform administrative functions", "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
