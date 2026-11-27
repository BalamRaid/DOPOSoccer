package doposoccer.presentation;

import doposoccer.domain.Stat;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Identidad visual compartida de DOPOSoccer.
 *
 * <p>Reune la paleta nocturna (negro, rojo y morado), las fuentes, los
 * degradados y las utilidades de dibujo que usan el menu, la configuracion y
 * la cancha. Todo se pinta con Java2D: no requiere imagenes ni dependencias
 * externas.</p>
 */
final class Theme {

    static final Color INK = new Color(8, 6, 13);
    static final Color INK_SOFT = new Color(17, 10, 25);
    static final Color INK_PURPLE = new Color(28, 15, 42);
    static final Color PANEL = new Color(24, 16, 34);
    static final Color PANEL_SOFT = new Color(36, 24, 50);
    static final Color PANEL_LIGHT = new Color(48, 33, 65);
    static final Color CRIMSON = new Color(220, 30, 66);
    static final Color RED = new Color(255, 62, 100);
    static final Color PURPLE = new Color(139, 92, 246);
    static final Color VIOLET = new Color(184, 92, 255);
    static final Color GOLD = new Color(255, 203, 92);
    static final Color TEXT = new Color(249, 246, 255);
    static final Color MUTED = new Color(184, 170, 202);
    static final Color DIM = new Color(118, 104, 136);
    static final Color LINE_STRONG = new Color(255, 255, 255, 62);
    static final Color GREEN = new Color(69, 219, 129);
    static final Color CYAN = new Color(94, 218, 255);
    static final Color FIELD_DARK = new Color(11, 58, 44);

    private static final String FAMILY = "SansSerif";
    private static final Color INVERSED_INK = new Color(43, 18, 51);
    private static final Map<Integer, Font> FONTS = new HashMap<>();

    private Theme() {
    }

    /**
     * Instala una apariencia base oscura y consistente para los controles
     * estandar de Swing. Los componentes propios se pintan aparte.
     */
    static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si el entorno no permite cambiar el LAF, los paneles propios
            // siguen siendo totalmente funcionales.
        }
        Font base = font(13, Font.PLAIN);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", base);
        UIManager.put("Panel.background", INK);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.background", PANEL_SOFT);
        UIManager.put("Button.font", font(13, Font.BOLD));
        UIManager.put("ToggleButton.foreground", TEXT);
        UIManager.put("ToggleButton.background", PANEL_SOFT);
        UIManager.put("RadioButton.foreground", TEXT);
        UIManager.put("RadioButton.background", INK);
        UIManager.put("ComboBox.background", PANEL_SOFT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.font", font(13, Font.BOLD));
        UIManager.put("ComboBox.selectionBackground", CRIMSON);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.background", PANEL_SOFT);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", CRIMSON);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("List.font", base);
        UIManager.put("PopupMenu.background", PANEL_SOFT);
        UIManager.put("PopupMenu.border", new LineBorder(LINE_STRONG));
        UIManager.put("ToolTip.background", new Color(46, 28, 58));
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ToolTip.font", base);
        UIManager.put("OptionPane.background", INK_SOFT);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", base);
        UIManager.put("OptionPane.buttonFont", font(13, Font.BOLD));
        UIManager.put("TextField.background", PANEL_SOFT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("Menu.foreground", TEXT);
        UIManager.put("Menu.background", INK_SOFT);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("MenuItem.background", INK_SOFT);
        UIManager.put("MenuBar.background", INK_SOFT);
        UIManager.put("MenuBar.foreground", TEXT);
        UIManager.put("Menu.font", base);
        UIManager.put("MenuItem.font", base);
    }

    /**
     * Devuelve una fuente de la familia base, reutilizando instancias para
     * evitar crear objetos en cada repintado.
     *
     * @param size tamano en puntos
     * @param style estilo de {@link Font}
     * @return fuente compartida
     */
    static Font font(int size, int style) {
        int key = (style << 16) ^ Math.max(0, size);
        return FONTS.computeIfAbsent(key,
                ignored -> new Font(FAMILY, style, Math.max(0, size)));
    }

    /**
     * Devuelve una fuente base en negrita para titulares.
     *
     * @param size tamano en puntos
     * @return fuente compartida
     */
    static Font display(int size) {
        return font(size, Font.BOLD);
    }

    static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, alpha)));
    }

    static Color blend(Color from, Color to, double amount) {
        double t = Math.max(0, Math.min(1, amount));
        return new Color(
                (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t));
    }

    static String statName(Stat stat) {
        return switch (stat) {
            case SPEED -> "Velocidad";
            case KICK -> "Patada";
            case JUMP -> "Salto";
            case SURVIVAL -> "Vida";
        };
    }

    static String statAbbreviation(Stat stat) {
        return switch (stat) {
            case SPEED -> "VEL";
            case KICK -> "PAT";
            case JUMP -> "SAL";
            case SURVIVAL -> "VID";
        };
    }

    static Color statColor(Stat stat) {
        return switch (stat) {
            case SPEED -> CYAN;
            case KICK -> RED;
            case JUMP -> GOLD;
            case SURVIVAL -> GREEN;
        };
    }

    static void drawCentered(Graphics2D g, String text, float centerX, float baseline,
            Font font, Color color) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, centerX - metrics.stringWidth(text) / 2.0f, baseline);
    }

    static void drawGlow(Graphics2D g, Shape shape, Color color, int layers) {
        for (int i = layers; i >= 1; i--) {
            g.setColor(alpha(color, 9 + i * 4));
            g.setStroke(new BasicStroke(i * 3.0f));
            g.draw(shape);
        }
    }

    static void paintAtmosphere(Graphics2D g, int width, int height,
            double phase, int seed) {
        if (width <= 0 || height <= 0) {
            return;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(7, 5, 12),
                0, height, new Color(22, 12, 34)));
        g.fillRect(0, 0, width, height);

        radial(g, width * 0.83, height * 0.08, width * 0.62,
                alpha(CRIMSON, 58));
        radial(g, width * 0.12, height * 0.82, width * 0.60,
                alpha(PURPLE, 48));
        radial(g, width * 0.55, height * 0.45, width * 0.45,
                alpha(INK_PURPLE, 105));

        for (int i = 0; i < 4; i++) {
            double sway = Math.sin(phase * 0.22 + i * 1.7) * 0.035;
            double top = width * (0.34 + i * 0.105) + width * sway;
            Path2D beam = new Path2D.Double();
            beam.moveTo(top, 0);
            beam.lineTo(top + width * 0.11, 0);
            beam.lineTo(top + width * 0.33, height);
            beam.lineTo(top - width * 0.06, height);
            beam.closePath();
            g.setPaint(new GradientPaint((float) top, 0,
                    alpha(i % 2 == 0 ? CRIMSON : PURPLE, 10),
                    (float) top, height, alpha(INK, 0)));
            g.fill(beam);
        }

        int dots = Math.max(34, width * height / 16000);
        for (int i = 0; i < dots; i++) {
            double x = noise(i * 3 + seed) * width;
            double y = noise(i * 7 + seed * 11) * height * 0.92;
            double size = 0.7 + noise(i * 13 + seed) * 1.9;
            double twinkle = 0.35 + 0.65 * (0.5 + 0.5
                    * Math.sin(phase * (0.7 + noise(i) * 1.3) + i));
            g.setColor(alpha(blend(MUTED, GOLD, noise(i * 5)), (int) (18 + 36 * twinkle)));
            g.fill(new Ellipse2D.Double(x, y, size, size));
        }

        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 4; i++) {
            double radius = width * (0.28 + i * 0.15);
            g.setColor(alpha(TEXT, 7 + i * 2));
            g.draw(new Ellipse2D.Double(width / 2.0 - radius,
                    height * 0.94 - radius * 0.42, radius * 2, radius * 0.84));
        }

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(width / 2.0, height / 2.0),
                Math.max(width, height) * 0.72f,
                new float[]{0.45f, 1f},
                new Color[]{alpha(INK, 0), alpha(INK, 180)}));
        g.fillRect(0, 0, width, height);
    }

    private static void radial(Graphics2D g, double x, double y, double radius,
            Color center) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y),
                (float) Math.max(1, radius),
                new float[]{0f, 1f},
                new Color[]{center, alpha(center, 0)}));
        g.fillRect(0, 0, (int) (x + radius), (int) (y + radius));
    }

    private static double noise(int n) {
        double value = Math.sin(n * 12.9898 + 78.233) * 43758.5453;
        return value - Math.floor(value);
    }

    /**
     * Genera el icono de la aplicacion y del logo sin depender de archivos.
     *
     * @param size lado del icono en pixeles
     * @return imagen lista para la ventana
     */
    static BufferedImage appIcon(int size) {
        BufferedImage image = new BufferedImage(size, size,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, INK_SOFT, size, size, INVERSED_INK));
        g.fillRoundRect(0, 0, size, size, size / 3, size / 3);
        g.setPaint(new GradientPaint(0, 0, CRIMSON, size, size, PURPLE));
        g.setStroke(new BasicStroke(Math.max(2, size / 28f)));
        g.drawRoundRect(2, 2, size - 5, size - 5, size / 3, size / 3);
        drawSoccerBall(g, size * 0.5, size * 0.5, size * 0.27, true);
        g.dispose();
        return image;
    }

    /**
     * Dibuja un balon estilizado reutilizable en el logo y en la cancha.
     *
     * @param g contexto grafico
     * @param cx centro horizontal
     * @param cy centro vertical
     * @param radius radio del balon
     * @param detail indica si se dibujan las costuras
     */
    static void drawSoccerBall(Graphics2D g, double cx, double cy,
            double radius, boolean detail) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - radius * 0.35,
                cy - radius * 0.45), (float) (radius * 1.45),
                new float[]{0f, 0.72f, 1f},
                new Color[]{Color.WHITE, new Color(228, 226, 235), new Color(150, 144, 167)}));
        g.fill(new Ellipse2D.Double(cx - radius, cy - radius,
                radius * 2, radius * 2));
        g.setColor(new Color(24, 20, 30));
        Path2D pentagon = new Path2D.Double();
        for (int i = 0; i < 5; i++) {
            double angle = -Math.PI / 2 + i * Math.PI * 2 / 5;
            double x = cx + Math.cos(angle) * radius * 0.36;
            double y = cy + Math.sin(angle) * radius * 0.36;
            if (i == 0) {
                pentagon.moveTo(x, y);
            } else {
                pentagon.lineTo(x, y);
            }
        }
        pentagon.closePath();
        g.fill(pentagon);
        if (detail) {
            g.setStroke(new BasicStroke((float) Math.max(1, radius * 0.06)));
            for (int i = 0; i < 5; i++) {
                double angle = -Math.PI / 2 + i * Math.PI * 2 / 5;
                g.draw(new java.awt.geom.Line2D.Double(
                        cx + Math.cos(angle) * radius * 0.36,
                        cy + Math.sin(angle) * radius * 0.36,
                        cx + Math.cos(angle) * radius * 0.84,
                        cy + Math.sin(angle) * radius * 0.84));
            }
            g.setColor(new Color(255, 255, 255, 105));
            g.setStroke(new BasicStroke((float) Math.max(1, radius * 0.05)));
            g.draw(new Ellipse2D.Double(cx - radius * 0.78, cy - radius * 0.78,
                    radius * 1.56, radius * 1.56));
        }
        g.setColor(new Color(30, 24, 38));
        g.setStroke(new BasicStroke((float) Math.max(1, radius * 0.07)));
        g.draw(new Ellipse2D.Double(cx - radius, cy - radius,
                radius * 2, radius * 2));
    }
}
