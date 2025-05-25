package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.player.Player;
import org.projectplatformer.weapon.SwordWeapon;
import org.projectplatformer.weapon.Weapon;
import java.util.List;

/**
 * Ворог «Гоблін» з патрулюванням, переслідуванням та атакою мечем.
 */
public class Goblin extends BaseEnemy {
    private static final float PATROL_RADIUS   = 80f;
    private static final float PATROL_SPEED    = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED     = 120f;
    private static final float MELEE_RANGE     = 80f;
    private static final int   ATTACK_DAMAGE   = 16;
    private static final float JUMP_SPEED = 400f;
    private static final float STEP_UP_SPEED = 400f;

    private final Weapon slashWeapon;
    private final float  patrolCenterX;
    private float        patrolDir   = 1f;
    private boolean      facingRight = true;

    public Goblin(float x, float y, Texture texture) {
        super(
            x, y,
            32f, 32f,     // розміри
            texture,
            50,           // початкове здоров’я
            -2000f,        // гравітація
            -1000f,        // макс. швидкість падіння
            0.9f,         // тертя (drag)
            16f, 400f          // макс. висота step-up
        );
        this.slashWeapon   = new SwordWeapon();
        this.patrolCenterX = x;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        // 1) Оновлюємо меч (таймери + хітбокс)
        Rectangle b      = getBounds();
        float     pivotX = b.x + b.width  / 2f;
        float     pivotY = b.y + b.height * 0.7f;
        slashWeapon.update(delta, pivotX, pivotY, facingRight);

        // 2) Атака гравця, якщо хітбокс активний
        Rectangle hb = slashWeapon.getHitbox();
        if (hb != null && hb.overlaps(player.getBounds())) {
            player.takeDamage(ATTACK_DAMAGE);
        }

        // 3) Далі запускаємо базовий update:
        //    – він викликає aiMove() → задає physics.setVelocityX(...)
        //    – обробляє фізику (прискорення, колізії тощо)
        super.update(delta, player, platforms);
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();

        // 0) Перевіряємо: чи під центром гобліна (по осі X) є хоч одна платформа нижче за ноги?
        float belowProbeX = b.x + b.width/2f;
        Rectangle belowProbe = new Rectangle(belowProbeX, 0f, 0.1f, b.y);
        boolean hasGroundBelow = false;
        for (Rectangle p : platforms) {
            // перевіряємо, чи платформа горизонтально накриває нашу X-координату
            // і її верх p.y + p.height лежить строго нижче за b.y (рівень ніг)
            if (belowProbeX >= p.x &&
                belowProbeX <= p.x + p.width &&
                p.y + p.height <= b.y)
            {
                hasGroundBelow = true;
                break;
            }
        }
        if (!hasGroundBelow) {
            // обрив — розвертаємо патруль і йдемо назад
            patrolDir   = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;
        }

        float     px = player.getBounds().x + player.getBounds().width  / 2f;
        float     cx = b.x + b.width  / 2f;
        float     dx = px - cx;
        float     dist2 = dx*dx;

        // 2) Якщо дуже близько — зупинка
        if (dist2 <= MELEE_RANGE * MELEE_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        float moveDir, speed;
        boolean onGround = physics.getVelocityY() == 0f;

        // 3) Визначаємо напрям і швидкість (переслідування vs патруль)
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

        // 4) «Зонд» перед перешкодою і «зонд»низу
        float probeX    = facingRight
            ? b.x + b.width  + 2f
            : b.x - 2f;
        Rectangle probe = new Rectangle(probeX, b.y, 2f, b.height);
        boolean wallAhead = false;
        Rectangle hitPlatform = null;
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                wallAhead    = true;
                hitPlatform  = p;
                break;
            }
        }

        // Позиція під ногами попереду
        float footX       = b.x + b.width/2f + (facingRight ? 6f : -6f);
        Rectangle footP   = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = false;
        for (Rectangle p : platforms) {
            if (footP.overlaps(p)) {
                groundAhead = true;
                break;
            }
        }

        // 5) Якщо перед невеликою «ступенькою» — step-up
        if (wallAhead && onGround && groundAhead) {
            float stepHeight = hitPlatform.y + hitPlatform.height - b.y;
            if (stepHeight <= physics.getMaxStepHeight()) {
                physics.setVelocityY(STEP_UP_SPEED);
            } else {
                // 6) Якщо це висока стіна — перепригнути
                physics.setVelocityY(JUMP_SPEED);
            }
        }

        // 7) Встановлюємо горизонтальну швидкість
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
        super.renderHitbox(r);  // малює bounds та базовий hitbox із BaseEnemy
        Rectangle hb = slashWeapon.getHitbox();
        if (hb != null) {
            r.setColor(1f, 0.5f, 0f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }
}

