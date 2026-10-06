package online.banking.system.exception;

public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String accountNo) {
        super("Account " + accountNo + " does not exist.");
    }
}
