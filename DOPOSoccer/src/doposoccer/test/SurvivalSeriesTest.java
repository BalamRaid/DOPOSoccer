package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.application.SurvivalSeries;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.SurvivalMatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la serie de rondas de supervivencia.
 */
class SurvivalSeriesTest {

    @Test
    void shouldRejectNullConfig() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new SurvivalSeries(null));
        assertEquals(DOPOSoccerException.NULL_ELEMENT, e.getMessage());
    }

    @Test
    void shouldRejectNonPvmSeries() throws DOPOSoccerException {
        MatchConfig config = config(GameMode.PVP, SurvivalMatch.standard());
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new SurvivalSeries(config));
        assertEquals(DOPOSoccerException.INVALID_MODE, e.getMessage());
    }

    @Test
    void shouldRejectNonSurvivalSeries() throws DOPOSoccerException {
        MatchConfig config = config(GameMode.PVM, GoalMatch.standard(3));
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new SurvivalSeries(config));
        assertEquals(DOPOSoccerException.INVALID_MODE, e.getMessage());
    }

    @Test
    void shouldStartFirstRound() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        GameSession session = series.startRound();
        assertEquals(3, session.match().getPlayer(Side.LEFT).getBalls());
        assertEquals(1, series.round());
        assertFalse(series.isOver());
    }

    @Test
    void shouldAdvanceAfterHumanWin() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(Side.LEFT));
        assertEquals(1, series.humanWins());
        assertEquals(2, series.round());
        assertFalse(series.isOver());
    }

    @Test
    void shouldEndAfterMachineWin() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(Side.RIGHT));
        assertEquals(1, series.machineWins());
        assertTrue(series.isOver());
    }

    @Test
    void shouldEndOnDraw() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(null));
        assertTrue(series.isOver());
    }

    @Test
    void shouldIgnoreResultsAfterEnd() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(Side.RIGHT));
        series.record(result(Side.LEFT));
        assertEquals(0, series.humanWins());
        assertEquals(1, series.machineWins());
    }

    @Test
    void shouldRejectStartAfterEnd() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(Side.RIGHT));
        assertThrows(DOPOSoccerException.class, series::startRound);
    }

    @Test
    void shouldCountMultipleHumanWins() throws DOPOSoccerException {
        SurvivalSeries series = new SurvivalSeries(config(GameMode.PVM, new SurvivalMatch(3)));
        series.record(result(Side.LEFT));
        series.record(result(Side.LEFT));
        assertEquals(2, series.humanWins());
        assertEquals(3, series.round());
    }

    @Test
    void shouldExposeBaseConfig() throws DOPOSoccerException {
        MatchConfig config = config(GameMode.PVM, new SurvivalMatch(5));
        assertSame(config, new SurvivalSeries(config).config());
    }

    private static MatchConfig config(GameMode mode, MatchType type) {
        return new MatchConfig(mode, type,
                new Profile("Humano"), TestSupport.character("cameroon"),
                new Profile("Maquina"), TestSupport.character("south-korea"));
    }

    private static MatchResult result(Side winner) {
        return new MatchResult(winner, winner == Side.LEFT ? 1 : 0,
                winner == Side.RIGHT ? 1 : 0, 2, 1, 100, 25, true,
                "Supervivencia (3 balones)");
    }
}
