package doposoccer.domain.match;

import doposoccer.domain.Side;
import doposoccer.domain.math.Vec2;
import java.util.Optional;

/**
 * Regla de deteccion de goles. Trabaja solo con geometria y no modifica el
 * partido, lo que permite probarla de forma aislada.
 */
public final class GoalDetector {

    /**
     * Constructor privado: la clase solo expone utilidades estaticas.
     */
    private GoalDetector() {
    }

    /**
     * Determina que lado anota cuando el balon esta completamente dentro de un
     * arco. Si el balon entra en el arco izquierdo, anota el jugador derecho, y
     * viceversa.
     *
     * @param ballCenter centro del balon
     * @param ballRadius radio del balon
     * @param arena cancha consultada
     * @return lado que anota, o vacio si aun no hay gol
     */
    public static Optional<Side> scorer(Vec2 ballCenter, double ballRadius, Arena arena) {
        if (ballCenter == null || arena == null) {
            throw new IllegalArgumentException("El balon y la cancha son obligatorios.");
        }
        if (ballRadius <= 0) {
            throw new IllegalArgumentException("El radio del balon debe ser positivo.");
        }
        if (arena.goalOf(Side.LEFT).containsFully(ballCenter, ballRadius)) {
            return Optional.of(Side.RIGHT);
        }
        if (arena.goalOf(Side.RIGHT).containsFully(ballCenter, ballRadius)) {
            return Optional.of(Side.LEFT);
        }
        return Optional.empty();
    }
}
