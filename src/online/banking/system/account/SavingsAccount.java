package online.banking.system.account;

import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Savings account: must keep Rs. 1,000 minimum balance and earns 4% interest
 * per year (paid monthly). Extends Account AND implements InterestBearing.
 */
public class SavingsAccount extends Account implements InterestBearing {

    public static final BigDecimal MIN_BALANCE = new BigDecimal("1000.00");
    public static final BigDecimal INTEREST_RATE = new BigDecimal("4.0");

    public SavingsAccount(String accountNo, int customerId, BigDecimal balance,
                          AccountStatus status, LocalDateTime openedOn) {
        super(accountNo, customerId, balance, status, openedOn);
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal getMinimumBalance() {
        return MIN_BALANCE;
    }

    @Override
    public BigDecimal getMinimumOpeningDeposit() {
        return MIN_BALANCE;
    }

    @Override
    public BigDecimal getAnnualInterestRate() {
        return INTEREST_RATE;
    }

    /** balance x rate / 100 / 12, rounded to 2 decimal places (paise). */
    @Override
    public BigDecimal calculateMonthlyInterest() {
        if (getBalance().signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return getBalance()
                .multiply(INTEREST_RATE)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
    }
}
