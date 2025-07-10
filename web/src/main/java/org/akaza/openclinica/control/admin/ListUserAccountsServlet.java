/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2025 LibreClinica
 */
package org.akaza.openclinica.control.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.UserAccountRow;

public class ListUserAccountsServlet extends SecureController {

	private static final long serialVersionUID = -7298014383987992477L;
	public static final String PATH = "ListUserAccounts";
    public static final String ARG_MESSAGE = "message";

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("you_may_not_perform_administrative_functions"));
            throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, respage.getString("you_may_not_perform_administrative_functions"), "1");
        }
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        UserAccountDAO userAccountDao = new UserAccountDAO(sm.getDataSource());
        EntityBeanTable table = fp.getEntityBeanTable();
        // table.setSortingIfNotExplicitlySet(1, false);

        ArrayList<UserAccountBean> allUsers = this.getAllUsers(userAccountDao);
        this.setStudyNamesInStudyUserRoles(allUsers);
        ArrayList<UserAccountRow> allUserRows = UserAccountRow.generateRowsFromBeans(allUsers);

        String[] columns = {
            resword.getString("user_name"),
            resword.getString("first_name"),
            resword.getString("last_name"),
            resword.getString("status"),
            resword.getString("auth_type"),
            resword.getString("actions")
        };

        table.setColumns(new ArrayList<>(Arrays.asList(columns)));
        table.hideColumnLink(5);
        table.setQuery("ListUserAccounts", new HashMap<>());
        table.addLink(resword.getString("create_a_new_user"), "CreateUserAccount");

        table.setRows(allUserRows);
        table.computeDisplay();

        request.setAttribute("table", table);

        String message = fp.getString(ARG_MESSAGE, true);
        request.setAttribute(ARG_MESSAGE, message);
        request.setAttribute("siteRoleMap", Role.siteRoleMap);
        request.setAttribute("studyRoleMap", Role.studyRoleMap);

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        if (!allUsers.isEmpty()) {
            setToPanel(resword.getString("users"), Integer.toString(allUsers.size()));
        }

        forwardPage(Page.LIST_USER_ACCOUNTS);
    }

    private ArrayList<UserAccountBean> getAllUsers(UserAccountDAO userAccountDao) {
        return userAccountDao.findAll();
    }

    /**
     * For each user, for each study user role, set the study user role studyName property.
     * @param users The users to display in the table of users. Each element is a UserAccountBean.
     */
    private void setStudyNamesInStudyUserRoles(ArrayList<UserAccountBean> users) {
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        ArrayList<StudyBean> allStudies = studyDao.findAll();
        Map<Integer, StudyBean> studiesById = allStudies.stream().collect(Collectors.toMap(EntityBean::getId, sb -> sb));

        for (UserAccountBean user : users) {
            ArrayList<StudyUserRoleBean> roles = user.getRoles();

            for (StudyUserRoleBean studyUserRoleBean : roles) {
                StudyBean studyBean = studiesById.get(studyUserRoleBean.getStudyId());
                if (studyBean != null) {

                    studyUserRoleBean.setParentStudyId(studyBean.getParentStudyId());

                    // Study Name will depend on whether it is study site or parent study role
                    if (studyBean.isSite(studyBean.getParentStudyId())) {

                        // Include parent study name
                        StudyBean parentStudyBean = studiesById.get(studyUserRoleBean.getParentStudyId());
                        studyUserRoleBean.setStudyName(parentStudyBean.getName() + " : " + studyBean.getName());

                    } else { // When parent study role study name is sufficient
                        studyUserRoleBean.setStudyName(studyBean.getName());
                    }
                }
            }
        }
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
