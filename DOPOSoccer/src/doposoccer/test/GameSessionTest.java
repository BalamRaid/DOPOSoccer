package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.SurvivalMatch;
import doposoccer.domain.TimeMatch;
import doposoccer.domain.control.AiController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la fachada de aplicacion y de la instalacion de controladores.
 */
class GameSessionTest {

    private static final double DT = TestSupport.DT;

    @Test
    void shouldCreatePvpSessionWithoutMachineControllers() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        assertSame(GameMode.PVP, session.mode());
        assertTrue(session.isHumanSide(Side.LEFT));
        assertTrue(session.isHumanSide(Side.RIGHT));
        assertNull(session.match().getPlayer(Side.LEFT).controller());
        assertNull(session.match().getPlayer(Side.RIGHT).controller());
    }

    @Test
    void shouldInstallMachineOnRightForPvm() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, TimeMatch.standard()));
        assertTrue(session.isHumanSide(Side.LEFT));
        assertFalse(session.isHumanSide(Side.RIGHT));
        assertNull(session.match().getPlayer(Side.LEFT).controller());
        assertTrue(session.match().getPlayer(Side.RIGHT).controller() instanceof AiController);
    }

    @Test
    void shouldInstallMachineOnBothSidesForMvm() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.MVM, TimeMatch.standard()));
        assertTrue(session.match().getPlayer(Side.LEFT).controller() instanceof AiController);
        assertTrue(session.match().getPlayer(Side.RIGHT).controller() instanceof AiController);
    }

    @Test
    void shouldRejectSurvivalInPvp() throws DOPOSoccerException {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> GameSession.create(config(GameMode.PVP, SurvivalMatch.standard())));
        assertEquals(DOPOSoccerException.INVALID_MODE, e.getMessage());
    }

    @Test
    void shouldConfigureSurvivalBalls() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, new SurvivalMatch(4)));
        assertEquals(4, session.match().getPlayer(Side.LEFT).getBalls());
        assertEquals(4, session.match().getPlayer(Side.RIGHT).getBalls());
    }

    @Test
    void shouldAdvanceClock() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        for (int i = 0; i < 60; i++) {
            session.update(DT);
        }
        assertEquals(1, session.match().getElapsedSeconds(), 0.02);
    }

    @Test
    void shouldPauseAndResumeClock() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        session.pause();
        assertTrue(session.isPaused());
        session.update(1);
        assertEquals(0, session.match().getElapsedSeconds(), 1e-9);
        session.resume();
        session.update(0.5);
        assertEquals(0.5, session.match().getElapsedSeconds(), 0.01);
    }

    @Test
    void shouldDelegateControllerAssignment() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        session.setController(Side.LEFT, new AiController());
        assertTrue(session.match().getPlayer(Side.LEFT).controller() instanceof AiController);
    }

    @Test
    void shouldBuildResultBeforeFinish() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        MatchResult result = session.result();
        assertFalse(session.isFinished());
        assertEquals(0, result.leftPoints());
        assertTrue(result.isDraw());
    }

    @Test
    void shouldBuildResultAfterFinish() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, GoalMatch.standard(3)));
        session.match().getScore().addGoal(Side.LEFT);
        session.finish();
        MatchResult result = session.result();
        assertTrue(session.isFinished());
        assertSame(Side.LEFT, result.winner());
        assertEquals(100, result.leftPoints());
        assertEquals(25, result.rightPoints());
    }

    @Test
    void shouldIncludeBallsInSurvivalResult() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, new SurvivalMatch(3)));
        session.match().getPlayer(Side.RIGHT).loseBall();
        session.finish();
        MatchResult result = session.result();
        assertTrue(result.survival());
        assertEquals(3, result.leftBalls());
        assertEquals(2, result.rightBalls());
    }

    @Test
    void shouldRestartWithFreshMatch() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        GameSession restarted = session.restart();
        assertNotSame(session.match(), restarted.match());
        assertSame(session.config(), restarted.config());
    }

    private static MatchConfig config(GameMode mode, MatchType type) {
        return new MatchConfig(mode, type,
                new Profile("Izquierda"), TestSupport.character("cameroon"),
                new Profile("Derecha"), TestSupport.character("south-korea"));
    }
}
