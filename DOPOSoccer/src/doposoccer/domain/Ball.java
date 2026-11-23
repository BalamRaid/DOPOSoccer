package doposoccer.domain;

import doposoccer.domain.match.Arena;
import doposoccer.domain.match.Goal;
import doposoccer.domain.math.Vec2;

/**
 * Balon del partido. Esta afectado por la gravedad y rebota en el suelo, en
 * los travesanos y postes de los arcos y en los bordes de la cancha.
 *
 * <p>El balon se mueve en las coordenadas logicas de la {@link Arena}; la
 * presentacion es la que escala esas coordenadas a la resolucion elegida. Un
 * tiro de poder puede desactivar temporalmente la gravedad o activar el modo
 * tornado, por eso el estado de la fisica se puede consultar y modificar.</p>
 */
public final class Ball {

    /** Radio del balon en unidades logicas de la cancha. */
    public static final double RADIUS = 15;

    /** Factor de restitucion aplicado a los rebotes normales. */
    public static final double BOUNCE = 0.75;

    /** Impulso vertical que se aplica al botar durante un tiro tornado. */
    public static final double TORNADO_BOUNCE = 380;

    /** Factor de friccion que reduce la velocidad horizontal al tocar el suelo. */
    public static final double GROUND_FRICTION = 0.995;

    /** Radio de la esquina del poste frontal usada para resolver el choque. */
    public static final double POST_RADIUS = 4;

    private final Arena arena;
    private Vec2 position;
    private Vec2 velocity;
    private boolean gravity = true;
    private boolean tornado;

    /**
     * Crea un balon detenido en la posicion indicada sobre la cancha oficial.
     *
     * @param x coordenada horizontal inicial
     * @param y coordenada vertical inicial
     */
    public Ball(double x, double y) {
        this(Arena.defaultArena(), x, y, 0, 0);
    }

    /**
     * Crea un balon con posicion y velocidad iniciales sobre la cancha oficial.
     *
     * @param x coordenada horizontal inicial
     * @param y coordenada vertical inicial
     * @param vx velocidad horizontal inicial
     * @param vy velocidad vertical inicial
     */
    public Ball(double x, double y, double vx, double vy) {
        this(Arena.defaultArena(), x, y, vx, vy);
    }

    /**
     * Crea un balon detenido en una cancha concreta.
     *
     * @param arena cancha a la que pertenece el balon
     * @param x coordenada horizontal inicial
     * @param y coordenada vertical inicial
     */
    public Ball(Arena arena, double x, double y) {
        this(arena, x, y, 0, 0);
    }

    /**
     * Crea un balon con posicion, velocidad y cancha.
     *
     * @param arena cancha a la que pertenece el balon
     * @param x coordenada horizontal inicial
     * @param y coordenada vertical inicial
     * @param vx velocidad horizontal inicial
     * @param vy velocidad vertical inicial
     */
    public Ball(Arena arena, double x, double y, double vx, double vy) {
        if (arena == null) {
            throw new IllegalArgumentException("La cancha no puede ser null.");
        }
        this.arena = arena;
        this.position = new Vec2(x, y);
        this.velocity = new Vec2(vx, vy);
    }

    /**
     * Mueve el balon {@code dt} segundos aplicando gravedad, rebotes y
     * friccion.
     *
     * @param dt tiempo transcurrido en segundos
     * @return {@code true} si el balon choco contra un borde lateral
     */
    public boolean update(double dt) {
        if (dt < 0 || Double.isNaN(dt)) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        if (gravity) {
            velocity = velocity.plus(new Vec2(0, arena.gravity() * dt));
        }
        position = position.plus(velocity.times(dt));

        boolean hitWall = false;
        if (position.y() + RADIUS > arena.groundY()) {
            position = new Vec2(position.x(), arena.groundY() - RADIUS);
            double bounce = tornado ? -TORNADO_BOUNCE : -Math.abs(velocity.y()) * BOUNCE;
            velocity = new Vec2(velocity.x() * GROUND_FRICTION, bounce);
            if (Math.abs(velocity.y()) < 60) {
                velocity = new Vec2(velocity.x(), 0);
            }
        }
        if (position.y() - RADIUS < 0) {
            position = new Vec2(position.x(), RADIUS);
            velocity = new Vec2(velocity.x(), Math.abs(velocity.y()) * BOUNCE);
        }
        if (position.x() - RADIUS < 0) {
            position = new Vec2(RADIUS, position.y());
            velocity = new Vec2(Math.abs(velocity.x()) * BOUNCE, velocity.y());
            hitWall = true;
        } else if (position.x() + RADIUS > arena.width()) {
            position = new Vec2(arena.width() - RADIUS, position.y());
            velocity = new Vec2(-Math.abs(velocity.x()) * BOUNCE, velocity.y());
            hitWall = true;
        }
        bounceOnGoalFrames();
        return hitWall;
    }

    /**
     * Resuelve el choque contra el travesano y la esquina superior del poste
     * frontal de cada arco. La boca del arco queda abierta para que el balon
     * pueda entrar.
     */
    private void bounceOnGoalFrames() {
        for (Goal goal : arena.goals()) {
            bounceOnCrossbar(goal);
            bounceOnPost(goal);
        }
    }

    /**
     * Corrige la posicion y la velocidad vertical cuando el balon toca el
     * travesano de un arco.
     *
     * @param goal arco que se comprueba
     */
    private void bounceOnCrossbar(Goal goal) {
        boolean underBar = position.x() >= goal.x() - RADIUS
                && position.x() <= goal.x() + goal.width() + RADIUS;
        if (!underBar || Math.abs(position.y() - goal.y()) >= RADIUS) {
            return;
        }
        if (position.y() < goal.y()) {
            position = new Vec2(position.x(), goal.y() - RADIUS);
            velocity = new Vec2(velocity.x(), -Math.abs(velocity.y()) * BOUNCE);
        } else {
            position = new Vec2(position.x(), goal.y() + RADIUS);
            velocity = new Vec2(velocity.x(), Math.abs(velocity.y()) * BOUNCE);
        }
    }

    /**
     * Resuelve el choque contra la esquina superior del poste frontal. Se usa
     * un circulo pequeno para que un balon que pasa por la boca del arco no
     * quede bloqueado por una linea infinita.
     *
     * @param goal arco que se comprueba
     */
    private void bounceOnPost(Goal goal) {
        Vec2 corner = goal.frontPostTop();
        Vec2 normal = position.minus(corner);
        double distance = normal.length();
        double minDistance = RADIUS + POST_RADIUS;
        if (distance >= minDistance) {
            return;
        }
        if (distance == 0) {
            normal = new Vec2(0, -1);
            distance = 1;
        }
        normal = normal.times(1 / distance);
        position = corner.plus(normal.times(minDistance));
        double dot = velocity.x() * normal.x() + velocity.y() * normal.y();
        if (dot < 0) {
            velocity = velocity.minus(normal.times((1 + BOUNCE) * dot));
        }
    }

    /**
     * Devuelve el balon a la fisica normal al terminar un tiro de poder:
     * reactiva la gravedad y desactiva el modo tornado.
     */
    public void clearPowerShot() {
        gravity = true;
        tornado = false;
    }

    /**
     * Calcula la distancia entre el centro del balon y un punto.
     *
     * @param px coordenada horizontal del punto
     * @param py coordenada vertical del punto
     * @return distancia euclidiana
     */
    public double distanceTo(double px, double py) {
        return position.distanceTo(new Vec2(px, py));
    }

    public Arena arena() {
        return arena;
    }

    public Vec2 position() {
        return position;
    }

    public Vec2 velocity() {
        return velocity;
    }

    public double getX() {
        return position.x();
    }

    public double getY() {
        return position.y();
    }

    public double getVx() {
        return velocity.x();
    }

    public double getVy() {
        return velocity.y();
    }

    public void setPosition(double x, double y) {
        position = new Vec2(x, y);
    }

    public void setPosition(Vec2 position) {
        if (position == null) {
            throw new IllegalArgumentException("La posicion no puede ser null.");
        }
        this.position = position;
    }

    public void setVelocity(double vx, double vy) {
        velocity = new Vec2(vx, vy);
    }

    public void setVelocity(Vec2 velocity) {
        if (velocity == null) {
            throw new IllegalArgumentException("La velocidad no puede ser null.");
        }
        this.velocity = velocity;
    }

    public boolean hasGravity() {
        return gravity;
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    public boolean isTornado() {
        return tornado;
    }

    public void setTornado(boolean tornado) {
        this.tornado = tornado;
    }
}
