package doposoccer.application;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Side;

/**
 * Serie de rondas de supervivencia para la modalidad Jugador vs Maquina.
 *
 * <p>El jugador humano ocupa el lado izquierdo. Cada ronda es un partido de
 * {@link doposoccer.domain.SurvivalMatch}; si el humano gana, la serie
 * continua con otra ronda y el marcador de rondas aumenta. Si la maquina gana
 * o hay empate, la serie termina.</p>
 */
public final class SurvivalSeries {

    private final MatchConfig baseConfig;
    private int round = 1;
    private int humanWins;
    private int machineWins;
    private boolean over;

    /**
     * Crea una serie de supervivencia.
     *
     * @param baseConfig configuracion base; debe ser PvsM con supervivencia
     * @throws DOPOSoccerException si la configuracion no es de supervivencia
     *                             o no es Jugador vs Maquina
     */
    public SurvivalSeries(MatchConfig baseConfig) throws DOPOSoccerException {
        if (baseConfig == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ELEMENT);
        }
        if (baseConfig.mode() != GameMode.PVM || !baseConfig.matchType().isSurvival()) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MODE);
        }
        this.baseConfig = baseConfig;
    }

    /**
     * Crea la sesion de la ronda actual.
     *
     * @return sesion lista para jugar
     * @throws DOPOSoccerException si la serie ya termino o la configuracion no
     *                             es valida
     */
    public GameSession startRound() throws DOPOSoccerException {
        if (over) {
            throw new DOPOSoccerException(DOPOSoccerException.GAME_FINISHED);
        }
        return GameSession.create(baseConfig);
    }

    /**
     * Registra el resultado de una ronda.
     *
     * @param result resultado del partido
     */
    public void record(MatchResult result) {
        if (result == null || over) {
            return;
        }
        if (result.winner() == Side.LEFT) {
            humanWins++;
            round++;
        } else if (result.winner() == Side.RIGHT) {
            machineWins++;
            over = true;
        } else {
            over = true;
        }
    }

    public MatchConfig config() {
        return baseConfig;
    }

    public int round() {
        return round;
    }

    public int humanWins() {
        return humanWins;
    }

    public int machineWins() {
        return machineWins;
    }

    public boolean isOver() {
        return over;
    }
}
