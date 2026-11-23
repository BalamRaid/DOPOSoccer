package doposoccer.test;

import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.Field;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Profile;
import doposoccer.domain.Stat;
import doposoccer.domain.character.CharacterCatalog;
import doposoccer.domain.character.CharacterDefinition;

/**
 * Utilidades compartidas por las pruebas: catalogo, creacion de partidos,
 * avance de simulacion y compra de mejoras.
 */
final class TestSupport {

    static final double DT = 1.0 / 60;
    static final double EPS = 1e-6;
    static final CharacterCatalog CATALOG = CharacterCatalog.defaultCatalog();

    private TestSupport() {
    }

    static CharacterDefinition character(String id) {
        return CATALOG.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Personaje de prueba inexistente: " + id));
    }

    static DOPOSoccer newGame(Profile p1, String c1, Profile p2, String c2, MatchType type)
            throws DOPOSoccerException {
        return new DOPOSoccer(p1, character(c1), p2, character(c2), type);
    }

    static DOPOSoccer newGame(Profile p1, Profile p2) throws DOPOSoccerException {
        return newGame(p1, "cameroon", p2, "south-korea", GoalMatch.standard(3));
    }

    static void run(DOPOSoccer game, double seconds) {
        int frames = (int) Math.round(seconds / DT);
        for (int i = 0; i < frames; i++) {
            game.step(DT);
        }
    }

    static void buy(Profile profile, Stat stat, int times) throws DOPOSoccerException {
        for (int i = 0; i < times; i++) {
            profile.addPoints(profile.getStats().upgradeCost(stat));
            profile.upgrade(stat);
        }
    }

    static void scoreInLeftGoal(DOPOSoccer game) {
        game.getBall().setPosition(30, Field.GROUND - 40);
    }

    static void scoreInRightGoal(DOPOSoccer game) {
        game.getBall().setPosition(Field.WIDTH - 30, Field.GROUND - 40);
    }
}
