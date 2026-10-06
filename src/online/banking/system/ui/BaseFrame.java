package online.banking.system.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * ABSTRACT parent of every window in the application (INHERITANCE).
 *
 * It does the work every screen needs - size, centring, the blue header and a
 * content panel - so child classes only add their own fields and buttons.
 * The helper methods (addLabel, addTextField, ...) are protected: usable by
 * children, hidden from other classes.
 */
public abstract class BaseFrame extends JFrame {

    protected final JPanel content;

    protected BaseFrame(String title, String subtitle, int width, int height) {
        super(title + " | " + UITheme.BANK_NAME);
        setSize(width, height);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new HeaderPanel(title, subtitle), BorderLayout.NORTH);

        content = new JPanel(null);                  // absolute positioning with setBounds
        content.setBackground(UITheme.BACKGROUND);
        getContentPane().add(content, BorderLayout.CENTER);
    }

    protected JLabel addLabel(String text, int x, int y, int w, int h) {
        return addLabel(content, text, x, y, w, h);
    }

    protected JLabel addLabel(JComponent parent, String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.font(Font.PLAIN, 14));
        l.setForeground(UITheme.TEXT);
        l.setBounds(x, y, w, h);
        parent.add(l);
        return l;
    }

    protected JLabel addValue(JComponent parent, String text, int x, int y, int w, int h) {
        JLabel l = addLabel(parent, text, x, y, w, h);
        l.setFont(UITheme.font(Font.BOLD, 14));
        return l;
    }

    protected JTextField addTextField(int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setFont(UITheme.font(Font.PLAIN, 14));
        f.setBounds(x, y, w, h);
        content.add(f);
        return f;
    }

    protected JPasswordField addPasswordField(int x, int y, int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setFont(UITheme.font(Font.PLAIN, 14));
        f.setBounds(x, y, w, h);
        content.add(f);
        return f;
    }

    protected JButton addButton(JButton button, int x, int y, int w, int h) {
        button.setBounds(x, y, w, h);
        content.add(button);
        return button;
    }
}
