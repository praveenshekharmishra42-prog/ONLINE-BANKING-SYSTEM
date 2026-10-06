package online.banking.system.ui;

import online.banking.system.account.Account;
import online.banking.system.dao.AccountDAO;
import online.banking.system.exception.BankException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.service.BankingService;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import online.banking.system.util.Validator;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Send money to another account in the bank. */
public class TransferFrame extends BaseFrame implements ActionListener {

    private final String fromAccount;
    private final Runnable onSuccess;
    private final JTextField toAccount, amount, remarks;
    private final JLabel balanceValue, limitValue, beneficiaryName;
    private final JButton verify, send, cancel;
    private String verifiedAccount;          // account number whose name was looked up

    public TransferFrame(String fromAccount, Runnable onSuccess) {
        super("Fund Transfer", "From account " + fromAccount, 600, 520);
        this.fromAccount = fromAccount;
        this.onSuccess = onSuccess;

        JPanel card = UITheme.card();
        card.setBounds(35, 15, 515, 95);
        content.add(card);
        addLabel(card, "Available Balance", 20, 8, 200, 22).setForeground(UITheme.MUTED);
        balanceValue = addValue(card, "Loading...", 20, 30, 450, 32);
        balanceValue.setFont(UITheme.font(Font.BOLD, 20));
        balanceValue.setForeground(UITheme.PRIMARY);
        limitValue = addLabel(card, "", 20, 64, 470, 22);
        limitValue.setFont(UITheme.font(Font.ITALIC, 12));
        limitValue.setForeground(UITheme.MUTED);

        addValue(content, "Beneficiary A/c No.", 35, 120, 170, 30);
        toAccount = addTextField(210, 120, 220, 32);
        verify = addButton(UITheme.primaryButton("Verify"), 440, 120, 110, 32);

        addLabel("Beneficiary Name", 35, 162, 170, 28).setForeground(UITheme.MUTED);
        beneficiaryName = addValue(content, "-", 210, 162, 340, 28);

        addValue(content, "Amount (Rs.)", 35, 205, 170, 30);
        amount = addTextField(210, 205, 340, 32);
        amount.setFont(UITheme.font(Font.BOLD, 16));

        addValue(content, "Remarks (optional)", 35, 250, 170, 30);
        remarks = addTextField(210, 250, 340, 32);

        JLabel note = addLabel("Money is debited and credited in a single database transaction.", 35, 295, 520, 22);
        note.setFont(UITheme.font(Font.ITALIC, 12));
        note.setForeground(UITheme.MUTED);

        send = addButton(UITheme.primaryButton("Transfer Now"), 250, 335, 150, 40);
        cancel = addButton(UITheme.greyButton("Cancel"), 410, 335, 140, 40);

        verify.addActionListener(this);
        send.addActionListener(this);
        cancel.addActionListener(this);
        toAccount.addFocusListener(new FocusAdapter() {       // look up the name when the field loses focus
            @Override
            public void focusLost(FocusEvent e) {
                lookupBeneficiary();
            }
        });

        BackgroundTask.run(this,
                () -> new AccountDAO().findById(fromAccount).orElseThrow(() -> new BankException("Account not found.")),
                (Account a) -> {
                    balanceValue.setText(MoneyUtil.format(a.getBalance()));
                    limitValue.setText("You can send up to " + MoneyUtil.format(a.getWithdrawableAmount())
                            + " (" + a.getType() + " account rules)");
                });
    }

    private void lookupBeneficiary() {
        String acc = toAccount.getText().trim();
        if (acc.isEmpty() || acc.equals(verifiedAccount)) {
            return;
        }
        beneficiaryName.setText("Checking...");
        beneficiaryName.setForeground(UITheme.MUTED);
        BackgroundTask.run(this, () -> new AccountDAO().findHolderName(acc), name -> {
            if (name.isPresent()) {
                verifiedAccount = acc;
                beneficiaryName.setText(name.get());
                beneficiaryName.setForeground(UITheme.SUCCESS);
            } else {
                verifiedAccount = null;
                beneficiaryName.setText("No account found with this number");
                beneficiaryName.setForeground(UITheme.DANGER);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel) {
            dispose();
            return;
        }
        if (e.getSource() == verify) {
            verifiedAccount = null;
            lookupBeneficiary();
            return;
        }
        try {
            String to = Validator.requireText("Beneficiary account number", toAccount.getText());
            if (!to.matches("\\d{12}")) {
                throw new InvalidInputException("Account number must be exactly 12 digits.");
            }
            if (!to.equals(verifiedAccount)) {
                throw new InvalidInputException("Please click Verify and check the beneficiary name first.");
            }
            BigDecimal value = Validator.amount(amount.getText());
            String note = remarks.getText().trim();
            if (note.length() > 50) {
                throw new InvalidInputException("Remarks can be at most 50 characters.");
            }
            if (!MessageUtil.confirm(this, "Transfer " + MoneyUtil.format(value) + "\nto " + beneficiaryName.getText()
                    + " (A/c " + to + ")?")) {
                return;
            }
            send.setEnabled(false);
            BackgroundTask.run(this, () -> new BankingService().transfer(fromAccount, to, value, note), newBalance -> {
                MessageUtil.info(this, "Transfer successful!\n\n" + MoneyUtil.format(value) + " sent to "
                        + beneficiaryName.getText() + ".\nYour new balance: " + MoneyUtil.format(newBalance));
                onSuccess.run();
                dispose();
            }, () -> send.setEnabled(true));
        } catch (BankException ex) {
            MessageUtil.error(this, ex.getMessage());
        }
    }
}
