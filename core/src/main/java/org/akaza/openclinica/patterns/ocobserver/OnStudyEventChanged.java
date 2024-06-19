/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.patterns.ocobserver;

import org.springframework.context.ApplicationEvent;
/**
 * The StudyEvent is propagated here on change/update to studyeventdao..
 * @author jnyayapathi
 *
 */
public abstract class OnStudyEventChanged extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StudyEventContainer container;
	
	public OnStudyEventChanged(StudyEventContainer container) {
		super(container);
		this.setContainer(container);
	}

	public StudyEventContainer getContainer() {
		return container;
	}

	public void setContainer(StudyEventContainer container) {
		this.container = container;
	}

}
