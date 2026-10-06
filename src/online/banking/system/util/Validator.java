package online.banking.system.util;

import online.banking.system.exception.InvalidInputException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Checks form input. Every method THROWS InvalidInputException with a clear
 * message instead of letting bad data reach the database.
 */
public final class Validator {

    public static final BigDecimal MAX_TRANSACTION = new BigDecimal("1000000");   // Rs. 10 lakh
    private static final DateTimeFormatter DOB_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Validator() {
    }

    public static String requireText(String field, String value) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        return value.trim();
    }

    public static String email(String value) throws InvalidInputException {
        String v = requireText("Email", value);
        if (!v.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new InvalidInputException("Please enter a valid email address.");
        }
        return v;
    }

    public static String phone(String value) throws InvalidInputException {
        String v = requireText("Phone number", value);
        if (!v.matches("[6-9]\\d{9}")) {
            throw new InvalidInputException("Phone number must be 10 digits and start with 6, 7, 8 or 9.");
        }
        return v;
    }

    public static String pincode(String value) throws InvalidInputException {
        String v = requireText("PIN code", value);
        if (!v.matches("[1-9]\\d{5}")) {
            throw new InvalidInputException("PIN code must be 6 digits.");
        }
        return v;
    }

    public static String username(String value) throws InvalidInputException {
        String v = requireText("Username", value);
        if (!v.matches("[A-Za-z0-9_]{4,20}")) {
            throw new InvalidInputException("Username must be 4-20 letters, digits or _ (no spaces).");
        }
        return v;
    }

    public static String password(String value) throws InvalidInputException {
        if (value == null || value.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        if (!value.matches(".*\\d.*") || !value.matches(".*[A-Za-z].*")) {
            throw new InvalidInputException("Password must contain at least one letter and one digit.");
        }
        return value;
    }

    /** Date of birth in dd-MM-yyyy format; the customer must be at least 18. */
    public static LocalDate dateOfBirth(String value) throws InvalidInputException {
        String v = requireText("Date of birth", value);
        LocalDate dob;
        try {
            dob = LocalDate.parse(v, DOB_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date of birth must be in dd-mm-yyyy format, e.g. 15-08-2003.");
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) {
            throw new InvalidInputException("You must be at least 18 years old to open an account.");
        }
        if (age > 120) {
            throw new InvalidInputException("Please enter a valid date of birth.");
        }
        return dob;
    }

    /** Parses an amount like "2500" or "2500.50": positive, max 2 decimals, max Rs. 10 lakh. */
    public static BigDecimal amount(String value) throws InvalidInputException {
        String v = requireText("Amount", value).replace(",", "");
        BigDecimal amount;
        try {
            amount = new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Amount must be a number, e.g. 2500 or 2500.50.");
        }
        if (amount.signum() <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
        if (amount.scale() > 2) {
            throw new InvalidInputException("Amount can have at most 2 decimal places (paise).");
        }
        if (amount.compareTo(MAX_TRANSACTION) > 0) {
            throw new InvalidInputException("A single transaction cannot exceed Rs. 10,00,000.");
        }
        return amount.setScale(2);
    }
}
