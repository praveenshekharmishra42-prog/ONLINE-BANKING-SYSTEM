package online.banking.system.ui;

import online.banking.system.model.AccountStatus;
import online.banking.system.model.TransactionType;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Colours table cells: green for credits / ACTIVE, red for debits / BLOCKED,
 * orange for PENDING. It OVERRIDES a method of Swing's DefaultTableCellRenderer
 * - Swing calls our version for every cell (polymorphism).
 */
public class BankCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(table.getFont());
        c.setForeground(UITheme.TEXT);
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? UITheme.CARD : UITheme.BACKGROUND);
        }
        if (value instanceof TransactionType) {
            c.setForeground(((TransactionType) value).isCredit() ? UITheme.SUCCESS : UITheme.DANGER);
            c.setFont(table.getFont().deriveFont(Font.BOLD));
        } else if (value instanceof AccountStatus) {
            AccountStatus s = (AccountStatus) value;
            c.setForeground(s == AccountStatus.ACTIVE ? UITheme.SUCCESS
                    : s == AccountStatus.PENDING ? UITheme.WARNING : UITheme.DANGER);
            c.setFont(table.getFont().deriveFont(Font.BOLD));
        }
        return c;
    }
}
