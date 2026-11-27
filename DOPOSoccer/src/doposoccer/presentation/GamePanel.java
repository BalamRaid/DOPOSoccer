package doposoccer.presentation;

import doposoccer.application.GameSession;
import doposoccer.domain.Side;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panel de juego: avanza la sesion con un paso fijo, traduce el teclado a
 * intenciones y delega el dibujo en {@link GameRenderer}.
 *
 * <p>El temporizador de Swing solo aporta el pulso de cuadro; la simulacion
 * usa un acumulador de pasos fijos de 1/60 s para que la fisica sea
 * determinista. El panel instala los controladores humanos de los lados que
 * correspondan y deja que la sesion conserve los controladores de maquina.</p>
 */
@SuppressWarnings("serial")
class GamePanel extends JPanel {

    private static final double STEP = 1.0 / 60;
    private static final double MAX_FRAME = 0.05;
    private static final int MAX_STEPS = 3;

    private final DOPOSoccerGUI gui;
    private final GameSession session;
    private final Resolution resolution;
    private final GameRenderer renderer;
    private final KeyboardState keyboard = new KeyboardState();
    private final Timer timer;
    private long lastTick;
    private double accumulator;
    private boolean reported;
    private int heldGlobalKey = KeyEvent.VK_UNDEFINED;

    /**
     * Crea el panel de juego para una sesion, una resolucion y un escenario.
     *
     * @param gui ventana principal que recibe los eventos de fin y error
     * @param session sesion que se va a dibujar y avanzar
     * @param resolution resolucion visual de la cancha
     * @param stage escenario visual elegido
     */
    GamePanel(DOPOSoccerGUI gui, GameSession session, Resolution resolution,
            Stage stage) {
        this.gui = gui;
        this.session = session;
        this.resolution = resolution;
        this.renderer = new GameRenderer(stage, session.mode());
        session.match().setWeather(stage.weather());
        setPreferredSize(new Dimension(resolution.getWidth(),
                resolution.getHeight() + GameRenderer.HUD));
        setBackground(Theme.INK);
        setFocusable(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                clearKeyboard();
            }
        });
        addKeyListener(new Controls());
        installHumanControllers();
        timer = new Timer(16, e -> tick());
    }

    /**
     * Instala los controladores humanos en los lados que no son de maquina.
     */
    private void installHumanControllers() {
        if (session.isHumanSide(Side.LEFT)) {
            session.setController(Side.LEFT,
                    new HumanController(keyboard, KeyBindings.playerOne()));
        }
        if (session.isHumanSide(Side.RIGHT)) {
            session.setController(Side.RIGHT,
                    new HumanController(keyboard, KeyBindings.playerTwo()));
        }
    }

    /**
     * Inicia el temporizador de simulacion y solicita el foco para el teclado.
     */
    void start() {
        lastTick = System.nanoTime();
        accumulator = 0;
        resetInput();
        timer.start();
        requestFocusInWindow();
    }

    /**
     * Detiene el temporizador de simulacion.
     */
    void stop() {
        timer.stop();
    }

    /**
     * Limpia las teclas mantenidas y las acciones pendientes, pero conserva
     * el control de repeticion de P/Esc. Se invoca al perder el foco y al
     * abrir o cerrar el dialogo de fin: asi un Esc mantenido no vuelve a abrir
     * el dialogo al recuperar el foco.
     */
    void clearKeyboard() {
        keyboard.clear();
    }

    /**
     * Reinicia todo el estado de entrada, incluida la proteccion contra
     * repeticion. Solo se usa al comenzar una partida nueva.
     */
    void resetInput() {
        clearKeyboard();
        heldGlobalKey = KeyEvent.VK_UNDEFINED;
    }

    GameSession getSession() {
        return session;
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    /**
     * Ejecuta los pasos de simulacion acumulados, repinta y detecta el fin del
     * partido. Si ocurre un error no controlado, detiene el temporizador y lo
     * informa a la ventana.
     */
    private void tick() {
        long now = System.nanoTime();
        double frame = Math.min(MAX_FRAME, (now - lastTick) / 1e9);
        lastTick = now;
        if (!session.isPaused()) {
            renderer.advance(frame);
        }
        if (!session.isPaused() && !renderer.isIntroActive()) {
            // Camara lenta cuando el balon va directo al arco.
            accumulator += frame * renderer.timeScale(session.match(), frame);
            try {
                int steps = 0;
                while (accumulator >= STEP && steps < MAX_STEPS) {
                    session.update(STEP);
                    accumulator -= STEP;
                    steps++;
                }
                if (steps == MAX_STEPS) {
                    accumulator = 0;
                }
            } catch (RuntimeException e) {
                timer.stop();
                gui.showError(e);
                return;
            }
        } else {
            accumulator = 0;
        }
        repaint();
        if (session.isFinished() && !reported) {
            reported = true;
            timer.stop();
            gui.gameFinished(session);
        }
    }

    /**
     * Dibuja el HUD, la cancha, los jugadores, el balon y los efectos.
     *
     * @param g0 contexto grafico proporcionado por Swing
     */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        renderer.paint((Graphics2D) g0, session.match(), getWidth(), getHeight());
    }

    /**
     * Traduce los eventos de teclado en estado compartido y acciones globales.
     *
     * <p>Jugador 1: A/D mover, W saltar, S patear, Q poder. Jugador 2:
     * flechas mover/saltar, abajo patear, Control derecho poder. P pausa, G
     * guarda la partida y Esc termina el partido.</p>
     */
    private class Controls extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_G) {
                if (heldGlobalKey == key) {
                    return;
                }
                keyboard.clear();
                heldGlobalKey = key;
                if (key == KeyEvent.VK_P) {
                    session.togglePause();
                    repaint();
                } else if (key == KeyEvent.VK_G) {
                    gui.saveGame();
                    repaint();
                } else {
                    gui.askEndGame();
                }
                return;
            }
            if (key == KeyEvent.VK_CONTROL
                    && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
                keyboard.pressRightControl();
                return;
            }
            keyboard.press(key);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_G) {
                if (heldGlobalKey == key) {
                    heldGlobalKey = KeyEvent.VK_UNDEFINED;
                }
                return;
            }
            if (key == KeyEvent.VK_CONTROL
                    && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
                keyboard.releaseRightControl();
                return;
            }
            keyboard.release(key);
        }
    }
}
