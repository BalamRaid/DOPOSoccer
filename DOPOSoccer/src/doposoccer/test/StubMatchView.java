package doposoccer.test;

import doposoccer.domain.Ball;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Player;
import doposoccer.domain.Side;
import doposoccer.domain.control.MatchView;
import doposoccer.domain.match.Arena;
import doposoccer.domain.math.Vec2;

/**
 * Vista de partido controlable para probar controladores sin crear un partido
 * completo.
 */
final class StubMatchView implements MatchView {

    Arena arena = Arena.defaultArena();
    Vec2 ballPosition = new Vec2(500, 300);
    Vec2 ballVelocity = Vec2.ZERO;
    Vec2 leftPosition = new Vec2(250, 450);
    Vec2 rightPosition = new Vec2(750, 450);
    Vec2 leftVelocity = Vec2.ZERO;
    Vec2 rightVelocity = Vec2.ZERO;
    double leftRadius = Player.BODY_RADIUS;
    double rightRadius = Player.BODY_RADIUS;
    int leftGoals;
    int rightGoals;
    int leftBalls = 3;
    int rightBalls = 3;
    double remainingSeconds = -1;
    boolean paused;
    boolean finished;
    boolean suddenDeath;
    double leftPower;
    double rightPower;
    boolean leftAura;
    boolean rightAura;
    boolean leftStunned;
    boolean rightStunned;
    boolean leftOnGround = true;
    boolean rightOnGround = true;
    String leftEffect;
    String rightEffect;
    double leftEffectRemaining;
    double rightEffectRemaining;
    MatchType matchType = goalMatch();

    @Override
    public Arena arena() {
        return arena;
    }

    @Override
    public Vec2 ballPosition() {
        return ballPosition;
    }

    @Override
    public Vec2 ballVelocity() {
        return ballVelocity;
    }

    @Override
    public Vec2 playerPosition(Side side) {
        return side == Side.LEFT ? leftPosition : rightPosition;
    }

    @Override
    public Vec2 playerVelocity(Side side) {
        return side == Side.LEFT ? leftVelocity : rightVelocity;
    }

    @Override
    public double bodyRadiusOf(Side side) {
        return side == Side.LEFT ? leftRadius : rightRadius;
    }

    @Override
    public boolean canKick(Side side) {
        Vec2 position = playerPosition(side);
        double radius = bodyRadiusOf(side);
        double footX = position.x() + side.attackDirection() * (radius * 0.8);
        double footY = position.y() - 12;
        return ballPosition.distanceTo(new Vec2(footX, footY))
                <= Player.KICK_REACH + Ball.RADIUS;
    }

    @Override
    public int goalsOf(Side side) {
        return side == Side.LEFT ? leftGoals : rightGoals;
    }

    @Override
    public int ballsOf(Side side) {
        return side == Side.LEFT ? leftBalls : rightBalls;
    }

    @Override
    public double remainingSeconds() {
        return remainingSeconds;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isSuddenDeath() {
        return suddenDeath;
    }

    @Override
    public double powerBarOf(Side side) {
        return side == Side.LEFT ? leftPower : rightPower;
    }

    @Override
    public boolean hasAura(Side side) {
        return side == Side.LEFT ? leftAura : rightAura;
    }

    @Override
    public boolean isStunned(Side side) {
        return side == Side.LEFT ? leftStunned : rightStunned;
    }

    @Override
    public boolean isOnGround(Side side) {
        return side == Side.LEFT ? leftOnGround : rightOnGround;
    }

    @Override
    public String effectNameOf(Side side) {
        return side == Side.LEFT ? leftEffect : rightEffect;
    }

    @Override
    public double effectRemainingOf(Side side) {
        return side == Side.LEFT ? leftEffectRemaining : rightEffectRemaining;
    }

    @Override
    public MatchType matchType() {
        return matchType;
    }

    private static MatchType goalMatch() {
        try {
            return GoalMatch.standard(3);
        } catch (DOPOSoccerException e) {
            throw new AssertionError(e);
        }
    }
}
