package doposoccer.test;

import doposoccer.domain.Player;
import doposoccer.domain.Side;
import doposoccer.domain.control.AiController;
import doposoccer.domain.control.OffensiveMachine;
import doposoccer.domain.control.PlayerIntent;
import doposoccer.domain.math.Vec2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comportamiento ofensivo de la maquina (el que antes era la
 * unica IA) y de las reglas comunes de {@link AiController}.
 */
class AiControllerTest {

    private static final AiController OFFENSIVE = new OffensiveMachine();

    private StubMatchView view;

    @BeforeEach
    void setUp() {
        view = new StubMatchView();
    }

    @Test
    void shouldMoveTowardBallOnItsSide() {
        view.ballPosition = new Vec2(500, 450);
        PlayerIntent intent = OFFENSIVE.decide(view, Side.LEFT);
        assertEquals(1, intent.moveDirection());
    }

    @Test
    void shouldMoveTowardBallFromRightSide() {
        view.ballPosition = new Vec2(500, 450);
        PlayerIntent intent = OFFENSIVE.decide(view, Side.RIGHT);
        assertEquals(-1, intent.moveDirection());
    }

    @Test
    void shouldStayStillWhenAlignedWithBall() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 61, 450);
        PlayerIntent intent = OFFENSIVE.decide(view, Side.LEFT);
        assertEquals(0, intent.moveDirection());
    }

    @Test
    void shouldKickWhenBallIsClose() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 450);
        assertTrue(OFFENSIVE.decide(view, Side.LEFT).kick());
    }

    @Test
    void shouldNotKickWhenBallIsFar() {
        view.ballPosition = new Vec2(700, 300);
        assertFalse(OFFENSIVE.decide(view, Side.LEFT).kick());
    }

    @Test
    void shouldJumpForHighBall() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 200);
        assertTrue(OFFENSIVE.decide(view, Side.LEFT).jump());
    }

    @Test
    void shouldNotJumpWhenAlreadyAirborne() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 200);
        view.leftOnGround = false;
        assertFalse(OFFENSIVE.decide(view, Side.LEFT).jump());
    }

    @Test
    void shouldActivateSpecialWhenReadyAndClose() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 450);
        view.leftPower = 1;
        assertTrue(OFFENSIVE.decide(view, Side.LEFT).special());
    }

    @Test
    void shouldNotActivateSpecialWithoutPower() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 450);
        view.leftPower = 0.5;
        assertFalse(OFFENSIVE.decide(view, Side.LEFT).special());
    }

    @Test
    void shouldNotActivateSpecialWhileAuraActive() {
        view.ballPosition = new Vec2(view.leftPosition.x() + 50, 450);
        view.leftPower = 1;
        view.leftAura = true;
        assertFalse(OFFENSIVE.decide(view, Side.LEFT).special());
    }

    @Test
    void shouldReturnIdleWhenPaused() {
        view.paused = true;
        assertEquals(PlayerIntent.IDLE, OFFENSIVE.decide(view, Side.LEFT));
    }

    @Test
    void shouldReturnIdleWhenFinished() {
        view.finished = true;
        assertEquals(PlayerIntent.IDLE, OFFENSIVE.decide(view, Side.LEFT));
    }

    @Test
    void shouldReturnIdleWhenStunned() {
        view.leftStunned = true;
        assertEquals(PlayerIntent.IDLE, OFFENSIVE.decide(view, Side.LEFT));
    }

    @Test
    void shouldMirrorBehaviorForBothSides() {
        view.ballPosition = new Vec2(500, 450);
        PlayerIntent left = OFFENSIVE.decide(view, Side.LEFT);
        PlayerIntent right = OFFENSIVE.decide(view, Side.RIGHT);
        assertEquals(left.moveDirection(), -right.moveDirection());
    }

    @Test
    void shouldRejectNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> OFFENSIVE.decide(null, Side.LEFT));
        assertThrows(IllegalArgumentException.class,
                () -> OFFENSIVE.decide(view, null));
    }

    @Test
    void shouldUseBodyRadiusForJumpDecision() {
        view.ballPosition = new Vec2(250, 415);
        view.leftRadius = Player.BODY_RADIUS;
        boolean normal = OFFENSIVE.decide(view, Side.LEFT).jump();
        view.leftRadius = 20;
        boolean shrunk = OFFENSIVE.decide(view, Side.LEFT).jump();
        assertFalse(normal);
        assertTrue(shrunk);
    }
}
