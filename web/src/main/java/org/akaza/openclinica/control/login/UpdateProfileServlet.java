/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.login;

import static org.akaza.openclinica.view.Page.MENU_SERVLET;
import static org.akaza.openclinica.view.Page.UPDATE_PROFILE;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;

/**
 * @author jxu
 * @version CVS: $Id: UpdateProfileServlet.java,v 1.9 2005/02/23 18:58:11 jxu
 *          Exp $
 *          Servlet for processing 'update profile' request from user
 */
public class UpdateProfileServlet extends SecureController {
    private static final long serialVersionUID = -2519124535258437372L;

    @Override
    public void mayProceed() throws InsufficientPermissionException {}

    @Override
    public void processRequest() throws Exception {
        String userSentAction = request.getParameter("action");
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean userBean1 = udao.findByUserName(ub.getName());

        ArrayList<StudyBean> studies = sdao.findAllByUser(ub.getName());

        if (isBlank(userSentAction)) {
            request.setAttribute("studies", studies);
            session.setAttribute("userBean1", userBean1);
            forwardPage(UPDATE_PROFILE);
            return;
        }

        switch (userSentAction.toLowerCase()) {
            case "confirm":
                if (logger.isInfoEnabled()) {
                    logger.info("confirm");
                }
                request.setAttribute("studies", studies);
                confirmProfile(userBean1, udao);
                break;
            case "submit":
                if (logger.isInfoEnabled()) {
                    logger.info("submit");
                }
                submitProfile(udao);

                addPageMessage(respage.getString("profile_updated_succesfully"));
                ub.incNumVisitsToMainMenu();
                forwardPage(MENU_SERVLET);
                break;
        }
    }

    private void confirmProfile(UserAccountBean userBean1, UserAccountDAO udao) throws Exception {
        Validator v = new Validator(request);
        FormProcessor formProcessor = new FormProcessor(request);

        v.addValidation("firstName", Validator.NO_BLANKS);
        v.addValidation("lastName", Validator.NO_BLANKS);
        v.addValidation("email", Validator.IS_A_EMAIL);
        if (!userBean1.isLdapUser()) {
            v.addValidation("passwdChallengeQuestion", Validator.NO_BLANKS);
            v.addValidation("passwdChallengeAnswer", Validator.NO_BLANKS);
            v.addValidation("oldPasswd", Validator.NO_BLANKS);// old password
            String password = formProcessor.getString("passwd").trim();

            ConfigurationDao configurationDao = SpringServletAccess.getApplicationContext(context).getBean(ConfigurationDao.class);

            org.akaza.openclinica.core.SecurityManager sm = (org.akaza.openclinica.core.SecurityManager) SpringServletAccess.getApplicationContext(context)
                    .getBean("securityManager");

            String newDigestPass = sm.encrytPassword(password, getUserDetails());
            List<String> pwdErrors = new ArrayList<String>();

            if (!StringUtils.isBlank(password)) {
                v.addValidation("passwd", Validator.IS_A_PASSWORD);// new
                                                                   // password
                v.addValidation("passwd1", Validator.CHECK_SAME, "passwd");// confirm
                // password

                PasswordRequirementsDao passwordRequirementsDao = new PasswordRequirementsDao(configurationDao);
                Locale locale = LocaleResolver.getLocale(request);
                ResourceBundle resexception = ResourceBundleProvider.getExceptionsBundle(locale);

                pwdErrors = PasswordValidator.validatePassword(passwordRequirementsDao, udao, userBean1.getId(), password, newDigestPass, resexception);
            }
            v.addValidation("phone", Validator.NO_BLANKS);
            errors = v.validate();
            for (String err : pwdErrors) {
                Validator.addError(errors, "passwd", err);
            }

            userBean1.setFirstName(formProcessor.getString("firstName"));
            userBean1.setLastName(formProcessor.getString("lastName"));
            userBean1.setEmail(formProcessor.getString("email"));
            userBean1.setInstitutionalAffiliation(formProcessor.getString("institutionalAffiliation"));
            userBean1.setPasswdChallengeQuestion(formProcessor.getString("passwdChallengeQuestion"));
            userBean1.setPasswdChallengeAnswer(formProcessor.getString("passwdChallengeAnswer"));
            userBean1.setPhone(formProcessor.getString("phone"));
            userBean1.setActiveStudyId(formProcessor.getInt("activeStudyId"));
            userBean1.setAuthtype(formProcessor.getString("authtype"));
            userBean1.setAuthsecret(formProcessor.getString("authsecret"));
            StudyDAO sdao = new StudyDAO(this.sm.getDataSource());

            StudyBean newActiveStudy = sdao.findByPK(userBean1.getActiveStudyId());
            request.setAttribute("newActiveStudy", newActiveStudy);

            if (errors.isEmpty()) {
                logger.info("no errors");

                session.setAttribute("userBean1", userBean1);
                String oldPass = formProcessor.getString("oldPasswd").trim();

                if (!userBean1.isLdapUser() && !sm.verifyPassword(oldPass, getUserDetails())) {
                    Validator.addError(errors, "oldPasswd", resexception.getString("wrong_old_password"));
                    request.setAttribute("formMessages", errors);
                    // addPageMessage("Wrong old password. Please try again.");
                    forwardPage(Page.UPDATE_PROFILE);
                } else {
                    if (!StringUtils.isBlank(formProcessor.getString("passwd"))) {
                        userBean1.setPasswd(newDigestPass);
                        userBean1.setPasswdTimestamp(new Date());
                    }
                    session.setAttribute("userBean1", userBean1);
                    forwardPage(Page.UPDATE_PROFILE_CONFIRM);
                }
            } else {
                logger.info("has validation errors");
                session.setAttribute("userBean1", userBean1);
                request.setAttribute("formMessages", errors);
                forwardPage(Page.UPDATE_PROFILE);
            }
        }
    }

    /**
     * Updates user new profile
     */
    private void submitProfile(UserAccountDAO udao) {
        logger.info("user bean to be updated:" + ub.getId() + ub.getFirstName());

        UserAccountBean userBean1 = (UserAccountBean) session.getAttribute("userBean1");
        if (userBean1 != null) {
            userBean1.setLastVisitDate(new Date());
            userBean1.setUpdater(ub);
            udao.update(userBean1);

            session.setAttribute("userBean", userBean1);
            ub = userBean1;
            session.removeAttribute("userBean1");
        }
    }

}
