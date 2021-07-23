package org.akaza.openclinica.control.login;

import org.springframework.security.authentication.AccountStatusException;

/**
 * A {@link AccountStatusException} which should be thrown when an account is
 * not validly configured for 2-FA but the due date has expired.
 * 
 * @see login-alertbox.jsp
 * @see notes.properties
 * @see applicationContext-security.xml
 * @author thillger
 */
@SuppressWarnings("serial")
public final class AccountConfigurationException extends AccountStatusException {

    public AccountConfigurationException() {
        this("");
    }

    public AccountConfigurationException(String msg) {
        super(msg);
    }
}
