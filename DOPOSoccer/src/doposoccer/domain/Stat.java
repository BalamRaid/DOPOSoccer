package doposoccer.domain;

/**
 * Estadisticas mejorables de un jugador.
 *
 * <p>Cada estadistica se representa con un valor entero entre
 * {@link Stats#MIN} y {@link Stats#MAX}; cada punto adicional modifica una
 * caracteristica distinta del jugador.</p>
 */
public enum Stat {

    /** Velocidad horizontal de movimiento. */
    SPEED,

    /** Multiplicador del dano de patada y de tiro de poder. */
    KICK,

    /** Altura y velocidad del salto. */
    JUMP,

    /** Vida maxima del jugador. */
    SURVIVAL
}
