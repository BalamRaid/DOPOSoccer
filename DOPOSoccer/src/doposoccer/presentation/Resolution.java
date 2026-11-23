package doposoccer.presentation;

/**
 * Resoluciones de la cancha disponibles (ancho en pixeles de la cancha).
 *
 * <p>La altura se calcula como la mitad del ancho para conservar la proporcion
 * de la cancha logica de {@link doposoccer.domain.Field}.</p>
 */
public enum Resolution {

    /** Resolucion pequena: 800 x 400. */
    SMALL("Pequena (800 x 400)", 800),

    /** Resolucion mediana: 1000 x 500. */
    MEDIUM("Mediana (1000 x 500)", 1000),

    /** Resolucion grande: 1200 x 600. */
    LARGE("Grande (1200 x 600)", 1200);

    private final String label;
    private final int width;

    /**
     * Crea una resolucion con su etiqueta y ancho.
     *
     * @param label texto que se muestra en la configuracion
     * @param width ancho de la cancha en pixeles
     */
    Resolution(String label, int width) {
        this.label = label;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return width / 2;
    }

    /**
     * Devuelve la etiqueta visible de la resolucion.
     *
     * @return texto que se muestra en el selector de resolucion
     */
    @Override
    public String toString() {
        return label;
    }
}
