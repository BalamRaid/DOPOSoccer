package doposoccer.domain.match;

/**
 * Reloj de un partido. Puede tener una duracion limitada o ser ilimitado.
 *
 * <p>El reloj solo avanza cuando el partido esta en juego; la pausa se maneja
 * en el agregado del partido. Mantener el tiempo separado evita que las
 * reglas de finalizacion dependan de un campo suelto.</p>
 */
public final class MatchClock {

    private final double durationSeconds;
    private double elapsedSeconds;

    private MatchClock(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * Crea un reloj sin limite de tiempo.
     *
     * @return reloj ilimitado
     */
    public static MatchClock unlimited() {
        return new MatchClock(Double.POSITIVE_INFINITY);
    }

    /**
     * Crea un reloj con la duracion indicada.
     *
     * @param durationSeconds duracion en segundos
     * @return reloj limitado
     */
    public static MatchClock ofSeconds(double durationSeconds) {
        if (durationSeconds <= 0 || Double.isNaN(durationSeconds)) {
            throw new IllegalArgumentException("La duracion debe ser positiva.");
        }
        return new MatchClock(durationSeconds);
    }

    /**
     * Avanza el reloj {@code dt} segundos sin superar la duracion maxima.
     *
     * @param dt tiempo transcurrido en segundos
     */
    public void advance(double dt) {
        if (dt < 0 || Double.isNaN(dt)) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        elapsedSeconds = Math.min(durationSeconds, elapsedSeconds + dt);
    }

    /**
     * Devuelve los segundos restantes. Para un reloj ilimitado devuelve
     * {@code -1}, el mismo convenio que usan los tipos de partido sin tiempo.
     *
     * @return segundos restantes, o {@code -1} si no hay limite
     */
    public double remainingSeconds() {
        if (!hasLimit()) {
            return -1;
        }
        return Math.max(0, durationSeconds - elapsedSeconds);
    }

    /**
     * Indica si el reloj llego a su duracion maxima.
     *
     * @return {@code true} si hay limite y ya se agoto
     */
    public boolean isExpired() {
        return hasLimit() && elapsedSeconds >= durationSeconds;
    }

    /**
     * Indica si el reloj tiene una duracion maxima.
     *
     * @return {@code true} si el tiempo esta limitado
     */
    public boolean hasLimit() {
        return !Double.isInfinite(durationSeconds);
    }

    public double durationSeconds() {
        return durationSeconds;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Vuelve a empezar el reloj desde cero.
     */
    public void reset() {
        elapsedSeconds = 0;
    }
}
