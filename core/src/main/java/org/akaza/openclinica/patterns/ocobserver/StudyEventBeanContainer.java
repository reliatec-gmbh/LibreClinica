/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.patterns.ocobserver;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;

public class StudyEventBeanContainer implements Listener{
	private StudyEventBean event = null;
	private StudyEventChangeDetails changeDetails = null;
	
	public StudyEventBeanContainer(StudyEventBean event, StudyEventChangeDetails changeDetails)
	{
		this.event = event;
		this.changeDetails = changeDetails;
	}

	public StudyEventBean getEvent() {
		return event;
	}

	public void setEvent(StudyEventBean event) {
		this.event = event;
	}

	public StudyEventChangeDetails getChangeDetails() {
		return changeDetails;
	}

	public void setChangeDetails(StudyEventChangeDetails changeDetails) {
		this.changeDetails = changeDetails;
	}

	@Override
	public void setObserver(Observer o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Observer getObserver() {
		// TODO Auto-generated method stub
		return null;
	}

}
