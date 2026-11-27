package doposoccer.presentation;

import doposoccer.domain.character.CharacterDefinition;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Paleta visual de los personajes. Traduce la clave visual de una definicion
 * a los colores usados por la cancha, los retratos y el HUD.
 *
 * <p>No contiene un {@code switch} por pais: agregar un personaje nuevo solo
 * requiere registrar su definicion con una clave visual y, si se desea una
 * identidad propia, agregar una entrada a este mapa. Si la clave no existe, se
 * usan colores neutros.</p>
 */
final class Look {

    private record Palette(Color jersey, Color accent, Color aura,
            Color hair, Color skinTone) {
    }

    private static final Map<String, Palette> PALETTES = new HashMap<>();
    private static final Palette NEUTRAL = new Palette(
            new Color(118, 108, 136), new Color(205, 193, 225),
            new Color(225, 220, 245), new Color(30, 25, 38),
            new Color(232, 180, 141));

    static {
        PALETTES.put("south-korea", new Palette(
                new Color(214, 40, 57), new Color(80, 153, 255),
                new Color(70, 151, 255), new Color(24, 20, 28),
                new Color(240, 199, 164)));
        PALETTES.put("cameroon", new Palette(
                new Color(235, 187, 24), new Color(24, 128, 74),
                new Color(255, 229, 70), new Color(27, 21, 18),
                new Color(141, 85, 42)));
        PALETTES.put("nigeria", new Palette(
                new Color(24, 150, 80), new Color(244, 244, 244),
                new Color(120, 255, 160), new Color(22, 19, 20),
                new Color(122, 74, 43)));
        PALETTES.put("usa", new Palette(
                new Color(52, 71, 159), new Color(214, 58, 72),
                new Color(200, 90, 255), new Color(48, 31, 25),
                new Color(232, 180, 141)));
        PALETTES.put("canada", new Palette(
                new Color(220, 40, 50), new Color(250, 247, 255),
                new Color(130, 220, 255), new Color(66, 38, 24),
                new Color(242, 201, 168)));
        PALETTES.put("china", new Palette(
                new Color(220, 50, 60), new Color(255, 202, 67),
                new Color(255, 180, 60), new Color(20, 18, 24),
                new Color(232, 182, 143)));
    }

    private Look() {
    }

    private static Palette palette(CharacterDefinition character) {
        return character == null ? NEUTRAL
                : PALETTES.getOrDefault(character.visualKey(), NEUTRAL);
    }

    /** Color principal de la camiseta del personaje. */
    static Color jersey(CharacterDefinition character) {
        return palette(character).jersey();
    }

    /** Color de contraste para franjas, cintas y detalles. */
    static Color accent(CharacterDefinition character) {
        return palette(character).accent();
    }

    /** Color del aura del poder especial. */
    static Color aura(CharacterDefinition character) {
        return palette(character).aura();
    }

    static Color hair(CharacterDefinition character) {
        return palette(character).hair();
    }

    static Color skinTone(CharacterDefinition character) {
        return palette(character).skinTone();
    }
}
