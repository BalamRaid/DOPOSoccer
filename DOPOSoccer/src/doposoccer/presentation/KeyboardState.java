package doposoccer.presentation;

import java.util.HashSet;
import java.util.Set;

/**
 * Estado del teclado compartido por los dos jugadores.
 *
 * <p>Distingue entre teclas mantenidas y teclas recien presionadas. Las
 * acciones de salto, patada y poder consumen la marca de "recien presionada"
 * para que un solo toque produzca una sola accion, mientras que el movimiento
 * usa las teclas mantenidas.</p>
 */
public final class KeyboardState {

    private final Set<Integer> pressed = new HashSet<>();
    private final Set<Integer> justPressed = new HashSet<>();
    private boolean rightControlDown;
    private boolean rightControlJustPressed;

    /**
     * Marca una tecla como presionada.
     *
     * @param keyCode codigo de la tecla
     */
    public void press(int keyCode) {
        if (pressed.add(keyCode)) {
            justPressed.add(keyCode);
        }
    }

    /**
     * Marca una tecla como liberada.
     *
     * @param keyCode codigo de la tecla
     */
    public void release(int keyCode) {
        pressed.remove(keyCode);
    }

    /**
     * Marca el Control derecho como presionado. Java no distingue ambos
     * controles por codigo, por eso la presentacion informa la ubicacion.
     */
    public void pressRightControl() {
        if (!rightControlDown) {
            rightControlDown = true;
            rightControlJustPressed = true;
        }
    }

    /**
     * Marca el Control derecho como liberado.
     */
    public void releaseRightControl() {
        rightControlDown = false;
    }

    /**
     * Indica si una tecla esta mantenida.
     *
     * @param keyCode codigo de la tecla
     * @return {@code true} si esta presionada
     */
    public boolean isPressed(int keyCode) {
        return pressed.contains(keyCode);
    }

    /**
     * Indica si el Control derecho esta mantenido.
     *
     * @return {@code true} si esta presionado
     */
    public boolean isRightControlPressed() {
        return rightControlDown;
    }

    /**
     * Consume la marca de tecla recien presionada.
     *
     * @param keyCode codigo de la tecla
     * @return {@code true} si la tecla se presiono desde la ultima consulta
     */
    public boolean consumeJustPressed(int keyCode) {
        return justPressed.remove(keyCode);
    }

    /**
     * Consume la marca del Control derecho recien presionado.
     *
     * @return {@code true} si se presiono desde la ultima consulta
     */
    public boolean consumeRightControlJustPressed() {
        boolean value = rightControlJustPressed;
        rightControlJustPressed = false;
        return value;
    }

    /**
     * Limpia todo el estado, por ejemplo al cambiar de pantalla.
     */
    public void clear() {
        pressed.clear();
        justPressed.clear();
        rightControlDown = false;
        rightControlJustPressed = false;
    }
}
