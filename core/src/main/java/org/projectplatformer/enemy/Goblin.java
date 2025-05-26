package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.player.Player;
import org.projectplatformer.weapon.SwordWeapon;

import java.util.List;

/**
 * Ворог «Гоблін» з патрулюванням, переслідуванням та атакою мечем.
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
    private final float       patrolCenterX;
    private float             patrolDir   = 1f;
    private boolean           facingRight = true;

    public Goblin(float x, float y, Texture texture) {
        super(
            x, y,
            32f, 48f,      // width, height
            texture,
            50,            // health
            -2000f,        // gravity
            -1000f,        // max fall speed
            0.9f,          // drag
            16f, 400f      // maxStepHeight, stepUpSpeed
        );

        // Передаємо лише duration, cooldown і damage
        this.slashWeapon   = new SwordWeapon(
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );
        this.patrolCenterX = x;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b      = getBounds();
        float     pivotX = b.x + b.width  / 2f;
        float     pivotY = b.y + b.height * 0.7f;

        // 1) Оновлюємо меч
        slashWeapon.update(delta, pivotX, pivotY, facingRight);

        // 2) Завжди дивимось на гравця
        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        facingRight = playerCX > pivotX;

        // 3) Якщо гравець в радіусі та кулдаун минув — атакуємо
        float dx = playerCX - pivotX;
        if (dx*dx <= MELEE_RANGE * MELEE_RANGE
            && slashWeapon.getCooldownRemaining() <= 0f)
        {
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
        }

        // 4) Наносимо шкоду (лише раз за удар)
        slashWeapon.applyDamage(player);

        // 5) Інше: рух/AI/фізика
        super.update(delta, player, platforms);
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();

        // 0) Якщо перед обрив — розвернутися
        float belowX = b.x + b.width/2f;
        Rectangle belowProbe = new Rectangle(belowX, 0f, 0.1f, b.y);
        boolean hasGround = false;
        for (Rectangle p : platforms) {
            if (belowX >= p.x && belowX <= p.x + p.width
                && p.y + p.height <= b.y)
            {
                hasGround = true;
                break;
            }
        }
        if (!hasGround) {
            patrolDir   = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;
        }

        // 1) Переслідування vs патруль
        float playerCX = player.getBounds().x + player.getBounds().width/2f;
        float cx       = b.x + b.width/2f;
        float dx       = playerCX - cx;
        float dist2    = dx*dx;

        if (dist2 <= MELEE_RANGE * MELEE_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        float moveDir, speed;
        boolean onGround = physics.getVelocityY() == 0f;

        if (dist2 <= DETECTION_RANGE * DETECTION_RANGE) {
            moveDir     = Math.signum(dx);
            speed       = CHASE_SPEED;
            facingRight = moveDir > 0;
        } else {
            moveDir = patrolDir;
            speed   = PATROL_SPEED;
            if (b.x > patrolCenterX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolCenterX - PATROL_RADIUS) patrolDir =  1f;
            moveDir     = patrolDir;
            facingRight = patrolDir > 0;
        }

        // 2) Step-up / перепригування
        float aheadX    = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(aheadX, b.y, 2f, b.height);
        boolean wallAhead = false;
        Rectangle hitP    = null;
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                wallAhead = true;
                hitP      = p;
                break;
            }
        }

        float footX       = b.x + b.width/2f + (facingRight ? 6f : -6f);
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
        if (facingRight) {
            batch.draw(texture, b.x, b.y, b.width, b.height);
        } else {
            batch.draw(texture,
                b.x + b.width, b.y,
                -b.width,      b.height);
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
}
