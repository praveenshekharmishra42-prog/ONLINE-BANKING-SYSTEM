package online.banking.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One row of the transactions table. Transactions are never edited after they
 * are created, so every field is final (an IMMUTABLE object).
 * Money uses BigDecimal, not double, because double cannot store values like
 * 0.1 exactly and would cause rounding errors in balances.
 */
public class Transaction {

    private final long id;
    private final String accountNo;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final LocalDateTime time;

    public Transaction(long id, String accountNo, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String description, LocalDateTime time) {
        this.id = id;
        this.accountNo = accountNo;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.time = time;
    }

    /** Constructor for a NEW transaction - the database assigns the id and time. */
    public Transaction(String accountNo, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String description) {
        this(0, accountNo, type, amount, balanceAfter, description, null);
    }

    public long getId()                 { return id; }
    public String getAccountNo()        { return accountNo; }
    public TransactionType getType()    { return type; }
    public BigDecimal getAmount()       { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription()      { return description; }
    public LocalDateTime getTime()      { return time; }
}
