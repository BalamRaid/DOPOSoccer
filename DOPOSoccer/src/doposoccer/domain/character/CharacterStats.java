package doposoccer.domain.character;

/**
 * Estadisticas innatas de un personaje: velocidad, salto y fuerza.
 *
 * <p>El reglamento maneja valores enteros de 1 a 5. El valor 3 es neutro y
 * produce un multiplicador de 1.0; los valores menores reducen ligeramente la
 * caracteristica y los mayores la aumentan. La progresion del perfil se aplica
 * despues, como una capa independiente de entrenamiento.</p>
 *
 * @param speed velocidad de movimiento, de 1 a 5
 * @param jump potencia de salto, de 1 a 5
 * @param strength fuerza de patada y de tiro de poder, de 1 a 5
 */
public record CharacterStats(int speed, int jump, int strength) {

    /** Valor minimo permitido para cualquier estadistica. */
    public static final int MIN = 1;

    /** Valor maximo permitido para cualquier estadistica. */
    public static final int MAX = 5;

    /** Valor neutro que deja todos los multiplicadores en 1.0. */
    public static final int NEUTRAL = 3;

    /** Estadisticas neutras, utiles para pruebas y personajes base. */
    public static final CharacterStats STANDARD = new CharacterStats(NEUTRAL, NEUTRAL, NEUTRAL);

    /**
     * Valida que las tres estadisticas esten dentro del rango oficial.
     */
    public CharacterStats {
        requireRange(speed, "speed");
        requireRange(jump, "jump");
        requireRange(strength, "strength");
    }

    /**
     * Multiplicador de velocidad. Cada punto por encima o por debajo de 3
     * cambia la velocidad un 5 %.
     *
     * @return factor de velocidad
     */
    public double speedFactor() {
        return 1 + 0.05 * (speed - NEUTRAL);
    }

    /**
     * Multiplicador de salto. Cada punto por encima o por debajo de 3 cambia
     * el impulso un 5 %.
     *
     * @return factor de salto
     */
    public double jumpFactor() {
        return 1 + 0.05 * (jump - NEUTRAL);
    }

    /**
     * Multiplicador de fuerza. Cada punto por encima o por debajo de 3 cambia
     * el dano un 10 %.
     *
     * @return factor de fuerza
     */
    public double strengthFactor() {
        return 1 + 0.10 * (strength - NEUTRAL);
    }

    private static void requireRange(int value, String name) {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                    name + " debe estar entre " + MIN + " y " + MAX + ".");
        }
    }
}
