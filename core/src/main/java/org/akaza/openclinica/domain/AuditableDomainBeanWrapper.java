/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain;

import java.util.ArrayList;

public class AuditableDomainBeanWrapper<T extends AbstractAuditableMutableDomainObject> {
    private T auditableBean;
    private boolean isSavable;
    private ArrayList<String> importErrors;

    public AuditableDomainBeanWrapper(T auditableBean) {
        importErrors = new ArrayList<String>();
        this.auditableBean = auditableBean;
        isSavable = true;
    }

    public void error(String message) {
        importErrors.add(message);
        setSavable(false);
    }

    public void warning(String message) {
        importErrors.add(message);
    }

    public T getAuditableBean() {
        return auditableBean;
    }

    public void setAuditableBean(T auditableBean) {
        this.auditableBean = auditableBean;
    }

    public ArrayList<String> getImportErrors() {
        return importErrors;
    }

    public void setImportErrors(ArrayList<String> importErrors) {
        this.importErrors = importErrors;
    }

    public boolean isSavable() {
        return isSavable;
    }

    public void setSavable(boolean isSavable) {
        this.isSavable = isSavable;
    }

}
