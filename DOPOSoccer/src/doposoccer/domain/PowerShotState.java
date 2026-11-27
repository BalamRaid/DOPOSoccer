package doposoccer.domain;

import java.io.Serializable;

import doposoccer.domain.effect.ConfusionEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado temporal de un tiro de poder: quien lo lanza y que balones falsos
 * siguen vivos.
 *
 * <p>Se extrajo del agregado del partido para que la Ilusion y los efectos de
 * duracion tengan una responsabilidad clara. El estado no decide goles ni
 * dano; solo administra el ciclo de vida del tiro.</p>
 */
public final class PowerShotState implements Serializable {

    private static final long serialVersionUID = 1L;


    private final Ball ball;
    private final List<Ball> fakeBalls = new ArrayList<>();
    private Player shooter;
    private double fakeTimer;

    /**
     * Crea el estado de tiros de poder para el balon real de un partido.
     *
     * @param ball balon real
     */
    public PowerShotState(Ball ball) {
        if (ball == null) {
            throw new IllegalArgumentException("El balon no puede ser null.");
        }
        this.ball = ball;
    }

    /**
     * Marca el inicio de un tiro de poder y olvida el tiro anterior.
     *
     * @param shooter jugador que lanza el tiro
     */
    public void begin(Player shooter) {
        if (shooter == null) {
            throw new IllegalArgumentException("El tirador no puede ser null.");
        }
        end();
        this.shooter = shooter;
    }

    /**
     * Finaliza el tiro actual sin eliminar los balones falsos.
     */
    public void end() {
        shooter = null;
        ball.clearPowerShot();
    }

    /**
     * Agrega un balon falso y renueva su temporizador.
     *
     * @param fake balon falso
     */
    public void addFakeBall(Ball fake) {
        if (fake == null) {
            throw new IllegalArgumentException("El balon falso no puede ser null.");
        }
        fakeBalls.add(fake);
        fakeTimer = ConfusionEffect.DURATION;
    }

    /**
     * Renueva el temporizador de los balones falsos cuando el tiro impacta al
     * rival.
     */
    public void renewFakeTimer() {
        if (!fakeBalls.isEmpty()) {
            fakeTimer = ConfusionEffect.DURATION;
        }
    }

    /**
     * Actualiza los balones falsos y los elimina cuando termina el efecto de
     * confusion.
     *
     * @param dt tiempo transcurrido en segundos
     */
    public void update(double dt) {
        if (dt < 0 || Double.isNaN(dt)) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        if (fakeBalls.isEmpty()) {
            return;
        }
        for (Ball fake : fakeBalls) {
            fake.update(dt);
        }
        fakeTimer -= dt;
        if (fakeTimer <= 0) {
            fakeBalls.clear();
        }
    }

    /**
     * Termina el tiro y elimina todos los balones falsos.
     */
    public void clear() {
        end();
        fakeBalls.clear();
        fakeTimer = 0;
    }

    public Player shooter() {
        return shooter;
    }

    public boolean isActive() {
        return shooter != null;
    }

    /**
     * Devuelve una vista inmutable de los balones falsos.
     *
     * @return balones falsos actuales
     */
    public List<Ball> fakeBalls() {
        return Collections.unmodifiableList(fakeBalls);
    }
}
