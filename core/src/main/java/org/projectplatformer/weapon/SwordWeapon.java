package org.projectplatformer.weapon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.player.Player;

import java.util.List;

/**
 * Меч: управляє таймінгом атаки, кулдауном,
 * хітбоксом та малюванням траєкторії удару.
 */
public class SwordWeapon implements Weapon {
    private final float duration;
    private final float cooldown;
    private final int   damage;

    private float timer         = 0f;
    private float cooldownTimer = 0f;
    private final Rectangle hitbox = new Rectangle();

    // Останні дані для побудови хітбоксу та дуги
    private float   pivotX, pivotY;
    private boolean facingRight;

    /** Конструктор з параметрами атаки */
    public SwordWeapon(float duration, float cooldown, int damage) {
        this.duration = duration;
        this.cooldown = cooldown;
        this.damage   = damage;
    }

    /** Конструктор за замовчуванням */
    public SwordWeapon() {
        this(0.2f, 0.5f, 25);
    }

    /**
     * Запустити атаку: ініціалізувати таймери й хітбокс,
     * зберегти останню точку pivot для дуги.
     */
    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        this.timer         = duration;
        this.cooldownTimer = cooldown;
        this.pivotX        = pivotX;
        this.pivotY        = pivotY;
        this.facingRight   = facingRight;
        // встановлюємо початковий hitbox
        float w = 40f, h = 10f;
        float x = facingRight ? pivotX : pivotX - w;
        float y = pivotY - h/2f;
        hitbox.set(x, y, w, h);
    }

    /**
     * Оновлює таймери та, якщо атака активна,
     * пересуває hitbox у нове положення.
     */
    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        // зберігаємо для дуги
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        if (timer > 0f) {
            float w = hitbox.width, h = hitbox.height;
            float x = facingRight ? pivotX : pivotX - w;
            float y = pivotY - h/2f;
            hitbox.set(x, y, w, h);
        }
    }

    /** Повертає хітбокс атаки або null, якщо атака неактивна. */
    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    /** Наносить шкоду всім ворогам у списку, які в hitbox. */
    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        if (timer <= 0f) return;
        for (BaseEnemy e : enemies) {
            if (e.isAlive() && hitbox.overlaps(e.getBounds())) {
                e.takeDamage(damage);
            }
        }
    }

    /** Наносить шкоду гравцю, якщо він у hitbox (для ворога). */
    @Override
    public void applyDamage(Player player) {
        if (timer > 0f && hitbox.overlaps(player.getBounds())) {
            player.takeDamage(damage);
        }
    }

    /**
     * Малює траєкторію удару — півколо від 90° до -45°
     * навколо точки (pivotX, pivotY), радіус = ширина hitbox.
     */
    public void renderTrajectory(ShapeRenderer r) {
        if (timer <= 0f) return;
        float radius = hitbox.width;
        r.setColor(1f, 1f, 0f, 1f);
        // arc(центрX, центрY, радіус, початковий кут, кутовий пробіг)
        r.arc(pivotX, pivotY, radius, 90f, -135f);
    }
}
