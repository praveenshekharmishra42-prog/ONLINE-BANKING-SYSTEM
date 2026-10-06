package online.banking.system.exception;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal or transfer is larger than the amount the account
 * is allowed to spend. It also stores the numbers, so the UI can show them.
 */
public class InsufficientFundsException extends BankException {

    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(BigDecimal requested, BigDecimal available, String reason) {
        super("Insufficient funds. " + reason);
        this.requested = requested;
        this.available = available;
    }

    public BigDecimal getRequested() { return requested; }
    public BigDecimal getAvailable() { return available; }
}
