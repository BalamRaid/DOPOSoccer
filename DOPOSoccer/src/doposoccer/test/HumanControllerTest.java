package doposoccer.test;

import doposoccer.domain.Side;
import doposoccer.domain.control.PlayerIntent;
import doposoccer.presentation.HumanController;
import doposoccer.presentation.KeyBindings;
import doposoccer.presentation.KeyboardState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del adaptador de teclado a intenciones.
 */
class HumanControllerTest {

    private KeyboardState keyboard;
    private StubMatchView view;

    @BeforeEach
    void setUp() {
        keyboard = new KeyboardState();
        view = new StubMatchView();
    }

    @Test
    void shouldMoveRightWithPlayerOneKey() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_D);
        assertEquals(1, controller.decide(view, Side.LEFT).moveDirection());
    }

    @Test
    void shouldMoveLeftWithPlayerOneKey() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_A);
        assertEquals(-1, controller.decide(view, Side.LEFT).moveDirection());
    }

    @Test
    void shouldStayStillWhenBothDirectionsAreHeld() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_A);
        keyboard.press(KeyEvent.VK_D);
        assertEquals(0, controller.decide(view, Side.LEFT).moveDirection());
    }

    @Test
    void shouldConsumeJumpOnce() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_W);
        assertTrue(controller.decide(view, Side.LEFT).jump());
        assertFalse(controller.decide(view, Side.LEFT).jump());
    }

    @Test
    void shouldKickWithS() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_S);
        assertTrue(controller.decide(view, Side.LEFT).kick());
    }

    @Test
    void shouldUseQForPower() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_Q);
        assertTrue(controller.decide(view, Side.LEFT).special());
    }

    @Test
    void shouldUseRightControlForPlayerTwoPower() {
        HumanController controller = new HumanController(keyboard, KeyBindings.playerTwo());
        keyboard.pressRightControl();
        assertTrue(controller.decide(view, Side.RIGHT).special());
    }

    @Test
    void shouldUseArrowKeysForPlayerTwo() {
        HumanController controller = new HumanController(keyboard, KeyBindings.playerTwo());
        keyboard.press(KeyEvent.VK_RIGHT);
        keyboard.press(KeyEvent.VK_UP);
        PlayerIntent intent = controller.decide(view, Side.RIGHT);
        assertEquals(1, intent.moveDirection());
        assertTrue(intent.jump());
    }

    @Test
    void shouldReturnIdleWhenPaused() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_D);
        view.paused = true;
        PlayerIntent intent = controller.decide(view, Side.LEFT);
        assertEquals(PlayerIntent.IDLE, intent);
    }

    @Test
    void shouldReturnIdleWhenFinished() {
        HumanController controller = playerOne();
        keyboard.press(KeyEvent.VK_D);
        view.finished = true;
        assertEquals(PlayerIntent.IDLE, controller.decide(view, Side.LEFT));
    }

    private HumanController playerOne() {
        return new HumanController(keyboard, KeyBindings.playerOne());
    }
}
