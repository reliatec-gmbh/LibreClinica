/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.patterns.ocobserver;

public class StudyEventChangeDetails {
	Boolean statusChanged = false;
	Boolean startDateChanged = false;
	Boolean runningInTransaction = false;
	
	public StudyEventChangeDetails()
	{
	}
	
	public StudyEventChangeDetails(Boolean statusChanged,Boolean startDateChanged)
	{
		this.statusChanged = statusChanged;
		this.startDateChanged = startDateChanged;
	}

	public Boolean getStatusChanged() {
		return statusChanged;
	}

	public void setStatusChanged(Boolean statusChanged) {
		this.statusChanged = statusChanged;
	}

	public Boolean getStartDateChanged() {
		return startDateChanged;
	}

	public void setStartDateChanged(Boolean startDateChanged) {
		this.startDateChanged = startDateChanged;
	}

    public Boolean getRunningInTransaction() {
        return runningInTransaction;
    }

    public void setRunningInTransaction(Boolean runningInTransaction) {
        this.runningInTransaction = runningInTransaction;
    }

	
	
}
