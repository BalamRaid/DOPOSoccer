package doposoccer.test;

import doposoccer.domain.control.PlayerIntent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del valor de intencion de control.
 */
class PlayerIntentTest {

    @Test
    void shouldExposeIdleIntent() {
        assertEquals(0, PlayerIntent.IDLE.moveDirection());
        assertFalse(PlayerIntent.IDLE.jump());
        assertFalse(PlayerIntent.IDLE.kick());
        assertFalse(PlayerIntent.IDLE.special());
    }

    @Test
    void shouldRejectDirectionOutsideRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new PlayerIntent(-2, false, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new PlayerIntent(2, false, false, false));
    }

    @Test
    void shouldCreateMovementIntent() {
        PlayerIntent intent = PlayerIntent.move(-1);
        assertEquals(-1, intent.moveDirection());
        assertFalse(intent.jump());
    }

    @Test
    void shouldCreateJumpIntent() {
        assertTrue(PlayerIntent.jump(1).jump());
    }

    @Test
    void shouldCreateKickIntent() {
        assertTrue(PlayerIntent.kick(0).kick());
    }

    @Test
    void shouldCreateSpecialIntent() {
        assertTrue(PlayerIntent.special(1).special());
    }
}
