package doposoccer.domain;

import doposoccer.domain.character.CharacterDefinition;
import doposoccer.domain.control.MatchView;
import doposoccer.domain.control.PlayerController;
import doposoccer.domain.effect.Effect;
import doposoccer.domain.match.Arena;
import doposoccer.domain.match.GoalDetector;
import doposoccer.domain.match.MatchClock;
import doposoccer.domain.match.Score;
import doposoccer.domain.math.Vec2;
import java.util.List;
import java.util.Optional;

/**
 * Partido de DOPOSoccer: dos jugadores, el balon, el marcador, el reloj y las
 * reglas de goles, tiros de poder, dano, aturdimiento y fin del partido.
 *
 * <p>Esta clase es el agregado del partido y tambien implementa
 * {@link MatchView}, la vista de solo lectura que consumen los controladores.
 * Las responsabilidades auxiliares se delegaron en colaboradores pequenos:
 * {@link Arena} describe la cancha, {@link Score} lleva los goles,
 * {@link MatchClock} lleva el tiempo y {@link PowerShotState} administra los
 * tiros de poder y los balones falsos. La presentacion llama a
 * {@link #step(double)} en cada cuadro y consulta el estado por la vista.</p>
 *
 * @see Player
 * @see Ball
 * @see MatchType
 */
public final class DOPOSoccer implements MatchView {

    /** Componente horizontal de la velocidad que se aplica al patear el balon. */
    public static final double KICK_SPEED_X = 750;

    /** Componente vertical de la velocidad que se aplica al patear el balon. */
    public static final double KICK_SPEED_Y = -480;

    /** Restitucion usada cuando el balon rebota contra un jugador. */
    public static final double RESTITUTION = 0.8;

    /** Altura desde la que cae el balon despues de un gol. */
    public static final double BALL_DROP_Y = 100;

    private final Arena arena;
    private final Player left;
    private final Player right;
    private final Ball ball;
    private final MatchType matchType;
    private final Score score = new Score();
    private final MatchClock clock;
    private final PowerShotState powerShots;

    private boolean paused;
    private boolean finished;
    private boolean suddenDeath;
    private Side winnerSide;
    private int leftPointsEarned;
    private int rightPointsEarned;

    /**
     * Crea un partido en la cancha oficial.
     *
     * @param p1 perfil del jugador izquierdo
     * @param c1 personaje del jugador izquierdo
     * @param p2 perfil del jugador derecho
     * @param c2 personaje del jugador derecho
     * @param type regla que define cuando termina el partido
     * @throws DOPOSoccerException si falta alguno de los datos obligatorios
     */
    public DOPOSoccer(Profile p1, CharacterDefinition c1, Profile p2,
            CharacterDefinition c2, MatchType type) throws DOPOSoccerException {
        this(p1, c1, p2, c2, type, Arena.defaultArena());
    }

    /**
     * Crea un partido en una cancha concreta.
     *
     * @param p1 perfil del jugador izquierdo
     * @param c1 personaje del jugador izquierdo
     * @param p2 perfil del jugador derecho
     * @param c2 personaje del jugador derecho
     * @param type regla que define cuando termina el partido
     * @param arena cancha del partido
     * @throws DOPOSoccerException si falta alguno de los datos obligatorios
     */
    public DOPOSoccer(Profile p1, CharacterDefinition c1, Profile p2,
            CharacterDefinition c2, MatchType type, Arena arena)
            throws DOPOSoccerException {
        if (p1 == null || p2 == null || c1 == null || c2 == null
                || type == null || arena == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ELEMENT);
        }
        this.arena = arena;
        this.left = new Player(p1, c1, Side.LEFT, arena);
        this.right = new Player(p2, c2, Side.RIGHT, arena);
        this.matchType = type;
        this.ball = new Ball(arena, arena.width() / 2, BALL_DROP_Y);
        this.powerShots = new PowerShotState(ball);
        this.clock = type.durationSeconds() > 0
                ? MatchClock.ofSeconds(type.durationSeconds())
                : MatchClock.unlimited();
        if (type.initialBalls() > 0) {
            left.setBalls(type.initialBalls());
            right.setBalls(type.initialBalls());
        }
    }

    /**
     * Avanza el partido {@code dt} segundos. Si el partido esta pausado o
     * terminado, no hace nada.
     *
     * <p>El orden de actualizacion es: reloj, controladores, jugadores,
     * separacion de cuerpos, patadas, balon, contactos con jugadores, balones
     * falsos, goles y verificacion del tipo de partido.</p>
     *
     * @param dt tiempo transcurrido en segundos
     */
    public void step(double dt) {
        if (paused || finished) {
            return;
        }
        clock.advance(dt);
        applyControllers();
        left.update(dt);
        right.update(dt);
        separatePlayers();
        processKick(left);
        processKick(right);
        if (ball.update(dt)) {
            powerShots.end();
        }
        handleBallContact(left);
        handleBallContact(right);
        powerShots.update(dt);
        checkGoals();
        if (!finished && matchType.isOver(this)) {
            finish();
        }
    }

    /**
     * Pide a cada controlador su intencion y la aplica al jugador. Un
     * controlador {@code null} significa control manual directo.
     */
    private void applyControllers() {
        PlayerController leftController = left.controller();
        if (leftController != null) {
            left.applyIntent(leftController.decide(this, Side.LEFT));
        }
        PlayerController rightController = right.controller();
        if (rightController != null) {
            right.applyIntent(rightController.decide(this, Side.RIGHT));
        }
    }

    // ---------------- Patadas y tiros de poder ----------------

    /**
     * Resuelve una patada pendiente: golpea el balon si esta dentro del
     * alcance del pie o, en caso contrario, dana al rival si esta cerca.
     *
     * @param p jugador cuya patada se va a resolver
     */
    private void processKick(Player p) {
        if (!p.consumeKick()) {
            return;
        }
        Player rival = getRival(p);
        if (ballInKickRange(p.getSide())) {
            kickBall(p);
        } else if (Math.hypot(rival.getBodyX() - p.getFootX(),
                rival.getBodyY() - p.getFootY())
                <= Player.KICK_REACH + rival.getBodyRadius()) {
            rival.receiveDamage(p.getKickDamage());
        }
    }

    /**
     * Impulsa el balon con una patada normal o, si el jugador tiene aura,
     * lanza su tiro de poder.
     *
     * @param p jugador que patea
     */
    private void kickBall(Player p) {
        if (p.hasAura()) {
            firePowerShot(p);
            return;
        }
        powerShots.end();
        ball.setVelocity(p.getSide().attackDirection() * KICK_SPEED_X, KICK_SPEED_Y);
    }

    /**
     * Coloca el balon delante del jugador, consume su aura y delega en el
     * poder del personaje la trayectoria del tiro.
     *
     * @param p jugador que lanza el tiro
     */
    private void firePowerShot(Player p) {
        powerShots.begin(p);
        p.consumeAura();
        double direction = p.getSide().attackDirection();
        double bx = p.getBodyX() + direction * (p.getBodyRadius() + Ball.RADIUS + 2);
        bx = Math.max(Ball.RADIUS, Math.min(arena.width() - Ball.RADIUS, bx));
        ball.setPosition(bx, Math.min(ball.getY(), arena.groundY() - Ball.RADIUS));
        p.getCharacter().specialAbility().launch(ball, p.getSide(), powerShots::addFakeBall);
    }

    /**
     * Resuelve el contacto entre el balon y un jugador. Si el balon viene de
     * un tiro de poder, aplica el impacto al rival; si el jugador tiene aura,
     * dispara su poder; en caso contrario rebota el balon.
     *
     * @param p jugador que se comprueba contra el balon
     */
    private void handleBallContact(Player p) {
        Vec2 offset = ball.position().minus(new Vec2(p.getBodyX(), p.getBodyY()));
        double distance = offset.length();
        double minDistance = p.getBodyRadius() + Ball.RADIUS;
        if (distance >= minDistance || distance == 0) {
            return;
        }
        if (powerShots.shooter() == p) {
            return;
        }
        if (powerShots.shooter() != null) {
            hitRival(p);
            return;
        }
        if (p.hasAura()) {
            firePowerShot(p);
            return;
        }
        Vec2 normal = offset.times(1 / distance);
        ball.setPosition(p.getBodyX() + normal.x() * minDistance,
                p.getBodyY() + normal.y() * minDistance);
        Vec2 relative = ball.velocity().minus(p.velocity());
        double dot = relative.x() * normal.x() + relative.y() * normal.y();
        if (dot < 0) {
            Vec2 bounced = relative.minus(normal.times((1 + RESTITUTION) * dot));
            ball.setVelocity(p.velocity().plus(bounced));
        }
    }

    /**
     * El tiro de poder golpeo al rival: aplica dano y efecto, aturde durante
     * tres segundos y termina el tiro. Si habia balones falsos, renueva su
     * temporizador.
     *
     * @param rival jugador que recibe el impacto
     */
    private void hitRival(Player rival) {
        Player shooter = powerShots.shooter();
        if (shooter == null) {
            return;
        }
        rival.receiveDamage(shooter.getPowerShotDamage());
        rival.stun(Player.STUN_SECONDS);
        rival.applyEffect(shooter.getCharacter().specialAbility().createEffect());
        powerShots.renewFakeTimer();
        powerShots.end();
        ball.setVelocity(-ball.getVx() * 0.3, -300);
    }

    // ---------------- Goles y fin ----------------

    /**
     * Comprueba si el balon entro completamente en alguno de los arcos y, en
     * ese caso, registra el gol del jugador contrario.
     */
    private void checkGoals() {
        if (finished) {
            return;
        }
        Optional<Side> scorer = GoalDetector.scorer(ball.position(), Ball.RADIUS, arena);
        scorer.ifPresent(this::goal);
    }

    /**
     * Registra un gol: incrementa el marcador del anotador, da una
     * bonificacion de barra al rival, descuenta un balon en supervivencia y
     * reinicia las posiciones.
     *
     * @param scorerSide lado que anoto
     */
    private void goal(Side scorerSide) {
        score.addGoal(scorerSide);
        Player conceder = getPlayer(scorerSide.opposite());
        conceder.addPowerBar(Player.GOAL_BONUS);
        if (matchType.isSurvival()) {
            conceder.loseBall();
        }
        resetAfterGoal();
    }

    /**
     * Despues de un gol los personajes vuelven a su lugar y el balon cae del
     * centro. Tambien se eliminan los tiros de poder y balones falsos.
     */
    private void resetAfterGoal() {
        powerShots.clear();
        left.resetPosition();
        right.resetPosition();
        ball.setPosition(arena.width() / 2, BALL_DROP_Y);
        ball.setVelocity(0, 0);
    }

    /**
     * Separa los cuerpos circulares de los jugadores cuando se superponen. El
     * empuje se reparte en ambos ejes para que dos jugadores que caen uno sobre
     * otro no queden atravesados.
     */
    private void separatePlayers() {
        double dx = right.getBodyX() - left.getBodyX();
        double dy = right.getBodyY() - left.getBodyY();
        double min = left.getBodyRadius() + right.getBodyRadius();
        double distance = Math.hypot(dx, dy);
        if (distance >= min) {
            return;
        }
        double nx;
        double ny;
        if (distance < 1e-6) {
            nx = 1;
            ny = 0;
        } else {
            nx = dx / distance;
            ny = dy / distance;
        }
        double push = (min - distance) / 2;
        left.setX(clampX(left.getX() - nx * push, left.getBodyRadius()));
        left.setY(clampY(left.getY() - ny * push, left.getBodyRadius()));
        right.setX(clampX(right.getX() + nx * push, right.getBodyRadius()));
        right.setY(clampY(right.getY() + ny * push, right.getBodyRadius()));
    }

    private double clampX(double x, double radius) {
        return Math.max(radius, Math.min(arena.width() - radius, x));
    }

    private double clampY(double y, double radius) {
        return Math.max(radius, Math.min(arena.groundY(), y));
    }

    /**
     * Activa el estado de muerte subita. Lo invoca {@link TimeMatch} cuando
     * termina el tiempo reglamentario con empate.
     */
    void startSuddenDeath() {
        suddenDeath = true;
    }

    /**
     * Termina el partido, define el ganador con la regla del tipo de partido y
     * reparte los puntos de victoria o derrota a ambos perfiles. Si el
     * marcador esta empatado, no hay ganador y ambos reciben los puntos de
     * derrota.
     */
    public void finish() {
        if (finished) {
            return;
        }
        finished = true;
        winnerSide = matchType.winner(this);
        leftPointsEarned = winnerSide == Side.LEFT ? Profile.WIN_POINTS : Profile.LOSE_POINTS;
        rightPointsEarned = winnerSide == Side.RIGHT ? Profile.WIN_POINTS : Profile.LOSE_POINTS;
        left.getProfile().addPoints(leftPointsEarned);
        right.getProfile().addPoints(rightPointsEarned);
    }

    /**
     * Activa el poder del jugador indicado.
     *
     * @param side lado del jugador que activa el poder
     * @throws DOPOSoccerException si el partido ya termino o si el jugador no
     *                             puede activar el poder
     */
    public void activatePower(Side side) throws DOPOSoccerException {
        if (finished) {
            throw new DOPOSoccerException(DOPOSoccerException.GAME_FINISHED);
        }
        getPlayer(side).activatePower();
    }

    /**
     * Asigna el controlador de un lado.
     *
     * @param side lado controlado
     * @param controller controlador humano o de maquina
     */
    public void setController(Side side, PlayerController controller) {
        getPlayer(side).setController(controller);
    }

    /**
     * Alterna el estado de pausa. Si el partido ya termino, no hace nada.
     */
    public void togglePause() {
        if (!finished) {
            paused = !paused;
        }
    }

    public void pause() {
        if (!finished) {
            paused = true;
        }
    }

    public void resume() {
        if (!finished) {
            paused = false;
        }
    }

    // ---------------- Consultas ----------------

    public Player getPlayer(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        return side == Side.LEFT ? left : right;
    }

    public Player getRival(Player p) {
        if (p == null) {
            throw new IllegalArgumentException("El jugador no puede ser null.");
        }
        if (p == left) {
            return right;
        }
        if (p == right) {
            return left;
        }
        throw new IllegalArgumentException("El jugador no pertenece a este partido.");
    }

    public Ball getBall() {
        return ball;
    }

    public List<Ball> getFakeBalls() {
        return powerShots.fakeBalls();
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public Arena getArena() {
        return arena;
    }

    public Score getScore() {
        return score;
    }

    public MatchClock getClock() {
        return clock;
    }

    public double getElapsedSeconds() {
        return clock.elapsedSeconds();
    }

    public double getRemainingSeconds() {
        return clock.remainingSeconds();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isSuddenDeath() {
        return suddenDeath;
    }

    public Player getWinner() {
        return winnerSide == null ? null : getPlayer(winnerSide);
    }

    public Side getWinnerSide() {
        return winnerSide;
    }

    public int getPointsEarned(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("El lado no puede ser null.");
        }
        return side == Side.LEFT ? leftPointsEarned : rightPointsEarned;
    }

    // ---------------- MatchView ----------------

    @Override
    public Arena arena() {
        return arena;
    }

    @Override
    public Vec2 ballPosition() {
        return ball.position();
    }

    @Override
    public Vec2 ballVelocity() {
        return ball.velocity();
    }

    @Override
    public Vec2 playerPosition(Side side) {
        return getPlayer(side).position();
    }

    @Override
    public Vec2 playerVelocity(Side side) {
        return getPlayer(side).velocity();
    }

    @Override
    public double bodyRadiusOf(Side side) {
        return getPlayer(side).getBodyRadius();
    }

    private boolean ballInKickRange(Side side) {
        Player player = getPlayer(side);
        return ball.distanceTo(player.getFootX(), player.getFootY())
                <= Player.KICK_REACH + Ball.RADIUS;
    }

    @Override
    public boolean canKick(Side side) {
        Player player = getPlayer(side);
        if (ballInKickRange(side)) {
            return true;
        }
        Player rival = getRival(player);
        return Math.hypot(rival.getBodyX() - player.getFootX(),
                rival.getBodyY() - player.getFootY())
                <= Player.KICK_REACH + rival.getBodyRadius();
    }

    @Override
    public int goalsOf(Side side) {
        return score.goalsOf(side);
    }

    @Override
    public int ballsOf(Side side) {
        return getPlayer(side).getBalls();
    }

    @Override
    public double remainingSeconds() {
        return clock.remainingSeconds();
    }

    @Override
    public boolean hasAura(Side side) {
        return getPlayer(side).hasAura();
    }

    @Override
    public boolean isStunned(Side side) {
        return getPlayer(side).isStunned();
    }

    @Override
    public boolean isOnGround(Side side) {
        return getPlayer(side).isOnGround();
    }

    @Override
    public double powerBarOf(Side side) {
        return getPlayer(side).getPowerBar();
    }

    @Override
    public String effectNameOf(Side side) {
        Effect effect = getPlayer(side).getEffect();
        return effect == null ? null : effect.getName();
    }

    @Override
    public double effectRemainingOf(Side side) {
        Effect effect = getPlayer(side).getEffect();
        return effect == null ? 0 : effect.getRemaining();
    }

    @Override
    public MatchType matchType() {
        return matchType;
    }
}
