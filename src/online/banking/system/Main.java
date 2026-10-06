package online.banking.system;

import online.banking.system.ui.SplashWindow;
import javax.swing.SwingUtilities;

/**
 * Program entry point.
 * Swing windows must be created on the Event Dispatch Thread (EDT),
 * so the splash screen is started with SwingUtilities.invokeLater().
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SplashWindow().start());
    }
}
