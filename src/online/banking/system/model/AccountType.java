package online.banking.system.model;

public enum AccountType {
    SAVINGS("Savings"),
    CURRENT("Current");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AccountType fromLabel(String label) {
        for (AccountType t : values()) {
            if (t.label.equalsIgnoreCase(label) || t.name().equalsIgnoreCase(label)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown account type: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
