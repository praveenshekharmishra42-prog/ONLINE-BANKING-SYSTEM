package online.banking.system.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Loading screen shown when the program starts.
 *
 * MULTITHREADING: implements Runnable. start() creates a new Thread that runs
 * run(): it fills the progress bar step by step (sleeping between steps) and
 * then opens the login window. Every change to the screen is sent back to the
 * UI thread with SwingUtilities.invokeLater().
 */
public class SplashWindow extends JWindow implements Runnable {

    private static final String[] STEPS = {
        "Starting application...", "Loading security module...",
        "Connecting to banking services...", "Preparing dashboard...", "Ready"
    };

    private final JProgressBar progress = new JProgressBar(0, 100);

    public SplashWindow() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), getHeight(), UITheme.PRIMARY_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                Logo.draw(g2, getWidth() / 2 - 45, 45, 90);
                g2.setColor(Color.WHITE);
                g2.setFont(UITheme.font(Font.BOLD, 30));
                drawCentered(g2, UITheme.BANK_NAME, 180);
                g2.setFont(UITheme.font(Font.PLAIN, 16));
                g2.setColor(new Color(200, 220, 245));
                drawCentered(g2, "Online Banking System", 210);
                g2.setFont(UITheme.font(Font.ITALIC, 13));
                drawCentered(g2, "Safe  |  Simple  |  Secure", 240);
                g2.dispose();
            }

            private void drawCentered(Graphics2D g2, String s, int y) {
                g2.drawString(s, (getWidth() - g2.getFontMetrics().stringWidth(s)) / 2, y);
            }
        };
        progress.setStringPainted(true);
        progress.setFont(UITheme.font(Font.PLAIN, 12));
        progress.setForeground(UITheme.ACCENT);
        progress.setBorder(BorderFactory.createEmptyBorder(0, 40, 25, 40));
        progress.setOpaque(false);
        panel.add(progress, BorderLayout.SOUTH);

        setContentPane(panel);
        setSize(520, 330);
        setLocationRelativeTo(null);
    }

    public void start() {
        setVisible(true);
        Thread loader = new Thread(this, "splash-loader");
        loader.start();
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i <= 100; i += 2) {
                final int value = i;
                final String step = STEPS[Math.min(i / 25, STEPS.length - 1)];
                SwingUtilities.invokeLater(() -> {
                    progress.setValue(value);
                    progress.setString(step + "  " + value + "%");
                });
                Thread.sleep(40);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        SwingUtilities.invokeLater(() -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}
