package online.banking.system.ui;

import online.banking.system.dao.ReportDAO;
import online.banking.system.model.Admin;
import online.banking.system.model.DashboardStats;
import online.banking.system.service.BankingService;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Home screen for the bank admin: live statistics and management tools. */
public class AdminDashboard extends BaseFrame implements ActionListener {

    private final Admin admin;
    private final JLabel customersValue, pendingValue, depositsValue, todayValue;
    private final JButton manageAccounts, allTransactions, applyInterest, changePassword, refresh, logout;
    private final ClockLabel clock = new ClockLabel();

    public AdminDashboard(Admin admin) {
        super("Admin Dashboard", admin.getWelcomeMessage(), 900, 560);
        this.admin = admin;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        clock.setBounds(600, 8, 280, 22);
        content.add(clock);

        customersValue = statCard("Total Customers", 30, UITheme.PRIMARY_LIGHT);
        pendingValue = statCard("Pending Approvals", 240, UITheme.WARNING);
        depositsValue = statCard("Total Deposits", 450, UITheme.SUCCESS);
        todayValue = statCard("Today's Transactions", 660, UITheme.ACCENT);

        manageAccounts = UITheme.primaryButton("Manage Accounts");
        allTransactions = UITheme.accentButton("All Transactions");
        applyInterest = UITheme.button("Apply Monthly Interest", UITheme.SUCCESS);
        changePassword = UITheme.primaryButton("Change Password");
        refresh = UITheme.greyButton("Refresh Stats");
        logout = UITheme.dangerButton("Logout");

        JButton[] all = {manageAccounts, allTransactions, applyInterest, changePassword, refresh, logout};
        for (int i = 0; i < all.length; i++) {
            addButton(all[i], 30 + (i % 3) * 280, 190 + (i / 3) * 85, 265, 65);
            all[i].setFont(UITheme.font(Font.BOLD, 15));
            all[i].addActionListener(this);
        }

        JLabel info = addLabel("Savings accounts earn 4% p.a. (credited monthly, once per month). "
                + "Current accounts have a Rs. 10,000 overdraft.", 30, 380, 840, 25);
        info.setFont(UITheme.font(Font.ITALIC, 12));
        info.setForeground(UITheme.MUTED);

        loadStats();
    }

    private JLabel statCard(String title, int x, Color color) {
        JPanel card = UITheme.card();
        card.setBounds(x, 40, 195, 120);
        content.add(card);
        JPanel stripe = new JPanel();
        stripe.setBackground(color);
        stripe.setBounds(0, 0, 195, 6);
        card.add(stripe);
        JLabel t = addLabel(card, title, 15, 18, 175, 22);
        t.setForeground(UITheme.MUTED);
        JLabel value = addValue(card, "...", 15, 50, 175, 40);
        value.setFont(UITheme.font(Font.BOLD, 24));
        value.setForeground(color);
        return value;
    }

    /** Statistics are loaded on a background thread (SwingWorker). */
    public void loadStats() {
        BackgroundTask.run(this, () -> new ReportDAO().getDashboardStats(), (DashboardStats s) -> {
            customersValue.setText(String.valueOf(s.getTotalCustomers()));
            pendingValue.setText(String.valueOf(s.getPendingAccounts()));
            depositsValue.setText(MoneyUtil.format(s.getTotalDeposits()));
            depositsValue.setFont(UITheme.font(Font.BOLD, s.getTotalDeposits().compareTo(new BigDecimal("10000000")) >= 0 ? 16 : 19));
            todayValue.setText(String.valueOf(s.getTodaysTransactions()));
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == manageAccounts) {
            new ManageAccountsFrame(this::loadStats).setVisible(true);
        } else if (src == allTransactions) {
            new AllTransactionsFrame().setVisible(true);
        } else if (src == applyInterest) {
            applyInterest();
        } else if (src == changePassword) {
            new ChangePasswordFrame(admin).setVisible(true);
        } else if (src == refresh) {
            loadStats();
        } else if (src == logout) {
            logout();
        }
    }

    private void applyInterest() {
        if (!MessageUtil.confirm(this, "Credit this month's interest to all ACTIVE savings accounts?\n"
                + "(Accounts that already received interest this month are skipped.)")) {
            return;
        }
        applyInterest.setEnabled(false);
        applyInterest.setText("Processing...");
        BackgroundTask.run(this, () -> new BankingService().applyMonthlyInterest(), (Map<String, BigDecimal> result) -> {
            if (result.isEmpty()) {
                MessageUtil.info(this, "No interest was credited.\nEither there are no active savings accounts, "
                        + "or interest has already been paid this month.");
            } else {
                BigDecimal total = BigDecimal.ZERO;
                StringBuilder sb = new StringBuilder();
                int shown = 0;
                for (Map.Entry<String, BigDecimal> entry : result.entrySet()) {
                    total = total.add(entry.getValue());
                    if (shown++ < 10) {
                        sb.append("A/c ").append(entry.getKey()).append("  :  ").append(MoneyUtil.format(entry.getValue())).append("\n");
                    }
                }
                if (result.size() > 10) {
                    sb.append("... and ").append(result.size() - 10).append(" more\n");
                }
                MessageUtil.info(this, "Interest credited to " + result.size() + " account(s).\nTotal: "
                        + MoneyUtil.format(total) + "\n\n" + sb);
            }
            loadStats();
        }, () -> {
            applyInterest.setEnabled(true);
            applyInterest.setText("Apply Monthly Interest");
        });
    }

    private void logout() {
        if (MessageUtil.confirm(this, "Do you want to logout?")) {
            clock.stop();
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != this && w.isDisplayable() && !(w instanceof LoginFrame)) {
                    w.dispose();
                }
            }
            dispose();
            LoginFrame.open();
        }
    }
}
