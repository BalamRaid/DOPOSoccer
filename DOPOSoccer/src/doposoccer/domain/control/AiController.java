package doposoccer.domain.control;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;
import doposoccer.domain.math.Vec2;

/**
 * Jugador maquina. Define el comportamiento comun de todos los perfiles
 * (patron plantilla) y deja a cada subclase decidir como juega.
 *
 * <p>La maquina lee solo la {@link MatchView} y produce las mismas
 * intenciones que un jugador humano, de modo que el partido no distingue
 * entre ambos. Si el partido esta en pausa o terminado, o la maquina esta
 * aturdida, no hace nada; en otro caso delega en {@link #think}.</p>
 *
 * <p>Para agregar un perfil nuevo se extiende esta clase y se registra en
 * {@link MachineProfile}.</p>
 */
public abstract class AiController implements PlayerController {

    /** Distancia horizontal muerta para evitar oscilaciones. */
    protected static final double DEAD_ZONE = 7;

    /** Distancia maxima al balon para intentar un tiro de poder. */
    protected static final double SPECIAL_DISTANCE = 165;

    @Override
    public final PlayerIntent decide(MatchView view, Side side) {
        if (view == null || side == null) {
            throw new IllegalArgumentException("La vista y el lado son obligatorios.");
        }
        if (view.isPaused() || view.isFinished() || view.isStunned(side)) {
            return PlayerIntent.IDLE;
        }
        return think(view, side);
    }

    /**
     * Decide la intencion de este perfil cuando la maquina puede jugar.
     *
     * @param view vista de solo lectura del partido
     * @param side lado controlado
     * @return intencion de este paso
     */
    protected abstract PlayerIntent think(MatchView view, Side side);

    /**
     * Perfil al que pertenece esta maquina.
     *
     * @return perfil
     */
    public abstract MachineProfile profile();

    // ---------------- Comportamientos reutilizables ----------------

    /**
     * Ataque: se coloca detras del balon para empujarlo hacia el arco rival,
     * salta si el balon viene alto y patea o usa el poder cuando esta cerca.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return intencion ofensiva
     */
    protected static PlayerIntent attack(MatchView view, Side side) {
        Vec2 ball = view.ballPosition();
        Vec2 me = view.playerPosition(side);
        double targetX = ball.x() - side.attackDirection()
                * (view.bodyRadiusOf(side) + Ball.RADIUS + 6);
        return new PlayerIntent(moveToward(targetX, me.x()), shouldJump(view, side),
                view.canKick(side), canUseSpecial(view, side));
    }

    /**
     * Defensa: si el balon esta en la mitad propia lo sale a despejar; si no,
     * vuelve a una posicion cercana a su arco y espera.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return intencion defensiva
     */
    protected static PlayerIntent defend(MatchView view, Side side) {
        if (ballInOwnHalf(view, side)) {
            return attack(view, side);
        }
        double width = view.arena().width();
        double homeX = side == Side.LEFT ? width * 0.15 : width * 0.85;
        Vec2 me = view.playerPosition(side);
        return new PlayerIntent(moveToward(homeX, me.x()), shouldJump(view, side),
                view.canKick(side), false);
    }

    /**
     * Direccion para acercarse a una posicion horizontal.
     *
     * @param targetX posicion deseada
     * @param x posicion actual
     * @return -1, 0 o 1
     */
    protected static int moveToward(double targetX, double x) {
        double delta = targetX - x;
        if (Math.abs(delta) <= DEAD_ZONE) {
            return 0;
        }
        return delta > 0 ? 1 : -1;
    }

    /**
     * Indica si conviene saltar: el balon viene alto, esta cerca y la maquina
     * esta en el suelo.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return true si debe saltar
     */
    protected static boolean shouldJump(MatchView view, Side side) {
        Vec2 ball = view.ballPosition();
        Vec2 me = view.playerPosition(side);
        double bodyRadius = view.bodyRadiusOf(side);
        boolean highBall = ball.y() < me.y() - bodyRadius * 1.15;
        boolean near = Math.abs(ball.x() - me.x()) <= bodyRadius * 2.2;
        return highBall && near && view.isOnGround(side);
    }

    /**
     * Indica si puede activar el poder: barra llena, sin aura y cerca del balon.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return true si conviene activar el poder
     */
    protected static boolean canUseSpecial(MatchView view, Side side) {
        return view.powerBarOf(side) >= 1
                && !view.hasAura(side)
                && view.playerPosition(side).distanceTo(view.ballPosition()) <= SPECIAL_DISTANCE;
    }

    /**
     * Indica si el balon esta en la mitad de cancha que defiende el lado.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return true si el balon esta en la mitad propia
     */
    protected static boolean ballInOwnHalf(MatchView view, Side side) {
        double middle = view.arena().width() / 2;
        double x = view.ballPosition().x();
        return side == Side.LEFT ? x < middle : x > middle;
    }
}
