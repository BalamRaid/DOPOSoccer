package doposoccer.domain.math;

import java.io.Serializable;

/**
 * Valor inmutable de dos dimensiones usado para posiciones, velocidades y
 * direcciones dentro de la cancha logica.
 *
 * <p>Al ser inmutable, cualquier operacion crea un vector nuevo. Esto evita
 * efectos laterales accidentales cuando el estado se comparte entre el
 * dominio, la inteligencia artificial y la presentacion.</p>
 *
 * @param x componente horizontal
 * @param y componente vertical; en la cancha logica crece hacia abajo
 */
public record Vec2(double x, double y) implements Serializable {

    private static final long serialVersionUID = 1L;


    /** Vector nulo, util como valor inicial o como direccion vacia. */
    public static final Vec2 ZERO = new Vec2(0, 0);

    /**
     * Crea un vector a partir de sus componentes.
     *
     * @param x componente horizontal
     * @param y componente vertical
     * @return vector con las componentes indicadas
     */
    public static Vec2 of(double x, double y) {
        return new Vec2(x, y);
    }

    /**
     * Suma otro vector de forma componente a componente.
     *
     * @param other vector que se suma
     * @return la suma de ambos vectores
     */
    public Vec2 plus(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    /**
     * Resta otro vector de forma componente a componente.
     *
     * @param other vector que se resta
     * @return la diferencia {@code this - other}
     */
    public Vec2 minus(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    /**
     * Escala el vector por un factor.
     *
     * @param factor multiplicador
     * @return vector escalado
     */
    public Vec2 times(double factor) {
        return new Vec2(x * factor, y * factor);
    }

    /**
     * Calcula el cuadrado de la longitud. Se usa cuando solo importa comparar
     * distancias y se quiere evitar una raiz cuadrada.
     *
     * @return {@code x * x + y * y}
     */
    public double lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Calcula la longitud euclidiana del vector.
     *
     * @return magnitud del vector
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Devuelve un vector unitario en la misma direccion. Si el vector es nulo,
     * devuelve {@link #ZERO} en lugar de producir {@code NaN}.
     *
     * @return vector normalizado
     */
    public Vec2 normalized() {
        double magnitude = length();
        return magnitude == 0 ? ZERO : times(1 / magnitude);
    }

    /**
     * Calcula la distancia euclidiana a otro punto.
     *
     * @param other punto de destino
     * @return distancia entre ambos puntos
     */
    public double distanceTo(Vec2 other) {
        return minus(other).length();
    }
}
