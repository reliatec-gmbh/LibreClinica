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
