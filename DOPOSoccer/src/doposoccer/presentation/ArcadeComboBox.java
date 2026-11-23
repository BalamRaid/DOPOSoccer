package doposoccer.presentation;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Selector desplegable oscuro y redondeado. Se usa para modalidad, resolucion,
 * personajes y meta de goles.
 *
 * @param <E> tipo de elemento del selector
 */
@SuppressWarnings("serial")
class ArcadeComboBox<E> extends JComboBox<E> {

    ArcadeComboBox(E[] items) {
        super(items);
        setOpaque(false);
        setBackground(Theme.PANEL_SOFT);
        setForeground(Theme.TEXT);
        setFont(Theme.font(13, Font.BOLD));
        setBorder(new EmptyBorder(5, 12, 5, 4));
        setMaximumRowCount(6);
        setFocusable(false);
        setPreferredSize(new Dimension(260, 38));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        setRenderer(new Renderer());
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        Path2D path = new Path2D.Double();
                        path.moveTo(getWidth() / 2.0 - 5, getHeight() / 2.0 - 2);
                        path.lineTo(getWidth() / 2.0 + 5, getHeight() / 2.0 - 2);
                        path.lineTo(getWidth() / 2.0, getHeight() / 2.0 + 3);
                        path.closePath();
                        g.setColor(Theme.MUTED);
                        g.fill(path);
                        g.dispose();
                    }
                };
                arrow.setOpaque(false);
                arrow.setContentAreaFilled(false);
                arrow.setBorder(BorderFactory.createEmptyBorder());
                arrow.setPreferredSize(new Dimension(28, 24));
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g,
                    java.awt.Rectangle bounds, boolean hasFocus) {
                // El fondo se pinta en paintComponent para respetar las
                // esquinas redondeadas.
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
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), 15, 15);
        g.setPaint(new GradientPaint(0, 0,
                Theme.alpha(Theme.PANEL_SOFT, isEnabled() ? 240 : 110),
                0, height,
                Theme.alpha(Theme.INK_PURPLE, isEnabled() ? 240 : 110)));
        g.fill(shape);
        g.setColor(isEnabled() ? Theme.alpha(Theme.PURPLE, 95)
                : new Color(255, 255, 255, 20));
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        if (!isEnabled()) {
            g.setColor(new Color(255, 255, 255, 8));
            g.fill(shape);
        }
        g.dispose();
        super.paintComponent(graphics);
    }

    private final class Renderer extends DefaultListCellRenderer
            implements ListCellRenderer<Object> {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            label.setOpaque(false);
            label.setFont(Theme.font(13, index < 0 ? Font.BOLD : Font.PLAIN));
            label.setForeground(ArcadeComboBox.this.isEnabled() ? Theme.TEXT : Theme.DIM);
            label.setBorder(new EmptyBorder(6, 10, 6, 10));
            if (isSelected && index >= 0) {
                label.setOpaque(true);
                label.setBackground(Theme.CRIMSON);
                label.setForeground(Color.WHITE);
            }
            return label;
        }
    }
}
