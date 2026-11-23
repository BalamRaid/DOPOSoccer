package doposoccer.domain;

/**
 * Tipo de partido: define cuando termina y quien gana. Se puede extender con
 * nuevos tipos sin tocar el agregado del partido.
 *
 * <p>{@link DOPOSoccer} consulta {@link #isOver(DOPOSoccer)} despues de
 * actualizar cada cuadro y usa {@link #winner(DOPOSoccer)} para repartir los
 * puntos. Los tipos con tiempo informan su duracion con
 * {@link #durationSeconds()}; los tipos sin tiempo devuelven un valor
 * negativo.</p>
 *
 * @see GoalMatch
 * @see TimeMatch
 * @see SurvivalMatch
 */
public abstract class MatchType {

    /**
     * Indica si el partido debe terminar en el estado actual.
     *
     * @param game partido que se esta evaluando
     * @return {@code true} si se cumplio la condicion de fin
     */
    public abstract boolean isOver(DOPOSoccer game);

    /**
     * Devuelve los segundos restantes del partido.
     *
     * @param game partido que se esta evaluando
     * @return segundos restantes, o {@code -1} si el tipo no tiene limite
     */
    public double remainingSeconds(DOPOSoccer game) {
        return -1;
    }

    /**
     * Devuelve una descripcion corta del tipo de partido para el HUD.
     *
     * @return texto legible, por ejemplo "Por goles (5)"
     */
    public abstract String describe();

    /**
     * Duracion maxima del partido en segundos.
     *
     * @return duracion, o {@code -1} si no hay limite de tiempo
     */
    public double durationSeconds() {
        return -1;
    }

    /**
     * Cantidad inicial de balones por jugador en el modo supervivencia.
     *
     * @return balones iniciales, o {@code 0} si el tipo no usa balones
     */
    public int initialBalls() {
        return 0;
    }

    /**
     * Indica si este tipo de partido es de supervivencia.
     *
     * @return {@code true} para {@link SurvivalMatch}
     */
    public boolean isSurvival() {
        return false;
    }

    /**
     * Determina el ganador segun la regla del tipo de partido. Por defecto
     * gana quien tenga mas goles; los tipos especiales pueden sobrescribirlo.
     *
     * @param game partido terminado
     * @return lado ganador, o {@code null} si hay empate
     */
    public Side winner(DOPOSoccer game) {
        int left = game.goalsOf(Side.LEFT);
        int right = game.goalsOf(Side.RIGHT);
        if (left > right) {
            return Side.LEFT;
        }
        if (right > left) {
            return Side.RIGHT;
        }
        return null;
    }
}
