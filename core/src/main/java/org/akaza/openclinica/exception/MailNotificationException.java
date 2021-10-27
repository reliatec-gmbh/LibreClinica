package org.akaza.openclinica.exception;


/**
 * 
 * @author jbley
 *
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
