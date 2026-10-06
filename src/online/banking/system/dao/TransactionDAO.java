package online.banking.system.dao;

import online.banking.system.db.DBConnection;
import online.banking.system.exception.DatabaseException;
import online.banking.system.model.Transaction;
import online.banking.system.model.TransactionType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Transaction history (transactions table).
 * update() is NOT overridden: a bank transaction must never be edited,
 * so the default method from GenericDAO (which throws) is used.
 */
public class TransactionDAO implements GenericDAO<Transaction, Long> {

    private static final String INSERT =
            "INSERT INTO transactions (account_no, txn_type, amount, balance_after, description) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT = "SELECT * FROM transactions";
    private static final String ORDER = " ORDER BY txn_time DESC, txn_id DESC";
    private static final String INTEREST_THIS_MONTH =
            "SELECT COUNT(*) FROM transactions WHERE account_no = ? AND txn_type = 'INTEREST' "
            + "AND YEAR(txn_time) = YEAR(CURRENT_DATE) AND MONTH(txn_time) = MONTH(CURRENT_DATE)";

    private final DBConnection db = DBConnection.getInstance();

    public void insert(Connection con, Transaction t) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setString(1, t.getAccountNo());
            ps.setString(2, t.getType().name());
            ps.setBigDecimal(3, t.getAmount());
            ps.setBigDecimal(4, t.getBalanceAfter());
            ps.setString(5, t.getDescription());
            ps.executeUpdate();
        }
    }

    @Override
    public void add(Transaction t) throws DatabaseException {
        try (Connection con = db.getConnection()) {
            insert(con, t);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) throws DatabaseException {
        List<Transaction> list = query(SELECT + " WHERE txn_id = ?", id, 0);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Transaction> findAll() throws DatabaseException {
        return query(SELECT + ORDER, null, 0);
    }

    public List<Transaction> findByAccount(String accountNo) throws DatabaseException {
        return query(SELECT + " WHERE account_no = ?" + ORDER, accountNo, 0);
    }

    /** The latest 'limit' transactions (for the mini statement). */
    public List<Transaction> findRecent(String accountNo, int limit) throws DatabaseException {
        return query(SELECT + " WHERE account_no = ?" + ORDER + " LIMIT ?", accountNo, limit);
    }

    /** Used inside the interest transaction so interest is paid only once per month. */
    public boolean interestPaidThisMonth(Connection con, String accountNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INTEREST_THIS_MONTH)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<Transaction> query(String sql, Object param, int limit) throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int index = 1;
            if (param != null) {
                ps.setObject(index++, param);
            }
            if (limit > 0) {
                ps.setInt(index, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load transactions: " + e.getMessage(), e);
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Timestamp time = rs.getTimestamp("txn_time");
        return new Transaction(
                rs.getLong("txn_id"),
                rs.getString("account_no"),
                TransactionType.valueOf(rs.getString("txn_type")),
                rs.getBigDecimal("amount"),
                rs.getBigDecimal("balance_after"),
                rs.getString("description"),
                time == null ? null : time.toLocalDateTime());
    }
}
