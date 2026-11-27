package doposoccer.presentation;

import java.awt.event.KeyEvent;

/**
 * Teclas asignadas a un jugador humano.
 *
 * <p>El jugador 1 usa A/D para moverse, W para saltar, S para patear y Q para
 * el poder. El jugador 2 usa las flechas y el Control derecho, tal como pide
 * el reglamento. Como Java representa ambos controles con el mismo codigo, la
 * marca {@code rightControlPower} indica que se debe consultar la ubicacion
 * fisica de la tecla.</p>
 *
 * @param left tecla para mover a la izquierda
 * @param right tecla para mover a la derecha
 * @param jump tecla para saltar
 * @param kick tecla para patear
 * @param power tecla para activar el poder
 * @param rightControlPower indica que el poder se activa con Control derecho
 */
public record KeyBindings(int left, int right, int jump, int kick, int power,
        boolean rightControlPower) {

    /**
     * Crea las teclas del jugador 1.
     *
     * @return A/D/W/S/Q
     */
    public static KeyBindings playerOne() {
        return new KeyBindings(KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_Q, false);
    }

    /**
     * Crea las teclas del jugador 2.
     *
     * @return flechas y Control derecho
     */
    public static KeyBindings playerTwo() {
        return new KeyBindings(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_CONTROL, true);
    }
}
