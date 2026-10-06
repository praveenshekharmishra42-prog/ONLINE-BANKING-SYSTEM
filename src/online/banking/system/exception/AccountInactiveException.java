package online.banking.system.exception;

import online.banking.system.model.AccountStatus;

/** The account is PENDING approval or BLOCKED, so money cannot move. */
public class AccountInactiveException extends BankException {

    public AccountInactiveException(String accountNo, AccountStatus status) {
        super("Account " + accountNo + " is " + status
                + (status == AccountStatus.PENDING
                    ? ". It must be approved by the bank before it can be used."
                    : ". Please contact the bank."));
    }
}
