package org.projectplatformer.weapon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Лук: стріли летять по простій балістичній траєкторії.
 */
public class BowWeapon implements Weapon {
    // час між пострілами
    private final float cooldown;
    private float cooldownTimer = 0f;

    // початкова швидкість стріли (px/s)
    private final float initialSpeed;
    // кут випуску в градусах (0 — горизонтально, +90 — вгору)
    private final float launchAngleDeg;
    // гравітація (px/s²)
    private static final float GRAVITY = 800f;

    // розміри та дальність
    private final float arrowW, arrowH;
    private final float maxRange;

    // шкода однієї стріли
    private final int damage;

    // активні стріли
    private final List<Projectile> arrows = new ArrayList<>();

    private static class Projectile {
        Rectangle hitbox;
        float     vx, vy;
        float     travelled = 0f;
        Projectile(Rectangle hb, float vx, float vy) {
            this.hitbox = hb;
            this.vx = vx;
            this.vy = vy;
        }
    }

    /**
     * @param cooldown       час між пострілами
     * @param speed          початкова швидкість стріли (px/s)
     * @param angleDeg       кут підйому в градусах
     * @param w              ширина стріли
     * @param h              висота стріли
     * @param maxRange       ліміт пройденої відстані (px)
     * @param damage         шкода при попаданні
     */
    public BowWeapon(float cooldown,
                     float speed,
                     float angleDeg,
                     float w,
                     float h,
                     float maxRange,
                     int   damage)
    {
        this.cooldown       = cooldown;
        this.initialSpeed   = speed;
        this.launchAngleDeg = angleDeg;
        this.arrowW         = w;
        this.arrowH         = h;
        this.maxRange       = maxRange;
        this.damage         = damage;
    }

    /** Стандартний лук */
    public BowWeapon() {
        this(0.8f,    // кулдаун
            500f,    // швидкість
            40f,     // кут підйому
            20f,     // ширина
            6f,      // висота
            800f,    // maxRange
            25       // damage
        );
    }

    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        cooldownTimer = cooldown;

        // розрахунок Vx, Vy
        double ang = Math.toRadians(launchAngleDeg);
        float vx = (float)(Math.cos(ang) * initialSpeed) * (facingRight ? +1 : -1);
        float vy = (float)(Math.sin(ang) * initialSpeed);

        // початковий hitbox
        float x = facingRight ? pivotX : pivotX - arrowW;
        float y = pivotY - arrowH/2f;
        Rectangle hb = new Rectangle(x, y, arrowW, arrowH);

        arrows.add(new Projectile(hb, vx, vy));
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        // оновлюємо кулдаун
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        // рухаємо всі стріли по балістичній траєкторії
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();

            // інтегруємо швидкості
            p.vy -= GRAVITY * delta;
            float dx = p.vx * delta;
            float dy = p.vy * delta;

            p.hitbox.x += dx;
            p.hitbox.y += dy;
            p.travelled += Math.sqrt(dx*dx + dy*dy);

            // знищуємо, якщо перелетіла ліміт або впала нижче екрану
            if (p.travelled >= maxRange || p.hitbox.y + p.hitbox.height < 0) {
                it.remove();
            }
        }
    }

    @Override
    public Rectangle getHitbox() {
        // у лука немає одного хітбоксу
        return null;
    }

    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            for (BaseEnemy e : enemies) {
                if (e.isAlive() && p.hitbox.overlaps(e.getBounds())) {
                    e.takeDamage(damage);
                    it.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void applyDamage(Player player) {
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (player.isAlive() && p.hitbox.overlaps(player.getBounds())) {
                player.takeDamage(damage);
                it.remove();
                break;
            }
        }
    }

    @Override
    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    /** Дебаг-рендеринг усіх стріл */
    public void renderProjectiles(ShapeRenderer r) {
        r.setColor(0f, 1f, 0f, 1f);
        for (Projectile p : arrows) {
            Rectangle hb = p.hitbox;
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }
}
