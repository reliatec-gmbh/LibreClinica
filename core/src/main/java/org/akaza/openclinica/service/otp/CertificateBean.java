/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.otp;

import java.io.InputStream;

/**
 * Simple Java bean holding information needed to printout a 2-FA certificate.
 * 
 * @author thillger
 */
public class CertificateBean {
    private String username;
    private String secret;
	private String login;
	private String email;
	private InputStream image;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLogin() {
		return login;
	}

    public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public InputStream getImage() {
		return image;
	}

	public void setImage(InputStream image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "TwoFactorReportingBean [login=" + login + ", email=" + email + ", secret=" + secret + "]";
	}
}
