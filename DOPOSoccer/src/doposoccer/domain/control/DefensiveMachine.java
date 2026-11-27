package doposoccer.domain.control;

import doposoccer.domain.Side;

/**
 * Maquina defensiva: su prioridad es defender el arco. Espera cerca de su
 * arco y solo sale a buscar el balon cuando entra en su mitad de cancha.
 */
public final class DefensiveMachine extends AiController {

    @Override
    protected PlayerIntent think(MatchView view, Side side) {
        return defend(view, side);
    }

    @Override
    public MachineProfile profile() {
        return MachineProfile.DEFENSIVE;
    }
}
