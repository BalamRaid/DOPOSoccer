package doposoccer.domain.control;

import doposoccer.domain.Side;

/**
 * Maquina ofensiva: su prioridad es anotar. Siempre persigue el balon para
 * empujarlo hacia el arco rival y usa el poder en cuanto puede.
 */
public final class OffensiveMachine extends AiController {

    @Override
    protected PlayerIntent think(MatchView view, Side side) {
        return attack(view, side);
    }

    @Override
    public MachineProfile profile() {
        return MachineProfile.OFFENSIVE;
    }
}
