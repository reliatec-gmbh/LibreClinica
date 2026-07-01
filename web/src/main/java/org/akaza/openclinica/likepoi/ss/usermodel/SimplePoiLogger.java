package org.akaza.openclinica.likepoi.ss.usermodel;  //derived from de.reliatec.likepoi.ss.usermodel

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Instead of the standard logger. For debugging.
 */
public class SimplePoiLogger {

    private static final String LOG_FILE_PATH = "C:\\tmp\\log\\poi.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    public static void debug(String format, Object... arguments) {
        log(Level.DEBUG, format, arguments);
    }

    public static void info(String format, Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        log(Level.WARN, format, arguments);
    }

    public static void error(String format, Object... arguments) {
        log(Level.ERROR, format, arguments);
    }

    private static synchronized void log(Level level, String format, Object... arguments) {
        String message = formatMessage(format, arguments);
        String timestamp = DATE_FORMAT.format(new Date());
        String logLine = String.format("%s [%s] %s - %s", timestamp, Thread.currentThread().getName(), level, message);

        // 1. In Datei schreiben
        try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logLine);
            
            // Falls das letzte Argument eine Exception ist, Stacktrace mitschreiben
            if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
                ((Throwable) arguments[arguments.length - 1]).printStackTrace(pw);
            }
        } catch (IOException e) {
            System.err.println("[PoiLogger-Failed] " + e.getMessage());
        }

        // 2. Erzwungener Fallback in catalina.out
        System.out.println("[POI-CUSTOM] " + logLine);
    }

    // Ersetzt {} durch die übergebenen Argumente (SLF4J-Verhalten)
    private static String formatMessage(String format, Object... arguments) {
        if (format == null || arguments == null || arguments.length == 0) {
            return format;
        }

        StringBuilder result = new StringBuilder();
        int argumentIndex = 0;
        int lastIndex = 0;
        int currentIndex;

        while ((currentIndex = format.indexOf("{}", lastIndex)) != -1) {
            result.append(format, lastIndex, currentIndex);
            
            if (argumentIndex < arguments.length) {
                Object arg = arguments[argumentIndex++];
                // Falls eine Exception als letztes Argument für den Stacktrace übergeben wird,
                // ignorieren wir das {} falls keine Argumente mehr da sind
                if (arg instanceof Throwable && argumentIndex == arguments.length && currentIndex == format.length() - 2) {
                    result.append("{}");
                } else {
                    result.append(arg != null ? arg.toString() : "null");
                }
            } else {
                result.append("{}");
            }
            lastIndex = currentIndex + 2;
        }
        result.append(format.substring(lastIndex));
        return result.toString();
    }
}
