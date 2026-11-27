package doposoccer.domain.control;

import doposoccer.domain.MatchType;
import doposoccer.domain.Side;
import doposoccer.domain.match.Arena;
import doposoccer.domain.math.Vec2;

/**
 * Vista de solo lectura del partido para controladores y presentacion.
 *
 * <p>No expone setters ni permite modificar el estado de las entidades. La
 * inteligencia artificial y el adaptador de teclado dependen de esta interfaz,
 * no del agregado completo del partido.</p>
 */
public interface MatchView {

    Arena arena();

    Vec2 ballPosition();

    Vec2 ballVelocity();

    Vec2 playerPosition(Side side);

    Vec2 playerVelocity(Side side);

    double bodyRadiusOf(Side side);

    /**
     * Indica si una patada en este instante tendria efecto: el balon esta al
     * alcance del pie o el rival esta al alcance del dano de la patada.
     *
     * @param side lado consultado
     * @return {@code true} si la intencion de patada no se desperdiciaria
     */
    boolean canKick(Side side);

    int goalsOf(Side side);

    int ballsOf(Side side);

    double remainingSeconds();

    boolean isPaused();

    boolean isFinished();

    boolean isSuddenDeath();

    double powerBarOf(Side side);

    boolean hasAura(Side side);

    boolean isStunned(Side side);

    boolean isOnGround(Side side);

    String effectNameOf(Side side);

    double effectRemainingOf(Side side);

    MatchType matchType();
}
