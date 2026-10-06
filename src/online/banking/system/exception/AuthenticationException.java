package online.banking.system.exception;

/** Wrong username, password or role. */
public class AuthenticationException extends BankException {

    public AuthenticationException(String message) {
        super(message);
    }
}
