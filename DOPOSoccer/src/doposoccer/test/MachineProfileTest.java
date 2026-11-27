package doposoccer.test;

import doposoccer.application.GameMode;
import doposoccer.application.GameSession;
import doposoccer.application.MatchConfig;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.Profile;
import doposoccer.domain.Side;
import doposoccer.domain.control.AiController;
import doposoccer.domain.control.ClumsyMachine;
import doposoccer.domain.control.DefensiveMachine;
import doposoccer.domain.control.ExpertMachine;
import doposoccer.domain.control.MachineProfile;
import doposoccer.domain.control.OffensiveMachine;
import doposoccer.domain.control.PlayerIntent;
import doposoccer.domain.math.Vec2;
import doposoccer.infrastructure.GameStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los cuatro perfiles de maquina: Torpe, Ofensiva, Defensiva y
 * Experta.
 */
class MachineProfileTest {

    private StubMatchView view;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        view = new StubMatchView();
    }

    /** Maquina izquierda en el centro y balon en la mitad rival. */
    private void ballInRivalHalf() {
        view.leftPosition = new Vec2(500, 450);
        view.ballPosition = new Vec2(800, 450);
    }

    // ---------------- Escenario: catalogo de perfiles ----------------

    /** Cada perfil crea la maquina que le corresponde. */
    @Test
    void shouldCreateMachineOfEachProfile() {
        assertTrue(MachineProfile.CLUMSY.create() instanceof ClumsyMachine);
        assertTrue(MachineProfile.OFFENSIVE.create() instanceof OffensiveMachine);
        assertTrue(MachineProfile.DEFENSIVE.create() instanceof DefensiveMachine);
        assertTrue(MachineProfile.EXPERT.create() instanceof ExpertMachine);
        for (MachineProfile profile : MachineProfile.values()) {
            assertSame(profile, profile.create().profile());
        }
    }

    /** Los perfiles se muestran con su nombre en espanol. */
    @Test
    void shouldShowSpanishNames() {
        assertEquals("Torpe", MachineProfile.CLUMSY.toString());
        assertEquals("Ofensiva", MachineProfile.OFFENSIVE.toString());
        assertEquals("Defensiva", MachineProfile.DEFENSIVE.toString());
        assertEquals("Experta", MachineProfile.EXPERT.toString());
    }

    /** Ningun perfil juega si el partido esta en pausa o si esta aturdido. */
    @Test
    void shouldStayIdleWhenPausedOrStunned() {
        for (MachineProfile profile : MachineProfile.values()) {
            AiController machine = profile.create();
            view.paused = true;
            assertEquals(PlayerIntent.IDLE, machine.decide(view, Side.LEFT));
            view.paused = false;
            view.leftStunned = true;
            assertEquals(PlayerIntent.IDLE, machine.decide(view, Side.LEFT));
            view.leftStunned = false;
        }
    }

    // ---------------- Escenario: ofensiva vs defensiva ----------------

    /** Con el balon en la mitad rival, la ofensiva va al balon y la defensiva vuelve a su arco. */
    @Test
    void shouldAttackOrDefendWithBallInRivalHalf() {
        ballInRivalHalf();
        assertEquals(1, new OffensiveMachine().decide(view, Side.LEFT).moveDirection());
        assertEquals(-1, new DefensiveMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** La defensiva sale a despejar cuando el balon entra en su mitad. */
    @Test
    void shouldClearBallInOwnHalfWhenDefensive() {
        view.leftPosition = new Vec2(100, 450);
        view.ballPosition = new Vec2(300, 450);
        assertEquals(1, new DefensiveMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** La defensiva no gasta el poder lejos de su arco; la ofensiva si lo usa. */
    @Test
    void shouldUsePowerOnlyWhenOffensive() {
        view.leftPosition = new Vec2(760, 450);
        view.ballPosition = new Vec2(800, 450);
        view.leftPower = 1;
        assertTrue(new OffensiveMachine().decide(view, Side.LEFT).special());
        assertFalse(new DefensiveMachine().decide(view, Side.LEFT).special());
    }

    // ---------------- Escenario: experta ----------------

    /** Empatando y con el balon en la mitad rival, la experta ataca. */
    @Test
    void shouldAttackWhenTiedAndBallInRivalHalf() {
        ballInRivalHalf();
        assertEquals(1, new ExpertMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** Si va ganando, la experta defiende la ventaja. */
    @Test
    void shouldDefendLeadWhenExpert() {
        ballInRivalHalf();
        view.leftGoals = 1;
        assertEquals(-1, new ExpertMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** Si el rival tiene el poder activo, la experta se repliega. */
    @Test
    void shouldDefendAgainstRivalAura() {
        ballInRivalHalf();
        view.rightAura = true;
        assertEquals(-1, new ExpertMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** Aunque vaya ganando, si el rival esta aturdido la experta ataca. */
    @Test
    void shouldAttackStunnedRival() {
        ballInRivalHalf();
        view.leftGoals = 1;
        view.rightStunned = true;
        assertEquals(1, new ExpertMachine().decide(view, Side.LEFT).moveDirection());
    }

    /** En los segundos finales, si no va ganando, la experta ataca aunque el balon este en su mitad. */
    @Test
    void shouldGoAllInAtTheEnd() {
        view.leftPosition = new Vec2(100, 450);
        view.ballPosition = new Vec2(300, 450);
        view.rightGoals = 1;
        view.remainingSeconds = ExpertMachine.FINAL_SECONDS - 1;
        assertEquals(1, new ExpertMachine().decide(view, Side.LEFT).moveDirection());
    }

    // ---------------- Escenario: torpe ----------------

    /** La torpe mantiene cada decision al azar varios pasos y luego cambia. */
    @Test
    void shouldKeepRandomDecisionForSomeSteps() {
        ClumsyMachine machine = new ClumsyMachine(new Random(7));
        PlayerIntent first = machine.decide(view, Side.LEFT);
        for (int i = 1; i < ClumsyMachine.STEPS_PER_DECISION; i++) {
            assertEquals(first, machine.decide(view, Side.LEFT));
        }
    }

    /** Con el tiempo la torpe se mueve en distintas direcciones al azar. */
    @Test
    void shouldMoveRandomlyWhenClumsy() {
        ClumsyMachine machine = new ClumsyMachine(new Random(42));
        Set<Integer> directions = new HashSet<>();
        for (int i = 0; i < ClumsyMachine.STEPS_PER_DECISION * 30; i++) {
            directions.add(machine.decide(view, Side.LEFT).moveDirection());
        }
        assertEquals(3, directions.size());
    }

    // ---------------- Escenario: partido ----------------

    /** El perfil elegido en la configuracion es el que juega en el partido. */
    @Test
    void shouldInstallChosenProfileInSession() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.MVM,
                MachineProfile.CLUMSY, MachineProfile.EXPERT));
        assertTrue(session.match().getPlayer(Side.LEFT).controller() instanceof ClumsyMachine);
        assertTrue(session.match().getPlayer(Side.RIGHT).controller() instanceof ExpertMachine);
    }

    /** Al abrir una partida guardada, la maquina conserva su perfil. */
    @Test
    void shouldKeepProfileAfterSaveAndOpen() throws DOPOSoccerException {
        GameSession session = GameSession.create(config(GameMode.PVM,
                MachineProfile.OFFENSIVE, MachineProfile.DEFENSIVE));
        File file = dir.resolve("partida.dopo").toFile();
        GameStore.save(session, file);
        GameSession opened = GameStore.open(file, GameSession.class);
        assertTrue(opened.match().getPlayer(Side.RIGHT).controller() instanceof DefensiveMachine);
    }

    private static MatchConfig config(GameMode mode, MachineProfile left, MachineProfile right)
            throws DOPOSoccerException {
        return new MatchConfig(mode, GoalMatch.standard(3),
                new Profile("Izquierda"), TestSupport.character("cameroon"),
                new Profile("Derecha"), TestSupport.character("south-korea"), left, right);
    }
}
