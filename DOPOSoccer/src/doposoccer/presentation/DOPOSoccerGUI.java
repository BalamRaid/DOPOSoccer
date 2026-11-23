package doposoccer.presentation;

import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.application.SurvivalSeries;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Profile;
import doposoccer.domain.character.CharacterDefinition;
import doposoccer.infrastructure.ErrorLog;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal de DOPOSoccer. Alterna entre menu, instrucciones,
 * configuracion, partido y resultado.
 *
 * <p>La ventana conserva los perfiles entre partidos para que los puntos y las
 * mejoras de estadisticas no se pierdan. La logica del partido vive en la capa
 * de aplicacion; esta clase solo construye las pantallas, inicia la sesion y
 * muestra los resultados.</p>
 */
@SuppressWarnings("serial")
public final class DOPOSoccerGUI extends JFrame {

    private final Profile p1 = new Profile("Jugador 1");
    private final Profile p2 = new Profile("Jugador 2");
    private final Profile machine1 = new Profile("Maquina 1");
    private final Profile machine2 = new Profile("Maquina 2");
    private SetupPanel setup;
    private GamePanel gamePanel;
    private SurvivalSeries survivalSeries;
    private Resolution lastResolution = Resolution.MEDIUM;
    private Stage lastStage = Stage.STADIUM;

    /**
     * Crea la ventana principal, configura el cierre y muestra el menu.
     */
    public DOPOSoccerGUI() {
        super("DOPOSoccer");
        Theme.install();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        setIconImage(Theme.appIcon(64));
        setMinimumSize(new Dimension(920, 650));
        showMainMenu();
    }

    /**
     * Muestra el menu principal.
     */
    void showMainMenu() {
        setContentPane(new MainMenuPanel(this));
        refreshWindow();
    }

    /**
     * Muestra la pantalla de instrucciones.
     */
    void showHowToPlay() {
        setContentPane(new HowToPlayPanel(this));
        refreshWindow();
    }

    /**
     * Muestra la pantalla de configuracion y actualiza los puntos y las
     * estadisticas visibles de ambos perfiles.
     */
    void showSetup() {
        if (setup == null) {
            setup = new SetupPanel(this, p1, p2, machine1, machine2);
        }
        setup.refresh();
        setContentPane(setup);
        refreshWindow();
    }

    private void refreshWindow() {
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    /**
     * Crea la sesion con la configuracion elegida y cambia a la pantalla de
     * juego.
     *
     * @param config configuracion del partido
     * @param resolution resolucion visual de la cancha
     * @param stage escenario visual elegido
     */
    void startGame(MatchConfig config, Resolution resolution, Stage stage) {
        try {
            GameSession session = GameSession.create(config);
            if (config.matchType().isSurvival()) {
                survivalSeries = new SurvivalSeries(config);
            } else {
                survivalSeries = null;
            }
            openSession(session, resolution, stage);
        } catch (DOPOSoccerException e) {
            showError(e);
        }
    }

    /**
     * Abre una sesion ya creada en el panel de juego.
     *
     * @param session sesion que se va a jugar
     * @param resolution resolucion visual
     * @param stage escenario visual
     */
    private void openSession(GameSession session, Resolution resolution,
            Stage stage) {
        lastResolution = resolution;
        lastStage = stage;
        gamePanel = new GamePanel(this, session, resolution, stage);
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
        gamePanel.start();
    }

    /**
     * Inicia la siguiente ronda de una serie de supervivencia.
     *
     * @param series serie activa
     * @param resolution resolucion visual
     * @param stage escenario visual
     */
    private void startSurvivalRound(SurvivalSeries series, Resolution resolution,
            Stage stage) {
        try {
            openSession(series.startRound(), resolution, stage);
        } catch (DOPOSoccerException e) {
            showError(e);
            survivalSeries = null;
            showSetup();
        }
    }

    /**
     * Pregunta al usuario si desea terminar el partido actual. Si acepta,
     * detiene el panel, marca el partido como terminado y muestra el
     * resultado; si cancela, reanuda el juego si estaba pausado.
     */
    void askEndGame() {
        if (gamePanel == null) {
            return;
        }
        GameSession session = gamePanel.getSession();
        boolean wasPaused = session.isPaused();
        if (!wasPaused) {
            session.pause();
        }
        gamePanel.clearKeyboard();
        int option = JOptionPane.showConfirmDialog(this, "Desea terminar el partido?",
                "Terminar partido", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            gamePanel.stop();
            session.finish();
            gameFinished(session);
        } else {
            if (!wasPaused) {
                session.resume();
            }
            gamePanel.clearKeyboard();
            gamePanel.requestFocusInWindow();
        }
    }

    /**
     * Muestra el resultado y gestiona la serie de supervivencia. En lugar de
     * un cuadro de dialogo, la aplicacion cambia a una pantalla de resultado
     * con acciones claras.
     *
     * @param session sesion que acaba de terminar
     */
    void gameFinished(GameSession session) {
        MatchResult result = session.result();
        MatchConfig config = session.config();
        gamePanel = null;

        String subtitle;
        if (result.winner() == null) {
            subtitle = "Empate: no hay ganador en esta cancha.";
        } else {
            Profile winnerProfile = config.profileOf(result.winner());
            CharacterDefinition winnerCharacter = config.characterOf(result.winner());
            subtitle = "Gana " + winnerProfile.getName() + " ("
                    + winnerCharacter.displayName() + ")";
        }

        if (!result.survival()) {
            showResult("FIN DEL PARTIDO", subtitle, "", result, config,
                    "REVANCHA", () -> startGame(config, lastResolution, lastStage),
                    this::showSetup, () -> {
                        survivalSeries = null;
                        showMainMenu();
                    });
            return;
        }

        SurvivalSeries series = survivalSeries;
        if (series == null) {
            showResult("FIN DE LA SUPERVIVENCIA", subtitle, "", result, config,
                    "REVANCHA", () -> startGame(config, lastResolution, lastStage),
                    this::showSetup, this::showMainMenu);
            return;
        }

        series.record(result);
        if (series.isOver()) {
            survivalSeries = null;
            String extra = "Rondas ganadas: " + series.humanWins();
            showResult("FIN DE LA SUPERVIVENCIA", subtitle, extra, result, config,
                    "NUEVA SERIE", () -> startGame(config, lastResolution, lastStage),
                    this::showSetup, this::showMainMenu);
            return;
        }

        SurvivalSeries activeSeries = series;
        String extra = "Rondas ganadas: " + series.humanWins()
                + "  ·  Siguiente ronda: " + series.round();
        showResult("RONDA SUPERADA", subtitle, extra, result, config,
                "SIGUIENTE RONDA",
                () -> startSurvivalRound(activeSeries, lastResolution, lastStage),
                () -> {
                    survivalSeries = null;
                    showSetup();
                },
                () -> {
                    survivalSeries = null;
                    showMainMenu();
                });
    }

    private void showResult(String title, String subtitle, String extra,
            MatchResult result, MatchConfig config, String primaryLabel,
            Runnable primary, Runnable secondary, Runnable menu) {
        setContentPane(new ResultPanel(title, subtitle, extra, result, config,
                primaryLabel, primary, secondary, menu));
        refreshWindow();
    }

    /**
     * Muestra un error al usuario. Los errores de dominio se presentan como
     * advertencia; los errores inesperados se registran en el log y se informa
     * donde quedo el detalle.
     *
     * @param e excepcion que se desea mostrar
     */
    void showError(Exception e) {
        if (e instanceof DOPOSoccerException) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DOPOSoccer",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            ErrorLog.record(e);
            JOptionPane.showMessageDialog(this, "Ocurrio un error inesperado: "
                    + e.getMessage() + "\nEl detalle quedo en " + ErrorLog.FILE_NAME,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Pregunta al usuario si desea salir y, si acepta, cierra la aplicacion.
     */
    void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this, "Desea salir de DOPOSoccer?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Punto de entrada de la aplicacion. Crea la ventana en el hilo de
     * eventos de Swing.
     *
     * @param args argumentos de la linea de comandos; no se usan
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DOPOSoccerGUI().setVisible(true));
    }
}
