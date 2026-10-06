package online.banking.system.model;

/** The two kinds of users. An ENUM is a fixed set of named constants. */
public enum Role {
    ADMIN("Admin"),
    CUSTOMER("Customer");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Converts the text shown in the dropdown back to the enum value. */
    public static Role fromLabel(String label) {
        for (Role r : values()) {
            if (r.label.equalsIgnoreCase(label) || r.name().equalsIgnoreCase(label)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
