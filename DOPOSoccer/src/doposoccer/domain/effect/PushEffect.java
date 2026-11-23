package doposoccer.domain.effect;

import doposoccer.domain.Player;

/**
 * Empuje (Corea del Sur): el rival es lanzado hacia su propio arco.
 *
 * <p>Mientras dura el efecto el jugador no puede moverse por voluntad propia;
 * su velocidad horizontal la impone el efecto y disminuye proporcionalmente
 * al tiempo restante.</p>
 */
public class PushEffect extends Effect {

    /** Velocidad horizontal inicial del empuje. */
    public static final double PUSH_SPEED = 650;

    /**
     * Crea el efecto de empuje con una duracion de 1,5 segundos.
     */
    public PushEffect() {
        super(1.5);
    }

    @Override
    public String getName() {
        return "Empuje";
    }

    /**
     * Actualiza la duracion y aplica al rival una velocidad horizontal que
     * decrece con el tiempo restante.
     *
     * @param target jugador empujado
     * @param dt tiempo transcurrido en segundos
     */
    @Override
    public void update(Player target, double dt) {
        super.update(target, dt);
        double strength = getRemaining() / getDuration();
        target.setVx(-target.getSide().attackDirection() * PUSH_SPEED * strength);
    }

    @Override
    public boolean canMove() {
        return false;
    }
}
