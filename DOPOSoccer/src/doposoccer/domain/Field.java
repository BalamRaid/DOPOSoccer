package doposoccer.domain;

/**
 * Medidas logicas oficiales de la cancha. La presentacion escala estas
 * medidas a la resolucion elegida, por eso el dominio no depende de la
 * resolucion.
 *
 * <p>La cancha tiene un sistema de coordenadas con origen en la esquina
 * superior izquierda. El suelo esta en {@link #GROUND} y los arcos ocupan los
 * extremos izquierdo y derecho por debajo de {@link #CROSSBAR_Y}. La clase
 * {@link doposoccer.domain.match.Arena} construye la descripcion geometrica
 * completa a partir de estas constantes.</p>
 */
public final class Field {

    /** Ancho logico total de la cancha. */
    public static final double WIDTH = 1000;

    /** Alto logico total de la cancha. */
    public static final double HEIGHT = 500;

    /** Coordenada vertical del suelo. */
    public static final double GROUND = 450;

    /** Profundidad horizontal de cada arco. */
    public static final double GOAL_DEPTH = 60;

    /** Coordenada vertical del travesano de los arcos. */
    public static final double CROSSBAR_Y = 290;

    /** Aceleracion de la gravedad aplicada al balon y a los jugadores. */
    public static final double GRAVITY = 1400;

    /**
     * Constructor privado: la clase solo contiene constantes.
     */
    private Field() {
    }
}
