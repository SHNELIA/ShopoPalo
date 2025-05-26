package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.player.Player;
import org.projectplatformer.weapon.SpearWeapon;

import java.util.List;

/**
 * Простий павук: патрулює у радіусі, чекає гравця, коли близько —
 * коле списом, при цьому може «підстрибувати» на невисокі блоки.
 */
public class Spider extends BaseEnemy {
    // --- Рух ---
    private static final float PATROL_RADIUS =  80f;
    private static final float PATROL_SPEED  =  60f;
    private static final float CHASE_RANGE   = 100f;
    private static final float MOVE_SPEED    =  80f;

    // --- Атака списом ---
    private static final float SPEAR_MAX_LENGTH = 50f;
    private static final float SPEAR_WIDTH      =  8f;
    private static final float ATTACK_RANGE     = 40f;
    private static final float ATTACK_DURATION  = 0.3f;
    private static final float ATTACK_COOLDOWN  = 1.2f;
    private static final int   ATTACK_DAMAGE    = 12;

    private static final float STEP_UP_SPEED = 400f;
    private static final float JUMP_SPEED = 400f;

    private final SpearWeapon spearWeapon;
    private final float       patrolStartX;
    private float             patrolDir    = 1f;
    private boolean           facingRight  = true;

    public Spider(float x, float y, Texture tex) {
        super(
            x, y,
            32f, 32f,
            tex,
            45,        // health
            -2000f,    // gravity
            -1000f,    // maxFallSpeed
            0.9f,      // drag
            16f,       // maxStepHeight
            STEP_UP_SPEED // stepUpSpeed
        );
        this.spearWeapon = new SpearWeapon(
            SPEAR_MAX_LENGTH,
            SPEAR_WIDTH,
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );
        this.patrolStartX = x;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b      = getBounds();
        float     pivotX = b.x + b.width/2f;
        float     pivotY = b.y + b.height/2f;

        // 1) Оновлюємо спис
        spearWeapon.update(delta, pivotX, pivotY, facingRight);

        // 2) Відстань до гравця
        float playerCX = player.getBounds().x + player.getBounds().width/2f;
        float dx       = playerCX - pivotX;
        float dist2    = dx*dx;

        // 3) Якщо гравець у радіусі атаки — зупиняємося завжди
        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE) {
            physics.setVelocityX(0f);

            // 3.1) І тільки коли кулдаун сплив, старт атаки
            if (spearWeapon.getCooldownRemaining() <= 0f) {
                facingRight = dx > 0;
                spearWeapon.startAttack(pivotX, pivotY, facingRight);
            }
        }
        // 4) Інакше (гравець поза радіусом атаки):
        else {
            // 4.1) Переслідування
            if (dist2 <= CHASE_RANGE * CHASE_RANGE) {
                float dir = Math.signum(dx);
                physics.setVelocityX(dir * MOVE_SPEED);
                facingRight = dir > 0;
            }
            // 4.2) Патруль
            else {
                physics.setVelocityX(patrolDir * PATROL_SPEED);
                if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
                if (b.x < patrolStartX - PATROL_RADIUS) patrolDir =  1f;
                facingRight = patrolDir > 0;
            }
        }

        // 5) Разове нанесення шкоди
        spearWeapon.applyDamage(player);

        // 6) Step-up
        if (physics.getVelocityX() != 0f) {
            physics.tryStepUp(platforms, physics.getVelocityX() > 0f);
        }

        // 7) Фізика та aiMove()
        super.update(delta, player, platforms);
    }


    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b  = getBounds();
        float     cx = b.x + b.width/2f;

        // центр гравця
        Rectangle pb = player.getBounds();
        float px = pb.x + pb.width/2f;
        float dx = px - cx;
        float dist2 = dx*dx;

        // **Якщо гравець в радіусі атаки — завжди зупиняємося і не рухаємось**
        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        // 2) Переслідування, якщо близько
        if (dist2 <= CHASE_RANGE*CHASE_RANGE) {
            float dir = Math.signum(dx);
            physics.setVelocityX(dir * MOVE_SPEED);
            facingRight = dir > 0;
        }
        // 3) Інакше — патруль
        else {
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolStartX - PATROL_RADIUS) patrolDir =  1f;
            facingRight = patrolDir > 0;
        }

        // 4) «Step-up»: якщо перед нами невисока стіна та під ногами є ґрунт
        boolean onGround = physics.getVelocityY() == 0f;

        // знаходимо стіну попереду
        float probeX = facingRight
            ? b.x + b.width  + 2f
            : b.x - 2f;
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

        // знаходимо, чи є ґрунт прямо перед ніг
        float footX = b.x + b.width/2f + (facingRight ? 6f : -6f);
        Rectangle footProbe = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = false;
        for (Rectangle p : platforms) {
            if (footProbe.overlaps(p)) {
                groundAhead = true;
                break;
            }
        }

        if (wallAhead || !groundAhead) {
            patrolDir = -patrolDir;            // змінюємо напрям патрулю
            facingRight = patrolDir > 0;       // оновлюємо фейсінг
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;                            // не продовжуємо далі в цьому кадрі
        }
    }



    @Override
    public void renderHitbox(ShapeRenderer r) {
        super.renderHitbox(r);
        Rectangle hb = spearWeapon.getHitbox();
        if (hb != null) {
            r.setColor(0f, 0.8f, 0.8f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
    }
}
