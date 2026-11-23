package doposoccer.application;

import doposoccer.domain.MatchType;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.character.CharacterDefinition;
import java.util.Objects;

/**
 * Configuracion completa de un partido: modalidad, tipo de partido, perfiles
 * y personajes de cada lado.
 *
 * <p>Es un valor inmutable que la pantalla de configuracion construye y la
 * capa de aplicacion convierte en una {@link GameSession}. No contiene
 * detalles de Swing ni de la resolucion de la ventana.</p>
 *
 * @param mode modalidad de juego
 * @param matchType regla de finalizacion
 * @param leftProfile perfil del lado izquierdo
 * @param leftCharacter personaje del lado izquierdo
 * @param rightProfile perfil del lado derecho
 * @param rightCharacter personaje del lado derecho
 */
public record MatchConfig(
        GameMode mode,
        MatchType matchType,
        Profile leftProfile,
        CharacterDefinition leftCharacter,
        Profile rightProfile,
        CharacterDefinition rightCharacter) {

    /**
     * Valida que todos los componentes esten presentes.
     */
    public MatchConfig {
        Objects.requireNonNull(mode, "mode no puede ser null.");
        Objects.requireNonNull(matchType, "matchType no puede ser null.");
        Objects.requireNonNull(leftProfile, "leftProfile no puede ser null.");
        Objects.requireNonNull(leftCharacter, "leftCharacter no puede ser null.");
        Objects.requireNonNull(rightProfile, "rightProfile no puede ser null.");
        Objects.requireNonNull(rightCharacter, "rightCharacter no puede ser null.");
    }

    /**
     * Devuelve el perfil de un lado.
     *
     * @param side lado consultado
     * @return perfil correspondiente
     */
    public Profile profileOf(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        return side == Side.LEFT ? leftProfile : rightProfile;
    }

    /**
     * Devuelve el personaje de un lado.
     *
     * @param side lado consultado
     * @return personaje correspondiente
     */
    public CharacterDefinition characterOf(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        return side == Side.LEFT ? leftCharacter : rightCharacter;
    }
}
