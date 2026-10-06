package online.banking.system.account;

import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Creates the right Account subclass from its type.
 * The return type is the parent (Account), so callers never need to know
 * which child they got.
 */
public final class AccountFactory {

    private AccountFactory() {
    }

    public static Account create(AccountType type, String accountNo, int customerId,
                                 BigDecimal balance, AccountStatus status, LocalDateTime openedOn) {
        switch (type) {
            case CURRENT:
                return new CurrentAccount(accountNo, customerId, balance, status, openedOn);
            case SAVINGS:
            default:
                return new SavingsAccount(accountNo, customerId, balance, status, openedOn);
        }
    }

    /** Minimum first deposit for a type, without needing an account object. */
    public static BigDecimal minimumOpeningDeposit(AccountType type) {
        return create(type, "", 0, BigDecimal.ZERO, AccountStatus.PENDING, null).getMinimumOpeningDeposit();
    }
}
