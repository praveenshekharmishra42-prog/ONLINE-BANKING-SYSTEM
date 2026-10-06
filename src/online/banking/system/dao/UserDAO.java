package online.banking.system.dao;

import online.banking.system.db.DBConnection;
import online.banking.system.exception.AuthenticationException;
import online.banking.system.exception.DatabaseException;
import online.banking.system.model.Admin;
import online.banking.system.model.Customer;
import online.banking.system.model.Person;
import online.banking.system.model.Role;
import online.banking.system.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Login accounts (users table): authentication, creation and password change. */
public class UserDAO {

    private static final String FIND_FOR_LOGIN =
            "SELECT user_id, username, full_name, password_hash, salt FROM users WHERE username = ? AND role = ?";
    private static final String FIND_BY_ID =
            "SELECT password_hash, salt FROM users WHERE user_id = ?";
    private static final String USERNAME_EXISTS = "SELECT COUNT(*) FROM users WHERE username = ?";
    private static final String INSERT =
            "INSERT INTO users (username, password_hash, salt, role, full_name) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_PASSWORD =
            "UPDATE users SET password_hash = ?, salt = ? WHERE user_id = ?";

    private final DBConnection db = DBConnection.getInstance();

    /**
     * Returns an Admin or a Customer object as the parent type Person.
     * The same error message is used for "no such user" and "wrong password"
     * so an attacker cannot find out which usernames exist.
     */
    public Person authenticate(String username, String password, Role role)
            throws DatabaseException, AuthenticationException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_FOR_LOGIN)) {
            ps.setString(1, username);
            ps.setString(2, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !PasswordUtil.verify(password, rs.getString("salt"), rs.getString("password_hash"))) {
                    throw new AuthenticationException("Invalid username, password or role.");
                }
                int id = rs.getInt("user_id");
                String name = rs.getString("full_name");
                return role == Role.ADMIN
                        ? new Admin(id, username, name)
                        : new Customer(id, username, name);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Login failed: " + e.getMessage(), e);
        }
    }

    public boolean usernameExists(String username) throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(USERNAME_EXISTS)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check username: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts a user using a connection supplied by the caller, so it can be
     * part of a bigger transaction. Returns the new auto-generated user_id.
     */
    public int insert(Connection con, String username, String password, Role role, String fullName)
            throws SQLException {
        String salt = PasswordUtil.generateSalt();
        try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password, salt));
            ps.setString(3, salt);
            ps.setString(4, role.name());
            ps.setString(5, fullName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("No user id was generated.");
            }
        }
    }

    public void changePassword(int userId, String oldPassword, String newPassword)
            throws DatabaseException, AuthenticationException {
        try (Connection con = db.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !PasswordUtil.verify(oldPassword, rs.getString("salt"), rs.getString("password_hash"))) {
                        throw new AuthenticationException("Current password is incorrect.");
                    }
                }
            }
            String salt = PasswordUtil.generateSalt();
            try (PreparedStatement ps = con.prepareStatement(UPDATE_PASSWORD)) {
                ps.setString(1, PasswordUtil.hash(newPassword, salt));
                ps.setString(2, salt);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not change password: " + e.getMessage(), e);
        }
    }
}
