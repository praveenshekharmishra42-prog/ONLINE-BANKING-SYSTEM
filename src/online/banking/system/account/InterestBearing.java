package online.banking.system.account;

import java.math.BigDecimal;

/**
 * INTERFACE for accounts that earn interest.
 * SavingsAccount implements it; CurrentAccount does NOT.
 * The interest service checks "account instanceof InterestBearing" to decide
 * which accounts get interest - this is interface-based polymorphism.
 */
public interface InterestBearing {

    /** Yearly interest rate in percent, e.g. 4.0 means 4% per year. */
    BigDecimal getAnnualInterestRate();

    /** Interest for one month on the current balance. */
    BigDecimal calculateMonthlyInterest();
}
