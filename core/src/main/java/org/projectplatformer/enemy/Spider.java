package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.EnemiesAnimation.SpiderAnimationManager;
import org.projectplatformer.player.Player;
import org.projectplatformer.weapon.SpearWeapon;

import java.util.List;

public class Spider extends BaseEnemy {
    // --- Рух ---
    private static final float PATROL_RADIUS = 80f;
    private static final float PATROL_SPEED = 60f;
    private static final float CHASE_RANGE = 100f;
    private static final float MOVE_SPEED = 80f;

    // --- Атака списом ---
    private static final float SPEAR_MAX_LENGTH = 50f;
    private static final float SPEAR_WIDTH = 8f;
    private static final float ATTACK_RANGE = 40f;
    private static final float ATTACK_DURATION = 0.3f;
    private static final float ATTACK_COOLDOWN = 1.2f;
    private static final int ATTACK_DAMAGE = 12;

    private static final float STEP_UP_SPEED = 400f;
    private static final float JUMP_SPEED = 400f;

    private final SpearWeapon spearWeapon;
    private final float patrolStartX;
    private float patrolDir = 1f;
    private boolean facingRight = true;

    private final SpiderAnimationManager animationManager;
    private SpiderAnimationManager.State currentState;

    private boolean isAttackAnimationPlaying = false;

    private static final boolean SPRITE_LOOKS_RIGHT = false;

    public Spider(float x, float y) {
        super(
            x, y,
            32f, 32f,
            null,
            45,
            -2000f,
            -1000f,
            0.9f,
            16f,
            STEP_UP_SPEED
        );
        this.spearWeapon = new SpearWeapon(
            SPEAR_MAX_LENGTH,
            SPEAR_WIDTH,
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );
        this.patrolStartX = x;
        this.animationManager = new SpiderAnimationManager();
        this.currentState = SpiderAnimationManager.State.WALK;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        if (isDeadAndGone()) return;
        if (!isAlive()) {
            setDeadAndGone();
            return;
        }

        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height / 2f;

        spearWeapon.update(delta, pivotX, pivotY, facingRight);

        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float playerCY = player.getBounds().y + player.getBounds().height / 2f;
        float dx = playerCX - pivotX;
        float dy = playerCY - pivotY;
        float dist2 = dx * dx + dy * dy;
        boolean playerSameLevel = Math.abs(dy) < b.height * 1.2f;

        if (isAttackAnimationPlaying) {
            if (animationManager.isAttackAnimationFinished()) {
                isAttackAnimationPlaying = false;
                currentState = SpiderAnimationManager.State.WALK;
            } else {
                currentState = SpiderAnimationManager.State.ATTACK;
            }
            animationManager.update(delta, currentState, facingRight);
            spearWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        // --- Почати атаку, якщо дуже близько і cooldown закінчився ---
        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE && playerSameLevel && spearWeapon.getCooldownRemaining() <= 0f) {
            isAttackAnimationPlaying = true;
            animationManager.resetAttackAnim();
            currentState = SpiderAnimationManager.State.ATTACK;
            spearWeapon.startAttack(pivotX, pivotY, dx > 0);
            facingRight = dx > 0;
            animationManager.update(delta, currentState, facingRight);
            spearWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        // --- Звичайна логіка руху (AI) ---
        if (dist2 <= CHASE_RANGE * CHASE_RANGE && playerSameLevel) {
            float moveDir = Math.signum(dx);
            physics.setVelocityX(moveDir * MOVE_SPEED);
            facingRight = moveDir > 0;
        } else {
            if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolStartX - PATROL_RADIUS) patrolDir = 1f;
            float moveDir = patrolDir;
            physics.setVelocityX(moveDir * PATROL_SPEED);
            facingRight = patrolDir > 0;
        }

        currentState = SpiderAnimationManager.State.WALK;
        animationManager.update(delta, currentState, facingRight);
        spearWeapon.applyDamage(player);

        // Step up якщо треба
        if (physics.getVelocityX() != 0f) {
            physics.tryStepUp(platforms, physics.getVelocityX() > 0f);
        }

        super.update(delta, player, platforms);
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();
        float cx = b.x + b.width / 2f;
        Rectangle pb = player.getBounds();
        float px = pb.x + pb.width / 2f;
        float dx = px - cx;
        float dist2 = dx * dx;

        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        if (dist2 <= CHASE_RANGE * CHASE_RANGE) {
            float moveDir = Math.signum(dx);
            physics.setVelocityX(moveDir * MOVE_SPEED);
            facingRight = moveDir > 0;
        } else {
            if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolStartX - PATROL_RADIUS) patrolDir = 1f;
            float moveDir = patrolDir;
            physics.setVelocityX(moveDir * PATROL_SPEED);
            facingRight = patrolDir > 0;
        }

        boolean onGround = physics.getVelocityY() == 0f;
        float probeX = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(probeX, b.y, 2f, b.height);
        boolean wallAhead = false;
        Rectangle hitPlatform = null;
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                wallAhead = true;
                hitPlatform = p;
                break;
            }
        }

        float footX = b.x + b.width / 2f + (facingRight ? 6f : -6f);
        Rectangle footProbe = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = false;
        for (Rectangle p : platforms) {
            if (footProbe.overlaps(p)) {
                groundAhead = true;
                break;
            }
        }

        if (wallAhead || !groundAhead) {
            patrolDir = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isDeadAndGone()) return;
        Rectangle b = getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();

        boolean flip = facingRight != SPRITE_LOOKS_RIGHT;
        if (flip) {
            batch.draw(frame, b.x + b.width, b.y, -b.width, b.height);
        } else {
            batch.draw(frame, b.x, b.y, b.width, b.height);
        }
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        if (isDeadAndGone()) return;
        super.renderHitbox(r);
        Rectangle hb = spearWeapon.getHitbox();
        if (hb != null) {
            r.setColor(0f, 0.8f, 0.8f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }

    public void dispose() {
        animationManager.dispose();
    }
}
