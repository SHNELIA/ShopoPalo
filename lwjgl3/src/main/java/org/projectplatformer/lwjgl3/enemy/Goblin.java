package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.EnemiesAnimation.GoblinAnimationManager;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.weapon.SwordWeapon;

import java.util.List;

/**
 * Ворог «Гоблін» з патрулюванням, переслідуванням, атакою мечем та анімацією.
 */
public class Goblin extends BaseEnemy {
    private static final float PATROL_RADIUS   = 80f;
    private static final float PATROL_SPEED    = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED     = 120f;
    private static final float JUMP_SPEED      = 400f;
    private static final float STEP_UP_SPEED   = 400f;

    private static final float ATTACK_DURATION = 0.45f; // тривалість слеша
    private static final float ATTACK_COOLDOWN = 1.20f; // кулдаун між ударами
    private static final int   ATTACK_DAMAGE   = 16;    // Шкода одного удару
    private static final float MELEE_RANGE     = 70f;   // Дальність атаки

    private final SwordWeapon slashWeapon;
    private final float patrolCenterX;
    private float patrolDir = 1f;
    private boolean facingRight = true;

    private final GoblinAnimationManager animationManager;
    private GoblinAnimationManager.State currentState;

    public Goblin(float x, float y) {
        super(
            x, y,
            32f, 48f,      // width, height
            null,          // texture — тепер не потрібен, бо малюємо анімацію
            50,            // health
            -2000f,        // gravity
            -1000f,        // max fall speed
            0.9f,          // drag
            16f, 400f      // maxStepHeight, stepUpSpeed
        );

        this.slashWeapon = new SwordWeapon(
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );
        this.patrolCenterX = x;

        this.animationManager = new GoblinAnimationManager();
        this.currentState = GoblinAnimationManager.State.WALK; // Початковий стан — ходьба
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

        // Визначаємо поточний стан анімації
        if (!isAlive()) {
            currentState = GoblinAnimationManager.State.DEATH;
        } else if (slashWeapon.isAttacking()) {
            currentState = GoblinAnimationManager.State.ATTACK;
        } else {
            // Коли не атакує і живий — ходьба або стоянка (у тебе немає idle, тому будемо ставити WALK якщо рухаємося, або WALK якщо стоїмо)
            if (Math.abs(physics.getVelocityX()) > 0.1f) {
                currentState = GoblinAnimationManager.State.WALK;
            } else {
                // Навмисне ставимо WALK навіть стоячи, бо idle немає
                currentState = GoblinAnimationManager.State.WALK;
            }
        }

        animationManager.update(delta, currentState, facingRight);

        // Атака
        if (isInAttackRange && canAttack) {
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
        }
        slashWeapon.applyDamage(player);

        super.update(delta, player, platforms);
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();

        // 0) Якщо перед обрив — розвернутися
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

        // 1) Переслідування vs патруль
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

        // 2) Step-up / перепригування
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

        // 3) Горизонтальна швидкість
        physics.setVelocityX(moveDir * speed);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        Rectangle b = getBounds();

        TextureRegion frame = animationManager.getCurrentFrame();

        if (facingRight) {
            batch.draw(frame, b.x, b.y, b.width, b.height);
        } else {
            batch.draw(frame,
                b.x + b.width, b.y,
                -b.width, b.height);
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
