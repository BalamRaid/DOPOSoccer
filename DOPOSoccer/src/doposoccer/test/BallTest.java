package doposoccer.test;

import doposoccer.domain.Ball;
import doposoccer.domain.Field;
import doposoccer.domain.match.Arena;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la fisica del balon: gravedad, rebotes, bordes y estado de los
 * tiros de poder.
 */
class BallTest {

    private static final double DT = TestSupport.DT;
    private static final double EPS = 1e-6;

    @Test
    void shouldStartWithDefaultState() {
        Ball ball = new Ball(500, 100);
        assertEquals(500, ball.getX(), EPS);
        assertEquals(100, ball.getY(), EPS);
        assertEquals(0, ball.getVx(), EPS);
        assertEquals(0, ball.getVy(), EPS);
        assertTrue(ball.hasGravity());
        assertFalse(ball.isTornado());
    }

    @Test
    void shouldAccelerateWithGravity() {
        Ball ball = new Ball(500, 100);
        ball.update(DT);
        assertTrue(ball.getVy() > 0);
    }

    @Test
    void shouldNeverPassThroughGround() {
        Ball ball = new Ball(500, 100);
        for (int i = 0; i < 300; i++) {
            ball.update(DT);
            assertTrue(ball.getY() <= Field.GROUND - Ball.RADIUS + EPS);
        }
    }

    @Test
    void shouldBounceOnGround() {
        Ball ball = new Ball(500, Field.GROUND - Ball.RADIUS);
        ball.setVelocity(0, 200);
        ball.update(DT);
        assertTrue(ball.getVy() < 0);
        assertEquals(Field.GROUND - Ball.RADIUS, ball.getY(), EPS);
    }

    @Test
    void shouldBounceOnCeiling() {
        Ball ball = new Ball(500, Ball.RADIUS + 1);
        ball.setVelocity(0, -100);
        ball.update(DT);
        assertTrue(ball.getVy() > 0);
        assertTrue(ball.getY() >= Ball.RADIUS - EPS);
    }

    @Test
    void shouldBounceOnLeftWall() {
        Ball ball = new Ball(Ball.RADIUS + 1, 300);
        ball.setVelocity(-100, 0);
        assertTrue(ball.update(DT));
        assertTrue(ball.getVx() > 0);
        assertTrue(ball.getX() >= Ball.RADIUS - EPS);
    }

    @Test
    void shouldBounceOnRightWall() {
        Ball ball = new Ball(Field.WIDTH - Ball.RADIUS - 1, 300);
        ball.setVelocity(100, 0);
        assertTrue(ball.update(DT));
        assertTrue(ball.getVx() < 0);
        assertTrue(ball.getX() <= Field.WIDTH - Ball.RADIUS + EPS);
    }

    @Test
    void shouldBounceOnCrossbar() {
        Ball ball = new Ball(30, Field.CROSSBAR_Y - 10);
        ball.setVelocity(0, 200);
        ball.update(DT);
        assertTrue(ball.getVy() < 0);
        assertTrue(ball.getY() <= Field.CROSSBAR_Y - Ball.RADIUS + EPS);
    }

    @Test
    void shouldBounceOnFrontPostCorner() {
        Ball ball = new Ball(78, Field.CROSSBAR_Y);
        ball.setVelocity(-100, 0);
        ball.update(DT);
        assertTrue(ball.getVx() > 0);
    }

    @Test
    void shouldBounceHigherInTornadoMode() {
        Ball ball = new Ball(500, Field.GROUND - Ball.RADIUS);
        ball.setTornado(true);
        ball.setVelocity(0, 200);
        ball.update(DT);
        assertEquals(-Ball.TORNADO_BOUNCE, ball.getVy(), EPS);
    }

    @Test
    void shouldResetPowerShotState() {
        Ball ball = new Ball(500, 100);
        ball.setGravity(false);
        ball.setTornado(true);
        ball.clearPowerShot();
        assertTrue(ball.hasGravity());
        assertFalse(ball.isTornado());
    }

    @Test
    void shouldComputeDistanceToPoint() {
        Ball ball = new Ball(10, 10);
        assertEquals(5, ball.distanceTo(13, 14), EPS);
    }

    @Test
    void shouldExposeImmutablePositionAndVelocity() {
        Ball ball = new Ball(10, 20, 30, 40);
        assertEquals(10, ball.position().x(), EPS);
        assertEquals(20, ball.position().y(), EPS);
        assertEquals(30, ball.velocity().x(), EPS);
        assertEquals(40, ball.velocity().y(), EPS);
    }

    @Test
    void shouldUseProvidedArena() {
        Arena arena = Arena.defaultArena();
        Ball ball = new Ball(arena, 500, 100);
        assertSame(arena, ball.arena());
    }

    @Test
    void shouldRejectNegativeTime() {
        Ball ball = new Ball(500, 100);
        assertThrows(IllegalArgumentException.class, () -> ball.update(-1));
    }
}
