package doposoccer.presentation;

import doposoccer.application.GameMode;
import doposoccer.domain.Ball;
import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.Player;
import doposoccer.domain.Side;
import doposoccer.domain.Weather;
import doposoccer.domain.character.CharacterDefinition;
import doposoccer.domain.effect.Effect;
import doposoccer.domain.match.Arena;
import doposoccer.domain.match.Goal;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;

/**
 * Dibuja la cancha, los jugadores, el balon, el HUD y las animaciones de la
 * partida con un lenguaje visual arcade.
 *
 * <p>Es una clase de presentacion pura: no avanza el partido ni procesa
 * teclado. Recibe el agregado y las dimensiones de la ventana, y escala las
 * coordenadas logicas de la cancha. El escenario se elige en la configuracion
 * y el HUD se pinta en pixeles de pantalla para conservar su legibilidad.</p>
 */
final class GameRenderer {

    /** Altura en pixeles reservada para el HUD superior. */
    static final int HUD = 112;

    private static final double INTRO_SECONDS = 1.8;
    private static final double GOAL_SECONDS = 2.1;
    /** Velocidad del juego durante la camara lenta. */
    static final double SLOW_MOTION = 0.3;
    /** Segundos reales maximos de camara lenta por jugada. */
    static final double SLOW_MOTION_MAX = 1.2;
    private static final double SHAKE_SECONDS = 0.45;
    private static final double SHAKE_PIXELS = 9;
    private static final int RAIN_DROPS = 140;

    private final Stage stage;
    private final GameMode mode;
    private double animationTime;
    private double intro = INTRO_SECONDS;
    private double goalFlash;
    private Side goalSide;
    private int lastLeftGoals = -1;
    private int lastRightGoals = -1;
    private double shake;
    private double slowBudget = SLOW_MOTION_MAX;
    private boolean slowMotion;

    /**
     * Crea el renderizador para un escenario y una modalidad.
     *
     * @param stage escenario visual; si es {@code null} se usa el estadio
     * @param mode modalidad del partido; si es {@code null} se asume PVP
     */
    GameRenderer(Stage stage, GameMode mode) {
        this.stage = stage == null ? Stage.STADIUM : stage;
        this.mode = mode == null ? GameMode.PVP : mode;
    }

    /**
     * Avanza las animaciones independientes de la simulacion.
     *
     * @param dt tiempo transcurrido en segundos
     */
    void advance(double dt) {
        if (dt <= 0) {
            return;
        }
        animationTime += dt;
        if (intro > 0) {
            intro = Math.max(0, intro - dt);
        }
        if (goalFlash > 0) {
            goalFlash = Math.max(0, goalFlash - dt);
        }
        if (shake > 0) {
            shake = Math.max(0, shake - dt);
        }
    }

    /**
     * Factor de velocidad de la simulacion para este cuadro: camara lenta
     * mientras el balon va directo al arco, con un limite de tiempo real
     * para que una jugada no se quede lenta indefinidamente.
     *
     * @param game partido actual
     * @param frame segundos reales del cuadro
     * @return 1 a velocidad normal o {@link #SLOW_MOTION}
     */
    double timeScale(DOPOSoccer game, double frame) {
        if (game.isGoalImminent() && slowBudget > 0) {
            slowBudget -= frame;
            slowMotion = true;
            return SLOW_MOTION;
        }
        if (!game.isGoalImminent()) {
            slowBudget = SLOW_MOTION_MAX;
        }
        slowMotion = false;
        return 1;
    }

    boolean isSlowMotion() {
        return slowMotion;
    }

    boolean isIntroActive() {
        return intro > 0;
    }

    /**
     * Dibuja una fotografia completa del partido.
     *
     * @param g contexto grafico del panel
     * @param game partido que se va a dibujar
     * @param width ancho del panel en pixeles
     * @param height alto del panel en pixeles
     */
    void paint(Graphics2D g, DOPOSoccer game, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        detectGoal(game);

        int fieldHeight = Math.max(1, height - HUD);
        double scale = Math.min(width / game.getArena().width(),
                fieldHeight / game.getArena().height());
        double fieldWidth = game.getArena().width() * scale;
        double logicalHeight = game.getArena().height() * scale;
        double offsetX = (width - fieldWidth) / 2.0;
        double offsetY = HUD + (fieldHeight - logicalHeight) / 2.0;

        g.setPaint(new GradientPaint(0, 0, Theme.INK, 0, height,
                Theme.FIELD_DARK));
        g.fillRect(0, 0, width, height);
        if (stage.hasPhoto()) {
            // La foto llena toda el area bajo el marcador, no solo la cancha:
            // asi no quedan franjas vacias arriba, abajo ni a los lados.
            Graphics2D area = (Graphics2D) g.create();
            area.translate(0, HUD);
            area.clip(new Rectangle2D.Double(0, 0, width, fieldHeight));
            double floorY = offsetY - HUD + game.getArena().groundY() * scale;
            stage.paintPhotoArea(area, width, fieldHeight, floorY);
            area.dispose();
        }

        Graphics2D field = (Graphics2D) g.create();
        if (shake > 0) {
            // Temblor al anotar: se apaga a medida que pasa el tiempo.
            double amount = SHAKE_PIXELS * shake / SHAKE_SECONDS;
            field.translate(Math.sin(animationTime * 90) * amount,
                    Math.cos(animationTime * 70) * amount * 0.6);
        }
        field.translate(offsetX, offsetY);
        field.scale(scale, scale);
        if (!stage.hasPhoto()) {
            field.clip(new Rectangle2D.Double(0, 0, game.getArena().width(),
                    game.getArena().height()));
        }
        drawScene(field, game);
        field.dispose();
        drawWeather(g, game, width, height);
        if (slowMotion) {
            drawSlowMotion(g, width, height);
        }

        if (goalFlash > 0) {
            drawGoalCelebration(g, game, width, height);
        }
        drawHud(g, game, width);
        if (intro > 0) {
            drawIntro(g, width, height);
        }
        if (game.isPaused()) {
            drawPause(g, game, width, height);
        }
    }

    /**
     * Lluvia sobre toda el area de juego y, con viento, lineas de rafaga y un
     * indicador con la direccion y fuerza del viento.
     */
    private void drawWeather(Graphics2D g, DOPOSoccer game, int width, int height) {
        Weather weather = game.getWeather();
        int top = HUD;
        int area = Math.max(1, height - HUD);
        if (weather == Weather.RAIN) {
            g.setColor(new Color(210, 225, 255, 95));
            g.setStroke(new BasicStroke(1.3f));
            for (int i = 0; i < RAIN_DROPS; i++) {
                double x = (hash(i) * width + animationTime * 60) % width;
                double y = top + (hash(i + 500) * area + animationTime * (520 + i % 7 * 40)) % area;
                g.draw(new java.awt.geom.Line2D.Double(x, y, x - 3, y + 14));
            }
            g.setColor(new Color(20, 30, 50, 38));
            g.fillRect(0, top, width, area);
        }
        if (weather.hasWind()) {
            double wind = game.currentWind();
            double strength = Math.min(1, Math.abs(wind) / 260);
            double dir = Math.signum(wind);
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, (int) (25 + 60 * strength)));
            for (int i = 0; i < 14; i++) {
                double speed = 260 + i % 5 * 70;
                double travel = (hash(i + 900) * (width + 200) + animationTime * speed) % (width + 200);
                double x = dir >= 0 ? travel - 100 : width + 100 - travel;
                double y = top + 20 + hash(i + 1300) * area * 0.65;
                double len = 30 + 50 * strength;
                g.draw(new java.awt.geom.Line2D.Double(x, y, x - dir * len, y));
            }
            String arrows = strength < 0.15 ? "·" : (dir > 0 ? ">" : "<").repeat(1 + (int) (strength * 2.99));
            String text = "VIENTO  " + arrows;
            g.setFont(Theme.font(11, Font.BOLD));
            int textWidth = g.getFontMetrics().stringWidth(text);
            int x = width / 2 - textWidth / 2 - 10;
            g.setColor(new Color(0, 0, 0, 120));
            int labelY = top + 8 + (slowMotion ? slowBar(height) : 0);
            g.fill(new RoundRectangle2D.Double(x, labelY, textWidth + 20, 22, 11, 11));
            g.setColor(new Color(255, 255, 255, 220));
            g.drawString(text, x + 10, labelY + 15);
        }
    }

    private static int slowBar(int height) {
        return Math.max(18, (height - HUD) / 14);
    }

    /** Franjas de cine y una etiqueta durante la camara lenta. */
    private void drawSlowMotion(Graphics2D g, int width, int height) {
        int bar = slowBar(height);
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, HUD, width, bar);
        g.fillRect(0, height - bar, width, bar);
        g.setFont(Theme.font(11, Font.BOLD));
        g.setColor(new Color(255, 255, 255, 200));
        g.drawString("CAMARA LENTA", 16, HUD + bar / 2 + 4);
    }

    private void detectGoal(DOPOSoccer game) {
        int left = game.goalsOf(Side.LEFT);
        int right = game.goalsOf(Side.RIGHT);
        if (lastLeftGoals < 0 || lastRightGoals < 0) {
            lastLeftGoals = left;
            lastRightGoals = right;
            return;
        }
        if (left > lastLeftGoals || right > lastRightGoals) {
            goalSide = left > lastLeftGoals ? Side.LEFT : Side.RIGHT;
            goalFlash = GOAL_SECONDS;
            shake = SHAKE_SECONDS;
            slowMotion = false;
        }
        lastLeftGoals = left;
        lastRightGoals = right;
    }

    // ---------------- Escena ----------------

    private void drawScene(Graphics2D g, DOPOSoccer game) {
        Arena arena = game.getArena();
        if (!stage.hasPhoto()) {
            stage.paintBackdrop(g, arena, animationTime);
            drawPitch(g, arena);
        } else if (!stage.railFloor()) {
            drawPitchLines(g, arena);
        }
        drawActors(g, game, arena);
    }

    private void drawActors(Graphics2D g, DOPOSoccer game, Arena arena) {
        for (Goal goal : arena.goals()) {
            drawGoal(g, goal);
        }
        for (Ball fake : game.getFakeBalls()) {
            drawBall(g, arena, fake, false);
        }
        drawPlayer(g, game.getPlayer(Side.LEFT));
        drawPlayer(g, game.getPlayer(Side.RIGHT));
        drawBall(g, arena, game.getBall(), true);
    }

    /** Lineas de la cancha sobre el piso real de la foto. */
    private void drawPitchLines(Graphics2D g, Arena arena) {
        double width = arena.width();
        double ground = arena.groundY();
        double height = arena.height();
        g.setColor(new Color(255, 255, 255, 140));
        g.setStroke(new BasicStroke(2.4f));
        g.drawLine(0, (int) ground + 1, (int) width, (int) ground + 1);
        g.setColor(new Color(255, 255, 255, 70));
        g.drawLine((int) (width / 2), (int) ground, (int) (width / 2), (int) height);
    }

    private void drawPitch(Graphics2D g, Arena arena) {
        double width = arena.width();
        double ground = arena.groundY();
        double height = arena.height();

        g.setPaint(new GradientPaint(0, (float) ground, new Color(30, 142, 89),
                0, (float) height, new Color(10, 70, 50)));
        g.fill(new Rectangle2D.Double(0, ground, width, height - ground));

        double stripe = 44;
        for (double x = 0; x < width; x += stripe * 2) {
            g.setColor(new Color(255, 255, 255, 18));
            g.fill(new Rectangle2D.Double(x, ground, stripe, height - ground));
        }
        for (int i = 0; i < 90; i++) {
            double x = hash(i * 7 + 1) * width;
            double y = ground + 5 + hash(i * 13 + 4) * (height - ground - 8);
            g.setColor(new Color(255, 255, 255, 16));
            g.drawLine((int) x, (int) y, (int) x + 3, (int) y - 2);
        }

        g.setColor(new Color(255, 255, 255, 105));
        g.setStroke(new BasicStroke(2.2f));
        g.drawLine(0, (int) ground + 1, (int) width, (int) ground + 1);
        g.setColor(new Color(255, 255, 255, 75));
        g.drawLine((int) (width / 2), (int) ground, (int) (width / 2),
                (int) height);
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Arc2D.Double(width / 2 - 62, ground - 14, 124, 30, 0, 180,
                Arc2D.OPEN));
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new Rectangle2D.Double(0, height - 7, width, 7));
    }

    private void drawGoal(Graphics2D g, Goal goal) {
        double x = goal.x();
        double y = goal.y();
        double width = goal.width();
        double height = goal.height();
        double ground = y + height;

        g.setPaint(new GradientPaint((float) x, (float) y,
                new Color(255, 255, 255, 18), (float) x, (float) ground,
                new Color(18, 33, 34, 105)));
        g.fill(new Rectangle2D.Double(x, y, width, height));

        g.setColor(new Color(255, 255, 255, 68));
        g.setStroke(new BasicStroke(0.8f));
        for (double i = 0; i <= width; i += 9) {
            g.drawLine((int) (x + i), (int) y, (int) (x + i), (int) ground);
        }
        for (double j = 0; j <= height; j += 9) {
            g.drawLine((int) x, (int) (y + j), (int) (x + width), (int) (y + j));
        }

        Color accent = goal.side() == Side.LEFT ? Theme.RED : Theme.VIOLET;
        Shape bar = new Rectangle2D.Double(x - 2, y - 4, width + 4, 8);
        g.setColor(Theme.alpha(accent, 65));
        g.setStroke(new BasicStroke(11, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(bar);
        g.setColor(new Color(244, 247, 255));
        g.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(bar);

        double postX = goal.frontPostX();
        g.setColor(new Color(244, 247, 255));
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawLine((int) postX, (int) y, (int) postX, (int) ground);
        g.setColor(Theme.alpha(accent, 180));
        g.setStroke(new BasicStroke(2));
        g.drawLine((int) postX + (goal.side() == Side.LEFT ? -3 : 3),
                (int) y + 5, (int) postX + (goal.side() == Side.LEFT ? -3 : 3),
                (int) ground - 3);

        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(x + 4, ground - 5, width - 8, 10));
    }

    // ---------------- Jugadores ----------------

    private void drawPlayer(Graphics2D g, Player player) {
        double cx = player.getBodyX();
        double cy = player.getBodyY();
        double radius = player.getBodyRadius();
        double factor = radius / Player.BODY_RADIUS;
        int direction = player.getSide().attackDirection();
        double ground = player.getArena().groundY();
        Color jersey = Look.jersey(player.getCharacter());
        Color accent = Look.accent(player.getCharacter());
        Color aura = Look.aura(player.getCharacter());
        Color hair = Look.hair(player.getCharacter());
        Color skin = Look.skinTone(player.getCharacter());

        double jump = Math.max(0, ground - player.getY());
        double shadowScale = Math.max(0.35, 1 - jump / 360.0);
        g.setColor(new Color(0, 0, 0, (int) (75 * shadowScale)));
        g.fill(new Ellipse2D.Double(cx - radius * 0.95 * shadowScale,
                ground - 7, radius * 1.9 * shadowScale,
                radius * 0.34 * shadowScale));

        if (player.hasAura()) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(animationTime * 7.5));
            g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy),
                    (float) (radius * 2.3), new float[]{0.40f, 1f},
                    new Color[]{Theme.alpha(aura, 90 + (int) (60 * pulse)),
                        Theme.alpha(aura, 0)}));
            g.fill(new Ellipse2D.Double(cx - radius * 2.3, cy - radius * 2.3,
                    radius * 4.6, radius * 4.6));
            g.setColor(Theme.alpha(aura, 150 + (int) (60 * pulse)));
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new Ellipse2D.Double(cx - radius - 10, cy - radius - 10,
                    radius * 2 + 20, radius * 2 + 20));
        }

        drawFoot(g, player, cx, cy, radius, factor, direction, accent);
        drawBody(g, cx, cy, radius, jersey, accent, hair, skin, direction,
                player.isStunned());
        drawEffect(g, player, cx, cy, radius);
        if (player.isStunned()) {
            drawStunStars(g, cx, cy, radius);
        }
        drawStatus(g, player, cx, cy - radius - 24);
    }

    private void drawFoot(Graphics2D g, Player player, double cx, double cy,
            double radius, double factor, int direction, Color accent) {
        double reach = player.isKicking() ? 45 : 12;
        double footX = cx + direction * reach * factor;
        double footY = player.getY() - 11;
        double shoeWidth = 32 * factor;
        double shoeHeight = 15 * factor;

        if (player.isKicking()) {
            double sweepX = footX - direction * 7;
            double sweepY = footY - 9;
            double start = direction > 0 ? -58 : 122;
            g.setColor(new Color(255, 255, 255, 72));
            g.setStroke(new BasicStroke(8 * (float) factor, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(sweepX - 30, sweepY - 24, 60, 44,
                    start, 116, Arc2D.OPEN));
            g.setColor(Theme.alpha(accent, 210));
            g.setStroke(new BasicStroke(2.8f));
            g.draw(new Arc2D.Double(sweepX - 27, sweepY - 21, 54, 38,
                    start, 116, Arc2D.OPEN));
            g.setColor(new Color(255, 255, 255, 92));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.drawLine((int) (footX - direction * 17), (int) (footY - 8),
                    (int) (footX - direction * 38), (int) (footY - 8));
            g.drawLine((int) (footX - direction * 15), (int) (footY - 2),
                    (int) (footX - direction * 31), (int) (footY + 2));
        }

        g.setColor(new Color(7, 6, 12));
        g.fill(new Ellipse2D.Double(footX - shoeWidth / 2, footY - shoeHeight,
                shoeWidth, shoeHeight));
        g.setColor(new Color(255, 255, 255, 115));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(footX - shoeWidth / 2, footY - shoeHeight,
                shoeWidth, shoeHeight));
        g.setColor(accent);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(new Ellipse2D.Double(footX - shoeWidth / 2 + 1.5,
                footY - shoeHeight + 1.5, shoeWidth - 3, shoeHeight - 3));
        g.setColor(new Color(255, 255, 255, 55));
        g.drawLine((int) (footX - shoeWidth * 0.3), (int) (footY - shoeHeight * 0.7),
                (int) (footX + shoeWidth * 0.3), (int) (footY - shoeHeight * 0.7));
    }

    private void drawBody(Graphics2D g, double cx, double cy, double radius,
            Color jersey, Color accent, Color hair, Color skin, int direction,
            boolean stunned) {
        Ellipse2D body = new Ellipse2D.Double(cx - radius, cy - radius,
                radius * 2, radius * 2);
        Color base = stunned ? Theme.blend(jersey, Color.GRAY, 0.58) : jersey;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - radius * 0.35,
                cy - radius * 0.45), (float) (radius * 1.55),
                new float[]{0f, 0.72f, 1f},
                new Color[]{Theme.blend(base, Color.WHITE, 0.22), base,
                    Theme.blend(base, Theme.INK, 0.38)}));
        g.fill(body);
        g.setColor(Theme.blend(base, Theme.INK, 0.62));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(body);

        Shape oldClip = g.getClip();
        g.clip(body);
        g.setColor(Theme.alpha(accent, 210));
        g.fill(new Rectangle2D.Double(cx - radius, cy + radius * 0.28,
                radius * 2, radius * 0.20));
        g.setColor(new Color(255, 255, 255, 45));
        g.fill(new Ellipse2D.Double(cx - radius * 0.82, cy - radius * 0.95,
                radius * 0.62, radius * 0.25));
        g.setColor(stunned ? Theme.blend(hair, Color.GRAY, 0.5) : hair);
        g.fill(new Ellipse2D.Double(cx - radius * 0.95, cy - radius * 1.04,
                radius * 1.90, radius * 0.86));
        g.setClip(oldClip);

        double faceShift = direction * radius * 0.14;
        Ellipse2D face = new Ellipse2D.Double(cx - radius * 0.74 + faceShift,
                cy - radius * 0.46, radius * 1.48, radius * 1.30);
        g.setPaint(new GradientPaint((float) cx, (float) (cy - radius * 0.5),
                Theme.blend(skin, Color.WHITE, 0.12), (float) cx,
                (float) (cy + radius * 0.8), Theme.blend(skin, Theme.INK, 0.10)));
        g.fill(face);

        g.setColor(Theme.alpha(accent, 235));
        g.setStroke(new BasicStroke((float) Math.max(2, radius * 0.09)));
        g.draw(new Arc2D.Double(cx - radius * 0.76 + faceShift,
                cy - radius * 0.52, radius * 1.52, radius * 0.60, 20, 140,
                Arc2D.OPEN));

        double eyeX = cx + direction * radius * 0.35;
        double eyeY = cy - radius * 0.22;
        double eyeSize = radius * 0.34;
        g.setColor(new Color(252, 249, 255));
        g.fill(new Ellipse2D.Double(eyeX - eyeSize / 2, eyeY - eyeSize / 2,
                eyeSize, eyeSize));
        if (stunned) {
            g.setColor(new Color(28, 21, 34));
            g.setStroke(new BasicStroke((float) Math.max(1.5, radius * 0.07),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double d = eyeSize * 0.24;
            g.drawLine((int) (eyeX - d), (int) (eyeY - d),
                    (int) (eyeX + d), (int) (eyeY + d));
            g.drawLine((int) (eyeX + d), (int) (eyeY - d),
                    (int) (eyeX - d), (int) (eyeY + d));
        } else {
            g.setColor(new Color(28, 21, 34));
            g.fill(new Ellipse2D.Double(eyeX - eyeSize * 0.20 + direction
                    * eyeSize * 0.10, eyeY - eyeSize * 0.16,
                    eyeSize * 0.40, eyeSize * 0.50));
            g.setColor(new Color(255, 255, 255, 190));
            g.fill(new Ellipse2D.Double(eyeX - eyeSize * 0.11 + direction
                    * eyeSize * 0.10, eyeY - eyeSize * 0.12,
                    eyeSize * 0.12, eyeSize * 0.12));
        }

        double mouthX = cx + direction * radius * 0.18;
        g.setColor(Theme.alpha(Theme.blend(skin, Theme.INK, 0.45), 190));
        g.setStroke(new BasicStroke((float) Math.max(1, radius * 0.045),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D mouth = new Path2D.Double();
        mouth.moveTo(mouthX - radius * 0.16, cy + radius * 0.35);
        mouth.quadTo(mouthX, cy + radius * 0.42,
                mouthX + radius * 0.16, cy + radius * 0.33);
        g.draw(mouth);
    }

    private void drawEffect(Graphics2D g, Player player, double cx, double cy,
            double radius) {
        Effect effect = player.getEffect();
        if (effect == null) {
            return;
        }
        Color color = effectColor(effect);
        if (effect.getName().equalsIgnoreCase("Congelado")) {
            g.setColor(Theme.alpha(color, 52));
            g.fill(new RoundRectangle2D.Double(cx - radius - 6, cy - radius - 5,
                    radius * 2 + 12, radius * 2 + 10, 12, 12));
            g.setColor(Theme.alpha(color, 190));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Double(cx - radius - 6, cy - radius - 5,
                    radius * 2 + 12, radius * 2 + 10, 12, 12));
            g.setColor(new Color(255, 255, 255, 120));
            g.drawLine((int) (cx - radius * 0.2), (int) (cy - radius * 0.8),
                    (int) (cx + radius * 0.2), (int) (cy + radius * 0.3));
            g.drawLine((int) (cx + radius * 0.2), (int) (cy + radius * 0.3),
                    (int) (cx - radius * 0.3), (int) (cy + radius * 0.8));
            return;
        }
        if (effect.getName().equalsIgnoreCase("Electrocutado")) {
            g.setColor(Theme.alpha(color, 210));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                double angle = animationTime * 9 + i * Math.PI * 2 / 3;
                double x = cx + Math.cos(angle) * radius * 1.16;
                double y = cy + Math.sin(angle) * radius * 1.16;
                Path2D bolt = new Path2D.Double();
                bolt.moveTo(x - 5, y - 9);
                bolt.lineTo(x + 2, y - 2);
                bolt.lineTo(x - 2, y);
                bolt.lineTo(x + 5, y + 9);
                g.draw(bolt);
            }
            return;
        }
        g.setColor(Theme.alpha(color, 85));
        g.setStroke(new BasicStroke(3));
        g.draw(new Ellipse2D.Double(cx - radius - 7, cy - radius - 7,
                radius * 2 + 14, radius * 2 + 14));
        for (int i = 0; i < 4; i++) {
            double angle = animationTime * 3.4 + i * Math.PI / 2;
            double x = cx + Math.cos(angle) * (radius + 8);
            double y = cy + Math.sin(angle) * (radius + 8);
            g.setColor(Theme.alpha(color, 210));
            g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
        }
    }

    private void drawStunStars(Graphics2D g, double cx, double cy,
            double radius) {
        g.setColor(Theme.GOLD);
        for (int i = 0; i < 3; i++) {
            double angle = animationTime * 3.5 + i * Math.PI * 2 / 3;
            double x = cx + Math.cos(angle) * radius * 0.9;
            double y = cy - radius - 11 + Math.sin(angle) * radius * 0.18;
            Path2D star = new Path2D.Double();
            for (int point = 0; point < 10; point++) {
                double r = point % 2 == 0 ? 5 : 2.2;
                double p = -Math.PI / 2 + point * Math.PI / 5;
                double px = x + Math.cos(p) * r;
                double py = y + Math.sin(p) * r;
                if (point == 0) {
                    star.moveTo(px, py);
                } else {
                    star.lineTo(px, py);
                }
            }
            star.closePath();
            g.fill(star);
        }
    }

    private Color effectColor(Effect effect) {
        return switch (effect.getName().toLowerCase(Locale.ROOT)) {
            case "congelado" -> Theme.CYAN;
            case "electrocutado" -> Theme.GOLD;
            case "empuje" -> Theme.RED;
            case "confusion" -> Theme.VIOLET;
            case "reducido" -> Theme.GOLD;
            case "inconsciente" -> Theme.MUTED;
            default -> Theme.PURPLE;
        };
    }

    private void drawStatus(Graphics2D g, Player player, double centerX,
            double baseline) {
        String text = null;
        Color color = Theme.TEXT;
        if (player.isStunned()) {
            text = String.format("ATURDIDO %.1f", player.getStunTimer());
            color = Theme.RED;
        } else if (player.getEffect() != null) {
            Effect effect = player.getEffect();
            text = String.format("%s %.1f", effect.getName(), effect.getRemaining());
            color = effectColor(effect);
        } else if (player.hasAura()) {
            text = "PODER ACTIVO";
            color = Look.aura(player.getCharacter());
        }
        if (text == null) {
            return;
        }
        Font font = Theme.font(12, Font.BOLD);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        double width = metrics.stringWidth(text) + 18;
        double height = 22;
        RoundRectangle2D pill = new RoundRectangle2D.Double(centerX - width / 2,
                baseline - 15, width, height, 11, 11);
        g.setColor(new Color(7, 7, 14, 205));
        g.fill(pill);
        g.setColor(Theme.alpha(color, 170));
        g.setStroke(new BasicStroke(1.1f));
        g.draw(pill);
        g.setColor(color);
        g.drawString(text, (float) (centerX - metrics.stringWidth(text) / 2.0),
                (float) baseline);
    }

    // ---------------- Balon ----------------

    private void drawBall(Graphics2D g, Arena arena, Ball ball, boolean real) {
        double x = ball.getX();
        double y = ball.getY();
        double radius = Ball.RADIUS;

        if (real) {
            double height = Math.max(0, arena.groundY() - y);
            double shadow = Math.max(0.25, 1 - height / 380);
            g.setColor(new Color(0, 0, 0, (int) (58 * shadow)));
            g.fill(new Ellipse2D.Double(x - radius * shadow,
                    arena.groundY() - 5, radius * 2 * shadow,
                    radius * 0.55 * shadow));
        }

        double speed = Math.hypot(ball.getVx(), ball.getVy());
        if (real && speed > 170) {
            double dx = ball.getVx() / speed;
            double dy = ball.getVy() / speed;
            for (int i = 3; i >= 1; i--) {
                double trail = i * 13;
                g.setColor(new Color(255, 255, 255, 16 + (4 - i) * 12));
                g.fill(new Ellipse2D.Double(x - dx * trail - radius * 0.72,
                        y - dy * trail - radius * 0.72, radius * 1.44,
                        radius * 1.44));
            }
        }

        if (real) {
            g.setColor(Theme.alpha(Theme.GOLD, 55));
            g.fill(new Ellipse2D.Double(x - radius - 4, y - radius - 4,
                    radius * 2 + 8, radius * 2 + 8));
        }
        Theme.drawSoccerBall(g, x, y, radius, true);
        if (!real) {
            g.setColor(new Color(8, 6, 13, 120));
            g.fill(new Ellipse2D.Double(x - radius, y - radius,
                    radius * 2, radius * 2));
        }
    }

    // ---------------- HUD ----------------

    private void drawHud(Graphics2D g, DOPOSoccer game, int width) {
        g.setPaint(new GradientPaint(0, 0, Theme.INK_SOFT, 0, HUD,
                new Color(12, 9, 20)));
        g.fillRect(0, 0, width, HUD);
        g.setPaint(new GradientPaint(0, 0, Theme.alpha(Theme.CRIMSON, 42),
                width / 2f, 0, Theme.alpha(Theme.PURPLE, 42)));
        g.fillRect(0, 0, width, 2);
        g.setColor(new Color(255, 255, 255, 17));
        g.fillRect(0, HUD - 1, width, 1);

        int pad = 12;
        int cardWidth = Math.min(400, Math.max(222, width / 3));
        int cardHeight = HUD - 20;
        drawPlayerCard(g, game.getPlayer(Side.LEFT), pad, 10,
                cardWidth, cardHeight, false);
        drawPlayerCard(g, game.getPlayer(Side.RIGHT),
                width - pad - cardWidth, 10, cardWidth, cardHeight, true);

        int centerWidth = Math.max(150, width - (cardWidth + pad) * 2);
        int centerX = width / 2;
        drawCentralScore(g, game, centerX, centerWidth);
    }

    private void drawPlayerCard(Graphics2D g, Player player, int x, int y,
            int width, int height, boolean right) {
        Color accent = right ? Theme.VIOLET : Theme.RED;
        RoundRectangle2D card = new RoundRectangle2D.Double(x, y, width, height,
                18, 18);
        g.setPaint(new GradientPaint(0, y, Theme.alpha(Theme.PANEL, 245),
                0, y + height, Theme.alpha(Theme.INK_SOFT, 238)));
        g.fill(card);
        g.setColor(Theme.alpha(accent, 72));
        g.setStroke(new BasicStroke(1f));
        g.draw(card);
        g.setColor(Theme.alpha(accent, 180));
        g.fill(new RoundRectangle2D.Double(right ? x + width - 28 : x + 10,
                y, 18, 3, 3, 3));

        int portraitSize = 54;
        int portraitX = right ? x + width - portraitSize - 8 : x + 8;
        int portraitY = y + 8;
        CharacterPortrait.paintPortrait(g, player.getCharacter(), portraitX,
                portraitY, portraitSize, portraitSize, 1.0,
                Look.aura(player.getCharacter()), false);

        int textWidth = width - portraitSize - 24;
        float textX = right ? x + width - portraitSize - 16 : x + portraitSize + 16;
        int align = right ? SwingConstants.RIGHT : SwingConstants.LEFT;

        g.setFont(Theme.font(13, Font.BOLD));
        g.setColor(Theme.TEXT);
        drawAligned(g, player.getProfile().getName(), textX, y + 20, align);
        g.setFont(Theme.font(9, Font.BOLD));
        g.setColor(Theme.alpha(accent, 230));
        String subtitle = player.getCharacter().displayName().toUpperCase(Locale.ROOT)
                + "  ·  " + player.getProfile().getPoints() + " PTS";
        drawAligned(g, subtitle, textX, y + 33, align);

        int barX = right ? x + 10 : x + portraitSize + 16;
        int barWidth = textWidth;
        double lifeRatio = player.getMaxLife() <= 0 ? 0
                : player.getLife() / player.getMaxLife();
        drawBar(g, barX, y + 39, barWidth, 10, lifeRatio,
                lifeRatio > 0.3 ? Theme.GREEN : Theme.RED);
        drawBarValue(g, "VIDA  " + Math.ceil(player.getLife()), barX,
                y + 39, barWidth, 10, align);

        double power = Math.max(0, Math.min(1, player.getPowerBar()));
        drawSegmentedBar(g, barX, y + 54, barWidth, 7, power,
                Look.aura(player.getCharacter()), player.getPowerBar() >= 1);

        g.setFont(Theme.font(9, Font.BOLD));
        g.setColor(powerColor(player));
        drawAligned(g, statusText(player), textX, y + 80, align);
    }

    private Color powerColor(Player player) {
        if (player.isStunned()) {
            return Theme.RED;
        }
        if (player.getEffect() != null) {
            return effectColor(player.getEffect());
        }
        if (player.hasAura()) {
            return Look.aura(player.getCharacter());
        }
        if (player.getPowerBar() >= 1) {
            return Theme.GOLD;
        }
        return Theme.MUTED;
    }

    private String statusText(Player player) {
        if (player.isStunned()) {
            return String.format("ATURDIDO %.1fs", player.getStunTimer());
        }
        if (player.getEffect() != null) {
            return String.format("%s %.1fs", player.getEffect().getName(),
                    player.getEffect().getRemaining());
        }
        if (player.hasAura()) {
            return "PODER ACTIVO · " + player.getCharacter().specialAbility().name();
        }
        if (player.getPowerBar() >= 1) {
            return "PODER LISTO · " + powerKeyLabel(player.getSide());
        }
        return String.format("PODER %d%%", (int) Math.round(player.getPowerBar() * 100));
    }

    /**
     * Etiqueta la tecla de poder segun la modalidad. Los lados controlados por
     * la maquina no muestran una tecla de teclado.
     *
     * @param side lado del jugador
     * @return tecla o texto de control automatico
     */
    private String powerKeyLabel(Side side) {
        if (mode.isMachine(side)) {
            return "MAQUINA";
        }
        return side == Side.LEFT ? "Q" : "CTRL DER";
    }

    private void drawCentralScore(Graphics2D g, DOPOSoccer game, int centerX,
            int availableWidth) {
        int scoreWidth = Math.min(196, Math.max(138, availableWidth - 12));
        int scoreX = centerX - scoreWidth / 2;
        RoundRectangle2D scoreBox = new RoundRectangle2D.Double(scoreX, 8,
                scoreWidth, 48, 14, 14);
        g.setPaint(new GradientPaint(0, 8, Theme.alpha(Theme.PANEL_LIGHT, 248),
                0, 56, Theme.alpha(Theme.INK_PURPLE, 245)));
        g.fill(scoreBox);
        g.setColor(new Color(255, 255, 255, 30));
        g.setStroke(new BasicStroke(1f));
        g.draw(scoreBox);

        g.setFont(Theme.display(34));
        FontMetrics metrics = g.getFontMetrics();
        String left = String.valueOf(game.goalsOf(Side.LEFT));
        String right = String.valueOf(game.goalsOf(Side.RIGHT));
        int leftWidth = metrics.stringWidth(left);
        int rightWidth = metrics.stringWidth(right);
        g.setColor(Theme.RED);
        g.drawString(left, centerX - 26 - leftWidth, 45);
        g.setColor(Theme.alpha(Theme.TEXT, 110));
        g.setFont(Theme.display(22));
        g.drawString("-", centerX - 6, 43);
        g.setColor(Theme.VIOLET);
        g.setFont(Theme.display(34));
        g.drawString(right, centerX + 26, 45);

        String timer;
        Color timerColor = Theme.TEXT;
        if (game.isSuddenDeath()) {
            timer = "MUERTE SUBITA";
            timerColor = Theme.GOLD;
        } else if (game.getRemainingSeconds() >= 0) {
            int seconds = (int) Math.ceil(game.getRemainingSeconds());
            timer = String.format("%d:%02d", seconds / 60, seconds % 60);
        } else if (game.getMatchType().isSurvival()) {
            timer = "SUPERVIVENCIA";
        } else {
            timer = game.getMatchType().describe().toUpperCase(Locale.ROOT);
        }
        int timerWidth = Math.min(150, Math.max(98, availableWidth - 30));
        int timerX = centerX - timerWidth / 2;
        RoundRectangle2D timerBox = new RoundRectangle2D.Double(timerX, 62,
                timerWidth, 23, 11, 11);
        g.setColor(new Color(255, 255, 255, 16));
        g.fill(timerBox);
        g.setColor(Theme.alpha(timerColor, 80));
        g.draw(timerBox);
        g.setFont(Theme.font(game.isSuddenDeath() ? 9 : 14, Font.BOLD));
        g.setColor(timerColor);
        drawCentered(g, timer, centerX, 78);

        g.setFont(Theme.font(9, Font.BOLD));
        g.setColor(Theme.DIM);
        if (game.getMatchType().isSurvival()) {
            drawCentered(g, "BALONES  " + game.getPlayer(Side.LEFT).getBalls()
                    + " - " + game.getPlayer(Side.RIGHT).getBalls(), centerX, 101);
        } else if (game.getRemainingSeconds() >= 0) {
            String mode = game.getMatchType().describe().toUpperCase(Locale.ROOT);
            drawCentered(g, mode, centerX, 101);
        }
    }

    private void drawBar(Graphics2D g, double x, double y, double width,
            double height, double ratio, Color color) {
        RoundRectangle2D track = new RoundRectangle2D.Double(x, y, width, height,
                height, height);
        g.setColor(new Color(255, 255, 255, 18));
        g.fill(track);
        double fill = Math.max(0, Math.min(1, ratio)) * width;
        if (fill > 1) {
            g.setColor(color);
            g.fill(new RoundRectangle2D.Double(x, y, fill, height,
                    height, height));
            g.setColor(Theme.alpha(Theme.TEXT, 44));
            g.fill(new RoundRectangle2D.Double(x + 1, y + 1,
                    Math.max(0, fill - 2), height * 0.34, 3, 3));
        }
        g.setColor(new Color(255, 255, 255, 42));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(track);
    }

    private void drawSegmentedBar(Graphics2D g, double x, double y,
            double width, double height, double ratio, Color color,
            boolean ready) {
        int segments = 12;
        double gap = 2;
        double segmentWidth = Math.max(2,
                (width - gap * (segments - 1)) / segments);
        if (ready) {
            g.setColor(Theme.alpha(color, 65));
            g.setStroke(new BasicStroke(4));
            g.draw(new RoundRectangle2D.Double(x - 2, y - 2, width + 4,
                    height + 4, height + 4, height + 4));
        }
        for (int i = 0; i < segments; i++) {
            double sx = x + i * (segmentWidth + gap);
            RoundRectangle2D block = new RoundRectangle2D.Double(sx, y,
                    segmentWidth, height, 3, 3);
            double threshold = i / (double) segments;
            g.setColor(threshold < ratio ? color : new Color(255, 255, 255, 22));
            g.fill(block);
        }
    }

    private void drawBarValue(Graphics2D g, String text, double x, double y,
            double width, double height, int align) {
        g.setFont(Theme.font(8, Font.BOLD));
        g.setColor(new Color(255, 255, 255, 210));
        FontMetrics metrics = g.getFontMetrics();
        float tx = align == SwingConstants.RIGHT
                ? (float) (x + width - metrics.stringWidth(text) - 5)
                : (float) (x + 5);
        g.drawString(text, tx, (float) (y + height - 2));
    }

    // ---------------- Overlays ----------------

    private void drawIntro(Graphics2D g, int width, int height) {
        int fieldTop = HUD;
        int fieldHeight = Math.max(1, height - HUD);
        g.setColor(new Color(4, 5, 10, 62));
        g.fillRect(0, fieldTop, width, fieldHeight);

        int phase = Math.max(1, (int) Math.ceil(intro / (INTRO_SECONDS / 4)));
        String text = phase > 1 ? String.valueOf(phase - 1) : "¡YA!";
        double pulse = 1.0 + 0.07 * Math.sin(animationTime * 12);
        Font font = Theme.display((int) (88 * pulse));
        FontMetrics metrics = g.getFontMetrics(font);
        int baseline = fieldTop + fieldHeight / 2 + metrics.getAscent() / 2 - 8;
        g.setFont(font);
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(text, (width - metrics.stringWidth(text)) / 2 + 4,
                baseline + 5);

        Paint paint = phase > 1
                ? new GradientPaint(width / 2f - 100, baseline - 80, Theme.GOLD,
                        width / 2f + 100, baseline, Theme.RED)
                : new GradientPaint(width / 2f - 100, baseline - 80, Theme.GREEN,
                        width / 2f + 100, baseline, Theme.GOLD);
        g.setPaint(paint);
        g.drawString(text, (width - metrics.stringWidth(text)) / 2, baseline);

    }

    private void drawGoalCelebration(Graphics2D g, DOPOSoccer game, int width,
            int height) {
        int fieldTop = HUD;
        int fieldHeight = Math.max(1, height - HUD);
        double progress = 1 - goalFlash / GOAL_SECONDS;
        int alpha = (int) Math.min(80, goalFlash * 60);
        g.setColor(new Color(255, 219, 140, alpha));
        g.fillRect(0, fieldTop, width, fieldHeight);

        for (int i = 0; i < 44; i++) {
            double x = hash(i * 11 + 4) * width;
            double speed = 120 + hash(i * 7 + 1) * 210;
            double y = fieldTop - 20 + (progress * speed + hash(i * 3) * fieldHeight)
                    % (fieldHeight + 30);
            double size = 3 + hash(i * 5) * 6;
            Color confetti = switch (i % 5) {
                case 0 -> Theme.GOLD;
                case 1 -> Theme.RED;
                case 2 -> Theme.CYAN;
                case 3 -> Theme.VIOLET;
                default -> Theme.GREEN;
            };
            g.setColor(Theme.alpha(confetti, 155));
            g.fill(new RoundRectangle2D.Double(x, y, size, size * 1.7,
                    size * 0.4, size * 0.4));
        }

        int baseline = fieldTop + fieldHeight / 2;
        String goal = "¡GOL!";
        Font font = Theme.display(76);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        float x = (width - metrics.stringWidth(goal)) / 2.0f;
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(goal, x + 5, baseline + 5);
        g.setPaint(new GradientPaint(width / 2f - 130, baseline - 90, Theme.GOLD,
                width / 2f + 130, baseline, Theme.RED));
        g.drawString(goal, x, baseline);

        CharacterDefinition character = goalSide == null ? null
                : game.getPlayer(goalSide).getCharacter();
        if (character != null) {
            g.setFont(Theme.font(15, Font.BOLD));
            g.setColor(Theme.TEXT);
            drawCentered(g, character.displayName().toUpperCase(Locale.ROOT)
                    + " ANOTA", width / 2.0, baseline + 31);
        }
    }

    private void drawPause(Graphics2D g, DOPOSoccer game, int width, int height) {
        g.setColor(new Color(4, 3, 9, 178));
        g.fillRect(0, 0, width, height);

        int cardWidth = 468;
        int cardHeight = 286;
        double x = (width - cardWidth) / 2.0;
        double y = (height - cardHeight) / 2.0;
        RoundRectangle2D card = new RoundRectangle2D.Double(x, y, cardWidth,
                cardHeight, 28, 28);
        g.setPaint(new GradientPaint(0, (float) y, Theme.alpha(Theme.PANEL_LIGHT, 250),
                0, (float) (y + cardHeight), Theme.alpha(Theme.INK_SOFT, 250)));
        g.fill(card);
        Theme.drawGlow(g, card, Theme.PURPLE, 5);
        g.setColor(Theme.alpha(Theme.PURPLE, 130));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(card);

        g.setFont(Theme.display(40));
        g.setColor(Theme.TEXT);
        drawCentered(g, "PAUSA", width / 2.0, y + 66);

        g.setFont(Theme.display(28));
        g.setColor(Theme.RED);
        FontMetrics scoreMetrics = g.getFontMetrics();
        String score = game.goalsOf(Side.LEFT) + "  -  " + game.goalsOf(Side.RIGHT);
        g.drawString(score, (float) (width / 2.0 - scoreMetrics.stringWidth(score) / 2.0),
                (float) y + 108);
        g.setFont(Theme.font(11, Font.PLAIN));
        g.setColor(Theme.MUTED);
        drawCentered(g, game.getMatchType().describe(), width / 2.0, y + 130);

        g.setFont(Theme.font(13, Font.BOLD));
        g.setColor(Theme.TEXT);
        drawCentered(g, "P    CONTINUAR", width / 2.0, y + 186);
        g.setColor(Theme.MUTED);
        g.setFont(Theme.font(12, Font.PLAIN));
        drawCentered(g, "ESC    TERMINAR EL PARTIDO", width / 2.0, y + 211);

        g.setFont(Theme.font(9, Font.BOLD));
        g.setColor(Theme.DIM);
        drawCentered(g, "EL PARTIDO ESTA DETENIDO", width / 2.0, y + 252);
    }

    // ---------------- Utilidades ----------------

    private void drawAligned(Graphics2D g, String text, float x, float baseline,
            int align) {
        FontMetrics metrics = g.getFontMetrics();
        float drawX = align == SwingConstants.RIGHT
                ? x - metrics.stringWidth(text) : x;
        g.drawString(text, drawX, baseline);
    }

    private void drawCentered(Graphics2D g, String text, double centerX,
            double baseline) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (float) (centerX - metrics.stringWidth(text) / 2.0),
                (float) baseline);
    }

    private static double hash(int value) {
        double x = Math.sin(value * 12.9898 + 78.233) * 43758.5453;
        return x - Math.floor(x);
    }
}
