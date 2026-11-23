package doposoccer.domain.control;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;
import doposoccer.domain.math.Vec2;

/**
 * Controlador automatico basico y determinista.
 *
 * <p>La maquina se coloca detras del balon para empujarlo hacia el arco rival,
 * salta cuando el balon esta alto y patea o usa el poder cuando esta cerca.
 * Lee solo la {@link MatchView} y produce las mismas intenciones que un
 * jugador humano, de modo que el partido no distingue entre ambos.</p>
 */
public final class AiController implements PlayerController {

    /** Distancia horizontal muerta para evitar oscilaciones. */
    private static final double DEAD_ZONE = 7;

    /** Distancia maxima al balon para intentar un tiro de poder. */
    private static final double SPECIAL_DISTANCE = 165;

    /**
     * Constructor por defecto. La maquina no guarda estado entre pasos, por
     * lo que dos instancias con la misma vista producen la misma intencion.
     */
    public AiController() {
    }

    @Override
    public PlayerIntent decide(MatchView view, Side side) {
        return evaluate(view, side);
    }

    /**
     * Calcula la intencion de la maquina para una vista y un lado.
     *
     * @param view vista de solo lectura del partido
     * @param side lado controlado
     * @return intencion calculada
     */
    public static PlayerIntent evaluate(MatchView view, Side side) {
        if (view == null || side == null) {
            throw new IllegalArgumentException("La vista y el lado son obligatorios.");
        }
        if (view.isPaused() || view.isFinished() || view.isStunned(side)) {
            return PlayerIntent.IDLE;
        }

        Vec2 ball = view.ballPosition();
        Vec2 me = view.playerPosition(side);
        double attackDirection = side.attackDirection();
        double bodyRadius = view.bodyRadiusOf(side);
        double distanceToBall = me.distanceTo(ball);

        double targetX = ball.x() - attackDirection * (bodyRadius + Ball.RADIUS + 6);
        double delta = targetX - me.x();
        int moveDirection = Math.abs(delta) <= DEAD_ZONE ? 0 : (delta > 0 ? 1 : -1);

        boolean highBall = ball.y() < me.y() - bodyRadius * 1.15;
        boolean nearHorizontally = Math.abs(ball.x() - me.x()) <= bodyRadius * 2.2;
        boolean jump = highBall && nearHorizontally && view.isOnGround(side);

        boolean kick = view.canKick(side);
        boolean special = view.powerBarOf(side) >= 1
                && !view.hasAura(side)
                && distanceToBall <= SPECIAL_DISTANCE;

        return new PlayerIntent(moveDirection, jump, kick, special);
    }
}
