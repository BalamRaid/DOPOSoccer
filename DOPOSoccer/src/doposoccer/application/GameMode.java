package doposoccer.application;

import doposoccer.domain.MatchType;
import doposoccer.domain.Side;

/**
 * Modalidades de juego del reglamento: humano contra humano, humano contra
 * maquina y maquina contra maquina (IA contra IA).
 */
public enum GameMode {

    /** Dos jugadores humanos comparten el teclado. */
    PVP("Jugador vs Jugador"),

    /** El jugador izquierdo es humano y el derecho lo controla la maquina. */
    PVM("Jugador vs Maquina"),

    /** Los dos lados son controlados por la maquina. */
    MVM("Maquina vs Maquina");

    private final String label;

    GameMode(String label) {
        this.label = label;
    }

    /**
     * Indica si el lado es controlado por la maquina en esta modalidad.
     *
     * @param side lado consultado
     * @return {@code true} si el lado es automatico
     */
    public boolean isMachine(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        return switch (this) {
            case PVP -> false;
            case PVM -> side == Side.RIGHT;
            case MVM -> true;
        };
    }

    /**
     * Indica si la modalidad admite el tipo de partido indicado. La
     * supervivencia solo esta disponible en Jugador vs Maquina.
     *
     * @param type tipo de partido
     * @return {@code true} si la combinacion es valida
     */
    public boolean supports(MatchType type) {
        if (type == null) {
            return false;
        }
        return !type.isSurvival() || this == PVM;
    }

    /**
     * Devuelve la etiqueta visible de la modalidad.
     *
     * @return texto para la interfaz
     */
    @Override
    public String toString() {
        return label;
    }
}
