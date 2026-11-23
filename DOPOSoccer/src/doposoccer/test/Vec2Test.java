package doposoccer.test;

import doposoccer.domain.math.Vec2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pruebas del valor inmutable {@link Vec2}.
 */
class Vec2Test {

    private static final double EPS = 1e-9;

    @Test
    void shouldCreateWithFactory() {
        Vec2 vector = Vec2.of(3, 4);
        assertEquals(3, vector.x(), EPS);
        assertEquals(4, vector.y(), EPS);
    }

    @Test
    void shouldAddVectors() {
        Vec2 result = new Vec2(1, 2).plus(new Vec2(3, 4));
        assertEquals(4, result.x(), EPS);
        assertEquals(6, result.y(), EPS);
    }

    @Test
    void shouldSubtractVectors() {
        Vec2 result = new Vec2(5, 7).minus(new Vec2(2, 3));
        assertEquals(3, result.x(), EPS);
        assertEquals(4, result.y(), EPS);
    }

    @Test
    void shouldScaleVector() {
        Vec2 result = new Vec2(2, -3).times(2.5);
        assertEquals(5, result.x(), EPS);
        assertEquals(-7.5, result.y(), EPS);
    }

    @Test
    void shouldComputeLength() {
        assertEquals(5, new Vec2(3, 4).length(), EPS);
    }

    @Test
    void shouldComputeLengthSquared() {
        assertEquals(25, new Vec2(3, 4).lengthSquared(), EPS);
    }

    @Test
    void shouldNormalizeToUnitLength() {
        Vec2 normalized = new Vec2(3, 4).normalized();
        assertEquals(1, normalized.length(), EPS);
        assertEquals(0.6, normalized.x(), EPS);
        assertEquals(0.8, normalized.y(), EPS);
    }

    @Test
    void shouldReturnZeroWhenNormalizingZero() {
        assertSame(Vec2.ZERO, Vec2.ZERO.normalized());
    }

    @Test
    void shouldComputeDistance() {
        assertEquals(5, new Vec2(1, 1).distanceTo(new Vec2(4, 5)), EPS);
    }

    @Test
    void shouldNotMutateOriginalVector() {
        Vec2 original = new Vec2(1, 2);
        original.plus(new Vec2(10, 20));
        assertEquals(1, original.x(), EPS);
        assertEquals(2, original.y(), EPS);
    }
}
