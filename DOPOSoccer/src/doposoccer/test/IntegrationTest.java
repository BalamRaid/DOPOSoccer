package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.application.SurvivalSeries;
import doposoccer.domain.Ball;
import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.SurvivalMatch;
import doposoccer.domain.TimeMatch;
import doposoccer.domain.control.PlayerController;
import doposoccer.presentation.HumanController;
import doposoccer.presentation.KeyBindings;
import doposoccer.presentation.KeyboardState;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion entre aplicacion, dominio, controladores y
 * presentacion no grafica.
 */
class IntegrationTest {

    private static final double DT = TestSupport.DT;

    @Test
    void shouldKeepAiSimulationFiniteAndInsideArena() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.MVM, TimeMatch.standard()));
        runSession(session, 15);
        DOPOSoccer game = session.match();
        assertFinite(game.getBall().getX(), game.getBall().getY());
        assertFinite(game.getBall().getVx(), game.getBall().getVy());
        assertTrue(game.getBall().getX() >= Ball.RADIUS - TestSupport.EPS);
        assertTrue(game.getBall().getX() <= game.getArena().width() - Ball.RADIUS + TestSupport.EPS);
        for (Side side : Side.values()) {
            Player player = game.getPlayer(side);
            assertFinite(player.getX(), player.getY(), player.getVx(), player.getVy());
            assertTrue(player.getX() >= 0);
            assertTrue(player.getX() <= game.getArena().width());
            assertTrue(player.getY() <= game.getArena().groundY() + TestSupport.EPS);
        }
    }

    @Test
    void shouldFinishOrEnterSuddenDeathAfterRegulationTime() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.MVM, TimeMatch.standard()));
        runSession(session, 61);
        assertEquals(60, session.match().getElapsedSeconds(), 0.02);
        assertTrue(session.isFinished() || session.match().isSuddenDeath());
    }

    @Test
    void shouldMoveHumanPlayerWithKeyboardThroughSession() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVP, TimeMatch.standard()));
        KeyboardState keyboard = new KeyboardState();
        PlayerController human = new HumanController(keyboard, KeyBindings.playerOne());
        session.setController(Side.LEFT, human);
        double startX = session.match().getPlayer(Side.LEFT).getX();
        keyboard.press(KeyEvent.VK_D);
        runSession(session, 0.25);
        assertTrue(session.match().getPlayer(Side.LEFT).getX() > startX);
    }

    @Test
    void shouldLetAiMoveTowardBall() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, TimeMatch.standard()));
        session.match().getBall().setPosition(500, 450);
        session.match().getBall().setVelocity(0, 0);
        double startX = session.match().getPlayer(Side.RIGHT).getX();
        runSession(session, 0.5);
        assertTrue(session.match().getPlayer(Side.RIGHT).getX() < startX);
    }

    @Test
    void shouldConsumeSurvivalBallsAndAdvanceSeries() throws DOPOSoccerException {
        MatchConfig config = config(GameMode.PVM, new SurvivalMatch(2));
        GameSession session = GameSession.create(config);
        TestSupport.scoreInRightGoal(session.match());
        session.update(DT);
        assertEquals(1, session.match().getPlayer(Side.RIGHT).getBalls());
        assertFalse(session.isFinished());
        TestSupport.scoreInRightGoal(session.match());
        session.update(DT);
        assertTrue(session.isFinished());
        assertSame(Side.LEFT, session.result().winner());

        SurvivalSeries series = new SurvivalSeries(config);
        series.record(session.result());
        assertEquals(1, series.humanWins());
        assertEquals(2, series.round());
        assertEquals(2, series.startRound().match().getPlayer(Side.LEFT).getBalls());
    }

    @Test
    void shouldEndSurvivalRoundWhenHumanLoses() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, new SurvivalMatch(1)));
        TestSupport.scoreInLeftGoal(session.match());
        session.update(DT);
        assertTrue(session.isFinished());
        MatchResult result = session.result();
        assertSame(Side.RIGHT, result.winner());
        SurvivalSeries series = new SurvivalSeries(session.config());
        series.record(result);
        assertTrue(series.isOver());
        assertEquals(1, series.machineWins());
    }

    @Test
    void shouldDrawSurvivalWhenBothLoseLastBall() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM, new SurvivalMatch(1)));
        session.match().getPlayer(Side.LEFT).loseBall();
        session.match().getPlayer(Side.RIGHT).loseBall();
        session.update(DT);
        assertTrue(session.isFinished());
        assertTrue(session.result().isDraw());
    }

    @Test
    void shouldKeepFakeBallsInsideArenaAndExpireThem() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.MVM, TimeMatch.standard()));
        DOPOSoccer game = session.match();
        runSession(session, 20);
        for (Ball fake : game.getFakeBalls()) {
            assertFinite(fake.getX(), fake.getY());
            assertTrue(fake.getX() >= 0);
            assertTrue(fake.getX() <= game.getArena().width());
        }
        assertTrue(game.getFakeBalls().size() < 50);
    }

    @Test
    void shouldPreserveProfilesAcrossRestart() throws DOPOSoccerException {
        MatchConfig config = config(GameMode.PVP, new GoalMatch(1));
        GameSession session = GameSession.create(config);
        TestSupport.scoreInRightGoal(session.match());
        session.update(DT);
        assertTrue(session.isFinished());
        assertEquals(Profile.WIN_POINTS, config.leftProfile().getPoints());
        GameSession restarted = session.restart();
        assertNotSame(session.match(), restarted.match());
        assertNotNull(restarted.match().getBall());
        assertNull(restarted.match().getWinner());
        assertEquals(Profile.WIN_POINTS, config.leftProfile().getPoints());
    }

    private static MatchConfig config(GameMode mode, MatchType type) {
        return new MatchConfig(mode, type,
                new Profile("Humano"), TestSupport.character("cameroon"),
                new Profile("Maquina"), TestSupport.character("south-korea"));
    }

    private static void runSession(GameSession session, double seconds) {
        int frames = (int) Math.round(seconds / DT);
        for (int i = 0; i < frames; i++) {
            session.update(DT);
        }
    }

    private static void assertFinite(double... values) {
        for (double value : values) {
            assertTrue(Double.isFinite(value), "Valor no finito: " + value);
        }
    }
}
