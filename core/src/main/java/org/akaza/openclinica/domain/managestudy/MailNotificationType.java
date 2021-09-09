package org.akaza.openclinica.domain.managestudy;

/**
 * Enumeration items of mail notification types.
 * 
 * @author jbley
 */
public enum MailNotificationType {
    /**
     * System user mail notification is enabled. Every successful login leads to
     * an e-mail.
     */
    ENABLED,
    /**
     * System user mail notification is disabled (default).
     */
    DISABLED;

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

        for (MailNotificationType item : values()) {
            if (item.name().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
