package util;

/**
 * Interface for logging activity messages with optional parameters.
 * Demonstrates varargs usage.
 */
public interface Loggable {

    /**
     * Logs an activity message.
     *
     * @param message The log message (can include format specifiers)
     * @param args    Optional arguments to fill into the message
     */
    void logActivity(String message, Object... args);
}
