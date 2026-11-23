package doposoccer.domain;

import java.util.Set;

/**
 * Partido por goles: gana el primero que llegue a la meta fijada.
 *
 * <p>El reglamento usa metas de 3, 5 o 7 goles. El constructor conserva el
 * rango amplio anterior para no romper configuraciones historicas, pero la
 * interfaz solo ofrece las metas oficiales mediante {@link #standard(int)}.
 * No tiene limite de tiempo.</p>
 */
public class GoalMatch extends MatchType {

    /** Cantidad minima aceptada por el constructor historico. */
    public static final int MIN_GOALS = 1;

    /** Cantidad maxima aceptada por el constructor historico. */
    public static final int MAX_GOALS = 20;

    /** Metas oficiales del reglamento. */
    public static final Set<Integer> STANDARD_GOALS = Set.of(3, 5, 7);

    private final int goalsToWin;

    /**
     * Crea un partido por goles.
     *
     * @param goalsToWin cantidad de goles necesaria para ganar
     * @throws DOPOSoccerException si la cantidad esta fuera del rango aceptado
     */
    public GoalMatch(int goalsToWin) throws DOPOSoccerException {
        if (goalsToWin < MIN_GOALS || goalsToWin > MAX_GOALS) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_GOALS);
        }
        this.goalsToWin = goalsToWin;
    }

    /**
     * Crea un partido con una de las metas oficiales: 3, 5 o 7 goles.
     *
     * @param goalsToWin meta oficial
     * @return partido configurado
     * @throws DOPOSoccerException si la meta no es oficial
     */
    public static GoalMatch standard(int goalsToWin) throws DOPOSoccerException {
        if (!STANDARD_GOALS.contains(goalsToWin)) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_GOALS);
        }
        return new GoalMatch(goalsToWin);
    }

    public int getGoalsToWin() {
        return goalsToWin;
    }

    @Override
    public boolean isOver(DOPOSoccer game) {
        return game.goalsOf(Side.LEFT) >= goalsToWin
                || game.goalsOf(Side.RIGHT) >= goalsToWin;
    }

    @Override
    public String describe() {
        return "Por goles (" + goalsToWin + ")";
    }
}
