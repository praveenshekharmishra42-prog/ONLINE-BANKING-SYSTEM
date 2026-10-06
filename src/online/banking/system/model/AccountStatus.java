package online.banking.system.model;

/**
 * PENDING  - account opened online, waiting for admin approval
 * ACTIVE   - normal account, all transactions allowed
 * BLOCKED  - frozen by the admin, no transactions allowed
 */
public enum AccountStatus {
    PENDING, ACTIVE, BLOCKED
}
