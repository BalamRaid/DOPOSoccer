package doposoccer.test;

import doposoccer.domain.match.MatchClock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del reloj de partido.
 */
class MatchClockTest {

    private static final double EPS = 1e-9;

    @Test
    void shouldStartUnlimitedWithoutRemainingTime() {
        MatchClock clock = MatchClock.unlimited();
        assertFalse(clock.hasLimit());
        assertEquals(-1, clock.remainingSeconds(), EPS);
        assertFalse(clock.isExpired());
    }

    @Test
    void shouldCountElapsedTime() {
        MatchClock clock = MatchClock.ofSeconds(60);
        clock.advance(10);
        assertEquals(10, clock.elapsedSeconds(), EPS);
        assertEquals(50, clock.remainingSeconds(), EPS);
    }

    @Test
    void shouldExpireAtDuration() {
        MatchClock clock = MatchClock.ofSeconds(2);
        clock.advance(2);
        assertTrue(clock.isExpired());
        assertEquals(0, clock.remainingSeconds(), EPS);
    }

    @Test
    void shouldNotExceedDuration() {
        MatchClock clock = MatchClock.ofSeconds(1);
        clock.advance(5);
        assertEquals(1, clock.elapsedSeconds(), EPS);
    }

    @Test
    void shouldRejectNegativeAdvance() {
        MatchClock clock = MatchClock.unlimited();
        assertThrows(IllegalArgumentException.class, () -> clock.advance(-0.1));
    }

    @Test
    void shouldRejectNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> MatchClock.ofSeconds(0));
        assertThrows(IllegalArgumentException.class, () -> MatchClock.ofSeconds(-1));
    }

    @Test
    void shouldRejectNaNDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> MatchClock.ofSeconds(Double.NaN));
    }

    @Test
    void shouldResetElapsedTime() {
        MatchClock clock = MatchClock.ofSeconds(10);
        clock.advance(4);
        clock.reset();
        assertEquals(0, clock.elapsedSeconds(), EPS);
        assertFalse(clock.isExpired());
    }

    @Test
    void shouldKeepUnlimitedClockUnexpired() {
        MatchClock clock = MatchClock.unlimited();
        clock.advance(1_000_000);
        assertFalse(clock.isExpired());
        assertEquals(-1, clock.remainingSeconds(), EPS);
    }
}
