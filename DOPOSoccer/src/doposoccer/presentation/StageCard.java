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
import java.awt.geom.RoundRectangle2D;

/**
 * Tarjeta seleccionable con una miniatura del escenario. Acompana al selector
 * de canchas de la pantalla de configuracion.
 */
@SuppressWarnings("serial")
class StageCard extends JToggleButton {

    private final Stage stage;
    private boolean hovered;

    StageCard(Stage stage) {
        this.stage = stage;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(72, 60));
        setMaximumSize(new Dimension(120, 70));
        setToolTipText(stage.label() + " - " + stage.subtitle());
        getAccessibleContext().setAccessibleName(stage.label());
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

    Stage stage() {
        return stage;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        boolean selected = isSelected();
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), 14, 14);

        g.setPaint(new GradientPaint(0, 0,
                Theme.alpha(selected ? Theme.PANEL_LIGHT : Theme.PANEL_SOFT, 235),
                0, height, Theme.alpha(Theme.INK_SOFT, 245)));
        g.fill(shape);
        if (selected) {
            Theme.drawGlow(g, shape, stage.accent(), 4);
        }
        g.setColor(selected ? Theme.alpha(stage.accent(), 220)
                : new Color(255, 255, 255, hovered ? 58 : 30));
        g.setStroke(new BasicStroke(selected ? 1.8f : 1f));
        g.draw(shape);

        // La foto ocupa toda la tarjeta; el nombre va encima, en la parte baja.
        int previewX = 4;
        int previewY = 4;
        int previewWidth = width - 8;
        int previewHeight = height - 8;
        java.awt.Shape oldClip = g.getClip();
        g.clip(new RoundRectangle2D.Double(previewX, previewY, previewWidth,
                previewHeight, 9, 9));
        Graphics2D preview = (Graphics2D) g.create();
        preview.translate(previewX, previewY);
        stage.paintPreview(preview, previewWidth, previewHeight);
        preview.dispose();
        g.setClip(oldClip);
        g.setColor(new Color(255, 255, 255, 32));
        g.draw(new RoundRectangle2D.Double(previewX, previewY, previewWidth,
                previewHeight, 9, 9));

        g.setPaint(new GradientPaint(0, height * 0.45f, new Color(0, 0, 0, 0),
                0, height - 4, new Color(0, 0, 0, 200)));
        g.fill(new RoundRectangle2D.Double(previewX, previewY, previewWidth,
                previewHeight, 9, 9));
        g.setFont(Theme.font(9, Font.BOLD));
        FontMetrics metrics = g.getFontMetrics();
        String text = stage.shortLabel();
        g.setColor(selected ? Theme.TEXT : new Color(225, 225, 235));
        g.drawString(text, (width - metrics.stringWidth(text)) / 2, height - 9);
        g.dispose();
    }
}
