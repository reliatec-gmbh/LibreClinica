/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.login;

import java.util.Date;

import org.akaza.openclinica.bean.login.PwdChallengeQuestion;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * Servlet of requesting password
 *
 * @author jxu  
 */
public class RequestPasswordServlet extends SecureController {
    
	private static final long serialVersionUID = -6525408217441592170L;

	@Override
    public void mayProceed() throws InsufficientPermissionException {
        // NOOP
    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");
        session.setAttribute("challengeQuestions", PwdChallengeQuestion.toArrayList());

        if (action == null || action.trim().isEmpty()) {
            request.setAttribute("userBean1", new UserAccountBean());
            forwardPage(Page.REQUEST_PWD);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirmPassword();
            } else {
                request.setAttribute("userBean1", new UserAccountBean());
                forwardPage(Page.REQUEST_PWD);
            }
        }
    }
    
    private void confirmPassword() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("email", Validator.IS_A_EMAIL);
        v.addValidation("passwdChallengeQuestion", Validator.NO_BLANKS);
        v.addValidation("passwdChallengeAnswer", Validator.NO_BLANKS);

        errors = v.validate();

        UserAccountBean ubForm = new UserAccountBean(); // user bean from web
        // form
        ubForm.setName(fp.getString("name"));
        ubForm.setEmail(fp.getString("email"));
        ubForm.setPasswdChallengeQuestion(fp.getString("passwdChallengeQuestion"));
        ubForm.setPasswdChallengeAnswer(fp.getString("passwdChallengeAnswer"));

        sm = new SessionManager(null, ubForm.getName(), SpringServletAccess.getApplicationContext(context));

        UserAccountDAO uDAO = new UserAccountDAO(sm.getDataSource());
        // see whether this user in the DB
        UserAccountBean ubDB = uDAO.findByUserName(ubForm.getName());

        UserAccountBean updater = ubDB;

        request.setAttribute("userBean1", ubForm);
        if (!errors.isEmpty()) {
            logger.info("after processing form,has errors");
            request.setAttribute("formMessages", errors);
            forwardPage(Page.REQUEST_PWD);
        } else {
            logger.info("after processing form,no errors");
            // whether this user's email is in the DB
            if (ubDB.getEmail() != null && ubDB.getEmail().equalsIgnoreCase(ubForm.getEmail())) {
                logger.info("ubDB.getPasswdChallengeQuestion()" + ubDB.getPasswdChallengeQuestion());
                logger.info("ubForm.getPasswdChallengeQuestion()" + ubForm.getPasswdChallengeQuestion());
                logger.info("ubDB.getPasswdChallengeAnswer()" + ubDB.getPasswdChallengeAnswer());
                logger.info("ubForm.getPasswdChallengeAnswer()" + ubForm.getPasswdChallengeAnswer());

                // if this user's password challenge can be verified
                if (ubDB.getPasswdChallengeQuestion().equals(ubForm.getPasswdChallengeQuestion()) &&
                    ubDB.getPasswdChallengeAnswer().equalsIgnoreCase(ubForm.getPasswdChallengeAnswer())) {

                    SecurityManager sm = ((SecurityManager) SpringServletAccess.getApplicationContext(context)
                        .getBean("securityManager"));

                    String newPass = sm.genPassword();
                    String newDigestPass = sm.encryptPassword(newPass, ubDB.getRunWebservices());
                    ubDB.setPasswd(newDigestPass);

                    //Date date = local_df.parse("01/01/1900");
                    //cal.setTime(date);
                    //ubDB.setPasswdTimestamp(cal.getTime());
                    ubDB.setPasswdTimestamp(null);
                    ubDB.setUpdater(updater);
                    ubDB.setLastVisitDate(new Date());

                    logger.info("user bean to be updated:" + ubDB.getId() + ubDB.getName() + ubDB.getActiveStudyId());

                    uDAO.update(ubDB);
                    sendPassword(newPass, ubDB);
                } else {
                    addPageMessage(respage.getString("your_password_not_verified_try_again"));
                    forwardPage(Page.REQUEST_PWD);
                }

            } else {
                addPageMessage(respage.getString("your_email_address_not_found_try_again"));
                forwardPage(Page.REQUEST_PWD);
            }
        }

    }

    /**
     * Gets user basic info and set email to the administrator
     * 
     * @param passwd password
     * @param ubDB user account
     */
    private void sendPassword(String passwd, UserAccountBean ubDB) throws Exception {

        logger.info("Sending email...");

        String emailBody = "Hello, " + ubDB.getFirstName() + ", <br>" +
            restext.getString("this_email_is_from_openclinica_admin") + "<br>" +
            restext.getString("your_password_has_been_reset_as") + ": " + passwd + "<br> " +
            restext.getString("you_will_be_required_to_change") + " " +
            restext.getString("time_you_login_to_the_system") + " " +
            restext.getString("use_the_following_link_to_log") + ":<br> " +
            SQLInitServlet.getField("sysURL");
        
        sendEmail(
            ubDB.getEmail().trim(),
            EmailEngine.getAdminEmail(),
            restext.getString("your_openclinica_password"),
            emailBody,
            true,
            respage.getString("your_password_reset_new_password_emailed"),
            respage.getString("your_password_not_send_due_mail_server_problem"),
            true
        );

        session.removeAttribute("challengeQuestions");
        forwardPage(Page.LOGIN);
    }
    
}
