package doposoccer.test;

import doposoccer.domain.Ball;
import doposoccer.domain.Field;
import doposoccer.domain.Side;
import doposoccer.domain.character.IllusionShot;
import doposoccer.domain.character.SpecialAbility;
import doposoccer.domain.character.StraightShot;
import doposoccer.domain.character.TornadoShot;
import doposoccer.domain.effect.PushEffect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la composicion de poderes: metadatos, fabrica de efectos y
 * trayectorias reutilizables.
 */
class SpecialAbilityTest {

    private static final double EPS = 1e-6;

    @Test
    void shouldExposeMetadata() {
        SpecialAbility ability = ability();
        assertEquals("Rayo", ability.name());
        assertEquals(60, ability.damage());
        assertEquals("Electrocutado", ability.effectName());
    }

    @Test
    void shouldCreateANewEffectEachTime() {
        SpecialAbility ability = ability();
        assertNotSame(ability.createEffect(), ability.createEffect());
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new SpecialAbility(" ", 1, "E", PushEffect::new, new StraightShot(1)));
    }

    @Test
    void shouldRejectBlankEffectName() {
        assertThrows(IllegalArgumentException.class,
                () -> new SpecialAbility("P", 1, "", PushEffect::new, new StraightShot(1)));
    }

    @Test
    void shouldRejectNegativeDamage() {
        assertThrows(IllegalArgumentException.class,
                () -> new SpecialAbility("P", -1, "E", PushEffect::new, new StraightShot(1)));
    }

    @Test
    void shouldRejectNullFactory() {
        assertThrows(NullPointerException.class,
                () -> new SpecialAbility("P", 1, "E", null, new StraightShot(1)));
    }

    @Test
    void shouldRejectNullShot() {
        assertThrows(NullPointerException.class,
                () -> new SpecialAbility("P", 1, "E", PushEffect::new, null));
    }

    @Test
    void shouldLaunchStraightShotWithoutGravity() {
        Ball ball = new Ball(500, 300);
        ability().launch(ball, Side.LEFT, fake -> {
        });
        assertTrue(ball.getVx() > 0);
        assertEquals(0, ball.getVy(), EPS);
        assertFalse(ball.hasGravity());
    }

    @Test
    void shouldLaunchTornadoShotFromGround() {
        Ball ball = new Ball(500, 100);
        new TornadoShot(800).launch(ball, Side.RIGHT, fake -> {
        });
        assertTrue(ball.getVx() < 0);
        assertTrue(ball.isTornado());
        assertEquals(Field.GROUND - Ball.RADIUS, ball.getY(), EPS);
    }

    @Test
    void shouldCreateFakeBallsWithIllusion() {
        List<Ball> fakes = new ArrayList<>();
        Ball ball = new Ball(500, 300);
        new IllusionShot(3).launch(ball, Side.LEFT, fakes::add);
        assertEquals(3, fakes.size());
        assertTrue(ball.getVx() > 0);
        for (Ball fake : fakes) {
            assertTrue(fake.getVx() > 0);
        }
    }

    @Test
    void shouldRejectNegativeFakeCount() {
        assertThrows(IllegalArgumentException.class, () -> new IllusionShot(-1));
    }

    private static SpecialAbility ability() {
        return new SpecialAbility("Rayo", 60, "Electrocutado",
                doposoccer.domain.effect.ElectrocutedEffect::new, new StraightShot(950));
    }
}
