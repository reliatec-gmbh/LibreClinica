/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
/* 
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

public class RuleSetAuditBean extends EntityBean {

    /**
	 * 
	 */
	private static final long serialVersionUID = 897059243940245637L;
	RuleSetBean ruleSetBean;
    Status status;
    UserAccountBean updater;
    Date dateUpdated;

    /**
     * @return the ruleSetBean
     */
    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    /**
     * @param ruleSetBean the ruleSetBean to set
     */
    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the updater
     */
    public UserAccountBean getUpdater() {
        return updater;
    }

    /**
     * @param updater the updater to set
     */
    public void setUpdater(UserAccountBean updater) {
        this.updater = updater;
    }

    /**
     * @return the dateUpdated
     */
    public Date getDateUpdated() {
        return dateUpdated;
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

}
