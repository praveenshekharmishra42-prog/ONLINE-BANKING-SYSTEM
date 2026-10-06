package online.banking.system.exception;

/** The user typed something invalid (empty field, bad email, wrong amount...). */
public class InvalidInputException extends BankException {

    public InvalidInputException(String message) {
        super(message);
    }
}
