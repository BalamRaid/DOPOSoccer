package doposoccer.domain.effect;

/**
 * Electrocutado (Camerun): no puede saltar y se mueve al 50 % de su
 * velocidad.
 *
 * <p>El efecto dura tres segundos y no impide patear.</p>
 */
public class ElectrocutedEffect extends Effect {

    /**
     * Crea el efecto de electrocutado con una duracion de tres segundos.
     */
    public ElectrocutedEffect() {
        super(3);
    }

    @Override
    public String getName() {
        return "Electrocutado";
    }

    @Override
    public double speedFactor() {
        return 0.5;
    }

    @Override
    public boolean canJump() {
        return false;
    }
}
