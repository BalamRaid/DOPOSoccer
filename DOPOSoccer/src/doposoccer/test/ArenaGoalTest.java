package doposoccer.test;

import doposoccer.domain.Field;
import doposoccer.domain.Side;
import doposoccer.domain.match.Arena;
import doposoccer.domain.match.Goal;
import doposoccer.domain.match.GoalDetector;
import doposoccer.domain.math.Vec2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas geometricas de la cancha, los arcos y la deteccion de goles.
 */
class ArenaGoalTest {

    private static final double EPS = 1e-6;
    private static final Arena ARENA = Arena.defaultArena();

    @Test
    void shouldExposeOfficialDimensions() {
        assertEquals(Field.WIDTH, ARENA.width(), EPS);
        assertEquals(Field.HEIGHT, ARENA.height(), EPS);
        assertEquals(Field.GROUND, ARENA.groundY(), EPS);
        assertEquals(Field.CROSSBAR_Y, ARENA.crossbarY(), EPS);
        assertEquals(Field.GOAL_DEPTH, ARENA.goalDepth(), EPS);
        assertEquals(Field.GRAVITY, ARENA.gravity(), EPS);
    }

    @Test
    void shouldReturnGoalBySide() {
        assertEquals(Side.LEFT, ARENA.goalOf(Side.LEFT).side());
        assertEquals(Side.RIGHT, ARENA.goalOf(Side.RIGHT).side());
    }

    @Test
    void shouldRejectNullSideInGoalLookup() {
        assertThrows(IllegalArgumentException.class, () -> ARENA.goalOf(null));
    }

    @Test
    void shouldExposeFrontPostAndBack() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertEquals(Field.GOAL_DEPTH, left.frontPostX(), EPS);
        assertEquals(0, left.backX(), EPS);
        Goal right = ARENA.goalOf(Side.RIGHT);
        assertEquals(Field.WIDTH - Field.GOAL_DEPTH, right.frontPostX(), EPS);
        assertEquals(Field.WIDTH, right.backX(), EPS);
    }

    @Test
    void shouldContainFullyInsideGoal() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertTrue(left.containsFully(new Vec2(30, Field.GROUND - 40), 15));
    }

    @Test
    void shouldRejectBallTouchingGoalLine() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertFalse(left.containsFully(new Vec2(14, Field.GROUND - 40), 15));
    }

    @Test
    void shouldRejectBallAboveCrossbar() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertFalse(left.containsFully(new Vec2(30, Field.CROSSBAR_Y + 5), 15));
    }

    @Test
    void shouldRejectBallBelowGround() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertFalse(left.containsFully(new Vec2(30, Field.GROUND - 5), 15));
    }

    @Test
    void shouldDetectGoalInLeftGoalForRightPlayer() {
        assertTrue(GoalDetector.scorer(new Vec2(30, Field.GROUND - 40), 15, ARENA)
                .filter(side -> side == Side.RIGHT).isPresent());
    }

    @Test
    void shouldDetectGoalInRightGoalForLeftPlayer() {
        assertTrue(GoalDetector.scorer(new Vec2(Field.WIDTH - 30, Field.GROUND - 40), 15, ARENA)
                .filter(side -> side == Side.LEFT).isPresent());
    }

    @Test
    void shouldNotDetectGoalOverCrossbar() {
        assertFalse(GoalDetector.scorer(new Vec2(30, 100), 15, ARENA).isPresent());
    }

    @Test
    void shouldRejectArenaWithoutTwoGoals() {
        Goal left = ARENA.goalOf(Side.LEFT);
        assertThrows(IllegalArgumentException.class,
                () -> new Arena(1000, 500, 450, 290, 60, 1400, List.of(left)));
    }

    @Test
    void shouldRejectInvalidGoalDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(Side.LEFT, 0, 290, 0, 160));
    }
}
