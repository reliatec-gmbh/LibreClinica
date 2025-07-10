/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.bean.login;

public class ResponseSuccessSiteDTO {

	private String uniqueSiteProtocolID;
	private String siteOid;
	private String message;

	public String getUniqueSiteProtocolID() {
		return uniqueSiteProtocolID;
	}

	public void setUniqueSiteProtocolID(String uniqueSiteProtocolID) {
		this.uniqueSiteProtocolID = uniqueSiteProtocolID;
	}

	public String getSiteOid() {
		return siteOid;
	}

	public void setSiteOid(String siteOid) {
		this.siteOid = siteOid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
