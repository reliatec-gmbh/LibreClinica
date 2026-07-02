package org.akaza.openclinica.lctable;


public class LCTableUtil {
    public static final String TIMESTAMP_FILTER_FOR_HTML_VALIDATION =
        "(?:(?:(?:00|20)(?:00|0[48]|[2468][048]|[13579][26])|(?:0[1-9]|1\\d)(?:0[48]|[2468][048]|[13579][26]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-9]))?))?|(?:(?:00|20)(?:0[1-35-79]|[13579][01345789]|[2468][1-35-79])|(?:0[1-9]|1\\d)(?:00|0[1-35-79]|[13579][01345789]|[2468][1-35-79]))(?:-(?:(?:0[13578]|1[02])(?:-(?:0[1-9]|[12]\\d|3[01]))?|(?:0[469]|11)(?:-(?:0[1-9]|[12]\\d|30))?|02(?:-(?:0[1-9]|1\\d|2[0-8]))?))?)(?: (?:[01]\\d|2[0-3])(?::[0-5]\\d)?)?";

    public static final String TIMESTAMP_FILTER_MESSAGE =
        "Please enter a valid format: yyyy, yyyy-mm, yyyy-mm-dd, yyyy-mm-dd hh, or yyyy-mm-dd hh:mm (years up to 2099)";

    private LCTableUtil() {
        // Private constructor to prevent instantiation of this utility class
    }

}
