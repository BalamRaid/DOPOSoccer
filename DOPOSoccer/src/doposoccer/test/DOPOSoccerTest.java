package doposoccer.test;

import doposoccer.domain.Ball;
import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Field;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.TimeMatch;
import doposoccer.domain.character.CharacterCatalog;
import doposoccer.domain.effect.ElectrocutedEffect;
import doposoccer.domain.effect.PushEffect;
import doposoccer.domain.effect.UnconsciousEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion del agregado de partido: goles, barra de poder,
 * patadas, tiros, efectos, tipos de partido y reglas de finalizacion.
 */
class DOPOSoccerTest {

    private static final double DT = TestSupport.DT;
    private static final double EPS = 0.001;

    private Profile p1;
    private Profile p2;

    @BeforeEach
    void setUp() {
        p1 = new Profile("Jugador 1");
        p2 = new Profile("Jugador 2");
    }

    @Test
    void shouldCreateGameWithPlayersAndBall() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        assertEquals("Camerun", game.getPlayer(Side.LEFT).getCharacter().displayName());
        assertEquals("Corea del Sur", game.getPlayer(Side.RIGHT).getCharacter().displayName());
        assertEquals(Field.WIDTH / 2, game.getBall().getX(), EPS);
        assertEquals(DOPOSoccer.BALL_DROP_Y, game.getBall().getY(), EPS);
        assertEquals(0, game.goalsOf(Side.LEFT));
    }

    @Test
    void shouldScoreWhenBallFullyEntersLeftGoal() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        TestSupport.scoreInLeftGoal(game);
        game.step(DT);
        assertEquals(1, game.goalsOf(Side.RIGHT));
        assertEquals(0, game.goalsOf(Side.LEFT));
    }

    @Test
    void shouldGiveQuarterBarToConceder() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        TestSupport.scoreInLeftGoal(game);
        game.step(DT);
        assertEquals(0.25 + DT / 10, game.getPlayer(Side.LEFT).getPowerBar(), EPS);
    }

    @Test
    void shouldResetAfterGoal() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        left.setX(100);
        TestSupport.scoreInRightGoal(game);
        game.step(DT);
        assertEquals(1, game.goalsOf(Side.LEFT));
        assertEquals(Field.WIDTH / 2, game.getBall().getX(), EPS);
        assertEquals(DOPOSoccer.BALL_DROP_Y, game.getBall().getY(), EPS);
        assertEquals(Field.WIDTH * 0.25, left.getX(), EPS);
    }

    @Test
    void shouldNotScoreOverCrossbar() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        game.getBall().setPosition(30, 100);
        game.step(DT);
        assertEquals(0, game.goalsOf(Side.RIGHT));
    }

    @Test
    void shouldNotScoreWhenBallIsPartiallyOutsideGoal() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        // El balon se solapa con el poste frontal: su centro esta dentro del
        // arco, pero el circulo completo no cabe en la boca.
        game.getBall().setPosition(Field.GOAL_DEPTH - Ball.RADIUS + 5, Field.GROUND - 40);
        game.step(DT);
        assertEquals(0, game.goalsOf(Side.RIGHT));
    }

    @Test
    void shouldFinishGoalMatchAndGivePoints() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        for (int i = 0; i < 3; i++) {
            TestSupport.scoreInRightGoal(game);
            game.step(DT);
        }
        assertTrue(game.isFinished());
        assertSame(game.getPlayer(Side.LEFT), game.getWinner());
        assertEquals(100, p1.getPoints());
        assertEquals(25, p2.getPoints());
    }

    @Test
    void shouldEndTiedGameWithoutWinner() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        game.finish();
        assertTrue(game.isFinished());
        assertNull(game.getWinner());
        assertEquals(25, p1.getPoints());
        assertEquals(25, p2.getPoints());
    }

    @Test
    void shouldNotAdvanceClockWhilePaused() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        game.togglePause();
        TestSupport.run(game, 2);
        assertEquals(0, game.getElapsedSeconds(), EPS);
        game.togglePause();
        TestSupport.run(game, 1);
        assertEquals(1, game.getElapsedSeconds(), 0.02);
    }

    @Test
    void shouldEnterSuddenDeathOnTie() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(p1, TestSupport.character("cameroon"),
                p2, TestSupport.character("south-korea"), TimeMatch.standard());
        TestSupport.run(game, 61);
        assertTrue(game.isSuddenDeath());
        assertFalse(game.isFinished());
        TestSupport.scoreInLeftGoal(game);
        game.step(DT);
        assertTrue(game.isFinished());
        assertSame(game.getPlayer(Side.RIGHT), game.getWinner());
    }

    @Test
    void shouldFinishTimeMatchWithLeader() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(p1, TestSupport.character("cameroon"),
                p2, TestSupport.character("south-korea"), TimeMatch.standard());
        TestSupport.scoreInRightGoal(game);
        game.step(DT);
        game.getBall().setPosition(Field.WIDTH / 2, 50);
        game.getBall().setVelocity(0, 0);
        game.getPlayer(Side.LEFT).setX(300);
        TestSupport.run(game, 60);
        assertTrue(game.isFinished());
        assertFalse(game.isSuddenDeath());
        assertSame(game.getPlayer(Side.LEFT), game.getWinner());
    }

    @Test
    void shouldRejectInvalidMatchValues() {
        assertThrows(DOPOSoccerException.class, () -> new TimeMatch(0));
        assertThrows(DOPOSoccerException.class, () -> new TimeMatch(11));
        assertThrows(DOPOSoccerException.class, () -> new GoalMatch(0));
        assertThrows(DOPOSoccerException.class, () -> new GoalMatch(21));
    }

    @Test
    void shouldFillPowerBarInTenSeconds() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(p1, TestSupport.character("cameroon"),
                p2, TestSupport.character("cameroon"), new TimeMatch(2));
        TestSupport.run(game, 5);
        assertEquals(0.5, game.getPlayer(Side.LEFT).getPowerBar(), 0.01);
        TestSupport.run(game, 5);
        assertEquals(1, game.getPlayer(Side.LEFT).getPowerBar(), EPS);
    }

    @Test
    void shouldRejectPowerBeforeFull() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> game.activatePower(Side.LEFT));
        assertEquals(DOPOSoccerException.POWER_NOT_READY, e.getMessage());
        assertFalse(game.getPlayer(Side.LEFT).hasAura());
    }

    @Test
    void shouldActivatePowerWithFullBar() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        left.addPowerBar(1);
        game.activatePower(Side.LEFT);
        assertTrue(left.hasAura());
        assertEquals(0, left.getPowerBar(), EPS);
    }

    @Test
    void shouldRejectSecondPowerWhileAuraActive() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        left.addPowerBar(1);
        left.activatePower();
        left.addPowerBar(1);
        assertFalse(left.canActivatePower());
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class, left::activatePower);
        assertEquals(DOPOSoccerException.POWER_ALREADY_ACTIVE, e.getMessage());
    }

    @Test
    void shouldKickBallTowardsRival() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        Ball ball = game.getBall();
        ball.setPosition(left.getFootX() + 20, left.getFootY());
        ball.setVelocity(0, 0);
        left.kick();
        game.step(DT);
        assertTrue(ball.getVx() > 0);
        assertTrue(ball.getVy() < 0);
    }

    @Test
    void shouldDamageRivalInReach() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        Player right = game.getPlayer(Side.RIGHT);
        left.setX(500);
        right.setX(560);
        left.kick();
        game.step(DT);
        assertEquals(100 - left.getKickDamage(), right.getLife(), EPS);
    }

    @Test
    void shouldApplyDamageAndEffectButNotStunWhenPowerShotHits() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player left = game.getPlayer(Side.LEFT);
        Player right = game.getPlayer(Side.RIGHT);
        left.addPowerBar(1);
        left.activatePower();
        right.setX(600);
        game.getBall().setVelocity(0, 0);
        game.getBall().setPosition(left.getBodyX() + 50, left.getBodyY());
        for (int i = 0; i < 120 && right.getEffect() == null; i++) {
            game.step(DT);
        }
        assertFalse(left.hasAura());
        assertTrue(right.getEffect() instanceof ElectrocutedEffect);
        assertEquals(100 - left.getPowerShotDamage(), right.getLife(), EPS);
        // El tiro de poder no aturde por si mismo: solo aplica su efecto.
        assertFalse(right.isStunned());
    }

    @Test
    void shouldScorePowerShotWithoutDamage() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(p1, TestSupport.character("south-korea"),
                p2, TestSupport.character("cameroon"), GoalMatch.standard(3));
        Player left = game.getPlayer(Side.LEFT);
        Player right = game.getPlayer(Side.RIGHT);
        left.addPowerBar(1);
        left.activatePower();
        left.setX(850);
        right.setX(600);
        game.getBall().setVelocity(0, 0);
        game.getBall().setPosition(left.getBodyX() + 50, left.getBodyY());
        TestSupport.run(game, 0.5);
        assertEquals(1, game.goalsOf(Side.LEFT));
        assertEquals(100, right.getLife(), EPS);
        assertNull(right.getEffect());
    }

    @Test
    void shouldReplacePreviousEffect() throws DOPOSoccerException {
        Player right = newGame().getPlayer(Side.RIGHT);
        right.applyEffect(new ElectrocutedEffect());
        right.applyEffect(new PushEffect());
        assertTrue(right.getEffect() instanceof PushEffect);
    }

    @Test
    void shouldSlowAndBlockJumpWhenElectrocuted() throws DOPOSoccerException {
        Player right = newGame().getPlayer(Side.RIGHT);
        double normal = right.getSpeed();
        right.applyEffect(new ElectrocutedEffect());
        assertEquals(normal / 2, right.getSpeed(), EPS);
        assertFalse(right.canJump());
        assertTrue(right.canKick());
    }

    @Test
    void shouldLaunchAndBlockWhenUnconscious() throws DOPOSoccerException {
        Player right = newGame().getPlayer(Side.RIGHT);
        right.applyEffect(new UnconsciousEffect());
        assertTrue(right.getVy() < 0);
        assertFalse(right.canMove());
        assertFalse(right.canKick());
    }

    @Test
    void shouldEndEffectAfterDuration() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        Player right = game.getPlayer(Side.RIGHT);
        right.applyEffect(new ElectrocutedEffect());
        TestSupport.run(game, 3.1);
        assertNull(right.getEffect());
    }

    @Test
    void shouldCreateAndExpireFakeBallsWithIllusion() throws DOPOSoccerException {
        DOPOSoccer game = new DOPOSoccer(p1, TestSupport.character("usa"),
                p2, TestSupport.character("cameroon"), GoalMatch.standard(3));
        Player left = game.getPlayer(Side.LEFT);
        left.addPowerBar(1);
        left.activatePower();
        game.getBall().setVelocity(0, 0);
        game.getBall().setPosition(left.getBodyX() + 50, left.getBodyY());
        game.step(DT);
        assertEquals(7, game.getFakeBalls().size());
        TestSupport.run(game, 3);
        assertTrue(game.getFakeBalls().isEmpty());
    }

    @Test
    void shouldOfferOfficialCharacterCatalog() throws DOPOSoccerException {
        CharacterCatalog catalog = CharacterCatalog.defaultCatalog();
        assertEquals(6, catalog.size());
        assertEquals("Nigeria", catalog.require("nigeria").displayName());
    }

    @Test
    void shouldRejectUnknownCharacter() {
        assertThrows(DOPOSoccerException.class,
                () -> CharacterCatalog.defaultCatalog().require("atlantida"));
    }

    @Test
    void shouldNotCreateGameWithMissingData() throws DOPOSoccerException {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> new DOPOSoccer(p1, null, p2, TestSupport.character("cameroon"),
                        GoalMatch.standard(3)));
        assertEquals(DOPOSoccerException.NULL_ELEMENT, e.getMessage());
    }

    @Test
    void shouldRejectActivationAfterFinish() throws DOPOSoccerException {
        DOPOSoccer game = newGame();
        game.finish();
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> game.activatePower(Side.LEFT));
        assertEquals(DOPOSoccerException.GAME_FINISHED, e.getMessage());
    }

    private DOPOSoccer newGame() throws DOPOSoccerException {
        return TestSupport.newGame(p1, "cameroon", p2, "south-korea",
                GoalMatch.standard(3));
    }
}
