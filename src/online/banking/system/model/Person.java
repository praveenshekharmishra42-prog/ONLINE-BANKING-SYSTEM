package online.banking.system.model;

/**
 * ABSTRACT parent of every user (Admin and Customer).
 * Common data (id, username, name) is written once here - INHERITANCE.
 * getRole() is abstract and getWelcomeMessage() is overridden, so the same
 * call behaves differently for an Admin and a Customer - POLYMORPHISM.
 */
public abstract class Person {

    private final int userId;
    private final String username;
    private String name;

    protected Person(int userId, String username, String name) {
        this.userId = userId;
        this.username = username;
        this.name = name;
    }

    public int getUserId()      { return userId; }
    public String getUsername() { return username; }
    public String getName()     { return name; }

    public void setName(String name) { this.name = name; }

    public abstract Role getRole();

    public String getWelcomeMessage() {
        return "Welcome, " + name;
    }

    @Override
    public String toString() {
        return getRole() + ": " + name;
    }
}
