package online.banking.system.service;

import online.banking.system.account.Account;
import online.banking.system.account.InterestBearing;
import online.banking.system.dao.AccountDAO;
import online.banking.system.dao.TransactionDAO;
import online.banking.system.db.DBConnection;
import online.banking.system.exception.AccountNotFoundException;
import online.banking.system.exception.BankException;
import online.banking.system.exception.DatabaseException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.Transaction;
import online.banking.system.model.TransactionType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * All money movement: deposit, withdraw, transfer and monthly interest.
 *
 * Two levels of protection make every operation safe:
 *
 * 1. JAVA SYNCHRONIZATION - synchronized (AccountLockManager.lockFor(accountNo))
 *    Only one thread in this program can change a given account at a time.
 *
 * 2. DATABASE TRANSACTION - SELECT ... FOR UPDATE + commit/rollback
 *    The balance update and the history row are saved together or not at all,
 *    and other programs using the same database also have to wait.
 */
public class BankingService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ------------------------------------------------------------ deposit

    public BigDecimal deposit(String accountNo, BigDecimal amount) throws BankException {
        synchronized (AccountLockManager.lockFor(accountNo)) {
            return inTransaction(con -> {
                Account account = lock(con, accountNo);
                account.ensureActive();
                account.deposit(amount);
                accountDAO.updateBalance(con, accountNo, account.getBalance());
                transactionDAO.insert(con, new Transaction(accountNo, TransactionType.DEPOSIT,
                        amount, account.getBalance(), "Cash deposit"));
                return account.getBalance();
            });
        }
    }

    // ------------------------------------------------------------ withdraw

    public BigDecimal withdraw(String accountNo, BigDecimal amount) throws BankException {
        synchronized (AccountLockManager.lockFor(accountNo)) {
            return inTransaction(con -> {
                Account account = lock(con, accountNo);
                account.ensureActive();
                account.withdraw(amount);          // Savings/Current rules decide (polymorphism)
                accountDAO.updateBalance(con, accountNo, account.getBalance());
                transactionDAO.insert(con, new Transaction(accountNo, TransactionType.WITHDRAWAL,
                        amount, account.getBalance(), "Cash withdrawal"));
                return account.getBalance();
            });
        }
    }

    // ------------------------------------------------------------ transfer

    /**
     * Moves money between two accounts.
     *
     * DEADLOCK PREVENTION: if thread 1 transfers A->B while thread 2 transfers
     * B->A, and each locked "its own" account first, both would wait forever.
     * Here BOTH threads always lock the smaller account number first, so they
     * can never block each other in a circle.
     *
     * @return the sender's new balance
     */
    public BigDecimal transfer(String fromAccount, String toAccount, BigDecimal amount, String remarks)
            throws BankException {
        if (fromAccount.equals(toAccount)) {
            throw new InvalidInputException("You cannot transfer money to the same account.");
        }
        String first = fromAccount.compareTo(toAccount) < 0 ? fromAccount : toAccount;
        String second = first.equals(fromAccount) ? toAccount : fromAccount;
        String note = (remarks == null || remarks.isBlank()) ? "" : " - " + remarks.trim();

        synchronized (AccountLockManager.lockFor(first)) {
            synchronized (AccountLockManager.lockFor(second)) {
                return inTransaction(con -> {
                    Account a = lock(con, first);            // same order for DB row locks
                    Account b = lock(con, second);
                    Account sender = a.getAccountNo().equals(fromAccount) ? a : b;
                    Account receiver = (sender == a) ? b : a;

                    sender.ensureActive();
                    receiver.ensureActive();
                    sender.withdraw(amount);
                    receiver.deposit(amount);

                    accountDAO.updateBalance(con, sender.getAccountNo(), sender.getBalance());
                    accountDAO.updateBalance(con, receiver.getAccountNo(), receiver.getBalance());

                    transactionDAO.insert(con, new Transaction(fromAccount, TransactionType.TRANSFER_OUT,
                            amount, sender.getBalance(), "To A/c " + toAccount + note));
                    transactionDAO.insert(con, new Transaction(toAccount, TransactionType.TRANSFER_IN,
                            amount, receiver.getBalance(), "From A/c " + fromAccount + note));
                    return sender.getBalance();
                });
            }
        }
    }

    // ------------------------------------------------------------ interest

    /**
     * Credits one month's interest to every ACTIVE interest-bearing account.
     *
     * MULTITHREADING: the accounts are processed in parallel by a thread pool
     * (ExecutorService with 4 threads). Each task returns a Future; results
     * are collected in a ConcurrentHashMap because several threads write to it.
     *
     * @return account number -> interest credited, sorted by account number
     */
    public Map<String, BigDecimal> applyMonthlyInterest() throws BankException {
        List<Account> eligible = new ArrayList<>();
        for (Account a : accountDAO.findAll()) {
            // Only accounts that implement the InterestBearing interface earn interest
            if (a instanceof InterestBearing && a.getStatus() == AccountStatus.ACTIVE) {
                eligible.add(a);
            }
        }

        Map<String, BigDecimal> credited = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (Account a : eligible) {
                futures.add(pool.submit(() -> {
                    BigDecimal interest = creditInterest(a.getAccountNo());
                    if (interest.signum() > 0) {
                        credited.put(a.getAccountNo(), interest);
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) {
                f.get();                              // wait for every task; rethrows its error
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankException("Interest calculation was interrupted.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof BankException
                    ? (BankException) cause
                    : new BankException("Interest calculation failed: " + cause.getMessage(), cause);
        } finally {
            pool.shutdown();
        }
        return new TreeMap<>(credited);               // TreeMap = sorted by key
    }

    private BigDecimal creditInterest(String accountNo) throws BankException {
        synchronized (AccountLockManager.lockFor(accountNo)) {
            return inTransaction(con -> {
                Account account = lock(con, accountNo);
                if (!(account instanceof InterestBearing)
                        || account.getStatus() != AccountStatus.ACTIVE
                        || transactionDAO.interestPaidThisMonth(con, accountNo)) {
                    return BigDecimal.ZERO;
                }
                BigDecimal interest = ((InterestBearing) account).calculateMonthlyInterest();
                if (interest.signum() <= 0) {
                    return BigDecimal.ZERO;
                }
                account.deposit(interest);
                accountDAO.updateBalance(con, accountNo, account.getBalance());
                transactionDAO.insert(con, new Transaction(accountNo, TransactionType.INTEREST, interest,
                        account.getBalance(), "Monthly interest @ "
                        + ((InterestBearing) account).getAnnualInterestRate().toPlainString() + "% p.a."));
                return interest;
            });
        }
    }

    // ------------------------------------------------------------ helpers

    private Account lock(Connection con, String accountNo) throws SQLException, AccountNotFoundException {
        return accountDAO.findByIdForUpdate(con, accountNo)
                .orElseThrow(() -> new AccountNotFoundException(accountNo));
    }

    /**
     * Runs the work inside a database transaction.
     * <T> is the result type - a GENERIC METHOD used by every operation above.
     */
    private <T> T inTransaction(TransactionWork<T> work) throws BankException {
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);
            T result = work.execute(con);
            con.commit();
            return result;
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Transaction failed and was cancelled: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);       // e.g. InsufficientFundsException
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
    }
}
