package doposoccer.domain.character;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.effect.ConfusionEffect;
import doposoccer.domain.effect.ElectrocutedEffect;
import doposoccer.domain.effect.FreezeEffect;
import doposoccer.domain.effect.PushEffect;
import doposoccer.domain.effect.ShrinkEffect;
import doposoccer.domain.effect.UnconsciousEffect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogo de personajes disponibles. Conserva el orden de registro y no
 * participa en la logica de un partido concreto.
 *
 * <p>Agregar un personaje nuevo consiste en registrar una
 * {@link CharacterDefinition} con sus estadisticas y su {@link SpecialAbility}.
 * No hace falta crear una subclase por pais ni modificar la presentacion.</p>
 */
public final class CharacterCatalog {

    private final Map<String, CharacterDefinition> byId = new LinkedHashMap<>();

    /**
     * Crea un catalogo vacio. Los personajes se agregan con
     * {@link #register(CharacterDefinition)}; el catalogo oficial se obtiene
     * con {@link #defaultCatalog()}.
     */
    public CharacterCatalog() {
    }

    /**
     * Registra un personaje nuevo.
     *
     * @param definition definicion inmutable del personaje
     * @throws IllegalArgumentException si la definicion es null o si el id ya
     *                                  existe
     */
    public void register(CharacterDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("La definicion no puede ser null.");
        }
        if (byId.containsKey(definition.id())) {
            throw new IllegalArgumentException("Ya existe un personaje con id " + definition.id() + ".");
        }
        byId.put(definition.id(), definition);
    }

    /**
     * Busca un personaje por su identificador estable.
     *
     * @param id identificador del personaje
     * @return definicion encontrada, o vacio si no existe
     */
    public Optional<CharacterDefinition> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Busca un personaje por su nombre visible.
     *
     * @param displayName nombre del pais
     * @return definicion encontrada, o vacio si no existe
     */
    public Optional<CharacterDefinition> findByDisplayName(String displayName) {
        if (displayName == null) {
            return Optional.empty();
        }
        return byId.values().stream()
                .filter(definition -> definition.displayName().equals(displayName))
                .findFirst();
    }

    /**
     * Busca un personaje por id y lanza una excepcion de dominio si no existe.
     *
     * @param id identificador del personaje
     * @return definicion encontrada
     * @throws DOPOSoccerException si el personaje no esta registrado
     */
    public CharacterDefinition require(String id) throws DOPOSoccerException {
        return findById(id).orElseThrow(() -> new DOPOSoccerException(
                DOPOSoccerException.UNKNOWN_CHARACTER + " (" + id + ")"));
    }

    /**
     * Devuelve todos los personajes en orden de registro.
     *
     * @return lista inmutable de definiciones
     */
    public List<CharacterDefinition> all() {
        return List.copyOf(byId.values());
    }

    /**
     * Devuelve los nombres visibles de los personajes en orden de registro.
     *
     * @return lista nueva con los nombres de los paises
     */
    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (CharacterDefinition definition : byId.values()) {
            names.add(definition.displayName());
        }
        return names;
    }

    /**
     * Devuelve la cantidad de personajes registrados.
     *
     * @return cantidad de personajes
     */
    public int size() {
        return byId.size();
    }

    /**
     * Crea el catalogo oficial del proyecto con los seis personajes de esta
     * entrega: los cuatro clasicos y los dos ejemplos de poderes del
     * reglamento (Canada y China).
     *
     * @return catalogo con los personajes oficiales
     */
    public static CharacterCatalog defaultCatalog() {
        CharacterCatalog catalog = new CharacterCatalog();
        catalog.register(new CharacterDefinition(
                "south-korea", "Corea del Sur", "south-korea",
                new CharacterStats(4, 3, 2),
                new SpecialAbility("Aura Azul", 40, "Empuje",
                        PushEffect::new, new StraightShot(1150))));
        catalog.register(new CharacterDefinition(
                "cameroon", "Camerun", "cameroon",
                new CharacterStats(3, 3, 5),
                new SpecialAbility("Rayo", 60, "Electrocutado",
                        ElectrocutedEffect::new, new StraightShot(950))));
        catalog.register(new CharacterDefinition(
                "nigeria", "Nigeria", "nigeria",
                new CharacterStats(3, 2, 4),
                new SpecialAbility("Tornado", 40, "Inconsciente",
                        UnconsciousEffect::new, new TornadoShot(800))));
        catalog.register(new CharacterDefinition(
                "usa", "Estados Unidos", "usa",
                new CharacterStats(5, 4, 2),
                new SpecialAbility("Ilusion", 30, "Confusion",
                        ConfusionEffect::new, new IllusionShot(7))));
        catalog.register(new CharacterDefinition(
                "canada", "Canada", "canada",
                new CharacterStats(2, 3, 3),
                new SpecialAbility("Congelacion", 35, "Congelado",
                        FreezeEffect::new, new StraightShot(850))));
        catalog.register(new CharacterDefinition(
                "china", "China", "china",
                new CharacterStats(3, 5, 2),
                new SpecialAbility("Reduccion", 25, "Reducido",
                        ShrinkEffect::new, new StraightShot(900))));
        return catalog;
    }
}
