package online.banking.system.dao;

import online.banking.system.db.DBConnection;
import online.banking.system.exception.DatabaseException;
import online.banking.system.model.DashboardStats;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Aggregate numbers (COUNT / SUM) for the admin dashboard. */
public class ReportDAO {

    private static final String STATS =
            "SELECT "
            + "(SELECT COUNT(*) FROM customers) AS customers, "
            + "(SELECT COUNT(*) FROM accounts WHERE status = 'PENDING') AS pending, "
            + "(SELECT COUNT(*) FROM accounts WHERE status = 'BLOCKED') AS blocked, "
            + "(SELECT COALESCE(SUM(balance), 0) FROM accounts) AS deposits, "
            + "(SELECT COUNT(*) FROM transactions WHERE DATE(txn_time) = CURRENT_DATE) AS today";

    private final DBConnection db = DBConnection.getInstance();

    public DashboardStats getDashboardStats() throws DatabaseException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(STATS);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            BigDecimal deposits = rs.getBigDecimal("deposits");
            return new DashboardStats(
                    rs.getInt("customers"),
                    rs.getInt("pending"),
                    rs.getInt("blocked"),
                    deposits == null ? BigDecimal.ZERO : deposits,
                    rs.getInt("today"));
        } catch (SQLException e) {
            throw new DatabaseException("Could not load dashboard: " + e.getMessage(), e);
        }
    }
}
