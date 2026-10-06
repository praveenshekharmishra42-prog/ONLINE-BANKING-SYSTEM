package online.banking.system.service;

import online.banking.system.account.Account;
import online.banking.system.account.AccountFactory;
import online.banking.system.dao.AccountDAO;
import online.banking.system.dao.CustomerDAO;
import online.banking.system.dao.TransactionDAO;
import online.banking.system.dao.UserDAO;
import online.banking.system.db.DBConnection;
import online.banking.system.exception.BankException;
import online.banking.system.exception.DatabaseException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountType;
import online.banking.system.model.Customer;
import online.banking.system.model.Role;
import online.banking.system.model.Transaction;
import online.banking.system.model.TransactionType;
import online.banking.system.util.MoneyUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Opens a new bank account. Four tables are written:
 *   users -> customers -> accounts -> transactions (initial deposit)
 * All four happen in ONE database transaction: if any step fails, rollback()
 * undoes the others, so there is never a user without an account.
 */
public class RegistrationService {

    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /** @return the new account number */
    public String openAccount(Customer details, String username, String password,
                              AccountType type, BigDecimal initialDeposit) throws BankException {

        BigDecimal minimum = AccountFactory.minimumOpeningDeposit(type);
        if (initialDeposit.compareTo(minimum) < 0) {
            throw new InvalidInputException("A " + type + " account needs an opening deposit of at least "
                    + MoneyUtil.format(minimum) + ".");
        }
        if (userDAO.usernameExists(username)) {
            throw new InvalidInputException("Username '" + username + "' is already taken. Please choose another.");
        }

        String accountNo = AccountNumberGenerator.generate();
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);                                     // BEGIN

            int userId = userDAO.insert(con, username, password, Role.CUSTOMER, details.getName());
            customerDAO.insert(con, userId, details);

            Account account = AccountFactory.create(type, accountNo, userId,
                    initialDeposit, AccountStatus.PENDING, null);
            accountDAO.insert(con, account);

            transactionDAO.insert(con, new Transaction(accountNo, TransactionType.DEPOSIT,
                    initialDeposit, initialDeposit, "Initial deposit - account opening"));

            con.commit();                                                 // COMMIT
            return accountNo;
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);                            // ROLLBACK
            throw new DatabaseException("Could not open the account: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(con);
        }
    }
}
