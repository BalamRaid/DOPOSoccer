package doposoccer.domain.match;

import java.io.Serializable;

import doposoccer.domain.Side;
import java.util.EnumMap;
import java.util.Map;

/**
 * Marcador de goles por lado. Es la unica fuente de verdad de los goles de un
 * partido, de modo que las entidades no mezclan su estado fisico con el
 * resultado.
 */
public final class Score implements Serializable {

    private static final long serialVersionUID = 1L;


    private final Map<Side, Integer> goals = new EnumMap<>(Side.class);

    /**
     * Crea un marcador 0 - 0.
     */
    public Score() {
        reset();
    }

    /**
     * Suma un gol al lado indicado.
     *
     * @param side lado que anota
     */
    public void addGoal(Side side) {
        requireSide(side);
        goals.merge(side, 1, Integer::sum);
    }

    /**
     * Devuelve los goles del lado indicado.
     *
     * @param side lado consultado
     * @return goles anotados
     */
    public int goalsOf(Side side) {
        requireSide(side);
        return goals.get(side);
    }

    /**
     * Devuelve el lado que va ganando, o {@code null} si hay empate.
     *
     * @return lider del marcador
     */
    public Side leader() {
        int left = goalsOf(Side.LEFT);
        int right = goalsOf(Side.RIGHT);
        if (left > right) {
            return Side.LEFT;
        }
        if (right > left) {
            return Side.RIGHT;
        }
        return null;
    }

    /**
     * Indica si ambos lados tienen los mismos goles.
     *
     * @return {@code true} si el marcador esta empatado
     */
    public boolean isTied() {
        return leader() == null;
    }

    /**
     * Suma los goles de ambos lados.
     *
     * @return total de goles del partido
     */
    public int totalGoals() {
        return goalsOf(Side.LEFT) + goalsOf(Side.RIGHT);
    }

    /**
     * Vuelve a dejar el marcador en 0 - 0.
     */
    public void reset() {
        goals.put(Side.LEFT, 0);
        goals.put(Side.RIGHT, 0);
    }

    private static void requireSide(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
    }
}
