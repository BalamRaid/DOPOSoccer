package doposoccer.domain.control;

import doposoccer.domain.Side;

import java.util.Random;

/**
 * Maquina torpe: escoge al azar sus movimientos.
 *
 * <p>Para que no tiemble cuadro a cuadro, mantiene cada decision al azar
 * durante varios pasos antes de escoger otra. El generador se puede fijar
 * con una semilla para que las pruebas sean repetibles.</p>
 */
public final class ClumsyMachine extends AiController {

    /** Pasos que dura cada decision al azar (unos 0,3 s a 60 pasos por segundo). */
    public static final int STEPS_PER_DECISION = 20;

    private final Random random;
    private PlayerIntent current = PlayerIntent.IDLE;
    private int stepsLeft;

    /**
     * Crea una maquina torpe con azar real.
     */
    public ClumsyMachine() {
        this(new Random());
    }

    /**
     * Crea una maquina torpe con el generador indicado.
     *
     * @param random generador de numeros al azar
     */
    public ClumsyMachine(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("El generador no puede ser null.");
        }
        this.random = random;
    }

    @Override
    protected PlayerIntent think(MatchView view, Side side) {
        if (stepsLeft <= 0) {
            int direction = random.nextInt(3) - 1;
            boolean jump = random.nextInt(10) == 0 && view.isOnGround(side);
            boolean kick = random.nextInt(4) == 0 && view.canKick(side);
            boolean special = view.powerBarOf(side) >= 1 && !view.hasAura(side)
                    && random.nextInt(3) == 0;
            current = new PlayerIntent(direction, jump, kick, special);
            stepsLeft = STEPS_PER_DECISION;
        }
        stepsLeft--;
        return current;
    }

    @Override
    public MachineProfile profile() {
        return MachineProfile.CLUMSY;
    }
}
