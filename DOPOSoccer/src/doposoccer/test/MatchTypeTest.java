package doposoccer.test;

import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.SurvivalMatch;
import doposoccer.domain.TimeMatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de finalizacion de los tres tipos de partido.
 */
class MatchTypeTest {

    private static final double EPS = 1e-6;

    @Test
    void shouldAcceptOfficialGoalTargets() throws DOPOSoccerException {
        assertEquals(3, GoalMatch.standard(3).getGoalsToWin());
        assertEquals(5, GoalMatch.standard(5).getGoalsToWin());
        assertEquals(7, GoalMatch.standard(7).getGoalsToWin());
    }

    @Test
    void shouldRejectNonOfficialGoalTargets() {
        assertThrows(DOPOSoccerException.class, () -> GoalMatch.standard(4));
        assertThrows(DOPOSoccerException.class, () -> GoalMatch.standard(0));
    }

    @Test
    void shouldKeepLegacyGoalRangeForConstructor() throws DOPOSoccerException {
        assertEquals(1, new GoalMatch(1).getGoalsToWin());
        assertEquals(20, new GoalMatch(20).getGoalsToWin());
        assertThrows(DOPOSoccerException.class, () -> new GoalMatch(0));
        assertThrows(DOPOSoccerException.class, () -> new GoalMatch(21));
    }

    @Test
    void shouldFinishGoalMatchAtTarget() throws DOPOSoccerException {
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", GoalMatch.standard(3));
        game.getScore().addGoal(Side.LEFT);
        game.getScore().addGoal(Side.LEFT);
        assertFalse(game.getMatchType().isOver(game));
        game.getScore().addGoal(Side.LEFT);
        assertTrue(game.getMatchType().isOver(game));
        assertSame(Side.LEFT, game.getMatchType().winner(game));
    }

    @Test
    void shouldDescribeGoalMatch() throws DOPOSoccerException {
        assertEquals("Por goles (5)", GoalMatch.standard(5).describe());
    }

    @Test
    void shouldUseSixtySecondsAsStandardTime() throws DOPOSoccerException {
        TimeMatch type = TimeMatch.standard();
        assertEquals(1, type.getMinutes());
        assertEquals(60, type.durationSeconds(), EPS);
    }

    @Test
    void shouldReportRemainingTime() throws DOPOSoccerException {
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", TimeMatch.standard());
        game.getClock().advance(10);
        assertEquals(50, game.getMatchType().remainingSeconds(game), EPS);
    }

    @Test
    void shouldEnterSuddenDeathOnTie() throws DOPOSoccerException {
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", TimeMatch.standard());
        game.getClock().advance(60);
        assertFalse(game.getMatchType().isOver(game));
        assertTrue(game.isSuddenDeath());
    }

    @Test
    void shouldFinishTimeMatchWhenLeaderExists() throws DOPOSoccerException {
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", TimeMatch.standard());
        game.getScore().addGoal(Side.LEFT);
        game.getClock().advance(60);
        assertTrue(game.getMatchType().isOver(game));
        assertSame(Side.LEFT, game.getMatchType().winner(game));
    }

    @Test
    void shouldRejectInvalidMinutes() {
        assertThrows(DOPOSoccerException.class, () -> new TimeMatch(0));
        assertThrows(DOPOSoccerException.class, () -> new TimeMatch(11));
    }

    @Test
    void shouldDescribeTimeMatch() throws DOPOSoccerException {
        assertEquals("Por tiempo (1 min)", TimeMatch.standard().describe());
    }

    @Test
    void shouldConfigureSurvivalBalls() throws DOPOSoccerException {
        SurvivalMatch type = SurvivalMatch.standard();
        assertEquals(3, type.initialBalls());
        assertTrue(type.isSurvival());
        assertEquals("Supervivencia (3 balones)", type.describe());
    }

    @Test
    void shouldFinishSurvivalWhenRivalLosesAllBalls() throws DOPOSoccerException {
        SurvivalMatch type = new SurvivalMatch(2);
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", type);
        game.getPlayer(Side.RIGHT).loseBall();
        assertFalse(type.isOver(game));
        game.getPlayer(Side.RIGHT).loseBall();
        assertTrue(type.isOver(game));
        assertSame(Side.LEFT, type.winner(game));
    }

    @Test
    void shouldTieSurvivalWhenBothRunOut() throws DOPOSoccerException {
        SurvivalMatch type = new SurvivalMatch(1);
        DOPOSoccer game = TestSupport.newGame(new Profile("A"), "cameroon",
                new Profile("B"), "south-korea", type);
        game.getPlayer(Side.LEFT).loseBall();
        game.getPlayer(Side.RIGHT).loseBall();
        assertNull(type.winner(game));
    }

    @Test
    void shouldRejectInvalidSurvivalBalls() {
        assertThrows(DOPOSoccerException.class, () -> new SurvivalMatch(0));
        assertThrows(DOPOSoccerException.class, () -> new SurvivalMatch(10));
    }
}
