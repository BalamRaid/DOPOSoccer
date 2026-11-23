package doposoccer.domain.character;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;

/**
 * Tiro rasante que activa el modo tornado del balon: cada rebote en el suelo
 * vuelve a impulsarlo hacia arriba con fuerza, como el poder de Nigeria.
 */
public final class TornadoShot implements PowerShot {

    private final double speed;

    /**
     * Crea un tiro tornado.
     *
     * @param speed rapidez horizontal en unidades logicas por segundo
     */
    public TornadoShot(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("La velocidad del tiro debe ser positiva.");
        }
        this.speed = speed;
    }

    @Override
    public void launch(Ball ball, Side shooter, PowerShotWorld world) {
        ball.setPosition(ball.getX(), ball.arena().groundY() - Ball.RADIUS);
        ball.setVelocity(shooter.attackDirection() * speed, -Ball.TORNADO_BOUNCE);
        ball.setTornado(true);
    }
}
