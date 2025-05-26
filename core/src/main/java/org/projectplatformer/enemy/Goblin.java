package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.EnemiesAnimation.GoblinAnimationManager;
import org.projectplatformer.player.Player;
import org.projectplatformer.weapon.SwordWeapon;

import java.util.List;

public class Goblin extends BaseEnemy {
    private static final float PATROL_RADIUS   = 80f;
    private static final float PATROL_SPEED    = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED     = 120f;
    private static final float JUMP_SPEED      = 400f;
    private static final float STEP_UP_SPEED   = 400f;

    private static final float ATTACK_DURATION = 0.45f;
    private static final float ATTACK_COOLDOWN = 1.20f;
    private static final int   ATTACK_DAMAGE   = 16;
    private static final float MELEE_RANGE     = 70f;

    private final SwordWeapon slashWeapon;
    private final float patrolCenterX;
    private float patrolDir = 1f;
    private boolean facingRight = true;

    private final GoblinAnimationManager animationManager;
    private GoblinAnimationManager.State currentState;

    private boolean isAttackAnimationPlaying = false;
    private boolean deathAnimationStarted = false;

    // Якщо спрайт дивиться ВЛІВО (OpenGameArt, itch.io) — false. Якщо вправо — true.
    private static final boolean SPRITE_LOOKS_RIGHT = false;

    public Goblin(float x, float y) {
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
        this.animationManager = new GoblinAnimationManager();
        this.currentState = GoblinAnimationManager.State.WALK;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        if (isDeadAndGone()) return;

        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height * 0.7f;

        // --- Анімація смерті ---
        if (!isAlive()) {
            currentState = GoblinAnimationManager.State.DEATH;

            // Скидаємо анімацію смерті лише один раз
            if (!deathAnimationStarted) {
                animationManager.resetDeathAnim();
                deathAnimationStarted = true;
            }
            animationManager.update(delta, currentState, facingRight);

            if (animationManager.isDeathAnimationFinished()) {
                setDeadAndGone();
            }
            return;
        }
        // Якщо воскресне (наприклад, через дебаг), скидаємо прапорець
        deathAnimationStarted = false;

        // --- Звичайна логіка (живий гоблін) ---
        slashWeapon.update(delta, pivotX, pivotY, facingRight);

        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float dx = playerCX - pivotX;
        boolean isInAttackRange = dx * dx <= MELEE_RANGE * MELEE_RANGE;
        boolean canAttack = slashWeapon.getCooldownRemaining() <= 0f;

        if (isAttackAnimationPlaying) {
            if (animationManager.isAttackAnimationFinished()) {
                isAttackAnimationPlaying = false;
                currentState = GoblinAnimationManager.State.WALK;
            } else {
                currentState = GoblinAnimationManager.State.ATTACK;
            }
        } else if (isInAttackRange && canAttack) {
            isAttackAnimationPlaying = true;
            animationManager.resetAttackAnim();
            currentState = GoblinAnimationManager.State.ATTACK;
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
        } else {
            if (Math.abs(physics.getVelocityX()) > 0.1f) {
                currentState = GoblinAnimationManager.State.WALK;
            } else {
                currentState = GoblinAnimationManager.State.WALK;
            }
        }

        animationManager.update(delta, currentState, facingRight);
        slashWeapon.applyDamage(player);

        // Поки триває анімація атаки, не AI-мо і не ходимо
        if (!isAttackAnimationPlaying) {
            super.update(delta, player, platforms);
        } else {
            physics.update(delta, platforms); // лише гравітація/фізика
        }
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
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
            moveDir = patrolDir;
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
