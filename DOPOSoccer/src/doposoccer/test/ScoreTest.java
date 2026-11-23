package doposoccer.test;

import doposoccer.domain.Side;
import doposoccer.domain.match.Score;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del marcador de goles.
 */
class ScoreTest {

    @Test
    void shouldStartAtZero() {
        Score score = new Score();
        assertEquals(0, score.goalsOf(Side.LEFT));
        assertEquals(0, score.goalsOf(Side.RIGHT));
        assertEquals(0, score.totalGoals());
    }

    @Test
    void shouldAddGoals() {
        Score score = new Score();
        score.addGoal(Side.LEFT);
        score.addGoal(Side.LEFT);
        score.addGoal(Side.RIGHT);
        assertEquals(2, score.goalsOf(Side.LEFT));
        assertEquals(1, score.goalsOf(Side.RIGHT));
        assertEquals(3, score.totalGoals());
    }

    @Test
    void shouldReportLeader() {
        Score score = new Score();
        score.addGoal(Side.RIGHT);
        assertSame(Side.RIGHT, score.leader());
        assertFalse(score.isTied());
    }

    @Test
    void shouldReportTie() {
        Score score = new Score();
        score.addGoal(Side.LEFT);
        score.addGoal(Side.RIGHT);
        assertNull(score.leader());
        assertTrue(score.isTied());
    }

    @Test
    void shouldResetScore() {
        Score score = new Score();
        score.addGoal(Side.LEFT);
        score.reset();
        assertEquals(0, score.totalGoals());
    }

    @Test
    void shouldRejectNullSideWhenAdding() {
        assertThrows(IllegalArgumentException.class, () -> new Score().addGoal(null));
    }

    @Test
    void shouldRejectNullSideWhenReading() {
        assertThrows(IllegalArgumentException.class, () -> new Score().goalsOf(null));
    }
}
