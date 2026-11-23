package doposoccer.domain.character;

import java.util.Objects;

/**
 * Plantilla inmutable de un personaje disponible en el juego.
 *
 * <p>No guarda posicion, vida, carga ni ningun otro estado de una partida
 * concreta: esos datos viven en {@link doposoccer.domain.Player}. Un pais es
 * una fila de datos y un poder compuesto, no una subclase distinta.</p>
 */
public final class CharacterDefinition {

    private final String id;
    private final String displayName;
    private final String visualKey;
    private final CharacterStats stats;
    private final SpecialAbility specialAbility;

    /**
     * Crea la definicion de un personaje.
     *
     * @param id identificador estable, por ejemplo {@code "south-korea"}
     * @param displayName nombre visible del pais
     * @param visualKey clave que usa la presentacion para elegir colores
     * @param stats estadisticas innatas de 1 a 5
     * @param specialAbility poder unico del personaje
     */
    public CharacterDefinition(String id, String displayName, String visualKey,
            CharacterStats stats, SpecialAbility specialAbility) {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.visualKey = requireText(visualKey, "visualKey");
        this.stats = Objects.requireNonNull(stats, "stats no puede ser null.");
        this.specialAbility = Objects.requireNonNull(specialAbility, "specialAbility no puede ser null.");
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String visualKey() {
        return visualKey;
    }

    public CharacterStats stats() {
        return stats;
    }

    public SpecialAbility specialAbility() {
        return specialAbility;
    }

    /**
     * Devuelve el nombre visible del pais, que es el texto que se muestra en
     * selectores, HUD y mensajes de resultado.
     *
     * @return nombre visible
     */
    @Override
    public String toString() {
        return displayName;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " no puede estar vacio.");
        }
        return value;
    }
}
