/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2025 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

public class ViewUserAccountServlet extends SecureController {

	private static final long serialVersionUID = 2096237550544873731L;
	public static final String PATH = "ViewUserAccount";
    public static final String ARG_USER_ID = "userId";

    public static String getLink(int userId) {
        return PATH + '?' + ARG_USER_ID + '=' + userId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int userId = fp.getInt(ARG_USER_ID, true);
        UserAccountDAO userDao = new UserAccountDAO(sm.getDataSource());

        UserAccountBean user = getBean(userDao, userId);

        if (user.isActive()) {
            request.setAttribute("user", user);
        } else {
            throw new InconsistentStateException(Page.ADMIN_SYSTEM, resexception.getString("the_user_attemping_to_view_not_exists"));
        }
        // BWP>>To provide the view with the correct date format pattern, locale sensitive
        String pattn = "";
        pattn = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        request.setAttribute("dateFormatPattern", pattn);
        forwardPage(Page.VIEW_USER_ACCOUNT);
    }

    private UserAccountBean getBean(UserAccountDAO userDao, int id) {
        UserAccountBean answer = userDao.findByPK(id);
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());

        ArrayList<StudyUserRoleBean> roles = answer.getRoles();

        for (int i = 0; i < roles.size(); i++) {
            StudyUserRoleBean studyUserRoleBean = roles.get(i);
            StudyBean studyBean = studyDao.findByPK(studyUserRoleBean.getStudyId());

            if (studyBean != null) {

                // Study Name will depend on whether it is study site or parent study role
                if (studyBean.isSite(studyBean.getParentStudyId())) {

                    // Include parent study name
                    StudyBean parentStudyBean =studyDao.findByPK(studyBean.getParentStudyId());
                    studyUserRoleBean.setStudyName(parentStudyBean.getName() + " : " + studyBean.getName());

                } else { // When parent study role study name is sufficient
                    studyUserRoleBean.setStudyName(studyBean.getName());
                }
            }

            roles.set(i, studyUserRoleBean);
        }

        answer.setRoles(roles);

        return answer;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
