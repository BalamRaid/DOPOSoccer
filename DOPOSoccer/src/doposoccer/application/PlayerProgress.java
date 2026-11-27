package doposoccer.application;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Profile;

import java.io.Serializable;

/**
 * Progreso que se conserva entre ejecuciones: los perfiles de los dos
 * jugadores humanos y de las dos maquinas, con sus puntos y estadisticas.
 *
 * @param player1 perfil del jugador 1
 * @param player2 perfil del jugador 2
 * @param machine1 perfil de la maquina 1
 * @param machine2 perfil de la maquina 2
 */
public record PlayerProgress(Profile player1, Profile player2, Profile machine1,
        Profile machine2) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Valida que esten los cuatro perfiles.
     *
     * @throws IllegalArgumentException si falta algun perfil
     */
    public PlayerProgress {
        if (player1 == null || player2 == null || machine1 == null || machine2 == null) {
            throw new IllegalArgumentException(DOPOSoccerException.NULL_ELEMENT);
        }
    }

    /**
     * Crea el progreso inicial: cuatro perfiles sin puntos y con todas las
     * estadisticas en 1.
     *
     * @return progreso nuevo
     */
    public static PlayerProgress fresh() {
        return new PlayerProgress(new Profile("Jugador 1"), new Profile("Jugador 2"),
                new Profile("Maquina 1"), new Profile("Maquina 2"));
    }
}
