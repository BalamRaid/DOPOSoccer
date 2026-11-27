package doposoccer.domain.match;

import java.io.Serializable;

import doposoccer.domain.Side;
import doposoccer.domain.math.Vec2;

/**
 * Zona rectangular de un arco dentro de la cancha logica.
 *
 * <p>El arco se describe por su esquina superior izquierda, su ancho y su
 * alto. La deteccion de gol exige que el balon este <b>completamente</b>
 * dentro del rectangulo, tal como exige el reglamento del proyecto.</p>
 */
public final class Goal implements Serializable {

    private static final long serialVersionUID = 1L;


    private final Side side;
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    /**
     * Crea un arco para un lado de la cancha.
     *
     * @param side lado que defiende el arco
     * @param x coordenada horizontal de la esquina superior izquierda
     * @param y coordenada vertical de la esquina superior izquierda
     * @param width ancho del arco
     * @param height alto del arco
     */
    public Goal(Side side, double x, double y, double width, double height) {
        if (side == null) {
            throw new IllegalArgumentException("El lado del arco no puede ser null.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Las dimensiones del arco deben ser positivas.");
        }
        this.side = side;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Side side() {
        return side;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    /**
     * Indica si un circulo esta completamente contenido en el arco. Se usa el
     * centro y el radio del balon para que un balon que solo toca la linea no
     * cuente como gol.
     *
     * @param center centro del balon
     * @param radius radio del balon
     * @return {@code true} si todo el circulo queda dentro del rectangulo
     */
    public boolean containsFully(Vec2 center, double radius) {
        return center.x() - radius >= x
                && center.x() + radius <= x + width
                && center.y() - radius >= y
                && center.y() + radius <= y + height;
    }

    /**
     * Coordenada horizontal del poste frontal, es decir, del borde del arco
     * que da hacia la cancha.
     *
     * @return coordenada del poste frontal
     */
    public double frontPostX() {
        return side == Side.LEFT ? x + width : x;
    }

    /**
     * Coordenada horizontal del fondo del arco.
     *
     * @return coordenada del fondo de la red
     */
    public double backX() {
        return side == Side.LEFT ? x : x + width;
    }

    /**
     * Punto superior del poste frontal, usado para resolver el choque entre el
     * balon y la esquina del travesano.
     *
     * @return esquina superior del poste frontal
     */
    public Vec2 frontPostTop() {
        return new Vec2(frontPostX(), y);
    }

    /**
     * Centro geometrico del arco.
     *
     * @return centro del rectangulo
     */
    public Vec2 center() {
        return new Vec2(x + width / 2, y + height / 2);
    }
}
