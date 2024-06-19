/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.controller.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.user.LdapUserService;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling listLdapUsers View for listing and selecting LDAP account during new user account creation
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 */
@Controller("ldapUserController")
public class LdapUserController {

    private static final String PAGE_CREATE_USER_ACCOUNT = "CreateUserAccount";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final LdapUserService ldapUserService;
    private final UserAccountDAO userAccountDao;

    @Autowired
    public LdapUserController(LdapUserService ldapUserService, UserAccountDAO userAccountDao) {
        
        this.ldapUserService = ldapUserService;
        this.userAccountDao = userAccountDao;
    }

    @RequestMapping("/admin/listLdapUsers")
    public ModelMap listLdapUsers(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam(value = "filter", required = false) String filter) {

        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }

        // If no filter is provided, just render the page without results table
        if (!StringUtils.isEmpty(filter)) {

            // Retrieve existing users to remove them from LDAP search results.
            Collection<UserAccountBean> existingAccounts = userAccountDao.findAll();

            Set<String> existingUsernames = new HashSet<>(existingAccounts.size());
            for (UserAccountBean existingAccount : existingAccounts) {
                existingUsernames.add(existingAccount.getName());
            }

            request.setAttribute("memberList", this.ldapUserService.listNewUsers(filter, existingUsernames));
        }

        return new ModelMap();
    }

    @RequestMapping("/admin/selectLdapUser")
    public String selectLdapUser(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam(value="dn", required = false) String dn) {

        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Error while redirecting to MainMenu: ", e);
            }
            return null;
        }

        // A request without distinguishedName "dn" parameter is used to just close the view and return back to user creation view
        if (!StringUtils.isEmpty(dn)) {
            request.getSession().setAttribute("ldapUser", this.ldapUserService.loadUser(dn));
        }

        return "redirect:/" + PAGE_CREATE_USER_ACCOUNT;
    }

    private boolean mayProceed(HttpServletRequest request) {
        UserAccountBean userBean = (UserAccountBean)request.getSession().getAttribute("userBean");

        return userBean.isTechAdmin() || userBean.isSysAdmin();
    }
    
}
