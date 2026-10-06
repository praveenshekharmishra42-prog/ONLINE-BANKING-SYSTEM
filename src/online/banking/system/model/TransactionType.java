package online.banking.system.model;

/**
 * Every kind of money movement. Each constant knows whether it adds money
 * to the account (credit) or takes money out (debit).
 */
public enum TransactionType {
    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TRANSFER_IN("Transfer In", true),
    TRANSFER_OUT("Transfer Out", false),
    INTEREST("Interest", true);

    private final String label;
    private final boolean credit;

    TransactionType(String label, boolean credit) {
        this.label = label;
        this.credit = credit;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCredit() {
        return credit;
    }

    @Override
    public String toString() {
        return label;
    }
}
