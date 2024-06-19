/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import javax.mail.MessagingException;
import javax.sql.DataSource;

import static org.akaza.openclinica.core.util.ClassCastHelper.asHashMap;

public class EmailActionProcessor implements ActionProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    EmailEngine emailEngine;

    public EmailActionProcessor(DataSource ds) {
        this.ds = ds;
    }

    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        HashMap<String, String> arg0 = asHashMap(arguments[0], String.class, String.class);
        sendEmail(ruleAction, ub, arg0.get("body"), arg0.get("subject"));
    }

    private void sendEmail(RuleActionBean ruleAction, UserAccountBean ub, String body, String subject) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            getEmailEngine().process(((EmailActionBean) ruleAction).getTo().trim(), EmailEngine.getAdminEmail(), subject, body);
            logger.info("Sending email done..");
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        }
    }

    private EmailEngine getEmailEngine() {
        emailEngine = emailEngine != null ? emailEngine : new EmailEngine(EmailEngine.getSMTPHost(), "5");
        return emailEngine;
    }
}
