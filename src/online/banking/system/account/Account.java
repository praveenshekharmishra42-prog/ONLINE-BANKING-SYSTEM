package online.banking.system.account;

import online.banking.system.exception.AccountInactiveException;
import online.banking.system.exception.InsufficientFundsException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountType;
import java.math.BigDecimal;
import online.banking.system.util.MoneyUtil;
import java.time.LocalDateTime;

/**
 * ABSTRACT parent for every bank account.
 *
 * Rules that are the same for all accounts (deposit, status checks) are written
 * once here. Rules that differ (minimum balance, how much can be withdrawn)
 * are abstract or overridden in SavingsAccount and CurrentAccount.
 *
 * validateWithdrawal() calls getWithdrawableAmount(), which each child class
 * answers differently - the same code gives different results at runtime
 * (POLYMORPHISM).
 */
public abstract class Account {

    private final String accountNo;
    private final int customerId;
    private BigDecimal balance;
    private AccountStatus status;
    private final LocalDateTime openedOn;

    protected Account(String accountNo, int customerId, BigDecimal balance,
                      AccountStatus status, LocalDateTime openedOn) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
        this.status = status;
        this.openedOn = openedOn;
    }

    // ---------- rules each account type must define ----------

    public abstract AccountType getType();

    /** Balance that must always remain in the account. */
    public abstract BigDecimal getMinimumBalance();

    /** Smallest first deposit allowed when opening this account type. */
    public abstract BigDecimal getMinimumOpeningDeposit();

    // ---------- shared behaviour ----------

    /** How much money can be taken out right now. CurrentAccount overrides this. */
    public BigDecimal getWithdrawableAmount() {
        BigDecimal amount = balance.subtract(getMinimumBalance());
        return amount.signum() > 0 ? amount : BigDecimal.ZERO;
    }

    public void ensureActive() throws AccountInactiveException {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(accountNo, status);
        }
    }

    public void validateWithdrawal(BigDecimal amount) throws InsufficientFundsException {
        BigDecimal limit = getWithdrawableAmount();
        if (amount.compareTo(limit) > 0) {
            throw new InsufficientFundsException(amount, limit,
                    "You can withdraw at most " + MoneyUtil.format(limit)
                    + " (" + getType() + " account rule: " + describeLimit() + ").");
        }
    }

    /** Text explaining the withdrawal rule, used in error messages. */
    protected String describeLimit() {
        return "minimum balance " + MoneyUtil.format(getMinimumBalance());
    }

    public void deposit(BigDecimal amount) throws InvalidInputException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidInputException("Deposit amount must be greater than zero.");
        }
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) throws InsufficientFundsException, InvalidInputException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidInputException("Withdrawal amount must be greater than zero.");
        }
        validateWithdrawal(amount);
        balance = balance.subtract(amount);
    }

    public String getAccountNo()      { return accountNo; }
    public int getCustomerId()        { return customerId; }
    public BigDecimal getBalance()    { return balance; }
    public AccountStatus getStatus()  { return status; }
    public LocalDateTime getOpenedOn() { return openedOn; }

    public void setStatus(AccountStatus status) { this.status = status; }
}
