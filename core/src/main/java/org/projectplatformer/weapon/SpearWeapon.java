package org.projectplatformer.weapon;

import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.player.Player;

import java.util.HashSet;
import java.util.List;

/**
 * Спис: пряма колюча атака вперед із поступовим висуванням
 */
public class SpearWeapon implements Weapon {
    private static final float MAX_LENGTH       = 100f;
    private static final float WIDTH            =   8f;
    private static final float DEFAULT_DURATION =   0.3f;
    private static final float DEFAULT_COOLDOWN =   1.0f;
    public  static final  int   DAMAGE          =  30;

    private float timer         = 0f;
    private float cooldownTimer = 0f;
    private final float duration;
    private final float cooldown;

    private final Rectangle hitbox = new Rectangle();
    private final HashSet<BaseEnemy> damagedThisThrust = new HashSet<>();

    private float pivotX, pivotY;
    private boolean facingRight;

    public SpearWeapon() {
        this(DEFAULT_DURATION, DEFAULT_COOLDOWN);
    }

    public SpearWeapon(float duration, float cooldown) {
        this.duration = duration;
        this.cooldown = cooldown;
    }

    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        this.timer         = duration;
        this.cooldownTimer = cooldown;
        this.pivotX        = pivotX;
        this.pivotY        = pivotY;
        this.facingRight   = facingRight;
        damagedThisThrust.clear();
        computeHitbox(0f);
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        if (timer > 0f) {
            float progress = 1f - (timer / duration);
            computeHitbox(progress);
        }
    }

    protected void computeHitbox(float progress) {
        float length = MAX_LENGTH * progress;
        float x = facingRight
            ? pivotX
            : pivotX - length;
        float y = pivotY - WIDTH * 0.5f;
        hitbox.set(x, y, length, WIDTH);
    }

    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        if (timer <= 0f) return;
        Rectangle hb = hitbox;
        for (BaseEnemy e : enemies) {
            if (e.isAlive() && hb.overlaps(e.getBounds()) && damagedThisThrust.add(e)) {
                e.takeDamage(DAMAGE);
            }
        }
    }

    @Override
    public void applyDamage(Player player) {
        // Spear не вражає гравця
    }
}
