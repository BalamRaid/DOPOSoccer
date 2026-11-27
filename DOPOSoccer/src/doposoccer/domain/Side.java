package doposoccer.domain;

/**
 * Lado de la cancha que defiende un jugador.
 *
 * <p>La direccion de ataque se deriva del lado: el jugador izquierdo ataca
 * hacia la derecha y el derecho ataca hacia la izquierda.</p>
 */
public enum Side {

    /** Jugador que defiende el arco izquierdo y ataca hacia la derecha. */
    LEFT(1),

    /** Jugador que defiende el arco derecho y ataca hacia la izquierda. */
    RIGHT(-1);

    private final int attackDirection;

    /**
     * Crea el lado con su direccion de ataque.
     *
     * @param attackDirection {@code 1} para atacar a la derecha, {@code -1}
     *                        para atacar a la izquierda
     */
    Side(int attackDirection) {
        this.attackDirection = attackDirection;
    }

    /**
     * Devuelve la direccion en la que ataca este lado.
     *
     * @return {@code 1} si ataca hacia la derecha, {@code -1} si ataca hacia
     *         la izquierda
     */
    public int attackDirection() {
        return attackDirection;
    }

    /**
     * Devuelve el lado contrario.
     *
     * @return {@link #RIGHT} si este lado es {@link #LEFT}, o {@link #LEFT}
     *         en caso contrario
     */
    public Side opposite() {
        return this == LEFT ? RIGHT : LEFT;
    }
}
