package doposoccer.domain;

/**
 * Partido de supervivencia: cada jugador empieza con una cantidad de balones.
 * Cada vez que un jugador anota, el rival pierde un balon; pierde el partido
 * quien se quede sin balones primero.
 *
 * <p>No tiene limite de tiempo. Es el tipo de partido que usa la modalidad
 * Jugador vs Maquina para jugar una serie de rondas.</p>
 */
public class SurvivalMatch extends MatchType {

    /** Cantidad de balones usada por defecto. */
    public static final int DEFAULT_BALLS = 3;

    /** Cantidad minima de balones. */
    public static final int MIN_BALLS = 1;

    /** Cantidad maxima de balones. */
    public static final int MAX_BALLS = 9;

    private final int ballsPerPlayer;

    /**
     * Crea un partido de supervivencia.
     *
     * @param ballsPerPlayer balones iniciales de cada jugador
     * @throws DOPOSoccerException si la cantidad esta fuera del rango
     */
    public SurvivalMatch(int ballsPerPlayer) throws DOPOSoccerException {
        if (ballsPerPlayer < MIN_BALLS || ballsPerPlayer > MAX_BALLS) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_BALLS);
        }
        this.ballsPerPlayer = ballsPerPlayer;
    }

    /**
     * Crea el partido de supervivencia con la cantidad oficial por defecto.
     *
     * @return partido de supervivencia con tres balones por jugador
     * @throws DOPOSoccerException si la configuracion no es valida
     */
    public static SurvivalMatch standard() throws DOPOSoccerException {
        return new SurvivalMatch(DEFAULT_BALLS);
    }

    @Override
    public int initialBalls() {
        return ballsPerPlayer;
    }

    @Override
    public boolean isSurvival() {
        return true;
    }

    @Override
    public boolean isOver(DOPOSoccer game) {
        return game.getPlayer(Side.LEFT).getBalls() <= 0
                || game.getPlayer(Side.RIGHT).getBalls() <= 0;
    }

    /**
     * Gana el jugador que conserva mas balones. Si ambos llegan a cero, el
     * resultado es empate.
     *
     * @param game partido terminado
     * @return lado ganador, o {@code null} si ambos quedaron sin balones
     */
    @Override
    public Side winner(DOPOSoccer game) {
        int left = game.getPlayer(Side.LEFT).getBalls();
        int right = game.getPlayer(Side.RIGHT).getBalls();
        if (left > right) {
            return Side.LEFT;
        }
        if (right > left) {
            return Side.RIGHT;
        }
        return null;
    }

    @Override
    public String describe() {
        return "Supervivencia (" + ballsPerPlayer + " balones)";
    }
}
