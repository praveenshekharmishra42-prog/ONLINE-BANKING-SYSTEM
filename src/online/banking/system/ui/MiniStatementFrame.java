package online.banking.system.ui;

import online.banking.system.dao.AccountDAO;
import online.banking.system.dao.TransactionDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.Transaction;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** A printable text statement of the last 10 transactions. */
public class MiniStatementFrame extends BaseFrame {

    private static final int COUNT = 10;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");

    private final JTextArea area = new JTextArea();

    public MiniStatementFrame(String accountNo, String holderName) {
        super("Mini Statement", "Last " + COUNT + " transactions", 640, 600);

        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setText("\n   Loading statement...");
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBounds(20, 15, 585, 420);
        content.add(scroll);

        JButton print = addButton(UITheme.primaryButton("Print"), 325, 450, 135, 38);
        JButton close = addButton(UITheme.greyButton("Close"), 470, 450, 135, 38);
        print.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                MessageUtil.error(this, "Could not print: " + ex.getMessage());
            }
        });
        close.addActionListener(e -> dispose());

        BackgroundTask.run(this, () -> build(accountNo, holderName), text -> {
            area.setText(text);
            area.setCaretPosition(0);
        });
    }

    /** Runs on the background thread: reads the data and builds the text. */
    private String build(String accountNo, String holderName) throws BankException {
        BigDecimal balance = new AccountDAO().findById(accountNo)
                .orElseThrow(() -> new BankException("Account not found.")).getBalance();
        List<Transaction> recent = new TransactionDAO().findRecent(accountNo, COUNT);

        String line = "-".repeat(70) + "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(center(UITheme.BANK_NAME.toUpperCase() + " - MINI STATEMENT")).append("\n\n");
        sb.append(String.format("  Account Holder : %s%n", holderName));
        sb.append(String.format("  Account Number : %s%n", accountNo));
        sb.append(String.format("  Generated On   : %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"))));
        sb.append(line);
        sb.append(String.format("  %-15s %-14s %4s %15s %15s%n", "Date", "Type", "", "Amount", "Balance"));
        sb.append(line);
        if (recent.isEmpty()) {
            sb.append("  No transactions yet.\n");
        }
        for (Transaction t : recent) {
            sb.append(String.format("  %-15s %-14s %4s %15s %15s%n",
                    t.getTime() == null ? "" : t.getTime().format(DATE),
                    t.getType().getLabel(),
                    t.getType().isCredit() ? "Cr" : "Dr",
                    t.getAmount().toPlainString(),
                    t.getBalanceAfter().toPlainString()));
        }
        sb.append(line);
        sb.append(String.format("  %-50s %15s%n", "Closing Balance", MoneyUtil.format(balance)));
        sb.append(line);
        sb.append("\n").append(center("This is a computer generated statement.")).append("\n");
        return sb.toString();
    }

    private static String center(String s) {
        int pad = Math.max(0, (70 - s.length()) / 2);
        return " ".repeat(pad) + s;
    }
}
