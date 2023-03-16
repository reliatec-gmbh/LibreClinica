/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
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
 * Sets a new study user role
 *
 * @author jxu
 */
public class SetStudyUserRoleServlet extends SecureController {

	private static final long serialVersionUID = 7607566814278848612L;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_USER_IN_STUDY_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        String name = request.getParameter("name");
        String studyIdString = request.getParameter("studyId");

        if ((name == null || name.trim().isEmpty()) ||
            (studyIdString == null || studyIdString.trim().isEmpty())) {

            addPageMessage(respage.getString("please_choose_a_user_to_set_role_for"));
            forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
        } else {
            String action = request.getParameter("action");
            FormProcessor fp = new FormProcessor(request);
            UserAccountBean user = udao.findByUserName(name);
            StudyBean userStudy = sdao.findByPK(fp.getInt("studyId"));
            if ("confirm".equalsIgnoreCase(action)) {

                int studyId = Integer.parseInt(studyIdString.trim());

                request.setAttribute("user", user);

                StudyUserRoleBean uRole = udao.findRoleByUserNameAndStudyId(name, studyId);
                uRole.setStudyName(userStudy.getName());
                request.setAttribute("uRole", uRole);

                ArrayList<Role> roles = Role.toArrayList();
                roles.remove(Role.ADMIN); // admin is not a user role, only used for tomcat
                roles.remove(Role.RESEARCHASSISTANT2);

                StudyBean studyBean = sdao.findByPK(uRole.getStudyId());

                if (currentStudy.getParentStudyId() > 0) {
                    roles.remove(Role.COORDINATOR);
                    roles.remove(Role.STUDYDIRECTOR);

                } else if (studyBean.getParentStudyId() > 0) {
                    roles.remove(Role.COORDINATOR);
                    roles.remove(Role.STUDYDIRECTOR);
                    // TODO: redo this fix
                    Role r = Role.RESEARCHASSISTANT;
                    r.setDescription("site_Data_Entry_Person");
                    roles.remove(Role.RESEARCHASSISTANT);
                    roles.add(r);

                    Role ri = Role.INVESTIGATOR;
                    ri.setDescription("site_investigator");
                    roles.remove(Role.INVESTIGATOR);
                    roles.add(ri);

                    Role r2 = Role.RESEARCHASSISTANT2;
                    r2.setDescription("site_Data_Entry_Person2");
                    roles.remove(Role.RESEARCHASSISTANT2);
                    roles.add(r2);

                }
                request.setAttribute("roles", roles);

                forwardPage(Page.SET_USER_ROLE_IN_STUDY);
            } else {
                // set role
                String userName = fp.getString("name");
                int studyId = fp.getInt("studyId");
                int roleId = fp.getInt("roleId");
                StudyUserRoleBean sur = new StudyUserRoleBean();
                sur.setName(userName);
                sur.setRole(Role.get(roleId));
                sur.setStudyId(studyId);
                sur.setStudyName(userStudy.getName());
                sur.setStatus(Status.AVAILABLE);
                sur.setUpdater(ub);
                sur.setUpdatedDate(new Date());
                udao.updateStudyUserRole(sur, userName);

                addPageMessage(generateAlert(user, sur));

                forwardPage(Page.LIST_USER_IN_STUDY_SERVLET);
            }
        }
    }

    /**
     * Generate message about user role set for alert box
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

        return u.getFirstName() + " " + u.getLastName() + " (" +
                resword.getString("username") + ": " + u.getName() + ") " +
                respage.getString("has_been_granted_the_role") + " " + roleNameTerm + " " +
                respage.getString("in_the_study_site") + " " + study.getName() + ".";
    }

}
