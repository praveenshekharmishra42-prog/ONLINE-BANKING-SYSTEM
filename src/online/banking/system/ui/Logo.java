package online.banking.system.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Draws the bank logo (a shield-like circle with a rupee-style mark) at any size. */
public final class Logo {

    private Logo() {
    }

    public static void draw(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.WHITE);
        g2.fillOval(x, y, size, size);
        g2.setColor(UITheme.ACCENT);
        g2.setStroke(new BasicStroke(Math.max(2f, size / 16f)));
        g2.drawOval(x + size / 10, y + size / 10, size - size / 5, size - size / 5);
        g2.setColor(UITheme.PRIMARY);
        g2.setFont(UITheme.font(Font.BOLD, (int) (size * 0.42)));
        FontMetrics fm = g2.getFontMetrics();
        String text = "SB";
        g2.drawString(text, x + (size - fm.stringWidth(text)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2);
    }
}
