package online.banking.system.exception;

/** A database operation failed. Wraps the original SQLException as the cause. */
public class DatabaseException extends BankException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
