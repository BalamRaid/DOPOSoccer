package doposoccer.domain.effect;

import doposoccer.domain.Player;

/**
 * Inconsciente (Nigeria): lanzado al aire, no puede moverse, saltar ni
 * patear.
 *
 * <p>Al aplicarse se impone un impulso vertical negativo (hacia arriba) y el
 * efecto dura tres segundos.</p>
 */
public class UnconsciousEffect extends Effect {

    /** Impulso vertical aplicado al jugador cuando recibe el efecto. */
    public static final double LAUNCH_SPEED = 750;

    /**
     * Crea el efecto de inconsciencia con una duracion de tres segundos.
     */
    public UnconsciousEffect() {
        super(3);
    }

    @Override
    public String getName() {
        return "Inconsciente";
    }

    /**
     * Lanza al jugador hacia arriba cuando recibe el efecto.
     *
     * @param target jugador afectado
     */
    @Override
    public void onApply(Player target) {
        target.setVy(-LAUNCH_SPEED);
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean canKick() {
        return false;
    }
}
