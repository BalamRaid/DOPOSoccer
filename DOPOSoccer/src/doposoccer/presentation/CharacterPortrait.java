package doposoccer.presentation;

import doposoccer.domain.character.CharacterDefinition;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Retrato circular y procedural de un personaje. Se reutiliza en el menu, en
 * la configuracion y en el HUD.
 *
 * <p>La identidad se obtiene de {@link Look}; no hay una clase por pais. Si en
 * el futuro se agregan fotografias o caricaturas, basta con sustituir este
 * dibujo por una imagen manteniendo el mismo componente.</p>
 */
@SuppressWarnings("serial")
class CharacterPortrait extends JComponent {

    private CharacterDefinition character;
    private Color ring = Theme.PURPLE;

    CharacterPortrait(CharacterDefinition character) {
        this.character = character;
        setOpaque(false);
    }

    void setCharacter(CharacterDefinition character) {
        this.character = character;
        repaint();
    }

    void setRingColor(Color ring) {
        this.ring = ring == null ? Theme.PURPLE : ring;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        paintPortrait(g, character, 0, 0, getWidth(), getHeight(), 1.0, ring, true);
        g.dispose();
    }

    /**
     * Dibuja un retrato en un cuadrado.
     *
     * @param g contexto grafico
     * @param character personaje que se dibuja
     * @param x coordenada izquierda
     * @param y coordenada superior
     * @param width ancho disponible
     * @param height alto disponible
     * @param alpha opacidad entre 0 y 1
     * @param ringColor color del anillo exterior
     * @param glow indica si se pinta un halo
     */
    static void paintPortrait(Graphics2D g, CharacterDefinition character,
            double x, double y, double width, double height, double alpha,
            Color ringColor, boolean glow) {
        if (width <= 1 || height <= 1) {
            return;
        }
        Composite previous = g.getComposite();
        if (alpha < 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) Math.max(0, Math.min(1, alpha))));
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        double size = Math.min(width, height);
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double radius = size * 0.46;
        Color jersey = Look.jersey(character);
        Color accent = Look.accent(character);
        Color aura = Look.aura(character);
        Color hair = Look.hair(character);
        Color skin = Look.skinTone(character);

        if (glow) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy),
                    (float) (radius * 1.32),
                    new float[]{0.52f, 1f},
                    new Color[]{Theme.alpha(aura, 70), Theme.alpha(aura, 0)}));
            g.fill(new Ellipse2D.Double(cx - radius * 1.3, cy - radius * 1.3,
                    radius * 2.6, radius * 2.6));
        }

        Ellipse2D disc = new Ellipse2D.Double(cx - radius, cy - radius,
                radius * 2, radius * 2);
        g.setPaint(new GradientPaint((float) (cx - radius), (float) (cy - radius),
                Theme.alpha(Theme.PANEL_LIGHT, 250),
                (float) (cx + radius), (float) (cy + radius),
                Theme.alpha(Theme.INK_SOFT, 250)));
        g.fill(disc);
        g.setPaint(new GradientPaint((float) (cx - radius), (float) (cy - radius),
                Theme.alpha(jersey, 80), (float) (cx + radius),
                (float) (cy + radius), Theme.alpha(Theme.PURPLE, 14)));
        g.fill(disc);

        // Hombros y camiseta.
        double shoulderTop = cy + radius * 0.45;
        Path2D shoulders = new Path2D.Double();
        shoulders.moveTo(cx - radius * 0.76, cy + radius * 0.94);
        shoulders.curveTo(cx - radius * 0.68, shoulderTop,
                cx - radius * 0.26, shoulderTop - radius * 0.04,
                cx, shoulderTop - radius * 0.04);
        shoulders.curveTo(cx + radius * 0.26, shoulderTop - radius * 0.04,
                cx + radius * 0.68, shoulderTop,
                cx + radius * 0.76, cy + radius * 0.94);
        shoulders.closePath();
        g.setPaint(new GradientPaint((float) cx, (float) shoulderTop,
                jersey, (float) cx, (float) (cy + radius), Theme.blend(jersey, Theme.INK, 0.45)));
        g.fill(shoulders);
        g.setColor(Theme.alpha(accent, 220));
        g.setStroke(new BasicStroke((float) Math.max(1, radius * 0.055)));
        g.draw(new Arc2D.Double(cx - radius * 0.23, shoulderTop - radius * 0.06,
                radius * 0.46, radius * 0.30, 200, 140, Arc2D.OPEN));

        // Cabeza, pelo y cara.
        double headR = radius * 0.68;
        Ellipse2D head = new Ellipse2D.Double(cx - headR, cy - headR * 1.08,
                headR * 2, headR * 2.08);
        g.setColor(Theme.blend(hair, Theme.INK, 0.18));
        g.fill(head);
        Ellipse2D face = new Ellipse2D.Double(cx - headR * 0.81,
                cy - headR * 0.63, headR * 1.62, headR * 1.64);
        g.setPaint(new GradientPaint((float) (cx - headR * 0.6),
                (float) (cy - headR * 0.6), Theme.blend(skin, Color.WHITE, 0.15),
                (float) cx, (float) (cy + headR), Theme.blend(skin, Theme.INK, 0.12)));
        g.fill(face);

        Ellipse2D leftEar = new Ellipse2D.Double(cx - headR * 0.91,
                cy - headR * 0.13, headR * 0.22, headR * 0.34);
        Ellipse2D rightEar = new Ellipse2D.Double(cx + headR * 0.69,
                cy - headR * 0.13, headR * 0.22, headR * 0.34);
        g.setColor(Theme.blend(skin, Theme.INK, 0.08));
        g.fill(leftEar);
        g.fill(rightEar);

        // Cinta deportiva con el color secundario del personaje.
        java.awt.Shape oldClip = g.getClip();
        g.clip(head);
        g.setColor(Theme.alpha(accent, 235));
        g.fill(new RoundRectangle2D.Double(cx - headR,
                cy - headR * 0.56, headR * 2, headR * 0.22, headR * 0.10, headR * 0.10));
        g.setColor(Theme.alpha(Theme.INK, 80));
        g.setStroke(new BasicStroke((float) Math.max(1, headR * 0.028)));
        g.drawLine((int) (cx - headR), (int) (cy - headR * 0.43),
                (int) (cx + headR), (int) (cy - headR * 0.43));
        g.setClip(oldClip);

        // Cejas, ojos y boca con una expresion decidida.
        g.setColor(Theme.blend(hair, Theme.INK, 0.25));
        g.setStroke(new BasicStroke((float) Math.max(1.2, headR * 0.055),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) (cx - headR * 0.47), (int) (cy - headR * 0.16),
                (int) (cx - headR * 0.14), (int) (cy - headR * 0.23));
        g.drawLine((int) (cx + headR * 0.14), (int) (cy - headR * 0.23),
                (int) (cx + headR * 0.47), (int) (cy - headR * 0.16));

        double eyeY = cy - headR * 0.01;
        double eyeR = headR * 0.145;
        g.setColor(new Color(252, 249, 255));
        g.fill(new Ellipse2D.Double(cx - headR * 0.40 - eyeR / 2, eyeY - eyeR / 2,
                eyeR, eyeR * 1.15));
        g.fill(new Ellipse2D.Double(cx + headR * 0.40 - eyeR / 2, eyeY - eyeR / 2,
                eyeR, eyeR * 1.15));
        g.setColor(new Color(28, 21, 34));
        g.fill(new Ellipse2D.Double(cx - headR * 0.40 - eyeR * 0.22,
                eyeY - eyeR * 0.14, eyeR * 0.48, eyeR * 0.67));
        g.fill(new Ellipse2D.Double(cx + headR * 0.40 - eyeR * 0.22,
                eyeY - eyeR * 0.14, eyeR * 0.48, eyeR * 0.67));
        g.setColor(new Color(255, 255, 255, 190));
        g.fill(new Ellipse2D.Double(cx - headR * 0.40 - eyeR * 0.05,
                eyeY - eyeR * 0.08, eyeR * 0.15, eyeR * 0.15));
        g.fill(new Ellipse2D.Double(cx + headR * 0.40 - eyeR * 0.05,
                eyeY - eyeR * 0.08, eyeR * 0.15, eyeR * 0.15));

        g.setColor(Theme.alpha(Theme.blend(skin, Theme.INK, 0.35), 190));
        g.setStroke(new BasicStroke((float) Math.max(1, headR * 0.04),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D mouth = new Path2D.Double();
        mouth.moveTo(cx - headR * 0.17, cy + headR * 0.35);
        mouth.quadTo(cx, cy + headR * 0.43, cx + headR * 0.21,
                cy + headR * 0.31);
        g.draw(mouth);
        g.setColor(new Color(255, 255, 255, 35));
        g.fill(new Ellipse2D.Double(cx - headR * 0.48, cy - headR * 0.50,
                headR * 0.22, headR * 0.12));

        g.setColor(Theme.alpha(ringColor, 205));
        g.setStroke(new BasicStroke((float) Math.max(1.4, radius * 0.035)));
        g.draw(new Ellipse2D.Double(cx - radius + 3, cy - radius + 3,
                radius * 2 - 6, radius * 2 - 6));
        g.setColor(Theme.alpha(Theme.TEXT, 34));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(cx - radius + 6.5, cy - radius + 6.5,
                radius * 2 - 13, radius * 2 - 13));
        g.setComposite(previous);
    }

}
