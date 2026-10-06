package online.banking.system.model;

import java.math.BigDecimal;

/** Numbers shown on the admin dashboard. */
public class DashboardStats {

    private final int totalCustomers;
    private final int pendingAccounts;
    private final int blockedAccounts;
    private final BigDecimal totalDeposits;
    private final int todaysTransactions;

    public DashboardStats(int totalCustomers, int pendingAccounts, int blockedAccounts,
                          BigDecimal totalDeposits, int todaysTransactions) {
        this.totalCustomers = totalCustomers;
        this.pendingAccounts = pendingAccounts;
        this.blockedAccounts = blockedAccounts;
        this.totalDeposits = totalDeposits;
        this.todaysTransactions = todaysTransactions;
    }

    public int getTotalCustomers()       { return totalCustomers; }
    public int getPendingAccounts()      { return pendingAccounts; }
    public int getBlockedAccounts()      { return blockedAccounts; }
    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public int getTodaysTransactions()   { return todaysTransactions; }
}
