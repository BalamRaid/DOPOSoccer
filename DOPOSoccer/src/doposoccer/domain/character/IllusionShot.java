package doposoccer.domain.character;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;

/**
 * Tiro que divide el balon real en varios balones falsos, como la Ilusion de
 * Estados Unidos.
 *
 * <p>Los balones falsos no anotan: solo existen para confundir al rival y
 * desaparecen cuando termina el efecto de confusion. La trayectoria del balon
 * real y la cantidad de falsos son parametros del tiro.</p>
 */
public final class IllusionShot implements PowerShot {

    /** Velocidad horizontal del balon real. */
    public static final double REAL_SPEED = 800;

    /** Componente vertical inicial del balon real. */
    public static final double REAL_LIFT = -420;

    /** Velocidad horizontal del primer balon falso. */
    public static final double FAKE_BASE_SPEED = 450;

    /** Incremento de velocidad horizontal entre balones falsos. */
    public static final double FAKE_SPEED_STEP = 90;

    /** Componente vertical inicial del primer balon falso. */
    public static final double FAKE_BASE_LIFT = -900;

    /** Incremento de la componente vertical entre balones falsos. */
    public static final double FAKE_LIFT_STEP = 110;

    private final int fakeCount;

    /**
     * Crea un tiro de ilusion.
     *
     * @param fakeCount cantidad de balones falsos que acompana al real
     */
    public IllusionShot(int fakeCount) {
        if (fakeCount < 0) {
            throw new IllegalArgumentException("La cantidad de balones falsos no puede ser negativa.");
        }
        this.fakeCount = fakeCount;
    }

    @Override
    public void launch(Ball ball, Side shooter, PowerShotWorld world) {
        double direction = shooter.attackDirection();
        ball.setVelocity(direction * REAL_SPEED, REAL_LIFT);
        for (int i = 0; i < fakeCount; i++) {
            double vx = direction * (FAKE_BASE_SPEED + FAKE_SPEED_STEP * i);
            double vy = FAKE_BASE_LIFT + FAKE_LIFT_STEP * i;
            world.addFakeBall(new Ball(ball.arena(), ball.getX(), ball.getY(), vx, vy));
        }
    }
}
