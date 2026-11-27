package doposoccer.test;

import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.Weather;
import doposoccer.domain.match.Goal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del clima (viento y lluvia) y de la deteccion de gol inminente que
 * activa la camara lenta.
 */
class WeatherTest {

    private static final double DT = 1 / 60.0;
    private static final double EPS = 1e-9;

    private DOPOSoccer game;

    @BeforeEach
    void setUp() throws DOPOSoccerException {
        game = new DOPOSoccer(new Profile("Jugador 1"), TestSupport.character("cameroon"),
                new Profile("Jugador 2"), TestSupport.character("south-korea"), GoalMatch.standard(3));
    }

    @Test
    void shouldStartWithClearWeather() {
        assertSame(Weather.CLEAR, game.getWeather());
        assertEquals(0, game.currentWind(), EPS);
    }

    @Test
    void shouldNotAllowNullWeather() {
        assertThrows(IllegalArgumentException.class, () -> game.setWeather(null));
    }

    @Test
    void windShouldChangeDirectionAlongTheGust() {
        double quarter = Weather.GUST_PERIOD / 4;
        assertTrue(Weather.WIND.windAt(quarter) > 0);
        assertTrue(Weather.WIND.windAt(3 * quarter) < 0);
        assertEquals(0, Weather.CLEAR.windAt(quarter), EPS);
        assertEquals(0, Weather.RAIN.windAt(quarter), EPS);
    }

    @Test
    void windShouldPushTheBallWhileItIsInTheAir() {
        DOPOSoccer calm = copyWithBallInAir(Weather.CLEAR);
        DOPOSoccer windy = copyWithBallInAir(Weather.WIND);
        for (int i = 0; i < 20; i++) {
            calm.step(DT);
            windy.step(DT);
        }
        assertTrue(windy.currentWind() > 0, "a los pocos segundos la rafaga sopla a la derecha");
        assertTrue(windy.getBall().velocity().x() > calm.getBall().velocity().x() + 1,
                "el viento debe empujar el balon en el aire");
    }

    @Test
    void windShouldNotPushTheBallOnTheGround() throws DOPOSoccerException {
        game.setWeather(Weather.WIND);
        advanceTo(game, Weather.GUST_PERIOD / 4);
        game.getBall().setPosition(500, game.getArena().groundY() - 12);
        game.getBall().setVelocity(0, 0);
        game.step(DT);
        assertEquals(0, game.getBall().velocity().x(), 1e-6);
    }

    @Test
    void rainShouldMakePlayersSlide() {
        game.setWeather(Weather.RAIN);
        Player left = game.getPlayer(Side.LEFT);
        left.setMovingRight(true);
        game.step(DT);
        double firstStep = left.getVx();
        assertTrue(firstStep > 0 && firstStep < left.getSpeed() * 0.5,
                "con lluvia no alcanza la velocidad de inmediato");
        for (int i = 0; i < 90; i++) {
            game.step(DT);
        }
        assertEquals(left.getSpeed(), left.getVx(), left.getSpeed() * 0.02);
        left.setMovingRight(false);
        game.step(DT);
        assertTrue(left.getVx() > 0, "al soltar la tecla sigue resbalando");
    }

    @Test
    void clearWeatherShouldKeepInstantMovement() {
        Player left = game.getPlayer(Side.LEFT);
        left.setMovingRight(true);
        game.step(DT);
        assertEquals(left.getSpeed(), left.getVx(), EPS);
        left.setMovingRight(false);
        game.step(DT);
        assertEquals(0, left.getVx(), EPS);
    }

    @Test
    void shouldRejectInvalidGrip() {
        Player left = game.getPlayer(Side.LEFT);
        assertThrows(IllegalArgumentException.class, () -> left.setGrip(0));
        assertThrows(IllegalArgumentException.class, () -> left.setGrip(1.5));
    }

    @Test
    void shouldDetectImminentGoalWhenBallHeadsIntoGoal() {
        Goal right = game.getArena().goalOf(Side.RIGHT);
        double y = game.getArena().groundY() - 40;
        game.getBall().setPosition(right.frontPostX() - 60, y);
        game.getBall().setVelocity(500, 0);
        assertTrue(game.isGoalImminent());
    }

    @Test
    void shouldNotDetectImminentGoalWhenBallMovesAway() {
        Goal right = game.getArena().goalOf(Side.RIGHT);
        double y = game.getArena().groundY() - 40;
        game.getBall().setPosition(right.frontPostX() - 60, y);
        game.getBall().setVelocity(-500, 0);
        assertFalse(game.isGoalImminent());
    }

    @Test
    void shouldNotDetectImminentGoalFarFromGoal() {
        game.getBall().setPosition(game.getArena().width() / 2, game.getArena().groundY() - 40);
        game.getBall().setVelocity(900, 0);
        assertFalse(game.isGoalImminent());
    }

    private DOPOSoccer copyWithBallInAir(Weather weather) {
        try {
            DOPOSoccer copy = new DOPOSoccer(new Profile("A"), TestSupport.character("cameroon"),
                    new Profile("B"), TestSupport.character("south-korea"), GoalMatch.standard(3));
            copy.setWeather(weather);
            advanceTo(copy, Weather.GUST_PERIOD / 4);
            copy.getBall().setPosition(copy.getArena().width() / 2, 120);
            copy.getBall().setVelocity(0, -200);
            return copy;
        } catch (DOPOSoccerException e) {
            throw new AssertionError(e);
        }
    }

    /** Avanza el reloj sin que la fisica se mueva: los jugadores quedan quietos. */
    private static void advanceTo(DOPOSoccer match, double seconds) {
        while (match.getElapsedSeconds() < seconds) {
            match.step(DT);
        }
    }
}
