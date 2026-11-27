package doposoccer.presentation;

import javax.swing.JButton;
import javax.swing.SwingConstants;
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
import java.awt.geom.RoundRectangle2D;

/**
 * Boton dibujado con Java2D. Admite cuatro jerarquias visuales para que la
 * accion principal de cada pantalla siempre sea evidente.
 */
@SuppressWarnings("serial")
class ArcadeButton extends JButton {

    enum Style {
        /** Accion principal: degradado rojo-morado y brillo. */
        PRIMARY,
        /** Accion secundaria: contorno y fondo translucido. */
        SECONDARY,
        /** Accion discreta: solo texto. */
        GHOST,
        /** Accion compacta dentro de una tarjeta. */
        SMALL
    }

    private final Style style;
    private boolean hovered;

    ArcadeButton(String text, Style style) {
        super(text);
        this.style = style;
        int fontSize = style == Style.SMALL ? 10 : style == Style.GHOST ? 13 : 15;
        setFont(Theme.font(fontSize, Font.BOLD));
        setForeground(Theme.TEXT);
        setHorizontalAlignment(SwingConstants.CENTER);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
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
        if (style == Style.SMALL) {
            setPreferredSize(new Dimension(104, 28));
        } else {
            setPreferredSize(new Dimension(300, 54));
        }
    }

    void setCompactSize(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        revalidate();
    }

    /**
     * Swing calcula los tamanos minimo y maximo del boton a partir del texto
     * antes de que se aplique {@link #setCompactSize}. Devolver el tamano
     * preferido mantiene estable el resultado de los layouts BoxLayout.
     */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        boolean pressed = getModel().isPressed();
        boolean enabled = isEnabled();
        int arc = style == Style.SMALL ? 14 : Math.min(22, height / 2);
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), arc, arc);

        if (!enabled) {
            g.setColor(new Color(255, 255, 255, 10));
            g.fill(shape);
            g.setColor(new Color(255, 255, 255, 22));
            g.draw(shape);
            g.setFont(getFont());
            g.setColor(Theme.DIM);
            drawLabel(g, width, height);
            g.dispose();
            return;
        }

        Color accent = switch (style) {
            case PRIMARY -> Theme.RED;
            case SECONDARY, SMALL -> Theme.PURPLE;
            case GHOST -> Theme.MUTED;
        };

        if (style == Style.PRIMARY) {
            // Sombra suave con desplazamiento: da profundidad sin halo de color.
            g.setColor(new Color(0, 0, 0, pressed ? 40 : 90));
            g.fill(new RoundRectangle2D.Double(1, pressed ? 2 : 4, width - 2, height - 3,
                    arc, arc));
        }

        if (style == Style.GHOST) {
            if (hovered) {
                g.setColor(new Color(255, 255, 255, 14));
                g.fill(shape);
            }
        } else if (style == Style.SECONDARY || style == Style.SMALL) {
            g.setPaint(new GradientPaint(0, 0,
                    Theme.alpha(Theme.PANEL_SOFT, hovered ? 255 : 224),
                    0, height, Theme.alpha(Theme.INK_PURPLE, 248)));
            g.fill(shape);
            g.setColor(Theme.alpha(accent, hovered ? 190 : 110));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
        } else {
            g.setColor(hovered ? Theme.RED.brighter() : Theme.RED);
            g.fill(shape);
            g.setColor(new Color(0, 0, 0, 60));
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
        }

        if (pressed) {
            g.setColor(new Color(0, 0, 0, 45));
            g.fill(shape);
        }
        if (isFocusOwner() && style != Style.PRIMARY) {
            g.setColor(Theme.alpha(Theme.GOLD, 150));
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Double(3, 3, width - 6, height - 6,
                    arc, arc));
        }

        g.setFont(getFont());
        g.setColor(style == Style.GHOST ? (hovered ? Theme.TEXT : Theme.MUTED) : Theme.TEXT);
        drawLabel(g, width, height);
        g.dispose();
    }

    private void drawLabel(Graphics2D g, int width, int height) {
        FontMetrics metrics = g.getFontMetrics();
        String text = getText();
        int textWidth = metrics.stringWidth(text);
        int x = (width - textWidth) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(text, x, y);
    }
}
