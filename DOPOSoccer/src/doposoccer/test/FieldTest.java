package doposoccer.test;

import doposoccer.domain.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las constantes logicas de la cancha.
 */
class FieldTest {

    @Test
    void shouldUseTwoToOneAspectRatio() {
        assertEquals(2, Field.WIDTH / Field.HEIGHT, 1e-9);
    }

    @Test
    void shouldKeepGoalsBelowCrossbar() {
        assertTrue(Field.CROSSBAR_Y < Field.GROUND);
        assertTrue(Field.GROUND < Field.HEIGHT);
    }

    @Test
    void shouldUsePositiveGravity() {
        assertTrue(Field.GRAVITY > 0);
    }

    @Test
    void shouldUseSymmetricGoalDepth() {
        assertTrue(Field.GOAL_DEPTH > 0);
        assertTrue(Field.GOAL_DEPTH < Field.WIDTH / 2);
    }
}
