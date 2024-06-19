/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.rule;

public class RunOnSchedule {

	private String runTime;

	
	public RunOnSchedule() {
		super();
	}


	public RunOnSchedule(String runTime){
		this.runTime=runTime;
		
	}


	public String getRunTime() {
		return runTime;
	}


	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}


	
	

	
	
	
	
}
