package doposoccer.domain;

/**
 * Partido por tiempo: gana quien tenga mas goles al acabar el tiempo. Si hay
 * empate se juega gol de oro: el primero que anote gana.
 *
 * <p>La duracion se expresa en minutos completos. El reglamento oficial usa
 * un minuto (60 segundos), disponible con {@link #standard()}; el constructor
 * conserva el rango anterior para pruebas y configuraciones historicas.</p>
 */
public class TimeMatch extends MatchType {

    /** Duracion minima permitida, en minutos. */
    public static final int MIN_MINUTES = 1;

    /** Duracion maxima permitida, en minutos. */
    public static final int MAX_MINUTES = 10;

    /** Duracion oficial del reglamento, en minutos. */
    public static final int STANDARD_MINUTES = 1;

    private final int minutes;

    /**
     * Crea un partido por tiempo.
     *
     * @param minutes duracion del partido en minutos
     * @throws DOPOSoccerException si la duracion esta fuera del rango
     */
    public TimeMatch(int minutes) throws DOPOSoccerException {
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MINUTES);
        }
        this.minutes = minutes;
    }

    /**
     * Crea el partido oficial de 60 segundos.
     *
     * @return partido por tiempo de un minuto
     * @throws DOPOSoccerException si la configuracion no es valida
     */
    public static TimeMatch standard() throws DOPOSoccerException {
        return new TimeMatch(STANDARD_MINUTES);
    }

    public int getMinutes() {
        return minutes;
    }

    public double getDurationSeconds() {
        return minutes * 60.0;
    }

    @Override
    public double durationSeconds() {
        return getDurationSeconds();
    }

    /**
     * Evalua si el tiempo termino. Si hay empate activa la muerte subita y
     * mantiene el partido en curso.
     *
     * @param game partido que se esta evaluando
     * @return {@code true} si el tiempo termino y hay un ganador
     */
    @Override
    public boolean isOver(DOPOSoccer game) {
        if (!game.getClock().isExpired()) {
            return false;
        }
        if (game.goalsOf(Side.LEFT) == game.goalsOf(Side.RIGHT)) {
            game.startSuddenDeath();
            return false;
        }
        return true;
    }

    @Override
    public double remainingSeconds(DOPOSoccer game) {
        return game.getClock().remainingSeconds();
    }

    @Override
    public String describe() {
        return "Por tiempo (" + minutes + " min)";
    }
}
