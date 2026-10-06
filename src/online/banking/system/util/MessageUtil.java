package online.banking.system.util;

import online.banking.system.exception.BankException;
import online.banking.system.exception.DatabaseException;
import java.awt.Component;
import javax.swing.JOptionPane;

/** Pop-up messages, so errors are visible to the user (not only in the console). */
public final class MessageUtil {

    private MessageUtil() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component parent, Throwable error) {
        // Expected business errors (e.g. insufficient funds) only need the pop-up.
        // Unexpected / database errors also print full details to the NetBeans Output panel.
        if (!(error instanceof BankException) || error instanceof DatabaseException) {
            error.printStackTrace();
        }
        String message = error.getMessage() != null ? error.getMessage() : error.toString();
        error(parent, message);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
