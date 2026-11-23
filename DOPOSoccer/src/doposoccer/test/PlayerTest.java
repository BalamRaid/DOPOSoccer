package doposoccer.test;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Field;
import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.character.CharacterDefinition;
import doposoccer.domain.control.PlayerIntent;
import doposoccer.domain.effect.ConfusionEffect;
import doposoccer.domain.effect.FreezeEffect;
import doposoccer.domain.effect.ShrinkEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la entidad jugador: atributos derivados, control, vida, efectos y
 * balones de supervivencia.
 */
class PlayerTest {

    private static final double DT = TestSupport.DT;
    private static final double EPS = 0.001;

    private Profile profile;
    private Player player;

    @BeforeEach
    void setUp() {
        profile = new Profile("Prueba");
        CharacterDefinition cameroon = TestSupport.character("cameroon");
        player = new Player(profile, cameroon, Side.LEFT);
    }

    @Test
    void shouldStartAtLeftQuarterOfArena() {
        assertEquals(Field.WIDTH * 0.25, player.getX(), EPS);
        assertEquals(Field.GROUND, player.getY(), EPS);
        assertTrue(player.isOnGround());
    }

    @Test
    void shouldStartWithFullLifeAndNoPower() {
        assertEquals(100, player.getLife(), EPS);
        assertEquals(0, player.getPowerBar(), EPS);
        assertFalse(player.hasAura());
        assertFalse(player.isStunned());
    }

    @Test
    void shouldApplyCharacterSpeed() {
        assertEquals(Player.BASE_SPEED, player.getSpeed(), EPS);
    }

    @Test
    void shouldApplyCharacterJump() {
        assertEquals(Player.BASE_JUMP, player.getJumpSpeed(), EPS);
    }

    @Test
    void shouldApplyCharacterStrengthToKick() {
        assertEquals(Player.KICK_BASE_DAMAGE * 1.2, player.getKickDamage(), EPS);
    }

    @Test
    void shouldApplyCharacterStrengthToPowerShot() {
        assertEquals(60 * 1.2, player.getPowerShotDamage(), EPS);
    }

    @Test
    void shouldMoveRight() {
        double x = player.getX();
        player.setMovingRight(true);
        player.update(DT);
        assertTrue(player.getX() > x);
    }

    @Test
    void shouldMoveLeft() {
        player.setX(500);
        player.setMovingLeft(true);
        player.update(DT);
        assertTrue(player.getX() < 500);
    }

    @Test
    void shouldJump() {
        player.jump();
        player.update(DT);
        assertTrue(player.getVy() < 0);
        assertTrue(player.getY() < Field.GROUND);
    }

    @Test
    void shouldApplyIntentMovement() {
        double x = player.getX();
        player.applyIntent(PlayerIntent.move(1));
        player.update(DT);
        assertTrue(player.getX() > x);
    }

    @Test
    void shouldApplyIntentJump() {
        player.applyIntent(new PlayerIntent(0, true, false, false));
        player.update(DT);
        assertTrue(player.getVy() < 0);
    }

    @Test
    void shouldApplyIntentKick() {
        player.applyIntent(new PlayerIntent(0, false, true, false));
        assertTrue(player.isKicking());
    }

    @Test
    void shouldApplyIntentSpecial() {
        player.addPowerBar(1);
        player.applyIntent(new PlayerIntent(0, false, false, true));
        assertTrue(player.hasAura());
    }

    @Test
    void shouldBlockMovementWhenFrozen() {
        player.setMovingRight(true);
        player.applyEffect(new FreezeEffect());
        double x = player.getX();
        player.update(DT);
        assertEquals(x, player.getX(), EPS);
    }

    @Test
    void shouldInvertIntentWhenConfused() {
        player.setX(500);
        player.setMovingRight(true);
        player.applyEffect(new ConfusionEffect());
        player.update(DT);
        assertTrue(player.getX() < 500);
    }

    @Test
    void shouldReduceBodyRadiusWhenShrunk() {
        player.applyEffect(new ShrinkEffect());
        assertEquals(Player.BODY_RADIUS * 0.5, player.getBodyRadius(), EPS);
    }

    @Test
    void shouldDamageAndStunAtZero() {
        player.receiveDamage(500);
        assertEquals(0, player.getLife(), EPS);
        assertTrue(player.isStunned());
        assertFalse(player.canMove());
    }

    @Test
    void shouldIgnoreDamageWhileStunned() {
        player.receiveDamage(500);
        player.receiveDamage(500);
        assertEquals(0, player.getLife(), EPS);
    }

    @Test
    void shouldRecoverLifeAfterStun() {
        player.receiveDamage(500);
        for (int i = 0; i < 190; i++) {
            player.update(DT);
        }
        assertFalse(player.isStunned());
        assertEquals(player.getMaxLife(), player.getLife(), EPS);
    }

    @Test
    void shouldFillPowerBarInTenSeconds() {
        for (int i = 0; i < 600; i++) {
            player.update(DT);
        }
        assertEquals(1, player.getPowerBar(), EPS);
    }

    @Test
    void shouldActivatePower() throws DOPOSoccerException {
        player.addPowerBar(1);
        player.activatePower();
        assertTrue(player.hasAura());
        assertEquals(0, player.getPowerBar(), EPS);
    }

    @Test
    void shouldRejectPowerBeforeFull() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                player::activatePower);
        assertEquals(DOPOSoccerException.POWER_NOT_READY, e.getMessage());
    }

    @Test
    void shouldRejectSecondAura() throws DOPOSoccerException {
        player.addPowerBar(1);
        player.activatePower();
        player.addPowerBar(1);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                player::activatePower);
        assertEquals(DOPOSoccerException.POWER_ALREADY_ACTIVE, e.getMessage());
    }

    @Test
    void shouldClampPowerBar() {
        player.addPowerBar(2);
        assertEquals(1, player.getPowerBar(), EPS);
        player.addPowerBar(-5);
        assertEquals(0, player.getPowerBar(), EPS);
    }

    @Test
    void shouldLoseSurvivalBalls() {
        player.setBalls(3);
        player.loseBall();
        assertEquals(2, player.getBalls());
        player.loseBall();
        player.loseBall();
        player.loseBall();
        assertEquals(0, player.getBalls());
    }

    @Test
    void shouldResetToInitialPosition() {
        player.setX(100);
        player.resetPosition();
        assertEquals(Field.WIDTH * 0.25, player.getX(), EPS);
        assertEquals(Field.GROUND, player.getY(), EPS);
    }

    @Test
    void shouldComputeBodyAndFootPositions() {
        assertEquals(player.getY() - player.getBodyRadius(), player.getBodyY(), EPS);
        assertEquals(player.getX() + player.getBodyRadius() * 0.8,
                player.getFootX(), EPS);
        assertEquals(player.getY() - 12, player.getFootY(), EPS);
    }

    @Test
    void shouldRejectNegativeBalls() {
        assertThrows(IllegalArgumentException.class, () -> player.setBalls(-1));
    }
}
