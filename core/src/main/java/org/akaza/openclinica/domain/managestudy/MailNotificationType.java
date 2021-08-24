package org.akaza.openclinica.domain.managestudy;


/**
 * enumeration for mail notification type
 * 
 * @author jbley
 */

public enum MailNotificationType {

    ENABLED,

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
