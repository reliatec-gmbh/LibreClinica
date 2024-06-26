/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.submit.crfdata;

public class UpsertOnBean {

    private boolean notStarted = true;
    private boolean dataEntryStarted = true;
    private boolean dataEntryComplete = true;

    public boolean isNotStarted() {
        return notStarted;
    }

    public void setNotStarted(boolean notStarted) {
        this.notStarted = notStarted;
    }

    public boolean isDataEntryStarted() {
        return dataEntryStarted;
    }

    public void setDataEntryStarted(boolean dataEntryStarted) {
        this.dataEntryStarted = dataEntryStarted;
    }

    public boolean isDataEntryComplete() {
        return dataEntryComplete;
    }

    public void setDataEntryComplete(boolean dataEntryComplete) {
        this.dataEntryComplete = dataEntryComplete;
    }

}
