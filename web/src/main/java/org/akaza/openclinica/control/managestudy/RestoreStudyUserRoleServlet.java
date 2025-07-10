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
 * Restores a removed study user role
 *
 * @author jxu
 */
public class RestoreStudyUserRoleServlet extends SecureController {
    
	private static final long serialVersionUID = 1772512586472947684L;

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

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());

        String name = request.getParameter("name");
        String studyIdString = request.getParameter("studyId");
        if ((name == null || name.trim().isEmpty()) ||
            (studyIdString == null || studyIdString.trim().isEmpty())) {

            addPageMessage(respage.getString("please_choose_a_user_to_restore_his_role"));
            forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
        } else {

            String action = request.getParameter("action");
            UserAccountBean user = udao.findByUserName(name);

            int studyId = Integer.parseInt(studyIdString.trim());
            StudyBean study = sdao.findByPK(studyId);
            
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("user", user);
                StudyUserRoleBean uRole = udao.findRoleByUserNameAndStudyId(name, studyId);
                request.setAttribute("uRole", uRole);
                request.setAttribute("uStudy", study);

                // Role names are specified in localised properties
                request.setAttribute("siteRoleMap", Role.siteRoleMap);

                forwardPage(Page.RESTORE_USER_ROLE_IN_STUDY);
            } else {
                // Restore role
                FormProcessor fp = new FormProcessor(request);
                String userName = fp.getString("name");
                int roleId = fp.getInt("roleId");
                StudyUserRoleBean sur = new StudyUserRoleBean();
                sur.setName(userName);
                sur.setRole(Role.get(roleId));
                sur.setStudyId(studyId);
                sur.setStatus(Status.AVAILABLE);
                sur.setUpdater(ub);
                sur.setUpdatedDate(new Date());
                udao.updateStudyUserRole(sur, userName);

                // Internationalised role name based on properties
                String roleNameTerm;
                // Parent study
                if (study.getParentStudyId() == 0) {
                    roleNameTerm = resterm.getString(Role.studyRoleMap.get(sur.getId())).trim();
                } else { // Study site
                    roleNameTerm = resterm.getString(Role.siteRoleMap.get(sur.getId())).trim();
                }

                String restoreMessage = user.getFirstName() + " " + user.getLastName() + " (" +
                        resword.getString("username") + ": " + user.getName() + ") " +
                        respage.getString("has_been_restored_to_the_study_site") + " " + study.getName() + " " +
                        respage.getString("with_the_role") + " " + roleNameTerm + ". " +
                        respage.getString("the_user_is_now_be_able_to_access_study");
                addPageMessage(restoreMessage);
                
                forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
            }
        }
    }
    
}
