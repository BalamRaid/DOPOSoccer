package doposoccer.test;

import doposoccer.presentation.KeyboardState;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del estado de teclado compartido.
 */
class KeyboardStateTest {

    @Test
    void shouldTrackHeldKey() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.press(KeyEvent.VK_A);
        assertTrue(keyboard.isPressed(KeyEvent.VK_A));
        keyboard.release(KeyEvent.VK_A);
        assertFalse(keyboard.isPressed(KeyEvent.VK_A));
    }

    @Test
    void shouldConsumeJustPressedOnlyOnce() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.press(KeyEvent.VK_W);
        assertTrue(keyboard.consumeJustPressed(KeyEvent.VK_W));
        assertFalse(keyboard.consumeJustPressed(KeyEvent.VK_W));
    }

    @Test
    void shouldNotRepeatedlyMarkHeldKey() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.press(KeyEvent.VK_S);
        assertTrue(keyboard.consumeJustPressed(KeyEvent.VK_S));
        keyboard.press(KeyEvent.VK_S);
        assertFalse(keyboard.consumeJustPressed(KeyEvent.VK_S));
    }

    @Test
    void shouldTrackRightControl() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.pressRightControl();
        assertTrue(keyboard.isRightControlPressed());
        assertTrue(keyboard.consumeRightControlJustPressed());
        assertFalse(keyboard.consumeRightControlJustPressed());
        keyboard.releaseRightControl();
        assertFalse(keyboard.isRightControlPressed());
    }

    @Test
    void shouldClearEverything() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.press(KeyEvent.VK_D);
        keyboard.pressRightControl();
        keyboard.clear();
        assertFalse(keyboard.isPressed(KeyEvent.VK_D));
        assertFalse(keyboard.isRightControlPressed());
        assertFalse(keyboard.consumeJustPressed(KeyEvent.VK_D));
        assertFalse(keyboard.consumeRightControlJustPressed());
    }

    @Test
    void shouldKeepIndependentKeys() {
        KeyboardState keyboard = new KeyboardState();
        keyboard.press(KeyEvent.VK_A);
        keyboard.press(KeyEvent.VK_D);
        assertTrue(keyboard.isPressed(KeyEvent.VK_A));
        assertTrue(keyboard.isPressed(KeyEvent.VK_D));
    }
}
