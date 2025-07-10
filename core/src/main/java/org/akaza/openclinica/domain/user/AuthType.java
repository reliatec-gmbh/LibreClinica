/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.domain.user;

/**
 * Enumeration of authentication types.
 * 
 * @author thillger
 */
public enum AuthType {
	/**
	 * The standard way doing normal user authentication (username, password).
	 */
	STANDARD,
	/**
	 * Similar to {@link #STANDARD} but user is marked to he/she will be using the
	 * 2-FA in the future.
	 */
	MARKED,
	/**
	 * 2-Factor authentication via username, password and 2nd factor (one-time
	 * password).
	 */
	TWO_FACTOR;

    /**
     * Returns true if an item of this enumeration can be found by name - false
     * otherwise. This method also handles null values.
     * 
     * @param name The name to search for. Maybe null.
     */
    public static boolean isValid(String name) {
        if (null == name) {
            return false;
        }

        for (AuthType item : values()) {
            if (item.name().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
