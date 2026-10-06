package online.banking.system.ui;

import online.banking.system.account.Account;
import online.banking.system.account.CurrentAccount;
import online.banking.system.account.InterestBearing;
import online.banking.system.dao.AccountDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.Customer;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Home screen for a logged-in customer: balance card + banking actions. */
public class CustomerDashboard extends BaseFrame implements ActionListener {

    private final Customer customer;
    private Account account;                              // loaded from the database

    private final JLabel accountNoValue, typeValue, statusValue, balanceValue, ruleValue, notice;
    private final JButton deposit, withdraw, transfer, statement, history, profile, changePassword, logout, refresh;
    private final List<JButton> moneyButtons = new ArrayList<>();
    private final ClockLabel clock = new ClockLabel();

    public CustomerDashboard(Customer customer) {
        super("My Banking Dashboard", customer.getWelcomeMessage(), 900, 600);
        this.customer = customer;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        clock.setBounds(600, 8, 280, 22);
        content.add(clock);

        // ---------------- account card ----------------
        JPanel card = UITheme.card();
        card.setBounds(30, 35, 830, 150);
        content.add(card);

        addLabel(card, "Account Holder", 25, 15, 150, 25).setForeground(UITheme.MUTED);
        addValue(card, customer.getName(), 25, 38, 300, 28).setFont(UITheme.font(Font.BOLD, 18));
        addLabel(card, "Account Number", 25, 78, 150, 25).setForeground(UITheme.MUTED);
        accountNoValue = addValue(card, "Loading...", 25, 100, 250, 28);

        addLabel(card, "Account Type", 300, 15, 150, 25).setForeground(UITheme.MUTED);
        typeValue = addValue(card, "-", 300, 38, 200, 28);
        addLabel(card, "Status", 300, 78, 150, 25).setForeground(UITheme.MUTED);
        statusValue = addValue(card, "-", 300, 100, 200, 28);

        addLabel(card, "Available Balance", 540, 15, 200, 25).setForeground(UITheme.MUTED);
        balanceValue = addValue(card, "-", 540, 38, 280, 40);
        balanceValue.setFont(UITheme.font(Font.BOLD, 26));
        balanceValue.setForeground(UITheme.PRIMARY);
        ruleValue = addLabel(card, "", 540, 85, 280, 22);
        ruleValue.setFont(UITheme.font(Font.ITALIC, 12));
        ruleValue.setForeground(UITheme.MUTED);
        refresh = UITheme.greyButton("Refresh");
        refresh.setBounds(540, 110, 100, 28);
        card.add(refresh);

        notice = addLabel("", 30, 190, 830, 25);
        notice.setFont(UITheme.font(Font.BOLD, 13));

        // ---------------- action buttons (2 rows x 4) ----------------
        deposit = moneyButton("Deposit Money", UITheme.SUCCESS);
        withdraw = moneyButton("Withdraw Money", UITheme.WARNING);
        transfer = moneyButton("Fund Transfer", UITheme.PRIMARY_LIGHT);
        statement = UITheme.accentButton("Mini Statement");
        history = UITheme.accentButton("Transaction History");
        profile = UITheme.primaryButton("My Profile");
        changePassword = UITheme.primaryButton("Change Password");
        logout = UITheme.dangerButton("Logout");

        JButton[] all = {deposit, withdraw, transfer, statement, history, profile, changePassword, logout};
        for (int i = 0; i < all.length; i++) {
            int col = i % 4, row = i / 4;
            addButton(all[i], 30 + col * 210, 225 + row * 90, 195, 70);
            all[i].setFont(UITheme.font(Font.BOLD, 14));
            all[i].addActionListener(this);
        }
        refresh.addActionListener(this);

        JLabel footer = addLabel("Tip: account numbers are 12 digits and start with 5021.", 30, 420, 600, 25);
        footer.setFont(UITheme.font(Font.ITALIC, 12));
        footer.setForeground(UITheme.MUTED);

        setMoneyButtonsEnabled(false);
        refreshAccount();
    }

    private JButton moneyButton(String text, Color color) {
        JButton b = UITheme.button(text, color);
        moneyButtons.add(b);
        return b;
    }

    private void setMoneyButtonsEnabled(boolean enabled) {
        for (JButton b : moneyButtons) {
            b.setEnabled(enabled);
        }
    }

    /** Reloads the account on a background thread (called after every transaction). */
    public void refreshAccount() {
        BackgroundTask.run(this,
                () -> new AccountDAO().findByCustomerId(customer.getUserId())
                        .orElseThrow(() -> new BankException("No bank account is linked to this login.")),
                this::showAccount);
    }

    private void showAccount(Account a) {
        this.account = a;
        accountNoValue.setText(a.getAccountNo());
        typeValue.setText(a.getType() + " Account");
        statusValue.setText(a.getStatus().name());
        statusValue.setForeground(a.getStatus() == AccountStatus.ACTIVE ? UITheme.SUCCESS
                : a.getStatus() == AccountStatus.PENDING ? UITheme.WARNING : UITheme.DANGER);
        balanceValue.setText(MoneyUtil.format(a.getBalance()));
        balanceValue.setForeground(a.getBalance().signum() < 0 ? UITheme.DANGER : UITheme.PRIMARY);

        // The text depends on which interface/subclass the object really is
        if (a instanceof InterestBearing) {
            ruleValue.setText("Earns " + ((InterestBearing) a).getAnnualInterestRate() + "% p.a. | Min balance "
                    + MoneyUtil.format(a.getMinimumBalance()));
        } else if (a instanceof CurrentAccount) {
            ruleValue.setText("Overdraft limit " + MoneyUtil.format(CurrentAccount.OVERDRAFT_LIMIT));
        }

        boolean active = a.getStatus() == AccountStatus.ACTIVE;
        setMoneyButtonsEnabled(active);
        if (a.getStatus() == AccountStatus.PENDING) {
            notice.setText("Your account is waiting for bank approval. Deposits, withdrawals and transfers will be enabled once approved.");
            notice.setForeground(UITheme.WARNING);
        } else if (a.getStatus() == AccountStatus.BLOCKED) {
            notice.setText("Your account has been blocked by the bank. Please contact your branch.");
            notice.setForeground(UITheme.DANGER);
        } else {
            notice.setText("");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == logout) {
            logout();
            return;
        }
        if (src == refresh) {
            refreshAccount();
            return;
        }
        if (src == profile) {
            new ProfileFrame(customer).setVisible(true);
            return;
        }
        if (src == changePassword) {
            new ChangePasswordFrame(customer).setVisible(true);
            return;
        }
        if (account == null) {
            MessageUtil.error(this, "Account details are still loading. Please wait a moment.");
            return;
        }
        String accNo = account.getAccountNo();
        if (src == deposit) {
            new DepositFrame(accNo, this::refreshAccount).setVisible(true);
        } else if (src == withdraw) {
            new WithdrawFrame(accNo, this::refreshAccount).setVisible(true);
        } else if (src == transfer) {
            new TransferFrame(accNo, this::refreshAccount).setVisible(true);
        } else if (src == statement) {
            new MiniStatementFrame(accNo, customer.getName()).setVisible(true);
        } else if (src == history) {
            new TransactionHistoryFrame(accNo).setVisible(true);
        }
    }

    private void logout() {
        if (MessageUtil.confirm(this, "Do you want to logout?")) {
            clock.stop();
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != this && w.isDisplayable() && !(w instanceof LoginFrame)) {
                    w.dispose();                     // close any open child windows
                }
            }
            dispose();
            LoginFrame.open();
        }
    }
}
