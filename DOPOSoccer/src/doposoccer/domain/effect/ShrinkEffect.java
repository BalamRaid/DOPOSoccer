package doposoccer.domain.effect;

/**
 * Reduccion (China): encoge al rival a la mitad de su tamano durante cuatro
 * segundos.
 *
 * <p>El radio reducido afecta las colisiones, el alcance del pie y el dibujo
 * del personaje. El jugador reducido conserva el control de sus acciones.</p>
 */
public class ShrinkEffect extends Effect {

    /** Duracion de la reduccion, en segundos. */
    public static final double DURATION = 4;

    /** Factor de tamano aplicado al cuerpo del rival. */
    public static final double SIZE_FACTOR = 0.5;

    /**
     * Crea el efecto de reduccion.
     */
    public ShrinkEffect() {
        super(DURATION);
    }

    @Override
    public String getName() {
        return "Reducido";
    }

    @Override
    public double sizeFactor() {
        return SIZE_FACTOR;
    }
}
