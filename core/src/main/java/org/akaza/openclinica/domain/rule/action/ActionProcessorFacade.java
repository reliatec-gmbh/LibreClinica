/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 * copyright (C) 2026 UMIN (University Hospital Medical Information Network)
 *
 * Author:  Yoshiteru Chiba
 * Notice:  Developed under a service-agreement contract with UMIN;
 *          copyright in this modification has been assigned to UMIN. The
 *          author retains moral rights inalienable under Article 59
 *          of the Japanese Copyright Act. See README for full
 *          attribution.
 *
 * Modifications:
 *   - RANDOMIZE case: restored dispatch to RandomizeActionProcessor
 *     (the LibreClinica fork had stubbed this case to throw
 *     "not supported"); date: 2026-03-11 - 2026-03-12.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
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
            return new RandomizeActionProcessor(ds, itemMetadataService, ruleActionRunLogDao, ruleSet, ruleSetRule);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
