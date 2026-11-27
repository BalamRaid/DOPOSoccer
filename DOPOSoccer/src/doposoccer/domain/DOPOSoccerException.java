package doposoccer.domain;

/**
 * Excepcion propia del juego DOPOSoccer. Agrupa los mensajes de error del
 * dominio para que la presentacion los muestre de forma clara al usuario.
 *
 * <p>Es una excepcion comprobada: los metodos que pueden fallar por una
 * accion invalida del usuario la declaran de forma explicita.</p>
 */
public class DOPOSoccerException extends Exception {

    private static final long serialVersionUID = 1L;

    /** La barra de poder aun no esta llena. */
    public static final String POWER_NOT_READY = "La barra de poder aun no esta llena.";

    /** El jugador ya tiene un aura activa. */
    public static final String POWER_ALREADY_ACTIVE = "El jugador ya tiene un poder activo (aura).";

    /** La estadistica ya alcanzo el maximo permitido. */
    public static final String STAT_AT_MAX =
            "Esta estadistica ya esta en su nivel maximo (10).\nMejora otra estadistica.";

    /** No hay puntos suficientes para comprar una mejora. */
    public static final String NOT_ENOUGH_POINTS = "No tienes puntos suficientes para esta mejora.";

    /** Los minutos del partido estan fuera del rango permitido. */
    public static final String INVALID_MINUTES = "Los minutos del partido deben estar entre 1 y 10.";

    /** Los goles para ganar estan fuera del rango permitido. */
    public static final String INVALID_GOALS = "Los goles para ganar deben estar entre 1 y 20.";

    /** Los balones de supervivencia estan fuera del rango permitido. */
    public static final String INVALID_BALLS = "Los balones de supervivencia deben estar entre 1 y 9.";

    /** La modalidad no admite el tipo de partido seleccionado. */
    public static final String INVALID_MODE = "La modalidad seleccionada no admite este tipo de partido.";

    /** El pais seleccionado no existe en el catalogo. */
    public static final String UNKNOWN_CHARACTER = "El personaje seleccionado no existe.";

    /** Falta un dato obligatorio para crear el partido. */
    public static final String NULL_ELEMENT = "Faltan datos para crear el partido.";

    /** El partido ya termino y no admite acciones. */
    public static final String GAME_FINISHED = "El partido ya termino.";

    /** No hay un partido en curso para guardar. */
    public static final String NO_GAME_TO_SAVE = "No hay un partido en curso para guardar.";

    /** No se pudo escribir el archivo. */
    public static final String SAVE_FAILED = "No se pudo guardar el archivo.";

    /** No se pudo leer el archivo. */
    public static final String OPEN_FAILED = "No se pudo abrir el archivo.";

    /** El archivo existe pero no es una partida valida de DOPOSoccer. */
    public static final String INVALID_SAVE_FILE =
            "El archivo no es una partida de DOPOSoccer o esta danado.";

    /**
     * Crea una excepcion con el mensaje indicado.
     *
     * @param message mensaje que se mostrara al usuario
     */
    public DOPOSoccerException(String message) {
        super(message);
    }
}
