package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Лук: стріли летять по простій балістичній траєкторії,
 * малюються як спрайт та зникають при попаданні у ворога чи платформу.
 */
public class BowWeapon implements Weapon {
    // --- Основні поля ---
    private final float cooldown;
    private float cooldownTimer = 0f;
    private final float initialSpeed;
    private final float launchAngleDeg;
    private static final float GRAVITY = 800f;
    private final float arrowW, arrowH;
    private final float maxRange;
    private final int damage;

    // --- Платформи для перевірки колізії ---
    private List<Rectangle> worldPlatforms = null;
    public void setPlatforms(List<Rectangle> platforms) {
        this.worldPlatforms = platforms;
    }

    // --- Текстура стріли ---
    private static final Texture arrowTexture = new Texture("arrow.png");

    // --- Активні стріли ---
    private final List<Projectile> arrows = new ArrayList<>();
    private static class Projectile {
        Rectangle hitbox;
        float     vx, vy;
        float     travelled = 0f;
        boolean   facingRight;
        Projectile(Rectangle hb, float vx, float vy, boolean facingRight) {
            this.hitbox = hb;
            this.vx = vx;
            this.vy = vy;
            this.facingRight = facingRight;
        }
    }

    // --- Конструктори ---
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

    // --- Для BowWeapon стріла створюється окремо! ---
    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        // Не використовується (стріла спавниться через releaseArrow у Player)
    }

    /** Викликається з Player у певний момент анімації */
    public void releaseArrow(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        cooldownTimer = cooldown;

        double ang = Math.toRadians(launchAngleDeg);
        float vx = (float)(Math.cos(ang) * initialSpeed) * (facingRight ? +1 : -1);
        float vy = (float)(Math.sin(ang) * initialSpeed);

        float x = facingRight ? pivotX : pivotX - arrowW;
        float y = pivotY - arrowH/2f;
        Rectangle hb = new Rectangle(x, y, arrowW, arrowH);

        arrows.add(new Projectile(hb, vx, vy, facingRight));
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.vy -= GRAVITY * delta;
            float dx = p.vx * delta;
            float dy = p.vy * delta;

            p.hitbox.x += dx;
            p.hitbox.y += dy;
            p.travelled += Math.sqrt(dx*dx + dy*dy);

            // --- ЗНИКНЕННЯ СТРІЛИ ПРИ ПОПАДАННІ В ПЛАТФОРМУ ---
            boolean touchedPlatform = false;
            if (worldPlatforms != null) {
                for (Rectangle plat : worldPlatforms) {
                    if (p.hitbox.overlaps(plat)) {
                        touchedPlatform = true;
                        break;
                    }
                }
            }
            // Видаляємо, якщо зіткнулась з платформою, вилетіла за межу, чи нижче екрану
            if (touchedPlatform || p.travelled >= maxRange || p.hitbox.y + p.hitbox.height < 0) {
                it.remove();
            }
        }
    }

    @Override
    public Rectangle getHitbox() {
        return arrows.isEmpty() ? null : arrows.get(0).hitbox;
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

    /** Малює всі стріли як спрайти (SpriteBatch) */
    public void renderProjectiles(SpriteBatch batch) {
        for (Projectile p : arrows) {
            float drawX = p.hitbox.x;
            float drawY = p.hitbox.y;
            float drawW = p.hitbox.width;
            float drawH = p.hitbox.height;
            if (p.facingRight) {
                batch.draw(arrowTexture, drawX, drawY, drawW, drawH);
            } else {
                batch.draw(arrowTexture, drawX + drawW, drawY, -drawW, drawH);
            }
        }
    }

    /** Для дебагу (опційно) */
    public void renderProjectiles(ShapeRenderer r) {
        r.setColor(0f, 1f, 0f, 1f);
        for (Projectile p : arrows) {
            Rectangle hb = p.hitbox;
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }
}
