package online.banking.system.exception;

/**
 * Parent of every custom exception in the application (CHECKED exception).
 * The UI can handle every expected problem with one catch (BankException e).
 */
public class BankException extends Exception {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
