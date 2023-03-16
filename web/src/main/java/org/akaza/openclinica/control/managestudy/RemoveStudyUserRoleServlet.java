/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.managestudy;

import java.util.Date;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Removes a study user role
 *
 * @author jxu
 */
public class RemoveStudyUserRoleServlet extends SecureController {

	private static final long serialVersionUID = 9002693250126772488L;

	/**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_USER_IN_STUDY_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());

        String name = request.getParameter("name");
        String studyIdString = request.getParameter("studyId");
        if ((name == null || name.trim().isEmpty()) || 
            (studyIdString == null || studyIdString.trim().isEmpty())) {

            addPageMessage(respage.getString("please_choose_a_user_to_remove_his_role"));
            forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
        } else {

            String action = request.getParameter("action");
            UserAccountBean user = udao.findByUserName(name);

            if ("confirm".equalsIgnoreCase(action)) {

                request.setAttribute("user", user);

                int studyId = Integer.parseInt(studyIdString.trim());
                StudyUserRoleBean uRole = udao.findRoleByUserNameAndStudyId(name, studyId);
                request.setAttribute("uRole", uRole);

                StudyDAO sdao = new StudyDAO(sm.getDataSource());
                StudyBean study = sdao.findByPK(studyId);
                request.setAttribute("uStudy", study);

                // Role names are specified in localised properties
                request.setAttribute("siteRoleMap", Role.siteRoleMap);

                forwardPage(Page.REMOVE_USER_ROLE_IN_STUDY);
            } else {
                // Remove role
                FormProcessor fp = new FormProcessor(request);
                String userName = fp.getString("name");
                int studyId = fp.getInt("studyId");
                int roleId = fp.getInt("roleId");
                StudyUserRoleBean sur = new StudyUserRoleBean();
                sur.setName(userName);
                sur.setRole(Role.get(roleId));
                sur.setStudyId(studyId);
                sur.setStatus(Status.DELETED);
                sur.setUpdater(ub);
                sur.setUpdatedDate(new Date());
                udao.updateStudyUserRole(sur, userName);

                addPageMessage(generateAlert(user, sur));

                forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
            }
        }
    }

    /**
     * Generate message about user removal for alert box
     *
     * @param u UserAccountBean
     * @param surb StudyUserRoleBean
     */
    private String generateAlert(UserAccountBean u, StudyUserRoleBean surb) {

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = sdao.findByPK(surb.getStudyId());

        // Internationalised role name based on properties
        String roleNameTerm;
        // Parent study
        if (study.getParentStudyId() == 0) {
            roleNameTerm = resterm.getString(Role.studyRoleMap.get(surb.getId())).trim();
        } else { // Study site
            roleNameTerm = resterm.getString(Role.siteRoleMap.get(surb.getId())).trim();
        }

        return u.getFirstName() + " " + u.getLastName() + "(" +
            resword.getString("username") + ": " + u.getName() + ") " +
            respage.getString("has_been_removed_from_the_study_site") + study.getName() + " " +
            respage.getString("with_role") + " " + roleNameTerm + ". " +
            respage.getString("the_user_will_no_longer_access_to_the_study");
    }

}
