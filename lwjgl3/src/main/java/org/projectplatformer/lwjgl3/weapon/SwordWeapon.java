package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

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

    // this is the little square that actually deals damage
    private final Rectangle hitbox = new Rectangle();

    // pivot + facing, stored so we can both update hitbox and render the arc
    private float   pivotX, pivotY;
    private boolean facingRight;

    // ensure we only damage once per swing
    private boolean damageDone;

    // sweep parameters:
    private static final float START_ANGLE =  90f;
    private static final float SWEEP_DELTA = -135f;   // from 90° down to -45°
    private static final float RADIUS      =  40f;   // length of the swing
    private static final float SIZE        =  20f;   // square hitbox size

    /** full‐args ctor */
    public SwordWeapon(float duration, float cooldown, int damage) {
        this.duration = duration;
        this.cooldown = cooldown;
        this.damage   = damage;
    }

    /** default values */
    public SwordWeapon() {
        this(0.35f, 0.2f, 20);
    }

    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;

        // reset timers & state
        this.timer         = duration;
        this.cooldownTimer = cooldown;
        this.pivotX        = pivotX;
        this.pivotY        = pivotY;
        this.facingRight   = facingRight;
        this.damageDone    = false;

        // initialize hitbox off‐screen until first update()
        hitbox.set(0,0,0,0);
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        // save for both hitbox recompute and arc rendering
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        // tick timers
        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        if (timer > 0f) {
            // прогрес від 0 до 1
            float t = 1f - (timer / duration);
            // обчислюємо кут слеша
            float angle = START_ANGLE + SWEEP_DELTA * t;
            // дзеркалимо, якщо дивимося вліво
            if (!facingRight) angle = 180f - angle;
            float rad = angle * MathUtils.degRad;

            // розташовуємо квадрат hitbox на відстані RADIUS
            float cx = pivotX + MathUtils.cos(rad) * RADIUS;
            float cy = pivotY + MathUtils.sin(rad) * RADIUS;
            hitbox.set(
                cx - SIZE/2f,
                cy - SIZE/2f,
                SIZE, SIZE
            );
        } else {
            // завершили атаку — ховаємо hitbox
            hitbox.set(0, 0, 0, 0);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

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

    @Override
    public void applyDamage(Player player) {
        if (timer <= 0f || damageDone) return;
        if (hitbox.overlaps(player.getBounds())) {
            player.takeDamage(damage);
            damageDone = true;
        }
    }

    @Override
    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    /**
     * Draws the debug arc from 90°→−45° around the pivot.
     */
    public void renderTrajectory(ShapeRenderer r) {
        if (timer <= 0f) return;
        r.setColor(1f, 1f, 0f, 1f);
        if (facingRight) {
            r.arc(pivotX, pivotY, RADIUS, START_ANGLE, SWEEP_DELTA);
        } else {
            r.arc(pivotX, pivotY, RADIUS, START_ANGLE, -SWEEP_DELTA);
        }
    }

    public boolean isAttacking() {
        return false;
    }
}
