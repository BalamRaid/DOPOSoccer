package doposoccer.domain;

/**
 * Clima del escenario. No es solo decorado: cambia la fisica del partido.
 *
 * <ul>
 *   <li>{@link #CLEAR}: sin efectos.</li>
 *   <li>{@link #WIND}: rafagas que empujan el balon mientras va por el aire.
 *       La rafaga cambia de sentido de forma suave cada medio periodo.</li>
 *   <li>{@link #RAIN}: piso mojado; los jugadores no frenan ni arrancan en
 *       seco, sino que resbalan hasta su velocidad.</li>
 * </ul>
 */
public enum Weather {

    /** Cielo despejado o escenario cubierto. */
    CLEAR("Despejado", "Sin efectos en el juego", 0, 1),

    /** Viento: desvia el balon en el aire. */
    WIND("Viento", "Las rafagas desvian el balon en el aire", 260, 1),

    /** Lluvia: el piso resbala. */
    RAIN("Lluvia", "Piso mojado: los jugadores resbalan", 0, 0.10);

    /** Segundos que tarda la rafaga en volver a su sentido inicial. */
    public static final double GUST_PERIOD = 12;

    private final String label;
    private final String description;
    private final double maxWind;
    private final double grip;

    Weather(String label, String description, double maxWind, double grip) {
        this.label = label;
        this.description = description;
        this.maxWind = maxWind;
        this.grip = grip;
    }

    /**
     * Aceleracion horizontal del viento en un instante del partido. Positiva
     * empuja hacia la derecha y negativa hacia la izquierda.
     *
     * @param seconds segundos transcurridos del partido
     * @return aceleracion en pixeles por segundo al cuadrado
     */
    public double windAt(double seconds) {
        return maxWind * Math.sin(2 * Math.PI * seconds / GUST_PERIOD);
    }

    /**
     * Agarre del piso: fraccion de la diferencia entre la velocidad actual y la
     * deseada que el jugador recupera en cada paso. 1 es agarre total.
     *
     * @return valor en (0, 1]
     */
    public double grip() {
        return grip;
    }

    /**
     * Indica si este clima tiene viento.
     *
     * @return true si hay rafagas
     */
    public boolean hasWind() {
        return maxWind > 0;
    }

    /**
     * Nombre visible del clima.
     *
     * @return nombre en espanol
     */
    public String label() {
        return label;
    }

    /**
     * Explicacion corta del efecto en el juego.
     *
     * @return descripcion
     */
    public String description() {
        return description;
    }
}
