package doposoccer.domain;

import java.io.Serializable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Estadisticas de un jugador: speed, kick, jump y survival. Todas inician en 1
 * y suben hasta 10. Cada punto por encima de 1 suma un 10 % sobre la base.
 *
 * <p>Los factores se aplican en {@link Player} para calcular velocidad,
 * salto, dano y vida maxima. El costo de mejora se duplica con cada punto
 * comprado.</p>
 */
public class Stats implements Serializable {

    private static final long serialVersionUID = 1L;


    /** Valor minimo de cualquier estadistica. */
    public static final int MIN = 1;

    /** Valor maximo de cualquier estadistica. */
    public static final int MAX = 10;

    /** Costo de la primera mejora; las siguientes duplican este valor. */
    public static final int FIRST_UPGRADE_COST = 50;

    /** Vida base antes de aplicar el factor de supervivencia. */
    public static final double BASE_LIFE = 100;

    private final Map<Stat, Integer> values = new EnumMap<>(Stat.class);

    /**
     * Crea un conjunto de estadisticas con todos los valores en el minimo.
     */
    public Stats() {
        for (Stat s : Stat.values()) {
            values.put(s, MIN);
        }
    }

    public int get(Stat stat) {
        return values.get(stat);
    }

    /**
     * Calcula el factor de crecimiento de una estadistica:
     * {@code 1 + 0,10 * (valor - 1)}.
     *
     * @param stat estadistica consultada
     * @return multiplicador que se aplica sobre el valor base
     */
    public double factor(Stat stat) {
        return 1 + 0.10 * (get(stat) - 1);
    }

    /**
     * Devuelve el multiplicador de dano de patada y de tiro de poder.
     *
     * @return factor correspondiente a la estadistica {@link Stat#KICK}
     */
    public double kickMultiplier() {
        return factor(Stat.KICK);
    }

    /**
     * Calcula la vida maxima a partir de la vida base y la supervivencia.
     *
     * @return vida maxima redondeada a dos decimales
     */
    public double maxLife() {
        return Math.round(BASE_LIFE * factor(Stat.SURVIVAL) * 100) / 100.0;
    }

    /**
     * Calcula el precio del siguiente punto de una estadistica. El costo se
     * duplica por cada punto ya comprado: 50, 100, 200, etc.
     *
     * @param stat estadistica que se quiere mejorar
     * @return costo en puntos de la siguiente mejora
     */
    public int upgradeCost(Stat stat) {
        return FIRST_UPGRADE_COST * (1 << (get(stat) - 1));
    }

    /**
     * Incrementa en uno el valor de una estadistica.
     *
     * @param stat estadistica que se incrementa
     * @throws DOPOSoccerException si la estadistica ya esta en el maximo
     */
    void increase(Stat stat) throws DOPOSoccerException {
        if (get(stat) >= MAX) {
            throw new DOPOSoccerException(DOPOSoccerException.STAT_AT_MAX);
        }
        values.put(stat, get(stat) + 1);
    }
}
