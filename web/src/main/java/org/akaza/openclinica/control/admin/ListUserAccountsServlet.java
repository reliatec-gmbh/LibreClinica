/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
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
    /**
	 * 
	 */
	private static final long serialVersionUID = -7298014383987992477L;
	public static final String PATH = "ListUserAccounts";
    public static final String ARG_MESSAGE = "message";

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("you_may_not_perform_administrative_functions"));
            throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, respage.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        EntityBeanTable table = fp.getEntityBeanTable();
        // table.setSortingIfNotExplicitlySet(1, false);

        ArrayList<UserAccountBean> allUsers = getAllUsers(udao);
        setStudyNamesInStudyUserRoles(allUsers);
        ArrayList<UserAccountRow> allUserRows = UserAccountRow.generateRowsFromBeans(allUsers);

        String[] columns =
            { resword.getString("user_name"), resword.getString("first_name"), resword.getString("last_name"), resword.getString("status"),
                        resword.getString("auth_type"), resword.getString("actions")};
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
        if (allUsers.size() > 0) {
            setToPanel(resword.getString("users"), new Integer(allUsers.size()).toString());
        }

        forwardPage(Page.LIST_USER_ACCOUNTS);
    }

    private ArrayList<UserAccountBean> getAllUsers(UserAccountDAO udao) {
        ArrayList<UserAccountBean> result = udao.findAll();
        return result;
    }

    /**
     * For each user, for each study user role, set the study user role's
     * studyName property.
     *
     * @param users
     *            The users to display in the table of users. Each element is a
     *            UserAccountBean.
     */
    private void setStudyNamesInStudyUserRoles(ArrayList<UserAccountBean> users) {
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList<StudyBean> allStudies = sdao.findAll();
        Map<Integer, StudyBean> studiesById = allStudies.stream().collect(Collectors.toMap(EntityBean::getId, sb -> sb));

        for(UserAccountBean u : users) {
            ArrayList<StudyUserRoleBean> roles = u.getRoles();

            for(StudyUserRoleBean surb : roles) {
                StudyBean sb = studiesById.get(new Integer(surb.getStudyId()));
                if (sb != null) {
                    surb.setStudyName(sb.getName());
                    surb.setParentStudyId(sb.getParentStudyId());
                }
            }
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
