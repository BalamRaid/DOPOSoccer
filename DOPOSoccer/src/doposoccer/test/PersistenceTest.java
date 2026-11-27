package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.application.PlayerProgress;
import doposoccer.domain.DOPOSoccer;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Player;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.Stat;
import doposoccer.domain.control.AiController;
import doposoccer.infrastructure.GameStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de persistencia: guardar y abrir partidas y el progreso de los
 * jugadores.
 */
class PersistenceTest {

    private static final double DT = TestSupport.DT;
    private static final double EPS = TestSupport.EPS;

    @TempDir
    Path dir;

    private static GameSession session(GameMode mode, Profile left, Profile right)
            throws DOPOSoccerException {
        return GameSession.create(new MatchConfig(mode, GoalMatch.standard(5),
                left, TestSupport.character("cameroon"),
                right, TestSupport.character("south-korea")));
    }

    private static void run(GameSession session, double seconds) {
        int frames = (int) Math.round(seconds / DT);
        for (int i = 0; i < frames; i++) {
            session.update(DT);
        }
    }

    // ---------------- Escenario: guardar y abrir partida ----------------

    /** Al abrir una partida guardada se recupera el marcador, el tiempo y las posiciones. */
    @Test
    void shouldKeepMatchStateAfterSaveAndOpen() throws DOPOSoccerException {
        GameSession session = session(GameMode.PVP, new Profile("A"), new Profile("B"));
        run(session, 1.5);
        TestSupport.scoreInRightGoal(session.match());
        session.update(DT);
        run(session, 0.5);
        DOPOSoccer before = session.match();

        File file = dir.resolve("partida.dopo").toFile();
        GameStore.save(session, file);
        GameSession opened = GameStore.open(file, GameSession.class);
        DOPOSoccer after = opened.match();

        assertEquals(1, after.goalsOf(Side.LEFT));
        assertEquals(before.goalsOf(Side.RIGHT), after.goalsOf(Side.RIGHT));
        assertEquals(before.getElapsedSeconds(), after.getElapsedSeconds(), EPS);
        assertEquals(before.getPlayer(Side.LEFT).getX(), after.getPlayer(Side.LEFT).getX(), EPS);
        assertEquals(before.getPlayer(Side.RIGHT).getLife(), after.getPlayer(Side.RIGHT).getLife(), EPS);
        assertEquals(before.getBall().getX(), after.getBall().getX(), EPS);
    }

    /** La partida abierta sigue jugandose: el tiempo avanza y se pueden hacer goles. */
    @Test
    void shouldContinuePlayingAfterOpen() throws DOPOSoccerException {
        GameSession session = session(GameMode.PVP, new Profile("A"), new Profile("B"));
        run(session, 1);
        File file = dir.resolve("partida.dopo").toFile();
        GameStore.save(session, file);
        GameSession opened = GameStore.open(file, GameSession.class);

        double elapsed = opened.match().getElapsedSeconds();
        run(opened, 1);
        assertTrue(opened.match().getElapsedSeconds() > elapsed);
        TestSupport.scoreInLeftGoal(opened.match());
        opened.update(DT);
        assertEquals(1, opened.match().goalsOf(Side.RIGHT));
    }

    /** El personaje se reconstruye desde el catalogo y su poder sigue funcionando. */
    @Test
    void shouldRestoreCharacterAndPowerAfterOpen() throws DOPOSoccerException {
        GameSession session = session(GameMode.PVP, new Profile("A"), new Profile("B"));
        File file = dir.resolve("partida.dopo").toFile();
        GameStore.save(session, file);
        GameSession opened = GameStore.open(file, GameSession.class);

        Player left = opened.match().getPlayer(Side.LEFT);
        Player right = opened.match().getPlayer(Side.RIGHT);
        assertEquals("cameroon", left.getCharacter().id());
        assertNotNull(left.getCharacter().specialAbility().createEffect());

        left.addPowerBar(1);
        left.activatePower();
        right.setX(600);
        opened.match().getBall().setVelocity(0, 0);
        opened.match().getBall().setPosition(left.getBodyX() + 50, left.getBodyY());
        for (int i = 0; i < 120 && right.getEffect() == null; i++) {
            opened.update(DT);
        }
        assertNotNull(right.getEffect());
    }

    /** Los controladores no se guardan: al abrir, la maquina vuelve a tener su IA. */
    @Test
    void shouldReinstallMachineControllerAfterOpen() throws DOPOSoccerException {
        GameSession session = session(GameMode.PVM, new Profile("A"), new Profile("B"));
        File file = dir.resolve("partida.dopo").toFile();
        GameStore.save(session, file);
        GameSession opened = GameStore.open(file, GameSession.class);

        assertTrue(opened.match().getPlayer(Side.RIGHT).controller() instanceof AiController);
    }

    // ---------------- Escenario: progreso de los jugadores ----------------

    /** El progreso guardado conserva puntos y estadisticas de los cuatro perfiles. */
    @Test
    void shouldKeepProgressAfterSaveAndOpen() throws DOPOSoccerException {
        PlayerProgress progress = PlayerProgress.fresh();
        TestSupport.buy(progress.player1(), Stat.KICK, 2);
        progress.player1().addPoints(250);
        progress.machine2().addPoints(75);

        File file = dir.resolve("progreso.dat").toFile();
        GameStore.save(progress, file);
        PlayerProgress opened = GameStore.open(file, PlayerProgress.class);

        assertEquals(3, opened.player1().getStats().get(Stat.KICK));
        assertEquals(250, opened.player1().getPoints());
        assertEquals(75, opened.machine2().getPoints());
        assertEquals("Jugador 2", opened.player2().getName());
    }

    /** Partida y progreso guardados juntos siguen compartiendo los mismos perfiles. */
    @Test
    void shouldShareProfilesBetweenMatchAndProgress() throws DOPOSoccerException {
        PlayerProgress progress = PlayerProgress.fresh();
        GameSession session = session(GameMode.PVP, progress.player1(), progress.player2());
        Object[] saved = {session, progress};

        File file = dir.resolve("todo.dopo").toFile();
        GameStore.save(saved, file);
        Object[] opened = GameStore.open(file, Object[].class);
        GameSession openedSession = (GameSession) opened[0];
        PlayerProgress openedProgress = (PlayerProgress) opened[1];

        assertSame(openedProgress.player1(), openedSession.match().getPlayer(Side.LEFT).getProfile());
        openedSession.finish();
        assertTrue(openedProgress.player1().getPoints() > 0);
    }

    // ---------------- Escenario: errores ----------------

    /** Abrir un archivo que no existe da un error claro. */
    @Test
    void shouldRejectMissingFile() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> GameStore.open(dir.resolve("no-existe.dopo").toFile(), GameSession.class));
        assertTrue(e.getMessage().startsWith(DOPOSoccerException.OPEN_FAILED));
    }

    /** Un archivo que no es una partida se rechaza. */
    @Test
    void shouldRejectDamagedFile() throws IOException {
        Path file = dir.resolve("danado.dopo");
        Files.writeString(file, "esto no es una partida");
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> GameStore.open(file.toFile(), GameSession.class));
        assertEquals(DOPOSoccerException.INVALID_SAVE_FILE, e.getMessage());
    }

    /** Un archivo valido pero de otro tipo (progreso en vez de partida) se rechaza. */
    @Test
    void shouldRejectFileOfAnotherType() throws DOPOSoccerException {
        File file = dir.resolve("progreso.dat").toFile();
        GameStore.save(PlayerProgress.fresh(), file);
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> GameStore.open(file, GameSession.class));
        assertEquals(DOPOSoccerException.INVALID_SAVE_FILE, e.getMessage());
    }

    /** No se puede guardar sin datos. */
    @Test
    void shouldRejectSavingNothing() {
        DOPOSoccerException e = assertThrows(DOPOSoccerException.class,
                () -> GameStore.save(null, dir.resolve("x.dopo").toFile()));
        assertEquals(DOPOSoccerException.NULL_ELEMENT, e.getMessage());
    }
}
