package online.banking.system.ui;

import online.banking.system.account.AccountFactory;
import online.banking.system.exception.BankException;
import online.banking.system.exception.InvalidInputException;
import online.banking.system.model.AccountType;
import online.banking.system.model.Customer;
import online.banking.system.service.RegistrationService;
import online.banking.system.util.BackgroundTask;
import online.banking.system.util.MessageUtil;
import online.banking.system.util.MoneyUtil;
import online.banking.system.util.Validator;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** Online account opening form. Creates a PENDING account that the admin approves. */
public class RegisterFrame extends BaseFrame implements ActionListener {

    /** Immutable List (Collections) used to fill the State dropdown. */
    private static final List<String> STATES = List.of(
            "Andhra Pradesh", "Assam", "Bihar", "Chhattisgarh", "Delhi", "Goa", "Gujarat", "Haryana",
            "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
            "Odisha", "Punjab", "Rajasthan", "Tamil Nadu", "Telangana", "Uttar Pradesh",
            "Uttarakhand", "West Bengal", "Other");

    private static final List<String> OCCUPATIONS = List.of(
            "Student", "Salaried", "Self-Employed", "Business", "Retired", "Homemaker", "Other");

    private final JTextField name, fatherName, dob, email, phone, address, city, pincode, deposit, username;
    private final JPasswordField password, confirm;
    private final JRadioButton male, female, other;
    private final JComboBox<String> occupation, state;
    private final JComboBox<AccountType> accountType;
    private final JLabel minDepositHint;
    private final JCheckBox agree;
    private final JButton submit, back;

    public RegisterFrame() {
        super("Open a New Account", "Fill in your details - the bank will approve your account", 920, 690);

        // ---------------- left column: personal details ----------------
        int lx = 35, fx = 185, fw = 240, y = 20, step = 44;
        addValue(content, "PERSONAL DETAILS", lx, y, 300, 25).setForeground(UITheme.PRIMARY);
        y += 36;
        addLabel("Full Name", lx, y, 140, 30);         name = addTextField(fx, y, fw, 30); y += step;
        addLabel("Father's Name", lx, y, 140, 30);     fatherName = addTextField(fx, y, fw, 30); y += step;
        addLabel("Date of Birth", lx, y, 140, 30);     dob = addTextField(fx, y, fw, 30);
        dob.setToolTipText("dd-mm-yyyy, e.g. 15-08-2003"); y += step;
        JLabel dobHint = addLabel("format: dd-mm-yyyy", fx, y - 14, fw, 14);
        dobHint.setFont(UITheme.font(Font.ITALIC, 11));
        dobHint.setForeground(UITheme.MUTED);
        y += 4;

        addLabel("Gender", lx, y, 140, 30);
        male = radio("Male", fx, y, 70);
        female = radio("Female", fx + 75, y, 85);
        other = radio("Other", fx + 165, y, 75);
        ButtonGroup gender = new ButtonGroup();       // only one can be selected
        gender.add(male);
        gender.add(female);
        gender.add(other);
        male.setSelected(true);
        y += step;

        addLabel("Email", lx, y, 140, 30);             email = addTextField(fx, y, fw, 30); y += step;
        addLabel("Mobile Number", lx, y, 140, 30);     phone = addTextField(fx, y, fw, 30); y += step;
        addLabel("Occupation", lx, y, 140, 30);
        occupation = new JComboBox<>(OCCUPATIONS.toArray(new String[0]));
        occupation.setFont(UITheme.font(Font.PLAIN, 14));
        occupation.setBounds(fx, y, fw, 30);
        content.add(occupation);
        y += step;
        addLabel("Address", lx, y, 140, 30);           address = addTextField(fx, y, fw, 30); y += step;
        addLabel("City", lx, y, 140, 30);              city = addTextField(fx, y, fw, 30); y += step;

        // ---------------- right column: address + account ----------------
        int rx = 470, rfx = 625, rfw = 250;
        y = 20;
        addValue(content, "ACCOUNT DETAILS", rx, y, 300, 25).setForeground(UITheme.PRIMARY);
        y += 36;
        addLabel("State", rx, y, 150, 30);
        state = new JComboBox<>(STATES.toArray(new String[0]));
        state.setSelectedItem("Uttar Pradesh");
        state.setFont(UITheme.font(Font.PLAIN, 14));
        state.setBounds(rfx, y, rfw, 30);
        content.add(state);
        y += step;
        addLabel("PIN Code", rx, y, 150, 30);          pincode = addTextField(rfx, y, rfw, 30); y += step;

        addLabel("Account Type", rx, y, 150, 30);
        accountType = new JComboBox<>(AccountType.values());
        accountType.setFont(UITheme.font(Font.PLAIN, 14));
        accountType.setBounds(rfx, y, rfw, 30);
        content.add(accountType);
        y += 32;
        minDepositHint = addLabel("", rfx, y, rfw + 30, 18);
        minDepositHint.setFont(UITheme.font(Font.ITALIC, 12));
        minDepositHint.setForeground(UITheme.ACCENT);
        accountType.addActionListener(e -> updateHint());
        updateHint();
        y += 26;

        addLabel("Opening Deposit (Rs.)", rx, y, 155, 30); deposit = addTextField(rfx, y, rfw, 30); y += step;
        addLabel("Choose Username", rx, y, 150, 30);   username = addTextField(rfx, y, rfw, 30); y += step;
        addLabel("Password", rx, y, 150, 30);          password = addPasswordField(rfx, y, rfw, 30); y += step;
        addLabel("Confirm Password", rx, y, 150, 30);  confirm = addPasswordField(rfx, y, rfw, 30); y += step;
        JLabel pwHint = addLabel("min 6 characters, with a letter and a digit", rfx, y - 12, rfw + 30, 14);
        pwHint.setFont(UITheme.font(Font.ITALIC, 11));
        pwHint.setForeground(UITheme.MUTED);

        agree = new JCheckBox("I confirm that the information given above is correct.");
        agree.setFont(UITheme.font(Font.PLAIN, 13));
        agree.setBackground(UITheme.BACKGROUND);
        agree.setBounds(lx, 470, 500, 25);
        content.add(agree);

        submit = addButton(UITheme.accentButton("Open Account"), 560, 500, 160, 38);
        back = addButton(UITheme.greyButton("Back to Login"), 730, 500, 150, 38);
        submit.addActionListener(this);
        back.addActionListener(this);
    }

    private JRadioButton radio(String text, int x, int y, int width) {
        JRadioButton r = new JRadioButton(text);
        r.setFont(UITheme.font(Font.PLAIN, 14));
        r.setBackground(UITheme.BACKGROUND);
        r.setBounds(x, y, width, 30);
        content.add(r);
        return r;
    }

    /** Asks the account class itself for its rule (polymorphism) instead of hard-coding it. */
    private void updateHint() {
        AccountType type = (AccountType) accountType.getSelectedItem();
        BigDecimal min = AccountFactory.minimumOpeningDeposit(type);
        String extra = type == AccountType.SAVINGS ? " | 4% interest p.a." : " | Rs. 10,000 overdraft";
        minDepositHint.setText("Min deposit " + MoneyUtil.format(min) + extra);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == back) {
            dispose();
            LoginFrame.open();
            return;
        }
        try {
            Customer details = readForm();
            String user = Validator.username(username.getText());
            String pass = Validator.password(new String(password.getPassword()));
            if (!pass.equals(new String(confirm.getPassword()))) {
                throw new InvalidInputException("Password and Confirm Password do not match.");
            }
            BigDecimal amount = Validator.amount(deposit.getText());
            AccountType type = (AccountType) accountType.getSelectedItem();
            if (!agree.isSelected()) {
                throw new InvalidInputException("Please tick the confirmation box.");
            }

            submit.setEnabled(false);
            submit.setText("Please wait...");
            BackgroundTask.run(this,
                    () -> new RegistrationService().openAccount(details, user, pass, type, amount),
                    accountNo -> {
                        MessageUtil.info(this,
                                "Account opened successfully!\n\n"
                                + "Account Number : " + accountNo + "\n"
                                + "Account Type   : " + type + "\n"
                                + "Username       : " + user + "\n\n"
                                + "Your account is PENDING approval by the bank.\n"
                                + "You can log in now; transactions are enabled after approval.");
                        dispose();
                        LoginFrame.open();
                    },
                    () -> {
                        submit.setEnabled(true);
                        submit.setText("Open Account");
                    });
        } catch (BankException ex) {
            MessageUtil.error(this, ex.getMessage());
        }
    }

    private Customer readForm() throws InvalidInputException {
        String fullName = Validator.requireText("Full Name", name.getText());
        if (!fullName.matches("[A-Za-z .]{3,60}")) {
            throw new InvalidInputException("Full Name should contain only letters and spaces.");
        }
        Customer c = new Customer(0, "", fullName);
        c.setFatherName(Validator.requireText("Father's Name", fatherName.getText()));
        c.setDateOfBirth(Validator.dateOfBirth(dob.getText()));
        c.setGender(male.isSelected() ? "Male" : female.isSelected() ? "Female" : "Other");
        c.setEmail(Validator.email(email.getText()));
        c.setPhone(Validator.phone(phone.getText()));
        c.setOccupation((String) occupation.getSelectedItem());
        c.setAddress(Validator.requireText("Address", address.getText()));
        c.setCity(Validator.requireText("City", city.getText()));
        c.setState((String) state.getSelectedItem());
        c.setPincode(Validator.pincode(pincode.getText()));
        return c;
    }
}
