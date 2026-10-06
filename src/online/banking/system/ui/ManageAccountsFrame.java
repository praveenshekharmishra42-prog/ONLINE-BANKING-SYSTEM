package online.banking.system.ui;

import online.banking.system.dao.AccountDAO;
import online.banking.system.dao.CustomerDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.AccountStatus;
import online.banking.system.model.AccountSummary;
import online.banking.system.model.Customer;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import online.banking.system.util.TableModelBuilder;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/** Admin screen: approve new accounts, block / unblock accounts, view customer details. */
public class ManageAccountsFrame extends BaseFrame {

    private static final String[] COLUMNS = {"Account No", "Customer Name", "Type", "Balance (Rs.)", "Status"};
    private static final String ALL = "All";

    private final Runnable onChange;
    private final JTable table = new JTable();
    private final JComboBox<String> statusFilter;
    private final JTextField search;
    private final JLabel countLabel;
    private List<AccountSummary> loaded = new ArrayList<>();
    private List<AccountSummary> shown = new ArrayList<>();

    public ManageAccountsFrame(Runnable onChange) {
        super("Manage Accounts", "Approve, block or unblock customer accounts", 1000, 640);
        this.onChange = onChange;

        addLabel("Status", 20, 15, 55, 30);
        statusFilter = new JComboBox<>();
        statusFilter.addItem(ALL);
        for (AccountStatus s : AccountStatus.values()) {
            statusFilter.addItem(s.name());
        }
        statusFilter.setFont(UITheme.font(Font.PLAIN, 14));
        statusFilter.setBounds(75, 15, 140, 30);
        content.add(statusFilter);

        addLabel("Search", 235, 15, 60, 30);
        search = addTextField(295, 15, 260, 30);
        search.setToolTipText("Account number or customer name");
        JButton find = addButton(UITheme.primaryButton("Search"), 565, 15, 100, 30);
        JButton reload = addButton(UITheme.accentButton("Reload"), 675, 15, 100, 30);
        countLabel = addLabel("", 790, 15, 190, 30);
        countLabel.setForeground(UITheme.MUTED);

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 60, 945, 400);
        content.add(scroll);

        JButton approve = addButton(UITheme.button("Approve", UITheme.SUCCESS), 20, 475, 140, 40);
        JButton block = addButton(UITheme.dangerButton("Block"), 170, 475, 140, 40);
        JButton unblock = addButton(UITheme.button("Unblock", UITheme.WARNING), 320, 475, 140, 40);
        JButton details = addButton(UITheme.primaryButton("Customer Details"), 470, 475, 180, 40);
        JButton close = addButton(UITheme.greyButton("Close"), 825, 475, 140, 40);

        statusFilter.addActionListener(e -> showFiltered());
        find.addActionListener(e -> showFiltered());
        search.addActionListener(e -> showFiltered());
        reload.addActionListener(e -> load());
        approve.addActionListener(e -> changeStatus(AccountStatus.ACTIVE, "approve", AccountStatus.PENDING));
        block.addActionListener(e -> changeStatus(AccountStatus.BLOCKED, "block", AccountStatus.ACTIVE));
        unblock.addActionListener(e -> changeStatus(AccountStatus.ACTIVE, "unblock", AccountStatus.BLOCKED));
        details.addActionListener(e -> showDetails());
        close.addActionListener(e -> dispose());

        load();
    }

    private void load() {
        BackgroundTask.run(this, () -> new AccountDAO().findAllSummaries(), list -> {
            loaded = list;
            showFiltered();
        });
    }

    private void showFiltered() {
        String status = (String) statusFilter.getSelectedItem();
        String text = search.getText().trim().toLowerCase();
        shown = loaded.stream()
                .filter(a -> ALL.equals(status) || a.getStatus().name().equals(status))
                .filter(a -> text.isEmpty() || a.getAccountNo().contains(text)
                        || a.getCustomerName().toLowerCase().contains(text))
                .collect(Collectors.toList());
        table.setModel(TableModelBuilder.build(COLUMNS, shown, (AccountSummary a) -> new Object[] {
                a.getAccountNo(), a.getCustomerName(), a.getType(), a.getBalance(), a.getStatus()
        }));
        long pending = loaded.stream().filter(a -> a.getStatus() == AccountStatus.PENDING).count();
        countLabel.setText(shown.size() + " shown, " + pending + " pending");
    }

    /** The selected row, or null. convertRowIndexToModel() handles a sorted table. */
    private AccountSummary selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageUtil.error(this, "Please select an account in the table first.");
            return null;
        }
        return shown.get(table.convertRowIndexToModel(row));
    }

    private void changeStatus(AccountStatus newStatus, String verb, AccountStatus requiredCurrent) {
        AccountSummary a = selected();
        if (a == null) {
            return;
        }
        if (a.getStatus() != requiredCurrent) {
            MessageUtil.error(this, "Only " + requiredCurrent + " accounts can be " + verb + (verb.endsWith("e") ? "d" : "ed")
                    + ".\nThis account is " + a.getStatus() + ".");
            return;
        }
        if (!MessageUtil.confirm(this, "Do you want to " + verb + " account " + a.getAccountNo()
                + " (" + a.getCustomerName() + ")?")) {
            return;
        }
        BackgroundTask.run(this, () -> new AccountDAO().updateStatus(a.getAccountNo(), newStatus), ok -> {
            MessageUtil.info(this, "Account " + a.getAccountNo() + " is now " + newStatus + ".");
            load();
            onChange.run();
        });
    }

    private void showDetails() {
        AccountSummary a = selected();
        if (a == null) {
            return;
        }
        BackgroundTask.run(this, () -> {
            int customerId = new AccountDAO().findById(a.getAccountNo())
                    .orElseThrow(() -> new BankException("Account not found.")).getCustomerId();
            return new CustomerDAO().findById(customerId).orElseThrow(() -> new BankException("Customer not found."));
        }, (Customer c) -> MessageUtil.info(this,
                "Account No   : " + a.getAccountNo() + "  (" + a.getType() + ", " + a.getStatus() + ")\n"
                + "Balance      : " + MoneyUtil.format(a.getBalance()) + "\n\n"
                + "Name         : " + c.getName() + "\n"
                + "Username     : " + c.getUsername() + "\n"
                + "Father       : " + c.getFatherName() + "\n"
                + "Date of Birth: " + (c.getDateOfBirth() == null ? "-" : c.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))) + "\n"
                + "Gender       : " + c.getGender() + "\n"
                + "Occupation   : " + c.getOccupation() + "\n"
                + "Email        : " + c.getEmail() + "\n"
                + "Mobile       : " + c.getPhone() + "\n"
                + "Address      : " + c.getAddress() + ", " + c.getCity() + ", " + c.getState() + " - " + c.getPincode()));
    }
}
