package online.banking.system.ui;

import online.banking.system.account.Account;
import online.banking.system.exception.BankException;
import online.banking.system.service.BankingService;
import java.awt.Color;
import java.math.BigDecimal;

/** Deposit screen - only the parts that differ from AmountTransactionFrame. */
public class DepositFrame extends AmountTransactionFrame {

    public DepositFrame(String accountNo, Runnable onSuccess) {
        super("Deposit Money", accountNo, onSuccess);
    }

    @Override
    protected String getActionName() {
        return "Deposit";
    }

    @Override
    protected Color getButtonColor() {
        return UITheme.SUCCESS;
    }

    @Override
    protected String describeRule(Account a) {
        return a.getType() + " account | deposits are credited instantly (max Rs. 10,00,000 per transaction)";
    }

    @Override
    protected BigDecimal perform(BigDecimal amount) throws BankException {
        return new BankingService().deposit(accountNo, amount);
    }
}
