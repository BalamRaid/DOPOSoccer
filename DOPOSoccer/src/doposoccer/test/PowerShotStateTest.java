package doposoccer.test;

import doposoccer.domain.Ball;
import doposoccer.domain.Player;
import doposoccer.domain.PowerShotState;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.effect.ConfusionEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del estado de tiros de poder y de los balones falsos.
 */
class PowerShotStateTest {

    private Ball ball;
    private Player shooter;
    private PowerShotState state;

    @BeforeEach
    void setUp() {
        ball = new Ball(500, 100);
        shooter = new Player(new Profile("Tirador"),
                TestSupport.character("usa"), Side.LEFT);
        state = new PowerShotState(ball);
    }

    @Test
    void shouldRejectNullBall() {
        assertThrows(IllegalArgumentException.class, () -> new PowerShotState(null));
    }

    @Test
    void shouldRejectNullShooter() {
        assertThrows(IllegalArgumentException.class, () -> state.begin(null));
    }

    @Test
    void shouldStartAndEndShot() {
        state.begin(shooter);
        assertTrue(state.isActive());
        assertSame(shooter, state.shooter());
        state.end();
        assertFalse(state.isActive());
        assertNull(state.shooter());
        assertTrue(ball.hasGravity());
    }

    @Test
    void shouldReplacePreviousShot() {
        Player other = new Player(new Profile("Otro"),
                TestSupport.character("cameroon"), Side.RIGHT);
        state.begin(shooter);
        state.begin(other);
        assertSame(other, state.shooter());
    }

    @Test
    void shouldExposeUnmodifiableFakeList() {
        Ball fake = new Ball(400, 200);
        state.addFakeBall(fake);
        assertSame(fake, state.fakeBalls().get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> state.fakeBalls().clear());
    }

    @Test
    void shouldRejectNullFakeBall() {
        assertThrows(IllegalArgumentException.class, () -> state.addFakeBall(null));
    }

    @Test
    void shouldExpireFakeBalls() {
        state.addFakeBall(new Ball(400, 200));
        state.update(ConfusionEffect.DURATION - 0.01);
        assertFalse(state.fakeBalls().isEmpty());
        state.update(0.02);
        assertTrue(state.fakeBalls().isEmpty());
    }

    @Test
    void shouldRenewFakeTimerAfterImpact() {
        state.addFakeBall(new Ball(400, 200));
        state.update(2);
        state.renewFakeTimer();
        state.update(2);
        assertFalse(state.fakeBalls().isEmpty());
        state.update(0.6);
        assertTrue(state.fakeBalls().isEmpty());
    }

    @Test
    void shouldKeepFakeBallsWhenEndingShot() {
        state.addFakeBall(new Ball(400, 200));
        state.begin(shooter);
        state.end();
        assertEquals(1, state.fakeBalls().size());
    }

    @Test
    void shouldClearShotAndFakeBalls() {
        state.addFakeBall(new Ball(400, 200));
        state.begin(shooter);
        state.clear();
        assertFalse(state.isActive());
        assertTrue(state.fakeBalls().isEmpty());
    }

    @Test
    void shouldRejectNegativeTime() {
        assertThrows(IllegalArgumentException.class, () -> state.update(-0.1));
    }
}
