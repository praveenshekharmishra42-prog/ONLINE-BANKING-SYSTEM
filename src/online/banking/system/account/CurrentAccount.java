package online.banking.system.account;

import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountType;
import java.math.BigDecimal;
import online.banking.system.util.MoneyUtil;
import java.time.LocalDateTime;

/**
 * Current account (for businesses): no minimum balance, no interest, but an
 * OVERDRAFT of Rs. 10,000 - the balance may go below zero down to -10,000.
 * It overrides getWithdrawableAmount() to allow that.
 */
public class CurrentAccount extends Account {

    public static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000.00");
    public static final BigDecimal MIN_OPENING_DEPOSIT = new BigDecimal("5000.00");

    public CurrentAccount(String accountNo, int customerId, BigDecimal balance,
                          AccountStatus status, LocalDateTime openedOn) {
        super(accountNo, customerId, balance, status, openedOn);
    }

    @Override
    public AccountType getType() {
        return AccountType.CURRENT;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMinimumOpeningDeposit() {
        return MIN_OPENING_DEPOSIT;
    }

    /** Balance + overdraft limit. */
    @Override
    public BigDecimal getWithdrawableAmount() {
        BigDecimal amount = getBalance().add(OVERDRAFT_LIMIT);
        return amount.signum() > 0 ? amount : BigDecimal.ZERO;
    }

    @Override
    protected String describeLimit() {
        return "overdraft up to " + MoneyUtil.format(OVERDRAFT_LIMIT);
    }
}
