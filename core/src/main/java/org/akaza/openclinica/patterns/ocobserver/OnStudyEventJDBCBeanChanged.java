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
import org.springframework.context.ApplicationEvent;

/**
 * Event class for JDBC(older) dao beans change, to implement publish/subscribe pattern
 * @author jnyayapathi
 *
 */
public class OnStudyEventJDBCBeanChanged  extends ApplicationEvent  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StudyEventBean studyEventBean;
	
	public OnStudyEventJDBCBeanChanged(StudyEventBean source) {
		super(source);
		this.setStudyEventBean(source);
	}

	public StudyEventBean getStudyEventBean() {
		return studyEventBean;
	}

	public void setStudyEventBean(StudyEventBean studyEventBean) {
		this.studyEventBean = studyEventBean;
	}

}
