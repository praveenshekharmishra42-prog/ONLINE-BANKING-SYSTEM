package online.banking.system.ui;

import online.banking.system.dao.UserDAO;
import online.banking.system.exception.BankException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.model.Person;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.Validator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPasswordField;

/**
 * Change password - works for BOTH Admin and Customer because the parameter
 * type is the parent class Person (polymorphism).
 */
public class ChangePasswordFrame extends BaseFrame implements ActionListener {

    private final Person user;
    private final JPasswordField current, newPass, confirm;
    private final JButton save, cancel;

    public ChangePasswordFrame(Person user) {
        super("Change Password", user.getRole() + " : " + user.getUsername(), 520, 370);
        this.user = user;

        addLabel("Current Password", 40, 30, 160, 30);  current = addPasswordField(210, 30, 260, 30);
        addLabel("New Password", 40, 80, 160, 30);      newPass = addPasswordField(210, 80, 260, 30);
        addLabel("Confirm Password", 40, 130, 160, 30); confirm = addPasswordField(210, 130, 260, 30);

        save = addButton(UITheme.accentButton("Update"), 210, 190, 125, 38);
        cancel = addButton(UITheme.greyButton("Cancel"), 345, 190, 125, 38);
        save.addActionListener(this);
        cancel.addActionListener(this);
        getRootPane().setDefaultButton(save);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel) {
            dispose();
            return;
        }
        try {
            String oldPw = new String(current.getPassword());
            String newPw = Validator.password(new String(newPass.getPassword()));
            if (!newPw.equals(new String(confirm.getPassword()))) {
                throw new InvalidInputException("New password and confirmation do not match.");
            }
            if (newPw.equals(oldPw)) {
                throw new InvalidInputException("New password must be different from the current one.");
            }
            save.setEnabled(false);
            BackgroundTask.run(this, () -> {
                new UserDAO().changePassword(user.getUserId(), oldPw, newPw);
                return Boolean.TRUE;
            }, ok -> {
                MessageUtil.info(this, "Password changed successfully.");
                dispose();
            }, () -> save.setEnabled(true));
        } catch (BankException ex) {
            MessageUtil.error(this, ex.getMessage());
        }
    }
}
