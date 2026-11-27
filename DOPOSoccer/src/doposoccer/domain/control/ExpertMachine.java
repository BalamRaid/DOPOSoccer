package doposoccer.domain.control;

import doposoccer.domain.Side;

/**
 * Maquina experta: decide entre atacar y defender segun el estado del
 * partido.
 *
 * <ul>
 *   <li>Si el rival tiene el poder activo (aura), defiende.</li>
 *   <li>Si el rival esta aturdido, ataca.</li>
 *   <li>Si quedan pocos segundos y no va ganando, ataca con todo.</li>
 *   <li>Si va ganando, defiende la ventaja.</li>
 *   <li>En otro caso defiende cuando el balon esta en su mitad y ataca cuando
 *       esta en la mitad rival.</li>
 * </ul>
 */
public final class ExpertMachine extends AiController {

    /** Segundos finales en los que la maquina se vuelca al ataque si no gana. */
    public static final double FINAL_SECONDS = 15;

    @Override
    protected PlayerIntent think(MatchView view, Side side) {
        return shouldAttack(view, side) ? attack(view, side) : defend(view, side);
    }

    /**
     * Decide si en este momento conviene atacar.
     *
     * @param view vista del partido
     * @param side lado controlado
     * @return true para atacar, false para defender
     */
    static boolean shouldAttack(MatchView view, Side side) {
        Side rival = side.opposite();
        if (view.hasAura(rival)) {
            return false;
        }
        if (view.isStunned(rival)) {
            return true;
        }
        int difference = view.goalsOf(side) - view.goalsOf(rival);
        double remaining = view.remainingSeconds();
        if (remaining >= 0 && remaining <= FINAL_SECONDS && difference <= 0) {
            return true;
        }
        if (difference > 0) {
            return false;
        }
        return !ballInOwnHalf(view, side);
    }

    @Override
    public MachineProfile profile() {
        return MachineProfile.EXPERT;
    }
}
