package doposoccer.presentation;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Contador compacto con botones menos y mas. Se usa para elegir la cantidad de
 * balones de supervivencia sin depender del {@code JSpinner} clasico.
 */
@SuppressWarnings("serial")
class ArcadeStepper extends JPanel {

    private final int minimum;
    private final int maximum;
    private final int step;
    private final JLabel valueLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton minus = new StepButton("-");
    private final JButton plus = new StepButton("+");
    private int value;

    /**
     * Crea el contador con un valor inicial y un rango inclusivo.
     *
     * @param value valor inicial
     * @param minimum valor minimo permitido
     * @param maximum valor maximo permitido
     * @param step incremento de cada pulsacion
     */
    ArcadeStepper(int value, int minimum, int maximum, int step) {
        super(new BorderLayout());
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = Math.max(1, step);
        this.value = clamp(value);
        setOpaque(false);
        setPreferredSize(new Dimension(128, 38));
        setMaximumSize(new Dimension(180, 38));
        valueLabel.setFont(Theme.font(15, Font.BOLD));
        valueLabel.setForeground(Theme.TEXT);
        valueLabel.setText(String.valueOf(this.value));
        minus.addActionListener(e -> change(-this.step));
        plus.addActionListener(e -> change(this.step));
        add(minus, BorderLayout.WEST);
        add(valueLabel, BorderLayout.CENTER);
        add(plus, BorderLayout.EAST);
        refreshButtons();
    }

    /**
     * Devuelve el valor actual.
     *
     * @return valor seleccionado
     */
    int getValue() {
        return value;
    }

    private void change(int delta) {
        int next = clamp(value + delta);
        if (next != value) {
            value = next;
            valueLabel.setText(String.valueOf(value));
            refreshButtons();
        }
    }

    private int clamp(int candidate) {
        return Math.max(minimum, Math.min(maximum, candidate));
    }

    private void refreshButtons() {
        minus.setEnabled(isEnabled() && value > minimum);
        plus.setEnabled(isEnabled() && value < maximum);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        valueLabel.setForeground(enabled ? Theme.TEXT : Theme.DIM);
        refreshButtons();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1,
                Math.max(0, width - 2), Math.max(0, height - 2), 14, 14);
        g.setPaint(new GradientPaint(0, 0,
                Theme.alpha(Theme.PANEL_SOFT, isEnabled() ? 235 : 105),
                0, height,
                Theme.alpha(Theme.INK_PURPLE, isEnabled() ? 235 : 105)));
        g.fill(shape);
        g.setColor(isEnabled() ? Theme.alpha(Theme.PURPLE, 80)
                : new Color(255, 255, 255, 18));
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        g.dispose();
        super.paintComponent(graphics);
    }

    /** Boton cuadrado que dibuja solo su simbolo y responde al hover. */
    private static final class StepButton extends JButton {

        private boolean hovered;

        StepButton(String text) {
            super(text);
            setFont(Theme.font(16, Font.BOLD));
            setForeground(Theme.TEXT);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(36, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
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
            if (hovered && isEnabled()) {
                g.setColor(new Color(255, 255, 255, 16));
                g.fill(new RoundRectangle2D.Double(2, 4, getWidth() - 4, getHeight() - 8, 9, 9));
            }
            g.setFont(getFont());
            FontMetrics metrics = g.getFontMetrics();
            String text = getText();
            int x = (getWidth() - metrics.stringWidth(text)) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.setColor(isEnabled() ? Theme.TEXT : Theme.DIM);
            g.drawString(text, x, y);
            g.dispose();
        }
    }
}
