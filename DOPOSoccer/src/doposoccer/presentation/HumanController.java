package doposoccer.presentation;

import doposoccer.domain.Side;
import doposoccer.domain.control.MatchView;
import doposoccer.domain.control.PlayerController;
import doposoccer.domain.control.PlayerIntent;
import java.util.Objects;

/**
 * Adaptador de presentacion: traduce el estado del teclado a
 * {@link PlayerIntent}.
 *
 * <p>Vive fuera del dominio porque conoce las teclas, pero implementa el
 * mismo contrato que la maquina. El partido no sabe si la intencion viene de
 * una persona o de un controlador automatico.</p>
 */
public final class HumanController implements PlayerController {

    private final KeyboardState keyboard;
    private final KeyBindings bindings;

    /**
     * Crea el controlador de un jugador humano.
     *
     * @param keyboard estado compartido del teclado
     * @param bindings teclas asignadas al jugador
     */
    public HumanController(KeyboardState keyboard, KeyBindings bindings) {
        this.keyboard = Objects.requireNonNull(keyboard, "keyboard no puede ser null.");
        this.bindings = Objects.requireNonNull(bindings, "bindings no puede ser null.");
    }

    @Override
    public PlayerIntent decide(MatchView view, Side side) {
        Objects.requireNonNull(view, "view no puede ser null.");
        Objects.requireNonNull(side, "side no puede ser null.");
        if (view.isPaused() || view.isFinished()) {
            return PlayerIntent.IDLE;
        }
        int direction = 0;
        if (keyboard.isPressed(bindings.left())) {
            direction--;
        }
        if (keyboard.isPressed(bindings.right())) {
            direction++;
        }
        boolean jump = keyboard.consumeJustPressed(bindings.jump());
        boolean kick = keyboard.consumeJustPressed(bindings.kick());
        boolean special = bindings.rightControlPower()
                ? keyboard.consumeRightControlJustPressed()
                : keyboard.consumeJustPressed(bindings.power());
        return new PlayerIntent(Integer.signum(direction), jump, kick, special);
    }
}
