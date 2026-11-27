package doposoccer.domain;

import java.io.Serializable;

/**
 * Progreso de un jugador que se mantiene entre partidos: puntos acumulados y
 * estadisticas compradas.
 *
 * <p>El perfil no contiene el estado de un partido; solo conserva el nombre
 * visible, las estadisticas permanentes y los puntos disponibles para
 * mejorarlas. La vida y la barra de poder pertenecen a {@link Player}.</p>
 */
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;


    /** Puntos que recibe el ganador de un partido. */
    public static final int WIN_POINTS = 100;

    /** Puntos que recibe el perdedor de un partido. */
    public static final int LOSE_POINTS = 25;

    private final String name;
    private final Stats stats = new Stats();
    private int points;

    /**
     * Crea un perfil con el nombre indicado y estadisticas iniciales.
     *
     * @param name nombre visible del jugador
     */
    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public int getPoints() {
        return points;
    }

    /**
     * Suma puntos al perfil.
     *
     * @param amount cantidad de puntos a agregar
     */
    public void addPoints(int amount) {
        points += amount;
    }

    /**
     * Compra un punto de la estadistica dada con los puntos acumulados.
     *
     * @param stat estadistica que se quiere mejorar
     * @throws DOPOSoccerException si la estadistica ya esta en el maximo o si
     *                             no hay puntos suficientes para pagar la
     *                             mejora
     */
    public void upgrade(Stat stat) throws DOPOSoccerException {
        if (stats.get(stat) >= Stats.MAX) {
            throw new DOPOSoccerException(DOPOSoccerException.STAT_AT_MAX);
        }
        int cost = stats.upgradeCost(stat);
        if (points < cost) {
            throw new DOPOSoccerException(DOPOSoccerException.NOT_ENOUGH_POINTS
                    + "\nTe faltan " + (cost - points) + " puntos: la mejora cuesta "
                    + cost + " y tienes " + points + ".\nGana partidos para conseguir mas.");
        }
        stats.increase(stat);
        points -= cost;
    }
}
