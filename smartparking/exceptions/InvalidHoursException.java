package exceptions;

/**
 * Thrown when entered hours for parking exit are invalid (e.g., < 1 or not an integer).
 */
public class InvalidHoursException extends Exception {
    public InvalidHoursException(String message) {
        super(message);
    }
}
