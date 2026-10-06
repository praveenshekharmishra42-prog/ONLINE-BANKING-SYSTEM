package online.banking.system.dao;

import online.banking.system.account.Account;
import online.banking.system.account.AccountFactory;
import online.banking.system.db.DBConnection;
import online.banking.system.exception.DatabaseException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountSummary;
import online.banking.system.model.AccountType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Bank accounts (accounts table). Rows are turned into SavingsAccount / CurrentAccount objects. */
public class AccountDAO implements GenericDAO<Account, String> {

    private static final String INSERT =
            "INSERT INTO accounts (account_no, customer_id, account_type, balance, status) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT = "SELECT * FROM accounts";
    private static final String SELECT_FOR_UPDATE = "SELECT * FROM accounts WHERE account_no = ? FOR UPDATE";
    private static final String UPDATE_BALANCE = "UPDATE accounts SET balance = ? WHERE account_no = ?";
    private static final String UPDATE_STATUS = "UPDATE accounts SET status = ? WHERE account_no = ?";
    private static final String SUMMARY =
            "SELECT a.account_no, u.full_name, a.account_type, a.balance, a.status "
            + "FROM accounts a JOIN users u ON u.user_id = a.customer_id ORDER BY a.opened_on DESC";
    private static final String HOLDER_NAME =
            "SELECT u.full_name FROM accounts a JOIN users u ON u.user_id = a.customer_id WHERE a.account_no = ?";

    private final DBConnection db = DBConnection.getInstance();

    public void insert(Connection con, Account a) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setString(1, a.getAccountNo());
            ps.setInt(2, a.getCustomerId());
            ps.setString(3, a.getType().name());
            ps.setBigDecimal(4, a.getBalance());
            ps.setString(5, a.getStatus().name());
            ps.executeUpdate();
        }
    }

    @Override
    public void add(Account a) throws DatabaseException {
        try (Connection con = db.getConnection()) {
            insert(con, a);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save account: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findById(String accountNo) throws DatabaseException {
        List<Account> list = query(SELECT + " WHERE account_no = ?", accountNo);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Account> findByCustomerId(int customerId) throws DatabaseException {
        List<Account> list = query(SELECT + " WHERE customer_id = ?", customerId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Account> findAll() throws DatabaseException {
        return query(SELECT + " ORDER BY account_no", null);
    }

    /**
     * Reads an account AND locks its row until the transaction ends
     * (SELECT ... FOR UPDATE). Another transaction that wants the same row
     * must wait, so two withdrawals can never both read the old balance.
     */
    public Optional<Account> findByIdForUpdate(Connection con, String accountNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_FOR_UPDATE)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public void updateBalance(Connection con, String accountNo, BigDecimal balance) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(UPDATE_BALANCE)) {
            ps.setBigDecimal(1, balance);
            ps.setString(2, accountNo);
            ps.executeUpdate();
        }
    }

    public boolean updateStatus(String accountNo, AccountStatus status) throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setString(2, accountNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not change account status: " + e.getMessage(), e);
        }
    }

    /** Account number + holder name for the admin table (JOIN of two tables). */
    public List<AccountSummary> findAllSummaries() throws DatabaseException {
        List<AccountSummary> list = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(SUMMARY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AccountSummary(
                        rs.getString("account_no"),
                        rs.getString("full_name"),
                        AccountType.valueOf(rs.getString("account_type")),
                        rs.getBigDecimal("balance"),
                        AccountStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load accounts: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<String> findHolderName(String accountNo) throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(HOLDER_NAME)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not look up account: " + e.getMessage(), e);
        }
    }

    /** Every account number as a HashSet - fast contains() when generating new numbers. */
    public Set<String> getAllAccountNumbers() throws DatabaseException {
        Set<String> numbers = new HashSet<>();
        for (Account a : findAll()) {
            numbers.add(a.getAccountNo());
        }
        return numbers;
    }

    private List<Account> query(String sql, Object param) throws DatabaseException {
        List<Account> list = new ArrayList<>();
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load account: " + e.getMessage(), e);
        }
        return list;
    }

    /** The factory decides whether this row becomes a SavingsAccount or a CurrentAccount. */
    private Account mapRow(ResultSet rs) throws SQLException {
        Timestamp opened = rs.getTimestamp("opened_on");
        return AccountFactory.create(
                AccountType.valueOf(rs.getString("account_type")),
                rs.getString("account_no"),
                rs.getInt("customer_id"),
                rs.getBigDecimal("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                opened == null ? null : opened.toLocalDateTime());
    }
}
