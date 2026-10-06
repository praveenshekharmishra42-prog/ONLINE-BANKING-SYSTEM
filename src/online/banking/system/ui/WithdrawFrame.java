package online.banking.system.ui;

import online.banking.system.exception.BankException;
import online.banking.system.service.BankingService;
import java.awt.Color;
import java.math.BigDecimal;

/** Withdraw screen - only the parts that differ from AmountTransactionFrame. */
public class WithdrawFrame extends AmountTransactionFrame {

    public WithdrawFrame(String accountNo, Runnable onSuccess) {
        super("Withdraw Money", accountNo, onSuccess);
    }

    @Override
    protected String getActionName() {
        return "Withdraw";
    }

    @Override
    protected Color getButtonColor() {
        return UITheme.WARNING;
    }

    @Override
    protected BigDecimal perform(BigDecimal amount) throws BankException {
        return new BankingService().withdraw(accountNo, amount);
    }
}
