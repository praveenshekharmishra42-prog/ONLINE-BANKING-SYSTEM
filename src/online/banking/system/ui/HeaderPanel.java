package online.banking.system.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * The blue banner at the top of every window, drawn with Java2D
 * (no image files needed). paintComponent() is OVERRIDDEN to draw it.
 */
public class HeaderPanel extends JPanel {

    public static final int HEIGHT = 70;

    private final String title;
    private final String subtitle;

    public HeaderPanel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setPreferredSize(new Dimension(100, HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), 0, UITheme.PRIMARY_LIGHT));
        g2.fillRect(0, 0, getWidth(), getHeight());

        Logo.draw(g2, 18, 13, 44);

        g2.setColor(Color.WHITE);
        g2.setFont(UITheme.font(Font.BOLD, 20));
        g2.drawString(title, 76, 34);
        if (subtitle != null && !subtitle.isEmpty()) {
            g2.setColor(new Color(200, 220, 245));
            g2.setFont(UITheme.font(Font.PLAIN, 13));
            g2.drawString(subtitle, 77, 54);
        }
        g2.setColor(new Color(200, 220, 245));
        g2.setFont(UITheme.font(Font.BOLD, 13));
        String bank = UITheme.BANK_NAME;
        g2.drawString(bank, getWidth() - g2.getFontMetrics().stringWidth(bank) - 20, 40);
        g2.dispose();
    }
}
