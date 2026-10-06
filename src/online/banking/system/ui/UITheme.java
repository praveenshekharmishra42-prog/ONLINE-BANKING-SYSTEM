package online.banking.system.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/** Colours, fonts and ready-made styled components, so every screen looks the same. */
public final class UITheme {

    public static final String BANK_NAME = "SecureBank";

    public static final Color PRIMARY = new Color(12, 52, 110);
    public static final Color PRIMARY_LIGHT = new Color(30, 100, 180);
    public static final Color ACCENT = new Color(0, 137, 123);
    public static final Color SUCCESS = new Color(46, 125, 50);
    public static final Color WARNING = new Color(221, 120, 20);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color BACKGROUND = new Color(244, 246, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(218, 223, 230);
    public static final Color TEXT = new Color(33, 37, 41);
    public static final Color MUTED = new Color(108, 117, 125);

    private static final String FONT_NAME = "Segoe UI";

    private UITheme() {
    }

    public static Font font(int style, int size) {
        return new Font(FONT_NAME, style, size);
    }

    public static JButton button(String text, Color background) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 13));
        b.setBackground(background);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primaryButton(String text) {
        return button(text, PRIMARY_LIGHT);
    }

    public static JButton accentButton(String text) {
        return button(text, ACCENT);
    }

    public static JButton dangerButton(String text) {
        return button(text, DANGER);
    }

    public static JButton greyButton(String text) {
        return button(text, MUTED);
    }

    /** A white rounded-looking box used to group information. */
    public static JPanel card() {
        JPanel p = new JPanel(null);
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createLineBorder(BORDER));
        return p;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(font(Font.PLAIN, 13));
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(207, 226, 255));
        table.setSelectionForeground(TEXT);
        table.setAutoCreateRowSorter(true);          // click a header to sort
        table.setFillsViewportHeight(true);
        table.setBackground(CARD);
        table.setDefaultRenderer(Object.class, new BankCellRenderer());
        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 13));
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
    }
}
