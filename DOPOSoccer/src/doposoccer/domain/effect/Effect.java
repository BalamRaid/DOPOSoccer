package doposoccer.domain.effect;

import doposoccer.domain.Player;

/**
 * Efecto temporal que un tiro de poder deja sobre el rival.
 *
 * <p>La clase base administra la duracion y ofrece ganchos para que cada
 * efecto decida si el rival puede moverse, saltar o patear, como cambia su
 * velocidad, si invierte sus controles y si cambia su tamano. Un efecto se
 * elimina del jugador cuando {@link #isFinished()} devuelve {@code true}.</p>
 */
public abstract class Effect {

    private final double duration;
    private double remaining;

    /**
     * Crea un efecto con la duracion indicada.
     *
     * @param duration duracion total en segundos
     */
    protected Effect(double duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("La duracion del efecto debe ser positiva.");
        }
        this.duration = duration;
        this.remaining = duration;
    }

    /**
     * Devuelve el nombre visible del efecto.
     *
     * @return nombre que se muestra en el HUD
     */
    public abstract String getName();

    /**
     * Se ejecuta una vez cuando el efecto cae sobre el jugador. Las subclases
     * pueden sobrescribirlo para aplicar un impulso o cambiar el estado
     * inicial del rival.
     *
     * @param target jugador que recibe el efecto
     */
    public void onApply(Player target) {
    }

    /**
     * Avanza el efecto en el tiempo. La implementacion base solo descuenta la
     * duracion; las subclases pueden consultar {@link #getRemaining()} para
     * modular su intensidad.
     *
     * @param target jugador afectado
     * @param dt tiempo transcurrido en segundos
     */
    public void update(Player target, double dt) {
        if (target == null) {
            throw new IllegalArgumentException("El jugador afectado no puede ser null.");
        }
        if (dt < 0 || Double.isNaN(dt)) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        remaining = Math.max(0, remaining - dt);
    }

    /**
     * Indica si el efecto ya cumplio su duracion.
     *
     * @return {@code true} cuando no queda tiempo de efecto
     */
    public boolean isFinished() {
        return remaining <= 0;
    }

    public double getDuration() {
        return duration;
    }

    public double getRemaining() {
        return remaining;
    }

    /**
     * Factor que se aplica a la velocidad de movimiento del jugador.
     *
     * @return multiplicador de velocidad; {@code 1} significa sin cambios
     */
    public double speedFactor() {
        return 1;
    }

    /**
     * Factor que se aplica al radio del cuerpo del jugador.
     *
     * @return multiplicador de tamano; {@code 1} significa tamano normal
     */
    public double sizeFactor() {
        return 1;
    }

    /**
     * Indica si el efecto invierte los controles de movimiento.
     *
     * @return {@code true} si izquierda y derecha se intercambian
     */
    public boolean invertControls() {
        return false;
    }

    /**
     * Indica si el efecto permite moverse al jugador.
     *
     * @return {@code true} si el movimiento no esta bloqueado
     */
    public boolean canMove() {
        return true;
    }

    /**
     * Indica si el efecto permite saltar al jugador.
     *
     * @return {@code true} si el salto no esta bloqueado
     */
    public boolean canJump() {
        return true;
    }

    /**
     * Indica si el efecto permite patear al jugador.
     *
     * @return {@code true} si la patada no esta bloqueada
     */
    public boolean canKick() {
        return true;
    }
}
