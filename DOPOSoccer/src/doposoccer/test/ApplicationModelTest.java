package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.SurvivalMatch;
import doposoccer.domain.TimeMatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los valores de la capa de aplicacion.
 */
class ApplicationModelTest {

    @Test
    void shouldIdentifyMachineSidesByMode() throws DOPOSoccerException {
        assertFalse(GameMode.PVP.isMachine(Side.LEFT));
        assertFalse(GameMode.PVP.isMachine(Side.RIGHT));
        assertFalse(GameMode.PVM.isMachine(Side.LEFT));
        assertTrue(GameMode.PVM.isMachine(Side.RIGHT));
        assertTrue(GameMode.MVM.isMachine(Side.LEFT));
        assertTrue(GameMode.MVM.isMachine(Side.RIGHT));
    }

    @Test
    void shouldRejectNullSideInMode() {
        assertThrows(IllegalArgumentException.class,
                () -> GameMode.PVP.isMachine(null));
    }

    @Test
    void shouldRestrictSurvivalToPvm() throws DOPOSoccerException {
        SurvivalMatch survival = SurvivalMatch.standard();
        assertFalse(GameMode.PVP.supports(survival));
        assertTrue(GameMode.PVM.supports(survival));
        assertFalse(GameMode.MVM.supports(survival));
        assertTrue(GameMode.PVP.supports(GoalMatch.standard(3)));
        assertTrue(GameMode.MVM.supports(TimeMatch.standard()));
    }

    @Test
    void shouldExposeReadableModeLabels() {
        assertEquals("Jugador vs Jugador", GameMode.PVP.toString());
        assertEquals("Jugador vs Maquina", GameMode.PVM.toString());
        assertEquals("Maquina vs Maquina", GameMode.MVM.toString());
    }

    @Test
    void shouldResolveConfigSides() throws DOPOSoccerException {
        Profile left = new Profile("Izquierda");
        Profile right = new Profile("Derecha");
        MatchConfig config = new MatchConfig(GameMode.PVP, TimeMatch.standard(),
                left, TestSupport.character("cameroon"),
                right, TestSupport.character("south-korea"));
        assertSame(left, config.profileOf(Side.LEFT));
        assertSame(right, config.profileOf(Side.RIGHT));
        assertSame(TestSupport.character("cameroon"), config.characterOf(Side.LEFT));
        assertSame(TestSupport.character("south-korea"), config.characterOf(Side.RIGHT));
    }

    @Test
    void shouldRejectNullSideInConfigLookup() throws DOPOSoccerException {
        MatchConfig config = new MatchConfig(GameMode.PVP, TimeMatch.standard(),
                new Profile("P1"), TestSupport.character("cameroon"),
                new Profile("P2"), TestSupport.character("south-korea"));
        assertThrows(IllegalArgumentException.class, () -> config.profileOf(null));
        assertThrows(IllegalArgumentException.class, () -> config.characterOf(null));
    }

    @Test
    void shouldRejectNullConfigElements() throws DOPOSoccerException {
        TimeMatch type = TimeMatch.standard();
        Profile profile = new Profile("P");
        var character = TestSupport.character("cameroon");
        assertThrows(NullPointerException.class,
                () -> new MatchConfig(null, type, profile, character, profile, character));
        assertThrows(NullPointerException.class,
                () -> new MatchConfig(GameMode.PVP, null, profile, character, profile, character));
        assertThrows(NullPointerException.class,
                () -> new MatchConfig(GameMode.PVP, type, null, character, profile, character));
        assertThrows(NullPointerException.class,
                () -> new MatchConfig(GameMode.PVP, type, profile, null, profile, character));
    }

    @Test
    void shouldExposeResultValues() {
        MatchResult result = new MatchResult(Side.LEFT, 3, 1, 0, 0, 100, 25,
                false, "Por goles (3)");
        assertEquals(Side.LEFT, result.winner());
        assertFalse(result.isDraw());
        assertEquals(3, result.leftGoals());
        assertEquals(25, result.rightPoints());
    }

    @Test
    void shouldReportDrawResult() {
        MatchResult result = new MatchResult(null, 1, 1, 0, 0, 25, 25,
                false, "Por tiempo (1 min)");
        assertTrue(result.isDraw());
    }

    @Test
    void shouldRejectNegativeResultCounters() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchResult(Side.LEFT, -1, 0, 0, 0, 0, 0, false, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> new MatchResult(Side.LEFT, 0, 0, -1, 0, 0, 0, false, "x"));
    }

    @Test
    void shouldRejectBlankResultDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchResult(null, 0, 0, 0, 0, 0, 0, false, " "));
    }
}
