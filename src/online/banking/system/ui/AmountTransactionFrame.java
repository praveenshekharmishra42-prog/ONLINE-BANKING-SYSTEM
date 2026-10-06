package online.banking.system.ui;

import online.banking.system.account.Account;
import online.banking.system.dao.AccountDAO;
import online.banking.system.exception.BankException;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import online.banking.system.util.Validator;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * ABSTRACT screen shared by Deposit and Withdraw (TEMPLATE METHOD pattern).
 *
 * This class builds the whole form and handles the button click.
 * The child classes only fill in the parts that differ:
 *   getActionName()  - "Deposit" / "Withdraw"
 *   perform(amount)  - which BankingService method to call
 *   getButtonColor() - green / orange
 */
public abstract class AmountTransactionFrame extends BaseFrame implements ActionListener {

    private static final String[] QUICK_AMOUNTS = {"500", "1000", "2000", "5000", "10000"};

    protected final String accountNo;
    private final Runnable onSuccess;
    private final JTextField amountField;
    private final JLabel balanceValue, ruleValue;
    private final JButton submit, cancel;

    protected AmountTransactionFrame(String title, String accountNo, Runnable onSuccess) {
        super(title, "Account " + accountNo, 560, 450);
        this.accountNo = accountNo;
        this.onSuccess = onSuccess;

        JPanel card = UITheme.card();
        card.setBounds(35, 20, 475, 110);
        content.add(card);
        addLabel(card, "Current Balance", 20, 12, 200, 22).setForeground(UITheme.MUTED);
        balanceValue = addValue(card, "Loading...", 20, 35, 300, 34);
        balanceValue.setFont(UITheme.font(Font.BOLD, 22));
        balanceValue.setForeground(UITheme.PRIMARY);
        ruleValue = addLabel(card, "", 20, 75, 440, 22);
        ruleValue.setFont(UITheme.font(Font.ITALIC, 12));
        ruleValue.setForeground(UITheme.MUTED);

        addValue(content, "Enter Amount (Rs.)", 35, 145, 200, 30);
        amountField = addTextField(210, 145, 300, 34);
        amountField.setFont(UITheme.font(Font.BOLD, 16));

        addLabel("Quick select:", 35, 190, 120, 28).setForeground(UITheme.MUTED);
        for (int i = 0; i < QUICK_AMOUNTS.length; i++) {
            String value = QUICK_AMOUNTS[i];
            JButton quick = UITheme.button(value, new Color(120, 144, 156));
            quick.setFont(UITheme.font(Font.BOLD, 12));
            quick.setMargin(new Insets(0, 0, 0, 0));
            quick.setBounds(140 + i * 75, 190, 70, 28);
            quick.addActionListener(e -> amountField.setText(value));   // lambda = short ActionListener
            content.add(quick);
        }

        submit = addButton(UITheme.button(getActionName(), getButtonColor()), 210, 250, 145, 40);
        cancel = addButton(UITheme.greyButton("Cancel"), 365, 250, 145, 40);
        submit.addActionListener(this);
        cancel.addActionListener(this);
        getRootPane().setDefaultButton(submit);

        loadBalance();
    }

    // ---------- the parts each child class must provide ----------

    protected abstract String getActionName();

    protected abstract Color getButtonColor();

    /** Performs the operation and returns the new balance. */
    protected abstract BigDecimal perform(BigDecimal amount) throws BankException;

    /** Hint under the balance. Children may OVERRIDE it (DepositFrame does). */
    protected String describeRule(Account a) {
        return "You can withdraw up to " + MoneyUtil.format(a.getWithdrawableAmount())
                + " (" + a.getType() + " account rules)";
    }

    // ---------- shared behaviour ----------

    private void loadBalance() {
        BackgroundTask.run(this,
                () -> new AccountDAO().findById(accountNo).orElseThrow(() -> new BankException("Account not found.")),
                (Account a) -> {
                    balanceValue.setText(MoneyUtil.format(a.getBalance()));
                    ruleValue.setText(describeRule(a));
                });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel) {
            dispose();
            return;
        }
        try {
            BigDecimal amount = Validator.amount(amountField.getText());
            if (!MessageUtil.confirm(this, getActionName() + " " + MoneyUtil.format(amount) + "?")) {
                return;
            }
            submit.setEnabled(false);
            BackgroundTask.run(this, () -> perform(amount), newBalance -> {
                MessageUtil.info(this, getActionName() + " of " + MoneyUtil.format(amount) + " successful.\n\n"
                        + "New balance: " + MoneyUtil.format(newBalance));
                onSuccess.run();
                dispose();
            }, () -> submit.setEnabled(true));
        } catch (BankException ex) {
            MessageUtil.error(this, ex.getMessage());
        }
    }
}
