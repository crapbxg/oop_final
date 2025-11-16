package exceptions;

/**
 * Thrown when attempting to register a username that already exists.
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
