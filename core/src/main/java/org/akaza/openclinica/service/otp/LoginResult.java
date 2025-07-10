/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.service.otp;

/**
 * Enumeration of login results.
 * 
 * @author thillger
 */
public enum LoginResult {
    /**
     * 
     */
    SUCCESSFUL_LOGIN {

        @Override
        public String textual() {
            return "successful";
        }
    },
    /**
     * 
     */
    DENIED_LOGIN {

        @Override
        public String textual() {
            return "unsuccessful";
        }
    };

    /**
     * Returns a textual representation of the given item.
     */
    public abstract String textual();
}
