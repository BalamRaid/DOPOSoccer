package doposoccer.presentation;

import doposoccer.application.GameSession;
import doposoccer.application.PlayerProgress;
import doposoccer.application.SurvivalSeries;

import java.io.Serializable;

/**
 * Todo lo necesario para continuar un partido guardado: la sesion con el
 * estado completo del partido, la serie de supervivencia si la hay, la
 * resolucion y el escenario elegidos, y el progreso de los jugadores.
 *
 * <p>Se guarda como un solo objeto para que los perfiles del partido y los
 * del progreso sigan siendo los mismos objetos al abrir el archivo.</p>
 *
 * @param session sesion del partido en curso
 * @param series serie de supervivencia activa, o {@code null}
 * @param resolution resolucion de la cancha
 * @param stage escenario visual
 * @param progress progreso de los jugadores
 */
record SavedMatch(GameSession session, SurvivalSeries series, Resolution resolution,
        Stage stage, PlayerProgress progress) implements Serializable {

    private static final long serialVersionUID = 1L;
}
