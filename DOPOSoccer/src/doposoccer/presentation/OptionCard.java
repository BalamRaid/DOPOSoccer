package doposoccer.presentation;

import javax.swing.JToggleButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Tarjeta seleccionable para elegir el tipo de partido. Sustituye a los
 * radios clasicos de Swing por una opcion grande con titulo y descripcion.
 */
@SuppressWarnings("serial")
class OptionCard extends JToggleButton {

    private final String title;
    private final String description;
    private boolean hovered;

    OptionCard(String title, String description) {
        this.title = title;
        this.description = description;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(286, 58));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        setAlignmentX(LEFT_ALIGNMENT);
        getAccessibleContext().setAccessibleName(title);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        boolean selected = isSelected();
        boolean enabled = isEnabled();
        int arc = 16;
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), arc, arc);

        Color accent = selected ? Theme.RED : (hovered && enabled ? Theme.PURPLE : Theme.LINE_STRONG);
        if (enabled) {
            g.setPaint(new GradientPaint(0, 0,
                    Theme.alpha(selected ? Theme.PANEL_LIGHT : Theme.PANEL_SOFT,
                            selected ? 255 : 205),
                    width, height,
                    Theme.alpha(selected ? new Color(66, 26, 54) : Theme.INK_SOFT,
                            selected ? 255 : 205)));
            g.fill(shape);
        } else {
            g.setColor(new Color(255, 255, 255, 8));
            g.fill(shape);
        }

        if (selected && enabled) {
            Theme.drawGlow(g, shape, Theme.CRIMSON, 4);
        }
        g.setColor(enabled ? Theme.alpha(accent, selected ? 200 : 85)
                : new Color(255, 255, 255, 24));
        g.setStroke(new BasicStroke(selected ? 1.6f : 1.0f));
        g.draw(shape);
        if (selected && enabled) {
            g.setColor(Theme.alpha(Theme.CRIMSON, 170));
            g.fill(new RoundRectangle2D.Double(0, 12, 4, height - 24, 4, 4));
        }

        double radioX = 16;
        double radioY = height / 2.0;
        g.setColor(enabled ? (selected ? Theme.RED : Theme.DIM)
                : new Color(255, 255, 255, 30));
        g.setStroke(new BasicStroke(selected ? 2f : 1.2f));
        g.draw(new Ellipse2D.Double(radioX - 7, radioY - 7, 14, 14));
        if (selected && enabled) {
            g.setColor(Theme.TEXT);
            g.fill(new Ellipse2D.Double(radioX - 3.2, radioY - 3.2, 6.4, 6.4));
        }

        int textX = 34;
        g.setFont(Theme.font(13, Font.BOLD));
        g.setColor(enabled ? Theme.TEXT : Theme.DIM);
        g.drawString(title, textX, 24);
        g.setFont(Theme.font(10, Font.PLAIN));
        g.setColor(enabled ? (selected ? Theme.MUTED : Theme.DIM) : new Color(110, 100, 123));
        FontMetrics metrics = g.getFontMetrics();
        String descriptionText = description;
        int available = Math.max(20, width - textX - 14);
        while (metrics.stringWidth(descriptionText) > available
                && descriptionText.length() > 5) {
            descriptionText = descriptionText.substring(0, descriptionText.length() - 1);
        }
        if (!descriptionText.equals(description)) {
            descriptionText = descriptionText.substring(0,
                    Math.max(0, descriptionText.length() - 1)) + "...";
        }
        g.drawString(descriptionText, textX, 43);
        g.dispose();
    }
}
