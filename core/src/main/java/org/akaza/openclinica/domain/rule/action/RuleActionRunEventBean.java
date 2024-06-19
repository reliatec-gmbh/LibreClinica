/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.rule.action;

public class RuleActionRunEventBean {
   
   Boolean not_started;
   Boolean scheduled;
   Boolean data_entry_started;
   Boolean complete;
   Boolean skipped;
   Boolean stopped;
	
   
   public Boolean getNot_started() {
		return not_started;
	}
	public void setNot_started(Boolean not_started) {
		this.not_started = not_started;
	}
	public Boolean getScheduled() {
		return scheduled;
	}
	public void setScheduled(Boolean scheduled) {
		this.scheduled = scheduled;
	}
	public Boolean getData_entry_started() {
		return data_entry_started;
	}
	public void setData_entry_started(Boolean data_entry_started) {
		this.data_entry_started = data_entry_started;
	}
	public Boolean getComplete() {
		return complete;
	}
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
	public Boolean getSkipped() {
		return skipped;
	}
	public void setSkipped(Boolean skipped) {
		this.skipped = skipped;
	}
	public Boolean getStopped() {
		return stopped;
	}
	public void setStopped(Boolean stopped) {
		this.stopped = stopped;
	}

   }