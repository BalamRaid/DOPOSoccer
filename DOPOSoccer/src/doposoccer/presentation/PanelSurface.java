package doposoccer.presentation;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Tarjeta translucida con borde y acento de color. Se usa para agrupar la
 * configuracion de cada jugador, las reglas y los controles.
 */
@SuppressWarnings("serial")
class PanelSurface extends JPanel {

    private Color accent = Theme.PURPLE;
    private boolean strong;

    PanelSurface(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    PanelSurface(LayoutManager layout, Color accent) {
        this(layout);
        this.accent = accent;
    }

    void setAccent(Color accent) {
        this.accent = accent == null ? Theme.PURPLE : accent;
        repaint();
    }

    void setStrong(boolean strong) {
        this.strong = strong;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int arc = 26;
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), arc, arc);

        g.setColor(new Color(0, 0, 0, 95));
        g.fill(new RoundRectangle2D.Double(3, 5, Math.max(0, width - 6),
                Math.max(0, height - 6), arc, arc));

        Color top = strong ? Theme.PANEL_LIGHT : Theme.PANEL;
        Color bottom = strong ? Theme.INK_PURPLE : Theme.INK_SOFT;
        g.setPaint(new GradientPaint(0, 0, Theme.alpha(top, 244),
                0, height, Theme.alpha(bottom, 238)));
        g.fill(shape);

        g.setPaint(new GradientPaint(0, 0, Theme.alpha(accent, 64),
                width, height, Theme.alpha(Theme.PURPLE, 12)));
        g.fill(shape);

        g.setColor(new Color(255, 255, 255, 16));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(1.5, 1.5,
                Math.max(0, width - 3), Math.max(0, height - 3), arc, arc));

        g.setPaint(new GradientPaint(24, 0, Theme.alpha(accent, 0),
                width / 2.0f, 0, Theme.alpha(accent, 210)));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new java.awt.geom.Line2D.Double(26, 2, width - 26, 2));

        g.setColor(Theme.alpha(accent, 22));
        g.fill(new RoundRectangle2D.Double(0, 0, 5, height, 4, 4));
        g.dispose();
        super.paintComponent(graphics);
    }
}
