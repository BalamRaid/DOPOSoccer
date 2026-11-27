package doposoccer.test;

import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.effect.ConfusionEffect;
import doposoccer.domain.effect.Effect;
import doposoccer.domain.effect.ElectrocutedEffect;
import doposoccer.domain.effect.FreezeEffect;
import doposoccer.domain.effect.PushEffect;
import doposoccer.domain.effect.ShrinkEffect;
import doposoccer.domain.effect.UnconsciousEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los efectos temporales y de su integracion con el jugador.
 */
class EffectTest {

    private static final double EPS = 1e-6;

    @Test
    void shouldCountDownAndFinish() {
        Effect effect = new ElectrocutedEffect();
        Player player = player();
        effect.update(player, 1);
        assertEquals(2, effect.getRemaining(), EPS);
        assertFalse(effect.isFinished());
        effect.update(player, 2.5);
        assertEquals(0, effect.getRemaining(), EPS);
        assertTrue(effect.isFinished());
    }

    @Test
    void shouldPushRivalAndBlockMovement() {
        PushEffect effect = new PushEffect();
        Player player = player();
        player.setVx(0);
        effect.update(player, 0.1);
        assertTrue(player.getVx() < 0);
        assertFalse(effect.canMove());
        assertTrue(effect.canJump());
        assertTrue(effect.canKick());
    }

    @Test
    void shouldSlowAndBlockJumpWhenElectrocuted() {
        ElectrocutedEffect effect = new ElectrocutedEffect();
        assertEquals(0.5, effect.speedFactor(), EPS);
        assertFalse(effect.canJump());
        assertTrue(effect.canMove());
        assertTrue(effect.canKick());
    }

    @Test
    void shouldLaunchAndBlockWhenUnconscious() {
        UnconsciousEffect effect = new UnconsciousEffect();
        Player player = player();
        effect.onApply(player);
        assertTrue(player.getVy() < 0);
        assertFalse(effect.canMove());
        assertFalse(effect.canJump());
        assertFalse(effect.canKick());
    }

    @Test
    void shouldInvertControlsWhenConfused() {
        ConfusionEffect effect = new ConfusionEffect();
        assertTrue(effect.invertControls());
        assertTrue(effect.canMove());
        assertTrue(effect.canJump());
        assertTrue(effect.canKick());
    }

    @Test
    void shouldFreezeAllActions() {
        FreezeEffect effect = new FreezeEffect();
        assertFalse(effect.canMove());
        assertFalse(effect.canJump());
        assertFalse(effect.canKick());
        assertTrue(effect.getName().startsWith("Congel"));
    }

    @Test
    void shouldShrinkBody() {
        ShrinkEffect effect = new ShrinkEffect();
        Player player = player();
        player.applyEffect(effect);
        assertEquals(Player.BODY_RADIUS * ShrinkEffect.SIZE_FACTOR,
                player.getBodyRadius(), EPS);
    }

    @Test
    void shouldReplacePreviousEffect() {
        Player player = player();
        player.applyEffect(new ElectrocutedEffect());
        player.applyEffect(new PushEffect());
        assertTrue(player.getEffect() instanceof PushEffect);
    }

    @Test
    void shouldExpireWhenPlayerUpdates() {
        Player player = player();
        player.applyEffect(new ElectrocutedEffect());
        for (int i = 0; i < 190; i++) {
            player.update(TestSupport.DT);
        }
        assertNull(player.getEffect());
    }

    @Test
    void shouldInvertMovementDirectionWhenConfused() {
        Player player = player();
        player.setX(500);
        player.setMovingRight(true);
        player.applyEffect(new ConfusionEffect());
        player.update(TestSupport.DT);
        assertTrue(player.getX() < 500);
    }

    @Test
    void shouldBlockPlayerActionsWhenFrozen() {
        Player player = player();
        player.applyEffect(new FreezeEffect());
        assertFalse(player.canMove());
        assertFalse(player.canJump());
        assertFalse(player.canKick());
    }

    @Test
    void shouldRejectNegativeTime() {
        Effect effect = new ElectrocutedEffect();
        assertThrows(IllegalArgumentException.class,
                () -> effect.update(player(), -0.1));
    }

    @Test
    void shouldRejectNullTarget() {
        Effect effect = new ElectrocutedEffect();
        assertThrows(IllegalArgumentException.class, () -> effect.update(null, 0.1));
    }

    @Test
    void shouldScaleSpeedWhenElectrocuted() {
        Player player = player();
        double normal = player.getSpeed();
        player.applyEffect(new ElectrocutedEffect());
        assertEquals(normal / 2, player.getSpeed(), EPS);
    }

    private static Player player() {
        Profile profile = new Profile("Prueba");
        return new Player(profile, TestSupport.character("cameroon"), Side.LEFT);
    }
}
