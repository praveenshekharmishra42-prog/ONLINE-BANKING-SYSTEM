package online.banking.system.ui;

import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * A label showing the live date and time.
 *
 * MULTITHREADING: it implements Runnable and runs on its own DAEMON thread
 * (a daemon thread does not keep the program alive when all windows close).
 * The thread wakes up every second and asks the UI thread to update the text
 * with SwingUtilities.invokeLater().
 *
 * "running" is VOLATILE: when stop() sets it to false on the UI thread, the
 * clock thread is guaranteed to see the new value and exit its loop.
 */
public class ClockLabel extends JLabel implements Runnable {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  hh:mm:ss a");

    private volatile boolean running = true;

    public ClockLabel() {
        setFont(UITheme.font(Font.PLAIN, 13));
        setForeground(UITheme.MUTED);
        Thread t = new Thread(this, "clock-thread");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        while (running) {
            String now = LocalDateTime.now().format(FORMAT);
            SwingUtilities.invokeLater(() -> setText(now));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void stop() {
        running = false;
    }
}
