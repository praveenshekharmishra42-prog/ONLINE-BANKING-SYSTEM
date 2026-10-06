package online.banking.system.ui;

import online.banking.system.dao.CustomerDAO;
import online.banking.system.exception.BankException;
import online.banking.system.model.Customer;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.Validator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** View personal details and update contact information. */
public class ProfileFrame extends BaseFrame implements ActionListener {

    private final int customerId;
    private Customer customer;
    private final JLabel nameValue, usernameValue, fatherValue, dobValue, genderValue;
    private final JTextField email, phone, address, city, state, pincode, occupation;
    private final JButton save, close;

    public ProfileFrame(Customer loggedIn) {
        super("My Profile", "Personal and contact details", 700, 600);
        this.customerId = loggedIn.getUserId();

        JPanel card = UITheme.card();
        card.setBounds(30, 15, 625, 130);
        content.add(card);
        addLabel(card, "Name", 20, 12, 120, 22).setForeground(UITheme.MUTED);
        nameValue = addValue(card, "-", 140, 12, 180, 22);
        addLabel(card, "Username", 330, 12, 110, 22).setForeground(UITheme.MUTED);
        usernameValue = addValue(card, "-", 440, 12, 170, 22);
        addLabel(card, "Father's Name", 20, 50, 120, 22).setForeground(UITheme.MUTED);
        fatherValue = addValue(card, "-", 140, 50, 180, 22);
        addLabel(card, "Date of Birth", 330, 50, 110, 22).setForeground(UITheme.MUTED);
        dobValue = addValue(card, "-", 440, 50, 170, 22);
        addLabel(card, "Gender", 20, 88, 120, 22).setForeground(UITheme.MUTED);
        genderValue = addValue(card, "-", 140, 88, 180, 22);

        addValue(content, "Contact details (editable)", 30, 155, 400, 25).setForeground(UITheme.PRIMARY);
        int y = 190;
        addLabel("Email", 30, y, 120, 30);       email = addTextField(160, y, 300, 30); y += 40;
        addLabel("Mobile", 30, y, 120, 30);      phone = addTextField(160, y, 300, 30); y += 40;
        addLabel("Address", 30, y, 120, 30);     address = addTextField(160, y, 495, 30); y += 40;
        addLabel("City", 30, y, 120, 30);        city = addTextField(160, y, 190, 30);
        addLabel("State", 370, y, 60, 30);       state = addTextField(430, y, 225, 30); y += 40;
        addLabel("PIN Code", 30, y, 120, 30);    pincode = addTextField(160, y, 190, 30);
        addLabel("Occupation", 370, y, 80, 30);  occupation = addTextField(455, y, 200, 30);

        save = addButton(UITheme.accentButton("Save Changes"), 365, 455, 150, 38);
        close = addButton(UITheme.greyButton("Close"), 525, 455, 130, 38);
        save.addActionListener(this);
        close.addActionListener(this);
        save.setEnabled(false);

        BackgroundTask.run(this,
                () -> new CustomerDAO().findById(customerId).orElseThrow(() -> new BankException("Profile not found.")),
                this::show);
    }

    private void show(Customer c) {
        customer = c;
        nameValue.setText(c.getName());
        usernameValue.setText(c.getUsername());
        fatherValue.setText(c.getFatherName());
        dobValue.setText(c.getDateOfBirth() == null ? "-" : c.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        genderValue.setText(c.getGender());
        email.setText(c.getEmail());
        phone.setText(c.getPhone());
        address.setText(c.getAddress());
        city.setText(c.getCity());
        state.setText(c.getState());
        pincode.setText(c.getPincode());
        occupation.setText(c.getOccupation());
        save.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == close) {
            dispose();
            return;
        }
        try {
            customer.setEmail(Validator.email(email.getText()));
            customer.setPhone(Validator.phone(phone.getText()));
            customer.setAddress(Validator.requireText("Address", address.getText()));
            customer.setCity(Validator.requireText("City", city.getText()));
            customer.setState(Validator.requireText("State", state.getText()));
            customer.setPincode(Validator.pincode(pincode.getText()));
            customer.setOccupation(Validator.requireText("Occupation", occupation.getText()));
            save.setEnabled(false);
            BackgroundTask.run(this, () -> new CustomerDAO().update(customer), ok -> {
                MessageUtil.info(this, "Profile updated successfully.");
                dispose();
            }, () -> save.setEnabled(true));
        } catch (BankException ex) {
            MessageUtil.error(this, ex.getMessage());
        }
    }
}
