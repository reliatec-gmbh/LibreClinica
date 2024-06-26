/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.exception;

/**
 * @author jbley
 */

@SuppressWarnings("serial")
public class MailNotificationException extends RuntimeException {

	public MailNotificationException() {
		super();
	}

	public MailNotificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailNotificationException(String message) {
		super(message);
	}

	public MailNotificationException(Throwable cause) {
		super(cause);
	}
}
