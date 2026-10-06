package online.banking.system.ui;

import online.banking.system.dao.TransactionDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.Transaction;
import online.banking.system.model.TransactionType;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import online.banking.system.util.TableModelBuilder;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Full transaction history with filter and summary.
 * AllTransactionsFrame (admin) EXTENDS this class and overrides fetch()
 * to show every account instead of one.
 */
public class TransactionHistoryFrame extends BaseFrame {

    protected static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private static final String[] COLUMNS = {"Txn ID", "Date & Time", "Account", "Type", "Amount (Rs.)", "Balance After", "Description"};
    private static final String ALL = "All Types";

    protected final String accountNo;
    private List<Transaction> loaded = new ArrayList<>();

    protected final JComboBox<String> typeFilter;
    protected final JTextField searchField;
    private final JTable table = new JTable();
    private final JLabel summary;

    public TransactionHistoryFrame(String accountNo) {
        this("Transaction History", "Account " + accountNo, accountNo);
    }

    protected TransactionHistoryFrame(String title, String subtitle, String accountNo) {
        super(title, subtitle, 1050, 640);
        this.accountNo = accountNo;

        addLabel("Type", 20, 15, 45, 30);
        typeFilter = new JComboBox<>();
        typeFilter.addItem(ALL);
        for (TransactionType t : TransactionType.values()) {
            typeFilter.addItem(t.getLabel());
        }
        typeFilter.setFont(UITheme.font(Font.PLAIN, 14));
        typeFilter.setBounds(65, 15, 170, 30);
        content.add(typeFilter);

        addLabel("Search", 255, 15, 60, 30);
        searchField = addTextField(315, 15, 250, 30);
        searchField.setToolTipText("Search in account number or description");

        JButton apply = addButton(UITheme.primaryButton("Apply"), 575, 15, 100, 30);
        JButton reload = addButton(UITheme.accentButton("Reload"), 685, 15, 100, 30);
        JButton print = addButton(UITheme.greyButton("Print"), 795, 15, 100, 30);
        JButton close = addButton(UITheme.greyButton("Close"), 905, 15, 110, 30);

        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 60, 995, 410);
        content.add(scroll);

        summary = addLabel("Loading...", 20, 480, 995, 50);
        summary.setFont(UITheme.font(Font.BOLD, 13));
        summary.setVerticalAlignment(JLabel.TOP);

        apply.addActionListener(e -> showFiltered());
        typeFilter.addActionListener(e -> showFiltered());
        searchField.addActionListener(e -> showFiltered());     // Enter key
        reload.addActionListener(e -> load());
        print.addActionListener(e -> {
            try {
                table.print();
            } catch (PrinterException ex) {
                MessageUtil.error(this, "Could not print: " + ex.getMessage());
            }
        });
        close.addActionListener(e -> dispose());

        load();
    }

    /** Which transactions to show. Overridden by AllTransactionsFrame. */
    protected List<Transaction> fetch() throws BankException {
        return new TransactionDAO().findByAccount(accountNo);
    }

    protected final void load() {
        summary.setText("Loading...");
        BackgroundTask.run(this, this::fetch, list -> {
            loaded = list;
            showFiltered();
        });
    }

    /** Filters the loaded List with the Stream API (no new database query needed). */
    private void showFiltered() {
        String type = (String) typeFilter.getSelectedItem();
        String search = searchField.getText().trim().toLowerCase();

        List<Transaction> rows = loaded.stream()
                .filter(t -> ALL.equals(type) || t.getType().getLabel().equals(type))
                .filter(t -> search.isEmpty()
                        || t.getAccountNo().contains(search)
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search)))
                .collect(Collectors.toList());

        table.setModel(TableModelBuilder.build(COLUMNS, rows, (Transaction t) -> new Object[] {
                t.getId(),
                t.getTime() == null ? "" : t.getTime().format(DATE_TIME),
                t.getAccountNo(),
                t.getType(),                             // the renderer colours it green/red
                t.getAmount(),
                t.getBalanceAfter(),
                t.getDescription()
        }));
        int[] widths = {60, 160, 120, 110, 110, 120, 300};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        summary.setText(buildSummary(rows));
    }

    /**
     * EnumMap<TransactionType, BigDecimal>: a Map whose keys are enum constants.
     * It keeps the enum order and is faster than a HashMap for enum keys.
     */
    private String buildSummary(List<Transaction> rows) {
        Map<TransactionType, BigDecimal> totals = new EnumMap<>(TransactionType.class);
        BigDecimal credit = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;
        for (Transaction t : rows) {
            totals.merge(t.getType(), t.getAmount(), BigDecimal::add);
            if (t.getType().isCredit()) {
                credit = credit.add(t.getAmount());
            } else {
                debit = debit.add(t.getAmount());
            }
        }
        StringBuilder sb = new StringBuilder("<html>")
                .append(rows.size()).append(" transactions &nbsp;|&nbsp; ")
                .append("<font color='#2e7d32'>Total Credit: ").append(MoneyUtil.format(credit)).append("</font> &nbsp;|&nbsp; ")
                .append("<font color='#c62828'>Total Debit: ").append(MoneyUtil.format(debit)).append("</font><br>");
        for (Map.Entry<TransactionType, BigDecimal> e : totals.entrySet()) {
            sb.append(e.getKey().getLabel()).append(": ").append(MoneyUtil.format(e.getValue())).append(" &nbsp;&nbsp; ");
        }
        return sb.append("</html>").toString();
    }
}
