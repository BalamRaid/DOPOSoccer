package doposoccer.domain.character;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;

/**
 * Trayectoria que sigue el balon cuando se lanza un tiro de poder.
 *
 * <p>Es la parte variable del poder: dos personajes pueden compartir el mismo
 * efecto y cambiar solo la forma en que viaja el balon. Las implementaciones
 * son sin estado, de modo que una misma instancia se puede compartir entre
 * todos los partidos.</p>
 */
@FunctionalInterface
public interface PowerShot {

    /**
     * Aplica al balon la posicion y velocidad propias del tiro.
     *
     * @param ball balon real del partido
     * @param shooter lado del jugador que ejecuta el tiro
     * @param world partido que recibe los elementos adicionales del tiro
     */
    void launch(Ball ball, Side shooter, PowerShotWorld world);
}
