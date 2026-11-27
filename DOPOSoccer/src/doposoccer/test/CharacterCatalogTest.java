package doposoccer.test;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.character.CharacterCatalog;
import doposoccer.domain.character.CharacterDefinition;
import doposoccer.domain.character.CharacterStats;
import doposoccer.domain.character.SpecialAbility;
import doposoccer.domain.character.StraightShot;
import doposoccer.domain.effect.PushEffect;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del catalogo de personajes y de la composicion de sus definiciones.
 */
class CharacterCatalogTest {

    @Test
    void shouldOfferSixCharacters() {
        assertEquals(6, CharacterCatalog.defaultCatalog().size());
    }

    @Test
    void shouldKeepRegistrationOrder() {
        List<String> names = CharacterCatalog.defaultCatalog().names();
        assertEquals("Corea del Sur", names.get(0));
        assertEquals("Camerun", names.get(1));
        assertEquals("Nigeria", names.get(2));
        assertEquals("Estados Unidos", names.get(3));
        assertEquals("Canada", names.get(4));
        assertEquals("China", names.get(5));
    }

    @Test
    void shouldFindCharacterById() {
        CharacterDefinition definition = CharacterCatalog.defaultCatalog()
                .findById("nigeria").orElseThrow();
        assertEquals("Nigeria", definition.displayName());
        assertEquals("nigeria", definition.visualKey());
    }

    @Test
    void shouldFindCharacterByDisplayName() {
        CharacterDefinition definition = CharacterCatalog.defaultCatalog()
                .findByDisplayName("Camerun").orElseThrow();
        assertEquals("cameroon", definition.id());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertFalse(CharacterCatalog.defaultCatalog().findById("atlantida").isPresent());
    }

    @Test
    void shouldReturnEmptyForUnknownName() {
        assertFalse(CharacterCatalog.defaultCatalog().findByDisplayName("Atlantida").isPresent());
    }

    @Test
    void shouldRequireKnownCharacter() throws DOPOSoccerException {
        assertEquals("Canada", CharacterCatalog.defaultCatalog().require("canada").displayName());
    }

    @Test
    void shouldRejectUnknownCharacterWithDomainMessage() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> CharacterCatalog.defaultCatalog().require("atlantida"));
        assertTrue(e.getMessage().startsWith(DOPOSoccerException.UNKNOWN_CHARACTER));
    }

    @Test
    void shouldRejectDuplicateRegistration() {
        CharacterCatalog catalog = new CharacterCatalog();
        CharacterDefinition definition = definition("test", "Test");
        catalog.register(definition);
        assertThrows(IllegalArgumentException.class, () -> catalog.register(definition));
    }

    @Test
    void shouldRejectNullRegistration() {
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterCatalog().register(null));
    }

    @Test
    void shouldUseUniqueVisualKeys() {
        Set<String> keys = new HashSet<>();
        for (CharacterDefinition definition : CharacterCatalog.defaultCatalog().all()) {
            assertTrue(keys.add(definition.visualKey()),
                    "Clave visual repetida: " + definition.visualKey());
        }
    }

    @Test
    void shouldExposeValidStatsAndAbilities() {
        for (CharacterDefinition definition : CharacterCatalog.defaultCatalog().all()) {
            assertTrue(definition.stats().speed() >= CharacterStats.MIN);
            assertTrue(definition.stats().speed() <= CharacterStats.MAX);
            assertTrue(definition.specialAbility().damage() > 0);
            assertFalse(definition.specialAbility().name().isBlank());
        }
    }

    private static CharacterDefinition definition(String id, String name) {
        return new CharacterDefinition(id, name, id, CharacterStats.STANDARD,
                new SpecialAbility("Prueba", 1, "Prueba", PushEffect::new,
                        new StraightShot(100)));
    }
}
