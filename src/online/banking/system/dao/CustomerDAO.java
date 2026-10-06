package online.banking.system.dao;

import online.banking.system.db.DBConnection;
import online.banking.system.exception.DatabaseException;
import online.banking.system.model.Customer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Personal details of customers (customers table, joined with users). */
public class CustomerDAO implements GenericDAO<Customer, Integer> {

    private static final String INSERT =
            "INSERT INTO customers (customer_id, father_name, dob, gender, email, phone, address, city, state, pincode, occupation) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT =
            "SELECT c.*, u.username, u.full_name FROM customers c JOIN users u ON u.user_id = c.customer_id";
    private static final String UPDATE =
            "UPDATE customers SET email = ?, phone = ?, address = ?, city = ?, state = ?, pincode = ?, occupation = ? "
            + "WHERE customer_id = ?";

    private final DBConnection db = DBConnection.getInstance();

    /** Insert inside an existing transaction. customerId is the id of the users row. */
    public void insert(Connection con, int customerId, Customer c) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setInt(1, customerId);
            ps.setString(2, c.getFatherName());
            ps.setDate(3, c.getDateOfBirth() == null ? null : Date.valueOf(c.getDateOfBirth()));
            ps.setString(4, c.getGender());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getPhone());
            ps.setString(7, c.getAddress());
            ps.setString(8, c.getCity());
            ps.setString(9, c.getState());
            ps.setString(10, c.getPincode());
            ps.setString(11, c.getOccupation());
            ps.executeUpdate();
        }
    }

    @Override
    public void add(Customer c) throws DatabaseException {
        try (Connection con = db.getConnection()) {
            insert(con, c.getUserId(), c);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findById(Integer customerId) throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE c.customer_id = ?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load customer: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> findAll() throws DatabaseException {
        List<Customer> list = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY u.full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load customers: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Customer c) throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, c.getEmail());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getCity());
            ps.setString(5, c.getState());
            ps.setString(6, c.getPincode());
            ps.setString(7, c.getOccupation());
            ps.setInt(8, c.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update profile: " + e.getMessage(), e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer(rs.getInt("customer_id"), rs.getString("username"), rs.getString("full_name"));
        c.setFatherName(rs.getString("father_name"));
        Date dob = rs.getDate("dob");
        c.setDateOfBirth(dob == null ? null : dob.toLocalDate());
        c.setGender(rs.getString("gender"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setState(rs.getString("state"));
        c.setPincode(rs.getString("pincode"));
        c.setOccupation(rs.getString("occupation"));
        return c;
    }
}
