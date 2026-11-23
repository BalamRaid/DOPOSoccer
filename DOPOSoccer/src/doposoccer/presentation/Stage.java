package doposoccer.presentation;

import doposoccer.domain.match.Arena;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Escenario visual de la cancha. Define el fondo que se dibuja detras de los
 * jugadores y ofrece un punto de extension para usar fotografias reales.
 *
 * <p>Cada escenario funciona sin imagenes: si no existe un archivo en
 * {@code assets/stages/<nombre>.png|jpg|jpeg}, se dibuja una version
 * procedural. Para usar una foto de la universidad basta con colocar el
 * archivo con el nombre indicado y el tamaño recomendado; no hay que tocar el
 * dominio ni la logica del partido.</p>
 */
enum Stage {

    /** Estadio nocturno con tribunas y reflectores. */
    STADIUM("Estadio DOPO", "Noche de estadio", "estadio",
            new Color(8, 11, 30), new Color(45, 26, 74),
            new Color(220, 60, 95), "assets/stages/estadio"),

    /** Campus universitario de dia, con edificios y arboles. */
    CAMPUS("Campus Universitario", "Dia de partido", "campus",
            new Color(86, 170, 240), new Color(205, 235, 255),
            new Color(38, 126, 92), "assets/stages/campus"),

    /** Atardecer calido con montanas y cielo violeta. */
    SUNSET("Atardecer", "Final de temporada", "atardecer",
            new Color(54, 27, 82), new Color(255, 145, 88),
            new Color(255, 196, 92), "assets/stages/atardecer");

    private final String label;
    private final String subtitle;
    private final String key;
    private final Color skyTop;
    private final Color skyBottom;
    private final Color accent;
    private final String photoBase;
    private BufferedImage photo;
    private boolean photoLoaded;

    Stage(String label, String subtitle, String key, Color skyTop,
            Color skyBottom, Color accent, String photoBase) {
        this.label = label;
        this.subtitle = subtitle;
        this.key = key;
        this.skyTop = skyTop;
        this.skyBottom = skyBottom;
        this.accent = accent;
        this.photoBase = photoBase;
    }

    String label() {
        return label;
    }

    /** Etiqueta compacta para las tarjetas del selector. */
    String shortLabel() {
        return switch (this) {
            case STADIUM -> "ESTADIO";
            case CAMPUS -> "CAMPUS";
            case SUNSET -> "ATARDECER";
        };
    }

    String subtitle() {
        return subtitle;
    }

    String key() {
        return key;
    }

    Color accent() {
        return accent;
    }

    /**
     * Devuelve la fotografia opcional del escenario. La primera busqueda se
     * hace relativa a la carpeta de trabajo del proyecto.
     *
     * @return imagen cargada, o {@code null} si se usa el dibujo procedural
     */
    BufferedImage photo() {
        if (!photoLoaded) {
            photo = loadPhoto();
            photoLoaded = true;
        }
        return photo;
    }

    boolean hasPhoto() {
        return photo() != null;
    }

    private BufferedImage loadPhoto() {
        String[] extensions = {".png", ".jpg", ".jpeg"};
        for (String extension : extensions) {
            File file = new File(photoBase + extension);
            if (file.isFile()) {
                BufferedImage loaded = read(file);
                if (loaded != null) {
                    return loaded;
                }
            }
        }
        for (String extension : extensions) {
            URL resource = Stage.class.getResource("/" + photoBase + extension);
            if (resource != null) {
                try {
                    BufferedImage loaded = ImageIO.read(resource);
                    if (loaded != null) {
                        return loaded;
                    }
                } catch (IOException ignored) {
                    // Se intenta la siguiente extension.
                }
            }
        }
        return null;
    }

    private static BufferedImage read(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Dibuja el fondo completo del escenario hasta el suelo de la cancha. El
     * cesped y las lineas los pinta {@link GameRenderer}.
     *
     * @param g contexto grafico en coordenadas logicas de la cancha
     * @param arena cancha que define el ancho y el suelo
     * @param time tiempo de animacion en segundos
     */
    void paintBackdrop(Graphics2D g, Arena arena, double time) {
        int width = Math.max(1, (int) Math.ceil(arena.width()));
        int ground = Math.max(1, (int) Math.ceil(arena.groundY()));
        BufferedImage image = photo();
        if (image != null) {
            paintPhoto(g, image, width, ground);
        } else {
            switch (this) {
                case STADIUM -> paintStadium(g, width, ground, time);
                case CAMPUS -> paintCampus(g, width, ground, time);
                case SUNSET -> paintSunset(g, width, ground, time);
            }
        }
        paintVignette(g, width, ground);
    }

    private void paintPhoto(Graphics2D g, BufferedImage image, int width,
            int ground) {
        double scale = Math.max(width / (double) image.getWidth(),
                ground / (double) image.getHeight());
        double drawWidth = image.getWidth() * scale;
        double drawHeight = image.getHeight() * scale;
        double x = (width - drawWidth) / 2.0;
        double y = (ground - drawHeight) / 2.0;
        g.drawImage(image, (int) Math.round(x), (int) Math.round(y),
                (int) Math.round(drawWidth), (int) Math.round(drawHeight), null);
        g.setPaint(new GradientPaint(0, 0, new Color(2, 7, 18, 58),
                0, ground, new Color(2, 7, 18, 118)));
        g.fillRect(0, 0, width, ground);
        g.setPaint(new GradientPaint(0, ground - 110,
                new Color(3, 9, 18, 0), 0, ground, new Color(3, 9, 18, 175)));
        g.fillRect(0, Math.max(0, ground - 110), width, 110);
        g.setColor(Theme.alpha(accent, 44));
        g.fillRect(0, 0, width, 4);
    }

    private void paintStadium(Graphics2D g, int width, int ground, double time) {
        g.setPaint(new GradientPaint(0, 0, skyTop, 0, ground * 0.8f, skyBottom));
        g.fillRect(0, 0, width, ground);

        for (int i = 0; i < 58; i++) {
            double x = hash(i * 7 + 3) * width;
            double y = hash(i * 11 + 9) * ground * 0.42;
            double twinkle = 0.35 + 0.65 * (0.5 + 0.5
                    * Math.sin(time * (0.5 + hash(i) * 1.5) + i));
            g.setColor(new Color(255, 246, 214, (int) (18 + 54 * twinkle)));
            g.fill(new Ellipse2D.Double(x, y, 1.6, 1.6));
        }

        paintFloodlight(g, width * 0.13, ground * 0.30, width, ground, true);
        paintFloodlight(g, width * 0.87, ground * 0.30, width, ground, false);

        int standTop = (int) (ground * 0.38);
        int standBottom = (int) (ground * 0.80);
        g.setPaint(new GradientPaint(0, standTop, new Color(20, 17, 38),
                0, standBottom, new Color(11, 10, 23)));
        g.fillRect(0, standTop, width, standBottom - standTop);
        g.setColor(new Color(0, 0, 0, 95));
        g.fillRect(0, standTop, width, 10);
        g.setColor(Theme.alpha(accent, 50));
        g.fillRect(0, standTop, width, 2);

        for (int row = 0; row < 7; row++) {
            double y = standTop + 18 + row * (standBottom - standTop - 24) / 7.0;
            g.setColor(new Color(255, 255, 255, row % 2 == 0 ? 24 : 14));
            g.fillRect(0, (int) y + 13, width, 1);
            g.setColor(new Color(0, 0, 0, 24));
            g.fillRect(0, (int) y + 14, width, 2);
            int dots = 74;
            for (int i = 0; i < dots; i++) {
                double x = (i + 0.5) * width / dots + (hash(row * 97 + i) - 0.5) * 8;
                Color shirt = switch ((int) (hash(i * 13 + row * 29) * 6)) {
                    case 0 -> new Color(255, 82, 116);
                    case 1 -> new Color(164, 112, 255);
                    case 2 -> new Color(255, 211, 96);
                    case 3 -> new Color(94, 218, 255);
                    case 4 -> new Color(69, 219, 129);
                    default -> new Color(238, 233, 248);
                };
                int size = 3 + (int) (hash(i + row) * 3);
                g.setColor(Theme.alpha(shirt, 160 + (int) (hash(i * 3 + row) * 85)));
                g.fill(new Ellipse2D.Double(x, y, size, size));
            }
        }
        g.setColor(new Color(255, 255, 255, 14));
        for (int i = 0; i < 9; i++) {
            double x = i * width / 9.0;
            g.fillRect((int) x, standTop + 10, 1,
                    standBottom - standTop - 20);
        }

        g.setColor(new Color(32, 24, 52));
        g.fillRect(0, standBottom, width, (int) (ground * 0.06));
        g.setColor(Theme.alpha(accent, 115));
        g.fillRect(0, standBottom + 3, width, 3);
        for (int i = 0; i < 18; i++) {
            double x = i * width / 18.0;
            g.setColor(i % 2 == 0 ? Theme.alpha(Theme.CRIMSON, 95)
                    : Theme.alpha(Theme.PURPLE, 95));
            g.fill(new RoundRectangle2D.Double(x + 5, standBottom + 13,
                    width / 18.0 - 10, 13, 4, 4));
        }

        int wallTop = standBottom + 30;
        g.setPaint(new GradientPaint(0, wallTop, new Color(16, 14, 31),
                0, ground, new Color(8, 8, 18)));
        g.fillRect(0, wallTop, width, ground - wallTop);
        g.setColor(new Color(255, 255, 255, 16));
        for (int i = 0; i < 24; i++) {
            g.fillRect((int) (i * width / 24.0), wallTop, 1,
                    ground - wallTop);
        }
        String[] ads = {"DOPO", "ARCADE", "CAMPUS", "GOL", "UNIVERSIDAD",
            "60 SEG", "PODER", "VS"};
        Color[] adColors = {Theme.CRIMSON, Theme.PURPLE, Theme.CYAN,
            Theme.GOLD, Theme.GREEN, Theme.RED, Theme.VIOLET, Theme.CYAN};
        int boardY = wallTop + 14;
        for (int i = 0; i < 12; i++) {
            double x = i * width / 12.0 + 4;
            double boardWidth = width / 12.0 - 8;
            g.setColor(Theme.alpha(adColors[i % adColors.length], 32));
            g.fill(new RoundRectangle2D.Double(x, boardY, boardWidth, 18, 5, 5));
            g.setColor(Theme.alpha(adColors[i % adColors.length], 95));
            g.draw(new RoundRectangle2D.Double(x, boardY, boardWidth, 18, 5, 5));
            g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF,
                    java.awt.Font.BOLD, 7));
            g.setColor(Theme.alpha(Theme.TEXT, 120));
            Theme.drawCentered(g, ads[i % ads.length],
                    (float) (x + boardWidth / 2), (float) (boardY + 12),
                    new java.awt.Font(java.awt.Font.SANS_SERIF,
                            java.awt.Font.BOLD, 7), Theme.alpha(Theme.TEXT, 120));
        }

        g.setPaint(new GradientPaint(0, (float) (ground * 0.86),
                new Color(6, 24, 22, 0), 0, ground, new Color(4, 18, 18, 190)));
        g.fillRect(0, (int) (ground * 0.86), width, ground);
    }

    private void paintFloodlight(Graphics2D g, double x, double lampY,
            int width, int ground, boolean left) {
        double direction = left ? 1 : -1;
        Path2D beam = new Path2D.Double();
        beam.moveTo(x - 26, lampY);
        beam.lineTo(x + 26, lampY);
        beam.lineTo(x + direction * width * 0.32 + 120, ground);
        beam.lineTo(x + direction * width * 0.08 - 120, ground);
        beam.closePath();
        g.setPaint(new GradientPaint((float) x, (float) lampY,
                new Color(255, 245, 214, 32), (float) x, ground,
                new Color(255, 245, 214, 0)));
        g.fill(beam);

        g.setColor(new Color(12, 14, 27));
        g.fill(new RoundRectangle2D.Double(x - 4, lampY, 8, ground - lampY, 4, 4));
        g.setColor(new Color(24, 26, 43));
        g.fill(new RoundRectangle2D.Double(x - 30, lampY - 12, 60, 13, 5, 5));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, lampY - 4),
                90f, new float[]{0f, 1f},
                new Color[]{new Color(255, 246, 211, 90),
                    new Color(255, 246, 211, 0)}));
        g.fill(new Ellipse2D.Double(x - 90, lampY - 94, 180, 180));
        for (int i = -2; i <= 2; i++) {
            g.setColor(new Color(255, 250, 226, 230));
            g.fill(new Ellipse2D.Double(x + i * 10 - 3.5, lampY - 8, 7, 5));
        }
    }

    private void paintCampus(Graphics2D g, int width, int ground, double time) {
        g.setPaint(new GradientPaint(0, 0, skyTop, 0, ground * 0.88f, skyBottom));
        g.fillRect(0, 0, width, ground);

        g.setPaint(new RadialGradientPaint(new Point2D.Double(width * 0.80,
                ground * 0.17), width * 0.22f, new float[]{0f, 0.35f, 1f},
                new Color[]{new Color(255, 250, 206, 210),
                    new Color(255, 244, 180, 50),
                    new Color(255, 244, 180, 0)}));
        g.fillRect(0, 0, width, (int) (ground * 0.55));
        g.setColor(new Color(255, 250, 214, 235));
        g.fill(new Ellipse2D.Double(width * 0.80 - 27, ground * 0.17 - 27, 54, 54));

        paintCloud(g, width * 0.16 + Math.sin(time * 0.04) * 16, ground * 0.15, 1.05);
        paintCloud(g, width * 0.48 + Math.sin(time * 0.03 + 2) * 20, ground * 0.10, 0.72);
        paintCloud(g, width * 0.68 + Math.sin(time * 0.05 + 1) * 12, ground * 0.25, 0.85);

        g.setPaint(new GradientPaint(0, ground * 0.55f, new Color(89, 165, 128),
                0, ground, new Color(45, 116, 84)));
        Path2D hills = new Path2D.Double();
        hills.moveTo(0, ground * 0.70);
        hills.curveTo(width * 0.18, ground * 0.55, width * 0.30, ground * 0.66,
                width * 0.46, ground * 0.58);
        hills.curveTo(width * 0.64, ground * 0.49, width * 0.82, ground * 0.65,
                width, ground * 0.56);
        hills.lineTo(width, ground);
        hills.lineTo(0, ground);
        hills.closePath();
        g.fill(hills);

        paintBuilding(g, width * 0.08, ground * 0.33, width * 0.22,
                ground * 0.48, new Color(58, 80, 112), new Color(255, 214, 126), 6);
        paintBuilding(g, width * 0.33, ground * 0.24, width * 0.30,
                ground * 0.57, new Color(78, 95, 122), new Color(255, 230, 158), 8);
        paintBuilding(g, width * 0.68, ground * 0.37, width * 0.20,
                ground * 0.44, new Color(63, 86, 110), new Color(255, 214, 126), 5);

        g.setColor(new Color(235, 229, 211));
        g.fill(new RoundRectangle2D.Double(width * 0.455, ground * 0.28,
                width * 0.09, 18, 8, 8));
        Theme.drawCentered(g, "CAMPUS", (float) (width * 0.50),
                (float) (ground * 0.28 + 13), Theme.font(10, java.awt.Font.BOLD),
                new Color(32, 47, 70));

        paintTree(g, width * 0.03, ground * 0.82, 38);
        paintTree(g, width * 0.16, ground * 0.86, 28);
        paintTree(g, width * 0.95, ground * 0.81, 42);
        paintTree(g, width * 0.86, ground * 0.87, 26);

        g.setPaint(new GradientPaint(0, ground * 0.86f, new Color(25, 72, 57, 0),
                0, ground, new Color(16, 55, 44, 120)));
        g.fillRect(0, (int) (ground * 0.86), width, ground);
    }

    private void paintCloud(Graphics2D g, double x, double y, double scale) {
        g.setColor(new Color(255, 255, 255, 185));
        g.fill(new Ellipse2D.Double(x, y, 92 * scale, 30 * scale));
        g.fill(new Ellipse2D.Double(x + 24 * scale, y - 15 * scale,
                52 * scale, 38 * scale));
        g.fill(new Ellipse2D.Double(x + 58 * scale, y + 2 * scale,
                42 * scale, 24 * scale));
    }

    private void paintBuilding(Graphics2D g, double x, double y, double width,
            double height, Color wall, Color window, int columns) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new RoundRectangle2D.Double(x + 8, y + 10, width, height, 10, 10));
        g.setColor(wall);
        g.fill(new RoundRectangle2D.Double(x, y, width, height, 10, 10));
        g.setColor(Theme.blend(wall, Color.WHITE, 0.12));
        g.fill(new RoundRectangle2D.Double(x + 6, y + 6, width - 12, 9, 6, 6));
        int rows = Math.max(2, (int) (height / 42));
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                double wx = x + 15 + column * (width - 30) / columns;
                double wy = y + 28 + row * (height - 44) / rows;
                g.setColor(new Color(window.getRed(), window.getGreen(),
                        window.getBlue(), 175));
                g.fill(new RoundRectangle2D.Double(wx, wy,
                        Math.max(6, (width - 30) / columns - 9),
                        Math.max(8, (height - 44) / rows - 11), 3, 3));
            }
        }
    }

    private void paintTree(Graphics2D g, double x, double groundY, double size) {
        g.setColor(new Color(76, 58, 42));
        g.fill(new RoundRectangle2D.Double(x - size * 0.08, groundY - size,
                size * 0.16, size, 3, 3));
        g.setColor(new Color(42, 116, 76));
        g.fill(new Ellipse2D.Double(x - size * 0.48, groundY - size * 1.55,
                size * 0.96, size * 0.95));
        g.setColor(new Color(59, 145, 91));
        g.fill(new Ellipse2D.Double(x - size * 0.30, groundY - size * 1.80,
                size * 0.62, size * 0.78));
    }

    private void paintSunset(Graphics2D g, int width, int ground, double time) {
        g.setPaint(new GradientPaint(0, 0, skyTop, 0, ground * 0.78f, skyBottom));
        g.fillRect(0, 0, width, ground);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(width * 0.68,
                ground * 0.56), width * 0.24f, new float[]{0f, 0.38f, 1f},
                new Color[]{new Color(255, 226, 150, 230),
                    new Color(255, 178, 94, 75),
                    new Color(255, 150, 90, 0)}));
        g.fillRect(0, 0, width, ground);
        g.setColor(new Color(255, 242, 196, 240));
        g.fill(new Ellipse2D.Double(width * 0.68 - 48, ground * 0.56 - 48,
                96, 96));

        for (int i = 0; i < 6; i++) {
            double y = ground * (0.16 + i * 0.055);
            g.setColor(new Color(255, 229, 190, 26 - i * 3));
            g.fillRect(0, (int) y, width, 9 + i * 4);
        }

        Path2D back = new Path2D.Double();
        back.moveTo(0, ground * 0.78);
        back.lineTo(width * 0.16, ground * 0.55);
        back.lineTo(width * 0.30, ground * 0.70);
        back.lineTo(width * 0.46, ground * 0.48);
        back.lineTo(width * 0.66, ground * 0.72);
        back.lineTo(width * 0.82, ground * 0.60);
        back.lineTo(width, ground * 0.76);
        back.lineTo(width, ground);
        back.lineTo(0, ground);
        back.closePath();
        g.setColor(new Color(60, 35, 78));
        g.fill(back);

        Path2D front = new Path2D.Double();
        front.moveTo(0, ground * 0.88);
        front.lineTo(width * 0.18, ground * 0.76);
        front.lineTo(width * 0.36, ground * 0.84);
        front.lineTo(width * 0.54, ground * 0.74);
        front.lineTo(width * 0.74, ground * 0.86);
        front.lineTo(width, ground * 0.78);
        front.lineTo(width, ground);
        front.lineTo(0, ground);
        front.closePath();
        g.setColor(new Color(35, 22, 52));
        g.fill(front);

        g.setColor(new Color(255, 226, 175, 150));
        for (int i = 0; i < 5; i++) {
            double x = width * (0.14 + i * 0.17) + Math.sin(time * 0.7 + i) * 10;
            double y = ground * (0.23 + (i % 3) * 0.05);
            g.drawLine((int) x - 7, (int) y, (int) x, (int) y - 4);
            g.drawLine((int) x, (int) y - 4, (int) x + 7, (int) y);
        }
        g.setPaint(new GradientPaint(0, ground * 0.86f, new Color(30, 18, 40, 0),
                0, ground, new Color(18, 10, 26, 130)));
        g.fillRect(0, (int) (ground * 0.86), width, ground);
    }

    private void paintVignette(Graphics2D g, int width, int ground) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(width / 2.0,
                ground * 0.55), Math.max(width, ground) * 0.78f,
                new float[]{0.48f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 105)}));
        g.fillRect(0, 0, width, ground);
    }

    /**
     * Dibuja una miniatura del escenario para el selector de la configuracion.
     *
     * @param g contexto grafico
     * @param width ancho de la miniatura
     * @param height alto de la miniatura
     */
    void paintPreview(Graphics2D g, int width, int height) {
        if (width <= 2 || height <= 2) {
            return;
        }
        int ground = Math.max(4, (int) (height * 0.78));
        BufferedImage image = photo();
        if (image != null) {
            double scale = Math.max(width / (double) image.getWidth(),
                    ground / (double) image.getHeight());
            int drawWidth = (int) Math.round(image.getWidth() * scale);
            int drawHeight = (int) Math.round(image.getHeight() * scale);
            g.drawImage(image, (width - drawWidth) / 2,
                    (ground - drawHeight) / 2, drawWidth, drawHeight, null);
            g.setColor(new Color(2, 7, 18, 70));
            g.fillRect(0, 0, width, ground);
        } else {
            g.setPaint(new GradientPaint(0, 0, skyTop, 0, ground, skyBottom));
            g.fillRect(0, 0, width, ground);
            g.setColor(new Color(16, 13, 29, 130));
            g.fillRect(0, (int) (ground * 0.46), width, (int) (ground * 0.34));
            for (int i = 0; i < 22; i++) {
                double x = (i + 0.5) * width / 22.0;
                double y = ground * (0.52 + 0.20 * hash(i + 7));
                g.setColor(Theme.alpha(switch (i % 4) {
                    case 0 -> Theme.CRIMSON;
                    case 1 -> Theme.PURPLE;
                    case 2 -> Theme.GOLD;
                    default -> Theme.CYAN;
                }, 150));
                g.fill(new Ellipse2D.Double(x, y, 2.2, 2.2));
            }
            if (this == CAMPUS) {
                g.setColor(new Color(46, 68, 94));
                g.fillRect(width / 10, (int) (ground * 0.34), (int) (width * 0.26),
                        (int) (ground * 0.34));
                g.fillRect((int) (width * 0.48), (int) (ground * 0.25),
                        (int) (width * 0.30), (int) (ground * 0.43));
            } else if (this == SUNSET) {
                g.setColor(new Color(255, 238, 180, 220));
                g.fill(new Ellipse2D.Double(width * 0.62, ground * 0.28, 17, 17));
            } else {
                g.setColor(new Color(255, 246, 211, 180));
                g.fill(new Ellipse2D.Double(width * 0.10, ground * 0.20, 9, 5));
                g.fill(new Ellipse2D.Double(width * 0.82, ground * 0.20, 9, 5));
            }
        }
        g.setColor(new Color(48, 132, 86));
        g.fillRect(0, ground, width, height - ground);
        g.setColor(new Color(44, 157, 101));
        for (int i = 0; i < width; i += 12) {
            g.fillRect(i, ground, 6, height - ground);
        }
        g.setColor(new Color(255, 255, 255, 110));
        g.fillRect(0, ground, width, 1);
        g.setColor(new Color(255, 255, 255, 160));
        g.fillRect(width / 2, ground, 1, height - ground);
    }

    private static double hash(int value) {
        double x = Math.sin(value * 12.9898 + 78.233) * 43758.5453;
        return x - Math.floor(x);
    }

    @Override
    public String toString() {
        return label;
    }
}
