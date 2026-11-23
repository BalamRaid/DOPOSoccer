package doposoccer.domain.effect;

/**
 * Congelado (Canada): el rival queda inmovilizado dentro de un bloque de
 * hielo durante tres segundos.
 *
 * <p>No puede moverse, saltar ni patear. Es un efecto de control puro: no
 * cambia la vida ni el tamano del personaje.</p>
 */
public class FreezeEffect extends Effect {

    /** Duracion del congelamiento, en segundos. */
    public static final double DURATION = 3;

    /**
     * Crea el efecto de congelamiento.
     */
    public FreezeEffect() {
        super(DURATION);
    }

    @Override
    public String getName() {
        return "Congelado";
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
