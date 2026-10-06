package online.banking.system.model;

/** A bank employee who approves accounts and monitors transactions. */
public class Admin extends Person {

    public Admin(int userId, String username, String name) {
        super(userId, username, name);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String getWelcomeMessage() {
        return "Admin Panel - Welcome, " + getName();
    }
}
