package doposoccer.domain.control;

/**
 * Acciones solicitadas por un controlador durante un paso del juego.
 *
 * <p>Es un valor inmutable y comun para humanos y maquinas. La direccion de
 * movimiento se expresa en el sistema de la pantalla: {@code -1} izquierda,
 * {@code 0} quieto y {@code 1} derecha. Los efectos de confusion invierten la
 * direccion dentro del jugador, no dentro del controlador.</p>
 *
 * @param moveDirection direccion deseada: -1, 0 o 1
 * @param jump solicita saltar
 * @param kick solicita patear
 * @param special solicita activar el poder
 */
public record PlayerIntent(int moveDirection, boolean jump, boolean kick, boolean special) {

    /** Intencion sin ninguna accion. */
    public static final PlayerIntent IDLE = new PlayerIntent(0, false, false, false);

    /**
     * Valida que la direccion este en el rango permitido.
     */
    public PlayerIntent {
        if (moveDirection < -1 || moveDirection > 1) {
            throw new IllegalArgumentException("moveDirection debe ser -1, 0 o 1.");
        }
    }

    /**
     * Crea una intencion que solo mueve al jugador.
     *
     * @param direction direccion deseada
     * @return intencion de movimiento
     */
    public static PlayerIntent move(int direction) {
        return new PlayerIntent(direction, false, false, false);
    }

    /**
     * Crea una intencion de salto.
     *
     * @param direction direccion de movimiento simultanea
     * @return intencion con salto
     */
    public static PlayerIntent jump(int direction) {
        return new PlayerIntent(direction, true, false, false);
    }

    /**
     * Crea una intencion de patada.
     *
     * @param direction direccion de movimiento simultanea
     * @return intencion con patada
     */
    public static PlayerIntent kick(int direction) {
        return new PlayerIntent(direction, false, true, false);
    }

    /**
     * Crea una intencion de poder.
     *
     * @param direction direccion de movimiento simultanea
     * @return intencion con poder
     */
    public static PlayerIntent special(int direction) {
        return new PlayerIntent(direction, false, false, true);
    }
}
