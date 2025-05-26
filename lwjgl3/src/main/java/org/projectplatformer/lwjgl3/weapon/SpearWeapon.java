package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.List;

/**
 * Спис: управляє прямою «уколом» вперед та поступовим висуванням,
 * а також кулдауном і хітбоксом.
 */
public class SpearWeapon implements Weapon {
    private final float maxLength;     // до якої довжини висувається спис
    private final float width;         // товщина (ширина) хітбоксу
    private final float duration;      // скільки триває анімація (секунд)
    private final float cooldown;      // час між ударами
    private final int   damage;        // шкода за один укол

    private float timer         = 0f;
    private float cooldownTimer = 0f;
    private boolean damageDone  = false;

    private final Rectangle hitbox = new Rectangle();

    // останні дані для грамотного трасування
    private float   pivotX, pivotY;
    private boolean facingRight;

    /**
     * @param maxLength  макс. довжина висування списа
     * @param width      товщина хітбоксу
     * @param duration   тривалість анімації в секундах
     * @param cooldown   кулдаун між ударами в секундах
     * @param damage     шкода одного уколу
     */
    public SpearWeapon(float maxLength, float width, float duration, float cooldown, int damage) {
        this.maxLength = maxLength;
        this.width     = width;
        this.duration  = duration;
        this.cooldown  = cooldown;
        this.damage    = damage;
    }

    /** Оновлює таймери та (якщо атакуємо) будує хітбокс */
    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        // зменшуємо таймери
        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        if (timer > 0f) {
            // прогрес анімації 0→1
            float t = 1f - (timer / duration);
            // лінійно висуваємо спис вперед від 0 до maxLength
            float lengthNow = t * maxLength;
            // положення бази хітбоксу (центру списа)
            float cx = pivotX + (facingRight ? lengthNow : -lengthNow);
            float cy = pivotY;
            // хітбокс — прямокутник width×width навколо точки (cx, cy)
            hitbox.set(
                cx - (width/2f),
                cy - (width/2f),
                width,
                width
            );
        } else {
            // атака завершена — «ховаємо» хітбокс
            hitbox.set(0,0,0,0);
        }
    }

    /** Якщо атакуємо (timer>0) — повертає хітбокс, інакше null */
    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    /** Починаємо атаку, якщо кулдаун сплив */
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        this.pivotX        = pivotX;
        this.pivotY        = pivotY;
        this.facingRight   = facingRight;
        this.timer         = duration;
        this.cooldownTimer = cooldown;
        this.damageDone    = false;
        // «сховаємо» старий хітбокс до першого апдейту
        hitbox.set(0,0,0,0);
    }

    /** Шкода ворогам — лише один раз за удар */
    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        if (timer <= 0f || damageDone) return;
        for (BaseEnemy e : enemies) {
            if (e.isAlive() && hitbox.overlaps(e.getBounds())) {
                e.takeDamage(damage);
                damageDone = true;
                break;
            }
        }
    }

    /** Шкода гравцю — лише один раз за удар */
    @Override
    public void applyDamage(Player player) {
        if (timer <= 0f || damageDone) return;
        if (hitbox.overlaps(player.getBounds())) {
            player.takeDamage(damage);
            damageDone = true;
        }
    }

    /** Ваш інтерфейс Weapon не мав цього методу, але вам потрібен доступ до кулдауну */
    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    public boolean isAttacking() {
        return timer > 0f;
    }

    /** Додатково — щоб малювати трасу руху (якщо треба) */
    public void renderTrajectory(ShapeRenderer r) {
        if (timer <= 0f) return;
        r.setColor(0f,1f,1f,1f);
        float angle = facingRight ? 0f : 180f;
        r.arc(pivotX, pivotY, maxLength, angle - 10f, 20f);
    }
}
