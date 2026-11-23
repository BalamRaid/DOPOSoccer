package doposoccer.domain.control;

import doposoccer.domain.Side;

/**
 * Contrato comun para controladores humanos e inteligencia artificial.
 *
 * <p>Ambos producen exactamente el mismo tipo de intencion y solo leen una
 * {@link MatchView} de solo lectura. Esto permite intercambiar un jugador
 * humano por una maquina sin cambiar el partido ni la presentacion.</p>
 */
@FunctionalInterface
public interface PlayerController {

    /**
     * Decide las acciones del jugador para el siguiente paso del partido.
     *
     * @param view vista de solo lectura del partido
     * @param side lado que controla este controlador
     * @return intencion que se aplicara al jugador
     */
    PlayerIntent decide(MatchView view, Side side);
}
