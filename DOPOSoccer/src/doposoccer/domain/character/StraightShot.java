package doposoccer.domain.character;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;

/**
 * Tiro recto que viaja sin gravedad, como el Aura Azul de Corea del Sur o el
 * Rayo de Camerun.
 */
public final class StraightShot implements PowerShot {

    private final double speed;
    private final double verticalSpeed;

    /**
     * Crea un tiro horizontal.
     *
     * @param speed rapidez horizontal en unidades logicas por segundo
     */
    public StraightShot(double speed) {
        this(speed, 0);
    }

    /**
     * Crea un tiro recto con una componente vertical fija.
     *
     * @param speed rapidez horizontal en unidades logicas por segundo
     * @param verticalSpeed componente vertical inicial
     */
    public StraightShot(double speed, double verticalSpeed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("La velocidad del tiro debe ser positiva.");
        }
        this.speed = speed;
        this.verticalSpeed = verticalSpeed;
    }

    @Override
    public void launch(Ball ball, Side shooter, PowerShotWorld world) {
        ball.setVelocity(shooter.attackDirection() * speed, verticalSpeed);
        ball.setGravity(false);
    }
}
