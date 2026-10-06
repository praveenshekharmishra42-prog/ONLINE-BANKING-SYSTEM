package online.banking.system.ui;

import online.banking.system.dao.UserDAO;
import online.banking.system.model.Admin;
import online.banking.system.model.Customer;
import online.banking.system.model.Person;
import online.banking.system.model.Role;
import online.banking.system.util.BackgroundTask;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Login screen for both admins and customers. */
public class LoginFrame extends BaseFrame implements ActionListener {

    private final JTextField username;
    private final JPasswordField password;
    private final JComboBox<Role> role;             // generic Swing component: holds Role values
    private final JButton login;
    private final JButton openAccount;
    private final JButton exit;

    public LoginFrame() {
        super("Internet Banking Login", "Sign in to manage your account", 560, 470);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel card = UITheme.card();
        card.setBounds(60, 25, 430, 300);
        content.add(card);

        addValue(card, "Login As", 30, 25, 110, 30);
        role = new JComboBox<>(Role.values());
        role.setSelectedItem(Role.CUSTOMER);
        role.setFont(UITheme.font(Font.PLAIN, 14));
        role.setBounds(150, 25, 240, 30);
        card.add(role);

        addValue(card, "Username", 30, 75, 110, 30);
        username = new JTextField();
        username.setFont(UITheme.font(Font.PLAIN, 14));
        username.setBounds(150, 75, 240, 30);
        card.add(username);

        addValue(card, "Password", 30, 125, 110, 30);
        password = new JPasswordField();
        password.setFont(UITheme.font(Font.PLAIN, 14));
        password.setBounds(150, 125, 240, 30);
        card.add(password);

        login = UITheme.primaryButton("Login");
        login.setBounds(150, 180, 115, 36);
        card.add(login);

        exit = UITheme.greyButton("Exit");
        exit.setBounds(275, 180, 115, 36);
        card.add(exit);

        JLabel newHere = addLabel(card, "New to " + UITheme.BANK_NAME + "?", 30, 240, 160, 30);
        newHere.setForeground(UITheme.MUTED);
        openAccount = UITheme.accentButton("Open New Account");
        openAccount.setBounds(190, 238, 200, 34);
        card.add(openAccount);

        JLabel hint = addLabel("Your password is protected with SHA-256 hashing.", 60, 335, 430, 25);
        hint.setFont(UITheme.font(Font.ITALIC, 12));
        hint.setForeground(UITheme.MUTED);

        login.addActionListener(this);
        exit.addActionListener(this);
        openAccount.addActionListener(this);
        getRootPane().setDefaultButton(login);      // Enter key = Login
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login) {
            doLogin();
        } else if (e.getSource() == openAccount) {
            dispose();
            new RegisterFrame().setVisible(true);
        } else if (e.getSource() == exit) {
            System.exit(0);
        }
    }

    private void doLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        Role selectedRole = (Role) role.getSelectedItem();
        if (user.isEmpty() || pass.isEmpty()) {
            online.banking.system.util.MessageUtil.error(this, "Please enter your username and password.");
            return;
        }
        login.setEnabled(false);
        login.setText("Checking...");

        // Database check runs on a background thread; the window stays responsive
        BackgroundTask.run(this,
                () -> new UserDAO().authenticate(user, pass, selectedRole),
                (Person person) -> {
                    dispose();
                    // Same variable type (Person), different object -> different dashboard
                    if (person instanceof Admin) {
                        new AdminDashboard((Admin) person).setVisible(true);
                    } else {
                        new CustomerDashboard((Customer) person).setVisible(true);
                    }
                },
                () -> {
                    login.setEnabled(true);
                    login.setText("Login");
                    password.setText("");
                });
    }

    /** Small helper so other screens can return to login. */
    public static void open() {
        new LoginFrame().setVisible(true);
    }

    static {
        // keep a consistent look on all systems
        javax.swing.UIManager.put("OptionPane.messageFont", UITheme.font(Font.PLAIN, 14));
        javax.swing.UIManager.put("OptionPane.buttonFont", UITheme.font(Font.BOLD, 13));
        javax.swing.UIManager.put("ComboBox.selectionBackground", new Color(207, 226, 255));
    }
}
