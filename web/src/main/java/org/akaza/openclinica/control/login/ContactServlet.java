/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

/**
 * Sends user message to the administrator
 *
 * @author jxu
 */
public class ContactServlet extends SecureController {

	private static final long serialVersionUID = 1418512105084748633L;

	@Override
    public void mayProceed() throws InsufficientPermissionException {
        // NOOP
    }

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            if (ub != null && ub.getId() > 0) {
                request.setAttribute("name", ub.getName());
                request.setAttribute("email", ub.getEmail());
            }
            forwardPage(Page.CONTACT);
        } else {
            if ("submit".equalsIgnoreCase(action)) {
                Validator v = new Validator(request);
                v.addValidation("name", Validator.NO_BLANKS);
                v.addValidation("email", Validator.IS_A_EMAIL);
                v.addValidation("subject", Validator.NO_BLANKS);
                v.addValidation("message", Validator.NO_BLANKS);

                errors = v.validate();

                FormProcessor fp = new FormProcessor(request);
                if (!errors.isEmpty()) {
                    request.setAttribute("name", fp.getString("name"));
                    request.setAttribute("email", fp.getString("email"));
                    request.setAttribute("subject", fp.getString("subject"));
                    request.setAttribute("message", fp.getString("message"));
                    request.setAttribute("formMessages", errors);
                    forwardPage(Page.CONTACT);
                } else {
                    sendEmail();
                }
            } else {
                if (ub != null && ub.getId() > 0) {
                    request.setAttribute("name", ub.getName());
                    request.setAttribute("email", ub.getEmail());
                }
                forwardPage(Page.CONTACT);
            }

        }

    }

    private void sendEmail() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        String name = fp.getString("name");
        String email = fp.getString("email");
        String subject = fp.getString("subject");
        String message = fp.getString("message");

        logger.info("Sending email...");

        StringBuilder emailBody = new StringBuilder(restext.getString("dear_openclinica_administrator") + ",<br>");
        emailBody.append("<br>").append(name).append(" ").append(restext.getString("sent_you_the_following_message_br")).append("<br>");
        emailBody.append("<br>").append(resword.getString("email")).append(": ").append(email);
        emailBody.append("<br>").append(resword.getString("subject")).append(": ").append(subject);
        emailBody.append("<br>").append(resword.getString("message")).append(": ").append(message);
        if (currentStudy != null && currentStudy.isActive()) {

            String activeStudyLabel;
            // If current study is site
            if (currentStudy.getParentStudyId() > 0) {
                activeStudyLabel = currentStudy.getParentStudyName() + ": " + currentStudy.getName() + " (" + currentStudy.getIdentifier()  + ")";
            } else { // Otherwise, it is parent study
                activeStudyLabel = currentStudy.getName() + " (" + currentStudy.getIdentifier()  + ")";
            }

            emailBody.append("<br>").append(resword.getString("active_study")).append(": ").append(activeStudyLabel);
        }
        String sysUrl = SQLInitServlet.getField("sysURL");
        emailBody.append("<br><br>").append("System URL: ").append("<a href='").append(sysUrl).append("'>").append(sysUrl).append("</a><br>");

        sendEmail(EmailEngine.getAdminEmail(),email, subject, emailBody.toString(),true);

        if (ub != null && ub.getId() > 0) {
            forwardPage(Page.MENU_SERVLET);
        } else {
            forwardPage(Page.LOGIN);
        }
    }

}
