package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.EnemiesAnimation.SkeletonAnimationManager;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.weapon.SwordWeapon;

import java.util.List;

public class Skeleton extends BaseEnemy {
    private static final float PATROL_RADIUS   = 80f;
    private static final float PATROL_SPEED    = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED     = 120f;
    private static final float JUMP_SPEED      = 400f;
    private static final float STEP_UP_SPEED   = 400f;

    private static final float ATTACK_DURATION = 0.30f;
    private static final float ATTACK_COOLDOWN = 1.5f;
    private static final int   ATTACK_DAMAGE   = 30;
    private static final float MELEE_RANGE     = 70f;

    private final SwordWeapon slashWeapon;
    private final float patrolCenterX;
    private float patrolDir = 1f;
    private boolean facingRight = true;

    private final SkeletonAnimationManager animationManager;
    private SkeletonAnimationManager.State currentState;

    public Skeleton(float x, float y) {
        super(
            x, y,
            32f, 48f,
            null,
            50,
            -2000f,
            -1000f,
            0.9f,
            16f, 400f
        );

        this.slashWeapon = new SwordWeapon(
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );

        this.patrolCenterX = x;
        this.animationManager = new SkeletonAnimationManager();
        this.currentState = SkeletonAnimationManager.State.WALK;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height * 0.7f;

        slashWeapon.update(delta, pivotX, pivotY, facingRight);

        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float dx = playerCX - pivotX;
        boolean isInAttackRange = dx * dx <= MELEE_RANGE * MELEE_RANGE;
        boolean canAttack = slashWeapon.getCooldownRemaining() <= 0f;

        // Визначаємо стан анімації
        if (!isAlive()) {
            currentState = SkeletonAnimationManager.State.DEATH;
        } else if (slashWeapon.isAttacking()) {
            currentState = SkeletonAnimationManager.State.ATTACK;
        } else {
            currentState = Math.abs(physics.getVelocityX()) > 0.1f
                ? SkeletonAnimationManager.State.WALK
                : SkeletonAnimationManager.State.WALK;
        }

        animationManager.update(delta, currentState, facingRight);

        if (isInAttackRange && canAttack && isAlive()) {
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
        }

        if (isAlive()) {
            slashWeapon.applyDamage(player);
        }

        super.update(delta, player, platforms);
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        if (!isAlive()) return;

        Rectangle b = getBounds();
        float belowX = b.x + b.width / 2f;
        boolean hasGround = false;
        for (Rectangle p : platforms) {
            if (belowX >= p.x && belowX <= p.x + p.width && p.y + p.height <= b.y) {
                hasGround = true;
                break;
            }
        }
        if (!hasGround) {
            patrolDir = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;
        }

        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float cx = b.x + b.width / 2f;
        float dx = playerCX - cx;
        float dist2 = dx * dx;

        if (dist2 <= MELEE_RANGE * MELEE_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        float moveDir, speed;
        boolean onGround = physics.getVelocityY() == 0f;

        if (dist2 <= DETECTION_RANGE * DETECTION_RANGE) {
            moveDir = Math.signum(dx);
            speed = CHASE_SPEED;
            facingRight = moveDir > 0;
        } else {
            moveDir = patrolDir;
            speed = PATROL_SPEED;
            if (b.x > patrolCenterX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolCenterX - PATROL_RADIUS) patrolDir = 1f;
            facingRight = patrolDir > 0;
        }

        float aheadX = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(aheadX, b.y, 2f, b.height);
        boolean wallAhead = false;
        Rectangle hitP = null;
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                wallAhead = true;
                hitP = p;
                break;
            }
        }

        float footX = b.x + b.width / 2f + (facingRight ? 6f : -6f);
        Rectangle footPrb = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = false;
        for (Rectangle p : platforms) {
            if (footPrb.overlaps(p)) {
                groundAhead = true;
                break;
            }
        }

        if (wallAhead && onGround && groundAhead) {
            float stepH = hitP.y + hitP.height - b.y;
            if (stepH <= physics.getMaxStepHeight()) {
                physics.setVelocityY(STEP_UP_SPEED);
            } else {
                physics.setVelocityY(JUMP_SPEED);
            }
        }

        physics.setVelocityX(moveDir * speed);
    }

    @Override
    public void render(SpriteBatch batch) {
        Rectangle b = getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();

        if (facingRight) {
            batch.draw(frame, b.x, b.y, b.width, b.height);
        } else {
            batch.draw(frame, b.x + b.width, b.y, -b.width, b.height);
        }
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        super.renderHitbox(r);
        Rectangle hb = slashWeapon.getHitbox();
        if (hb != null) {
            r.setColor(1f, 0.5f, 0f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
        slashWeapon.renderTrajectory(r);
    }

    public void dispose() {
        animationManager.dispose();
    }
}
