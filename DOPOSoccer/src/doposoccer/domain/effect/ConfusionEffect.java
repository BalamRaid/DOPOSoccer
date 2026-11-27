package doposoccer.domain.effect;

/**
 * Confusion (Estados Unidos): invierte los controles de movimiento del rival.
 *
 * <p>El efecto no bloquea movimiento, salto ni patada; su influencia es sobre
 * la direccion que el jugador pretende tomar. Los balones falsos que crea el
 * tiro de Ilusion son administrados por el partido y duran lo mismo que este
 * efecto.</p>
 */
public class ConfusionEffect extends Effect {

    /** Duracion del efecto y de los balones falsos, en segundos. */
    public static final double DURATION = 2.5;

    /**
     * Crea el efecto de confusion con su duracion predefinida.
     */
    public ConfusionEffect() {
        super(DURATION);
    }

    @Override
    public String getName() {
        return "Confusion";
    }

    @Override
    public boolean invertControls() {
        return true;
    }
}
