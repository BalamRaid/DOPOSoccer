package doposoccer.domain;

import java.io.Serializable;

import doposoccer.domain.character.CharacterDefinition;
import doposoccer.domain.control.PlayerController;
import doposoccer.domain.control.PlayerIntent;
import doposoccer.domain.effect.Effect;
import doposoccer.domain.match.Arena;
import doposoccer.domain.math.Vec2;

/**
 * Jugador dentro de un partido: personaje elegido, vida, barra de poder, aura,
 * efecto recibido, balones de supervivencia y las estadisticas de su perfil.
 *
 * <p>El jugador recibe las intenciones de un {@link PlayerController} o, en
 * las pruebas y en el control manual, acciones directas. No conoce Swing. Su
 * posicion se expresa en las coordenadas logicas de la {@link Arena}. La vida
 * solo llega a cero mediante dano; al llegar a cero el jugador queda aturdido
 * durante {@link #STUN_SECONDS} segundos y recupera la vida maxima al
 * terminar.</p>
 */
public final class Player implements Serializable {

    private static final long serialVersionUID = 1L;


    /** Radio del cuerpo normal usado para colisiones y dibujo. */
    public static final double BODY_RADIUS = 40;

    /** Velocidad horizontal base antes de aplicar las estadisticas. */
    public static final double BASE_SPEED = 260;

    /** Velocidad vertical base del salto antes de aplicar las estadisticas. */
    public static final double BASE_JUMP = 640;

    /** Dano base de una patada antes de aplicar los multiplicadores. */
    public static final double KICK_BASE_DAMAGE = 10;

    /** Alcance adicional del pie para golpear el balon o al rival. */
    public static final double KICK_REACH = 48;

    /** Tiempo que tarda la barra de poder en llenarse por completo. */
    public static final double POWER_CHARGE_SECONDS = 10;

    /** Porcion de barra que recibe el jugador que concede un gol. */
    public static final double GOAL_BONUS = 0.25;

    /** Duracion del aturdimiento cuando la vida llega a cero o hay impacto. */
    public static final double STUN_SECONDS = 3;

    /** Duracion visual de la animacion de patada. */
    public static final double KICK_ANIMATION = 0.2;

    private final Profile profile;
    private final CharacterDefinition character;
    private final Side side;
    private final Arena arena;

    private Vec2 position;
    private Vec2 velocity;
    private int balls;
    private double life;
    private double powerBar;
    private boolean aura;
    private Effect effect;
    private double stunTimer;
    private boolean recoverLifeAfterStun;
    private double kickTimer;
    /** No se guarda en archivo: la sesion o la pantalla lo reinstalan al abrir. */
    private transient PlayerController controller;

    private boolean movingLeft;
    private boolean movingRight;
    private boolean jumpRequested;
    /** Agarre del piso; 1 = sin resbalar. Lo fija el clima. */
    private double grip = 1;
    private boolean kickRequested;

    /**
     * Crea un jugador para un lado de la cancha oficial.
     *
     * @param profile perfil con nombre, puntos y estadisticas
     * @param character definicion del personaje elegido
     * @param side lado que defiende el jugador
     */
    public Player(Profile profile, CharacterDefinition character, Side side) {
        this(profile, character, side, Arena.defaultArena());
    }

    /**
     * Crea un jugador para un lado de una cancha concreta.
     *
     * @param profile perfil con nombre, puntos y estadisticas
     * @param character definicion del personaje elegido
     * @param side lado que defiende el jugador
     * @param arena cancha en la que se mueve
     */
    public Player(Profile profile, CharacterDefinition character, Side side, Arena arena) {
        if (profile == null || character == null || side == null || arena == null) {
            throw new IllegalArgumentException("El perfil, el personaje, el lado y la cancha son obligatorios.");
        }
        this.profile = profile;
        this.character = character;
        this.side = side;
        this.arena = arena;
        this.velocity = Vec2.ZERO;
        this.life = profile.getStats().maxLife();
        resetPosition();
    }

    /**
     * Ajusta la velocidad horizontal hacia la deseada segun el agarre del
     * piso. Con agarre 1 el cambio es inmediato; con lluvia el jugador resbala.
     */
    private void slideToward(double targetVx) {
        double g = grip > 0 && grip <= 1 ? grip : 1;
        velocity = new Vec2(velocity.x() + (targetVx - velocity.x()) * g, velocity.y());
    }

    /**
     * Cambia el agarre del piso (lo fija el clima del partido).
     *
     * @param grip valor en (0, 1]; 1 es agarre total
     * @throws IllegalArgumentException si esta fuera de rango
     */
    public void setGrip(double grip) {
        if (!(grip > 0 && grip <= 1)) {
            throw new IllegalArgumentException("El agarre debe estar entre 0 y 1.");
        }
        this.grip = grip;
    }

    /**
     * Ubica al jugador en su posicion de saque: 25 % o 75 % del ancho segun el
     * lado, sobre el suelo y con velocidad cero.
     */
    public void resetPosition() {
        double x = side == Side.LEFT ? arena.width() * 0.25 : arena.width() * 0.75;
        position = new Vec2(x, arena.groundY());
        velocity = Vec2.ZERO;
        jumpRequested = false;
        kickRequested = false;
    }

    /**
     * Avanza el estado del jugador {@code dt} segundos: barra de poder,
     * aturdimiento, efecto activo, movimiento, salto y gravedad.
     *
     * @param dt tiempo transcurrido en segundos
     */
    public void update(double dt) {
        if (dt < 0 || Double.isNaN(dt)) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        powerBar = Math.min(1, powerBar + dt / POWER_CHARGE_SECONDS);
        updateStun(dt);
        if (canMove()) {
            int direction = (movingRight ? 1 : 0) - (movingLeft ? 1 : 0);
            if (effect != null && effect.invertControls()) {
                direction = -direction;
            }
            slideToward(direction * getSpeed());
        } else {
            slideToward(0);
        }
        if (effect != null) {
            effect.update(this, dt);
            if (effect.isFinished()) {
                effect = null;
            }
        }
        if (jumpRequested && isOnGround() && canJump()) {
            velocity = new Vec2(velocity.x(), -getJumpSpeed());
        }
        jumpRequested = false;
        velocity = new Vec2(velocity.x(), velocity.y() + arena.gravity() * dt);
        position = position.plus(velocity.times(dt));
        if (position.y() >= arena.groundY()) {
            position = new Vec2(position.x(), arena.groundY());
            velocity = new Vec2(velocity.x(), 0);
        }
        double radius = getBodyRadius();
        double clampedX = Math.max(radius, Math.min(arena.width() - radius, position.x()));
        position = new Vec2(clampedX, position.y());
        kickTimer = Math.max(0, kickTimer - dt);
    }

    private void updateStun(double dt) {
        if (stunTimer <= 0) {
            return;
        }
        stunTimer -= dt;
        if (stunTimer <= 0) {
            stunTimer = 0;
            if (recoverLifeAfterStun) {
                life = getMaxLife();
                recoverLifeAfterStun = false;
            }
        }
    }

    // ---------------- Control e intenciones ----------------

    /**
     * Asigna el controlador que produce las intenciones del jugador. Un valor
     * {@code null} significa control manual directo, usado por las pruebas y
     * por los metodos de accion del propio jugador.
     *
     * @param controller controlador humano o de maquina
     */
    public void setController(PlayerController controller) {
        this.controller = controller;
    }

    public PlayerController controller() {
        return controller;
    }

    /**
     * Aplica una intencion de control. Las acciones de salto, patada y poder
     * se validan con las mismas reglas que los metodos directos.
     *
     * @param intent intencion del controlador
     */
    public void applyIntent(PlayerIntent intent) {
        if (intent == null) {
            return;
        }
        movingLeft = intent.moveDirection() < 0;
        movingRight = intent.moveDirection() > 0;
        if (intent.jump()) {
            jump();
        }
        if (intent.kick()) {
            kick();
        }
        if (intent.special() && canActivatePower()) {
            try {
                activatePower();
            } catch (DOPOSoccerException ignored) {
                // La comprobacion previa evita el error; se ignora por seguridad.
            }
        }
    }

    public void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    /**
     * Solicita un salto para el siguiente ciclo de actualizacion. El salto
     * solo se aplica si el jugador esta en el suelo y puede saltar.
     */
    public void jump() {
        jumpRequested = true;
    }

    /**
     * Solicita una patada si el jugador puede actuar. La patada se consume en
     * el siguiente ciclo de {@link DOPOSoccer#step(double)}.
     */
    public void kick() {
        if (canKick()) {
            kickRequested = true;
            kickTimer = KICK_ANIMATION;
        }
    }

    /**
     * Devuelve si hay una patada pendiente y la consume.
     *
     * @return {@code true} si la patada estaba solicitada y el jugador puede
     *         patear en este momento
     */
    boolean consumeKick() {
        boolean pending = kickRequested;
        kickRequested = false;
        return pending && canKick();
    }

    /**
     * Indica si el jugador puede activar su poder en este momento.
     *
     * @return {@code true} si la barra esta llena y no hay un aura activa
     */
    public boolean canActivatePower() {
        return powerBar >= 1 && !aura;
    }

    /**
     * Activa el poder: vacia la barra y deja al personaje con aura hasta que
     * golpee el balon.
     *
     * @throws DOPOSoccerException si ya hay un aura activa o si la barra aun
     *                             no esta llena
     */
    public void activatePower() throws DOPOSoccerException {
        if (aura) {
            throw new DOPOSoccerException(DOPOSoccerException.POWER_ALREADY_ACTIVE);
        }
        if (powerBar < 1) {
            throw new DOPOSoccerException(DOPOSoccerException.POWER_NOT_READY);
        }
        powerBar = 0;
        aura = true;
    }

    /**
     * Consume el aura actual, normalmente cuando el jugador lanza su tiro de
     * poder.
     */
    void consumeAura() {
        aura = false;
    }

    // ---------------- Dano y efectos ----------------

    /**
     * Resta vida al jugador. La vida nunca baja de cero; si llega a cero, el
     * jugador queda aturdido durante {@link #STUN_SECONDS} segundos. Un
     * jugador que ya esta aturdido no recibe dano adicional.
     *
     * @param damage cantidad de dano que se intenta aplicar
     */
    public void receiveDamage(double damage) {
        if (isStunned() || damage <= 0) {
            return;
        }
        life = Math.max(0, life - damage);
        if (life == 0) {
            recoverLifeAfterStun = true;
            stun(STUN_SECONDS);
        }
    }

    /**
     * Aturde al jugador durante el tiempo indicado. Si ya estaba aturdido, se
     * conserva el temporizador mas largo. El aturdimiento por impacto no
     * recupera vida; solo el aturdimiento por llegar a cero lo hace.
     *
     * @param seconds duracion del aturdimiento
     */
    public void stun(double seconds) {
        if (seconds <= 0) {
            return;
        }
        stunTimer = Math.max(stunTimer, seconds);
    }

    /**
     * Aplica un efecto al jugador. Si ya tenia uno, el nuevo reemplaza al
     * anterior y ejecuta su accion inicial sobre este jugador.
     *
     * @param newEffect efecto que se aplica
     */
    public void applyEffect(Effect newEffect) {
        if (newEffect == null) {
            throw new IllegalArgumentException("El efecto no puede ser null.");
        }
        effect = newEffect;
        newEffect.onApply(this);
    }

    /**
     * Suma una cantidad a la barra de poder sin superar el maximo de 1.
     *
     * @param amount cantidad a sumar; puede ser negativa si se desea restar
     */
    public void addPowerBar(double amount) {
        powerBar = Math.max(0, Math.min(1, powerBar + amount));
    }

    // ---------------- Supervivencia ----------------

    /**
     * Asigna los balones iniciales del modo supervivencia.
     *
     * @param balls cantidad de balones
     */
    public void setBalls(int balls) {
        if (balls < 0) {
            throw new IllegalArgumentException("La cantidad de balones no puede ser negativa.");
        }
        this.balls = balls;
    }

    /**
     * Quita un balon al jugador cuando el rival anota. Nunca queda negativo.
     */
    public void loseBall() {
        balls = Math.max(0, balls - 1);
    }

    public int getBalls() {
        return balls;
    }

    // ---------------- Consultas ----------------

    /**
     * Indica si el jugador puede moverse en este momento.
     *
     * @return {@code false} si esta aturdido o si el efecto activo bloquea el
     *         movimiento
     */
    public boolean canMove() {
        return !isStunned() && (effect == null || effect.canMove());
    }

    /**
     * Indica si el jugador puede saltar en este momento.
     *
     * @return {@code false} si esta aturdido o si el efecto activo bloquea el
     *         salto
     */
    public boolean canJump() {
        return !isStunned() && (effect == null || effect.canJump());
    }

    /**
     * Indica si el jugador puede patear en este momento.
     *
     * @return {@code false} si esta aturdido o si el efecto activo bloquea la
     *         patada
     */
    public boolean canKick() {
        return !isStunned() && (effect == null || effect.canKick());
    }

    /**
     * Indica si el jugador esta aturdido.
     *
     * @return {@code true} mientras el temporizador de aturdimiento sea mayor
     *         que cero
     */
    public boolean isStunned() {
        return stunTimer > 0;
    }

    /**
     * Indica si el jugador esta apoyado sobre el suelo.
     *
     * @return {@code true} si su coordenada vertical alcanzo el suelo
     */
    public boolean isOnGround() {
        return position.y() >= arena.groundY();
    }

    public double getSpeed() {
        double effectFactor = effect == null ? 1 : effect.speedFactor();
        return BASE_SPEED * character.stats().speedFactor()
                * profile.getStats().factor(Stat.SPEED) * effectFactor;
    }

    public double getJumpSpeed() {
        return BASE_JUMP * character.stats().jumpFactor()
                * profile.getStats().factor(Stat.JUMP);
    }

    public double getKickDamage() {
        return KICK_BASE_DAMAGE * character.stats().strengthFactor()
                * profile.getStats().kickMultiplier();
    }

    public double getPowerShotDamage() {
        return character.specialAbility().damage() * character.stats().strengthFactor()
                * profile.getStats().kickMultiplier();
    }

    public double getMaxLife() {
        return profile.getStats().maxLife();
    }

    /**
     * Devuelve el radio actual del cuerpo, que puede reducirse por un efecto
     * de reduccion.
     *
     * @return radio de colision y dibujo
     */
    public double getBodyRadius() {
        double factor = effect == null ? 1 : effect.sizeFactor();
        return Math.max(8, BODY_RADIUS * factor);
    }

    public double getBodyX() {
        return position.x();
    }

    public double getBodyY() {
        return position.y() - getBodyRadius();
    }

    public double getFootX() {
        return position.x() + side.attackDirection() * (getBodyRadius() * 0.8);
    }

    public double getFootY() {
        return position.y() - 12;
    }

    public Profile getProfile() {
        return profile;
    }

    public CharacterDefinition getCharacter() {
        return character;
    }

    public Side getSide() {
        return side;
    }

    public Arena getArena() {
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

    public void setX(double x) {
        position = new Vec2(x, position.y());
    }

    public void setY(double y) {
        position = new Vec2(position.x(), y);
    }

    public double getVx() {
        return velocity.x();
    }

    public double getVy() {
        return velocity.y();
    }

    public void setVx(double vx) {
        velocity = new Vec2(vx, velocity.y());
    }

    public void setVy(double vy) {
        velocity = new Vec2(velocity.x(), vy);
    }

    public double getLife() {
        return life;
    }

    public double getPowerBar() {
        return powerBar;
    }

    public boolean hasAura() {
        return aura;
    }

    public Effect getEffect() {
        return effect;
    }

    public double getStunTimer() {
        return stunTimer;
    }

    public boolean isKicking() {
        return kickTimer > 0;
    }
}
