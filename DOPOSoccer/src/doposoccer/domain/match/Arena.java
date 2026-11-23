package doposoccer.domain.match;

import doposoccer.domain.Field;
import doposoccer.domain.Side;
import java.util.List;

/**
 * Descripcion completa de la cancha: limites, suelo, gravedad y arcos.
 *
 * <p>La presentacion y el dominio comparten las mismas coordenadas logicas.
 * Tener la cancha en un objeto permite crear escenarios de prueba con otras
 * dimensiones sin duplicar constantes ni tocar la logica del partido.</p>
 */
public final class Arena {

    private final double width;
    private final double height;
    private final double groundY;
    private final double crossbarY;
    private final double goalDepth;
    private final double gravity;
    private final List<Goal> goals;

    /**
     * Crea una cancha con sus medidas y sus dos arcos.
     *
     * @param width ancho total
     * @param height alto total
     * @param groundY coordenada vertical del suelo
     * @param crossbarY coordenada vertical del travesano
     * @param goalDepth profundidad horizontal de cada arco
     * @param gravity aceleracion de la gravedad
     * @param goals lista con exactamente dos arcos
     */
    public Arena(double width, double height, double groundY, double crossbarY,
            double goalDepth, double gravity, List<Goal> goals) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Las dimensiones de la cancha deben ser positivas.");
        }
        if (groundY <= 0 || groundY > height) {
            throw new IllegalArgumentException("El suelo debe estar dentro de la cancha.");
        }
        if (crossbarY <= 0 || crossbarY >= groundY) {
            throw new IllegalArgumentException("El travesano debe estar entre el techo y el suelo.");
        }
        if (goalDepth <= 0 || goalDepth >= width / 2) {
            throw new IllegalArgumentException("La profundidad del arco no es valida.");
        }
        if (gravity <= 0) {
            throw new IllegalArgumentException("La gravedad debe ser positiva.");
        }
        if (goals == null || goals.size() != 2) {
            throw new IllegalArgumentException("La cancha debe tener exactamente dos arcos.");
        }
        this.width = width;
        this.height = height;
        this.groundY = groundY;
        this.crossbarY = crossbarY;
        this.goalDepth = goalDepth;
        this.gravity = gravity;
        this.goals = List.copyOf(goals);
    }

    /**
     * Crea la cancha oficial del proyecto a partir de las constantes logicas
     * de {@link Field}.
     *
     * @return cancha oficial con sus dos arcos
     */
    public static Arena defaultArena() {
        double goalHeight = Field.GROUND - Field.CROSSBAR_Y;
        Goal left = new Goal(Side.LEFT, 0, Field.CROSSBAR_Y, Field.GOAL_DEPTH, goalHeight);
        Goal right = new Goal(Side.RIGHT, Field.WIDTH - Field.GOAL_DEPTH, Field.CROSSBAR_Y,
                Field.GOAL_DEPTH, goalHeight);
        return new Arena(Field.WIDTH, Field.HEIGHT, Field.GROUND, Field.CROSSBAR_Y,
                Field.GOAL_DEPTH, Field.GRAVITY, List.of(left, right));
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double groundY() {
        return groundY;
    }

    public double crossbarY() {
        return crossbarY;
    }

    public double goalDepth() {
        return goalDepth;
    }

    public double gravity() {
        return gravity;
    }

    public List<Goal> goals() {
        return goals;
    }

    /**
     * Devuelve el arco que defiende el lado indicado.
     *
     * @param side lado consultado
     * @return arco del lado
     */
    public Goal goalOf(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        for (Goal goal : goals) {
            if (goal.side() == side) {
                return goal;
            }
        }
        throw new IllegalStateException("No existe arco para el lado " + side + ".");
    }
}
