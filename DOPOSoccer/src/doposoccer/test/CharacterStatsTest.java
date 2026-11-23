package doposoccer.test;

import doposoccer.domain.character.CharacterStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de las estadisticas innatas de personaje.
 */
class CharacterStatsTest {

    private static final double EPS = 1e-9;

    @Test
    void shouldKeepNeutralFactorsAtOne() {
        CharacterStats stats = CharacterStats.STANDARD;
        assertEquals(1, stats.speedFactor(), EPS);
        assertEquals(1, stats.jumpFactor(), EPS);
        assertEquals(1, stats.strengthFactor(), EPS);
    }

    @Test
    void shouldIncreaseSpeedFactor() {
        assertEquals(1.1, new CharacterStats(5, 3, 3).speedFactor(), EPS);
    }

    @Test
    void shouldDecreaseSpeedFactor() {
        assertEquals(0.9, new CharacterStats(1, 3, 3).speedFactor(), EPS);
    }

    @Test
    void shouldIncreaseJumpFactor() {
        assertEquals(1.1, new CharacterStats(3, 5, 3).jumpFactor(), EPS);
    }

    @Test
    void shouldDecreaseJumpFactor() {
        assertEquals(0.9, new CharacterStats(3, 1, 3).jumpFactor(), EPS);
    }

    @Test
    void shouldIncreaseStrengthFactor() {
        assertEquals(1.2, new CharacterStats(3, 3, 5).strengthFactor(), EPS);
    }

    @Test
    void shouldDecreaseStrengthFactor() {
        assertEquals(0.8, new CharacterStats(3, 3, 1).strengthFactor(), EPS);
    }

    @Test
    void shouldRejectValuesBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(0, 3, 3));
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(3, 0, 3));
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(3, 3, 0));
    }

    @Test
    void shouldRejectValuesAboveFive() {
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(6, 3, 3));
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(3, 6, 3));
        assertThrows(IllegalArgumentException.class, () -> new CharacterStats(3, 3, 6));
    }
}
