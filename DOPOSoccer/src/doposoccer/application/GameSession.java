package doposoccer.application;

import java.io.Serializable;

import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Side;

import doposoccer.domain.control.PlayerController;

/**
 * Fachada de aplicacion para iniciar y controlar una partida.
 *
 * <p>La presentacion no necesita conocer todos los objetos internos del
 * partido. La sesion valida la combinacion de modalidad y tipo de partido,
 * crea el agregado, instala los controladores automaticos que correspondan y
 * expone operaciones de ciclo de vida y un resultado inmutable.</p>
 */
public final class GameSession implements Serializable {

    private static final long serialVersionUID = 1L;


    private final MatchConfig config;
    private final DOPOSoccer match;
    private boolean started;

    private GameSession(MatchConfig config) throws DOPOSoccerException {
        if (!config.mode().supports(config.matchType())) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MODE);
        }
        this.config = config;
        this.match = new DOPOSoccer(
                config.leftProfile(), config.leftCharacter(),
                config.rightProfile(), config.rightCharacter(),
                config.matchType());
        configureMachineSides();
        this.started = true;
    }

    /**
     * Crea una sesion a partir de una configuracion valida.
     *
     * @param config configuracion del partido
     * @return sesion lista para actualizarse
     * @throws DOPOSoccerException si la modalidad no admite el tipo de partido
     */
    public static GameSession create(MatchConfig config) throws DOPOSoccerException {
        if (config == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ELEMENT);
        }
        return new GameSession(config);
    }

    /**
     * Al abrir una partida guardada los controladores no vienen en el archivo
     * (son transitorios), asi que se vuelven a instalar los de la maquina.
     *
     * @param in flujo de lectura
     * @throws java.io.IOException si falla la lectura
     * @throws ClassNotFoundException si falta una clase del archivo
     */
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        configureMachineSides();
    }

    private void configureMachineSides() {
        for (Side side : Side.values()) {
            if (config.mode().isMachine(side)) {
                match.setController(side, config.machineOf(side).create());
            }
        }
    }

    /**
     * Avanza el partido si la sesion esta iniciada.
     *
     * @param dt tiempo transcurrido en segundos
     */
    public void update(double dt) {
        if (started) {
            match.step(dt);
        }
    }

    public void pause() {
        match.pause();
    }

    public void resume() {
        match.resume();
    }

    public void togglePause() {
        match.togglePause();
    }

    /**
     * Termina el partido de inmediato, por ejemplo cuando el usuario elige
     * abandonar.
     */
    public void finish() {
        match.finish();
    }

    /**
     * Asigna un controlador a un lado. La presentacion lo usa para instalar
     * los controladores humanos; la sesion ya instalo los de maquina.
     *
     * @param side lado controlado
     * @param controller controlador
     */
    public void setController(Side side, PlayerController controller) {
        match.setController(side, controller);
    }

    public MatchConfig config() {
        return config;
    }

    public GameMode mode() {
        return config.mode();
    }

    public DOPOSoccer match() {
        return match;
    }

    public boolean isPaused() {
        return match.isPaused();
    }

    public boolean isFinished() {
        return match.isFinished();
    }

    /**
     * Indica si un lado es humano en esta modalidad.
     *
     * @param side lado consultado
     * @return {@code true} si no es controlado por la maquina
     */
    public boolean isHumanSide(Side side) {
        return !config.mode().isMachine(side);
    }

    /**
     * Construye el resultado del partido. Si aun no termino, los puntos
     * ganados valen cero.
     *
     * @return resultado inmutable
     */
    public MatchResult result() {
        return new MatchResult(
                match.getWinnerSide(),
                match.goalsOf(Side.LEFT),
                match.goalsOf(Side.RIGHT),
                match.getPlayer(Side.LEFT).getBalls(),
                match.getPlayer(Side.RIGHT).getBalls(),
                match.getPointsEarned(Side.LEFT),
                match.getPointsEarned(Side.RIGHT),
                config.matchType().isSurvival(),
                config.matchType().describe());
    }

    /**
     * Crea una sesion nueva con la misma configuracion. Los perfiles se
     * comparten, por lo que los puntos y mejoras se conservan.
     *
     * @return sesion nueva
     * @throws DOPOSoccerException si la configuracion dejo de ser valida
     */
    public GameSession restart() throws DOPOSoccerException {
        return create(config);
    }
}
