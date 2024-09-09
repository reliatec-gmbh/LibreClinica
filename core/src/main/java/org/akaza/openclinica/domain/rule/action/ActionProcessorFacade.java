/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule)
            throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds, ruleActionRunLogDao, ruleSetRule);
        case EMAIL:
            return new EmailActionProcessor(ds, mailSender, ruleActionRunLogDao, ruleSetRule);
        case NOTIFICATION:
            return new NotificationActionProcessor(ds, mailSender, ruleSetRule);
        case SHOW:
            return new ShowActionProcessor(ds, itemMetadataService, ruleSet);
        case HIDE:
            return new HideActionProcessor(ds, itemMetadataService, ruleSet);
        case INSERT:
            return new InsertActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule);
        case RANDOMIZE:
            // Randomization is removed from LibreClinica
            throw new OpenClinicaSystemException("actionType", "action type 'RANDOMIZE' is not supported in LibreClinica!");
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
