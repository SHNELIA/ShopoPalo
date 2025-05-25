package org.projectplatformer.weapon;

import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.player.Player;

import java.util.HashSet;
import java.util.List;

/**
 * Спис: пряма колюча атака вперед із поступовим висуванням.
 */
public class SpearWeapon implements Weapon {
    private static final float MAX_LENGTH       = 80f;
    private static final float WIDTH            =   8f;
    private static final float DEFAULT_DURATION =   0.4f;
    private static final float DEFAULT_COOLDOWN =   1.0f;
    public  static final int   DAMAGE           =  35;

    // attack & cooldown timers
    private float timer           = 0f;
    private float cooldownTimer   = 0f;
    private final float duration;
    private final float cooldown;

    // keeps track of who we've already hit in this thrust
    private final HashSet<BaseEnemy> damagedThisThrust = new HashSet<>();

    // the current hitbox
    private final Rectangle hitbox = new Rectangle();

    // last pivot & facing for hitbox recomputation
    private float   pivotX, pivotY;
    private boolean facingRight;

    /** default ctor */
    public SpearWeapon() {
        this(DEFAULT_DURATION, DEFAULT_COOLDOWN);
    }

    /** ctor with custom timing */
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

        // build initial hitbox (length = 0 at start)
        hitbox.set(pivotX, pivotY - WIDTH/2f, 0f, WIDTH);
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        // tick timers
        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        // if we're still in the attack window, grow the spear outwards
        if (timer > 0f) {
            float progress = 1f - (timer / duration);  // 0 → 1 over the swing
            float length   = MAX_LENGTH * progress;

            float x = facingRight
                ? pivotX
                : pivotX - length;
            float y = pivotY - WIDTH/2f;
            hitbox.set(x, y, length, WIDTH);
        } else {
            // attack finished: clear hitbox
            hitbox.set(0,0,0,0);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        if (timer <= 0f) return;

        // only damage each enemy once per thrust
        for (BaseEnemy e : enemies) {
            if (e.isAlive() &&
                hitbox.overlaps(e.getBounds()) &&
                damagedThisThrust.add(e))
            {
                e.takeDamage(DAMAGE);
            }
        }
    }

    @Override
    public void applyDamage(Player player) {
        // typically unused for the player's own weapon,
        // but we provide it to satisfy the interface:
        if (timer > 0f && hitbox.overlaps(player.getBounds())) {
            player.takeDamage(DAMAGE);
        }
    }

    @Override
    public float getCooldownRemaining() {
        return cooldownTimer;
    }
}
