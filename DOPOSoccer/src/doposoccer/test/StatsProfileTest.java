package doposoccer.test;

import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Profile;
import doposoccer.domain.Stat;
import doposoccer.domain.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las estadisticas de perfil y de la progresion entre partidos.
 */
class StatsProfileTest {

    private static final double EPS = 1e-9;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile("Jugador");
    }

    @Test
    void shouldStartEveryStatAtOne() {
        Stats stats = new Stats();
        for (Stat stat : Stat.values()) {
            assertEquals(1, stats.get(stat));
        }
        assertEquals(100, stats.maxLife(), EPS);
        assertEquals(1, stats.kickMultiplier(), EPS);
    }

    @Test
    void shouldComputeLinearFactor() {
        Stats stats = profile.getStats();
        assertEquals(1, stats.factor(Stat.SPEED), EPS);
        for (int i = 1; i < 10; i++) {
            profile.addPoints(stats.upgradeCost(Stat.SPEED));
            try {
                profile.upgrade(Stat.SPEED);
            } catch (DOPOSoccerException e) {
                throw new AssertionError(e);
            }
        }
        assertEquals(1.9, stats.factor(Stat.SPEED), EPS);
    }

    @Test
    void shouldDoubleUpgradeCost() throws DOPOSoccerException {
        assertEquals(50, profile.getStats().upgradeCost(Stat.SPEED));
        buy(Stat.SPEED);
        assertEquals(100, profile.getStats().upgradeCost(Stat.SPEED));
        buy(Stat.SPEED);
        assertEquals(200, profile.getStats().upgradeCost(Stat.SPEED));
    }

    @Test
    void shouldIncreaseMaxLifeWithSurvival() throws DOPOSoccerException {
        for (int i = 0; i < 9; i++) {
            buy(Stat.SURVIVAL);
        }
        assertEquals(190, profile.getStats().maxLife(), EPS);
    }

    @Test
    void shouldSpendPointsWhenUpgrading() throws DOPOSoccerException {
        profile.addPoints(60);
        profile.upgrade(Stat.KICK);
        assertEquals(10, profile.getPoints());
        assertEquals(2, profile.getStats().get(Stat.KICK));
    }

    @Test
    void shouldRejectUpgradeWithoutPoints() {
        profile.addPoints(49);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> profile.upgrade(Stat.JUMP));
        assertTrue(e.getMessage().startsWith(DOPOSoccerException.NOT_ENOUGH_POINTS));
        assertEquals(49, profile.getPoints());
    }

    @Test
    void shouldRejectUpgradePastMaximum() throws DOPOSoccerException {
        for (int i = 0; i < 9; i++) {
            buy(Stat.SURVIVAL);
        }
        profile.addPoints(1_000_000);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> profile.upgrade(Stat.SURVIVAL));
        assertEquals(DOPOSoccerException.STAT_AT_MAX, e.getMessage());
    }

    @Test
    void shouldExposeProfileName() {
        assertEquals("Jugador", profile.getName());
    }

    @Test
    void shouldAddPoints() {
        profile.addPoints(125);
        assertEquals(125, profile.getPoints());
    }

    @Test
    void shouldKeepNameAndStatsTogether() {
        profile.addPoints(500);
        try {
            profile.upgrade(Stat.KICK);
        } catch (DOPOSoccerException e) {
            throw new AssertionError(e);
        }
        assertEquals("Jugador", profile.getName());
        assertEquals(2, profile.getStats().get(Stat.KICK));
    }

    private void buy(Stat stat) throws DOPOSoccerException {
        profile.addPoints(profile.getStats().upgradeCost(stat));
        profile.upgrade(stat);
    }
}
