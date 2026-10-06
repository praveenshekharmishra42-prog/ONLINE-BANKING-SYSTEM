package online.banking.system.model;

import java.math.BigDecimal;

/** One row for the admin's "Manage Accounts" table (account + owner name). */
public class AccountSummary {

    private final String accountNo;
    private final String customerName;
    private final AccountType type;
    private final BigDecimal balance;
    private final AccountStatus status;

    public AccountSummary(String accountNo, String customerName, AccountType type,
                          BigDecimal balance, AccountStatus status) {
        this.accountNo = accountNo;
        this.customerName = customerName;
        this.type = type;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNo()     { return accountNo; }
    public String getCustomerName()  { return customerName; }
    public AccountType getType()     { return type; }
    public BigDecimal getBalance()   { return balance; }
    public AccountStatus getStatus() { return status; }
}
