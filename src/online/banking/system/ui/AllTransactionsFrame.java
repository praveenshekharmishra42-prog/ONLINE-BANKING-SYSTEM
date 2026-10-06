package online.banking.system.ui;

import online.banking.system.dao.TransactionDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.Transaction;
import java.util.List;

/**
 * Admin view of every transaction in the bank.
 * It INHERITS the whole screen from TransactionHistoryFrame and only
 * OVERRIDES fetch() - a good example of reuse through inheritance.
 */
public class AllTransactionsFrame extends TransactionHistoryFrame {

    public AllTransactionsFrame() {
        super("All Transactions", "Every transaction across all accounts", null);
    }

    @Override
    protected List<Transaction> fetch() throws BankException {
        return new TransactionDAO().findAll();
    }
}
