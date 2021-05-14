package org.akaza.openclinica.domain.user;

/**
 * Enumeration of authentication types.
 * 
 * @author thillger
 */
public enum AuthType {
    /**
     * The standard way doing user authentication (username, password).
     */
    STANDARD,
    /**
     * 2-Factor authentication via username, password and 2nd factor (one-time
     * password).
     */
    TWO_FACTOR;
}
