package org.projectplatformer.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.physics.PhysicsComponent;
import org.projectplatformer.weapon.SpearWeapon;
import org.projectplatformer.weapon.SwordWeapon;
import org.projectplatformer.weapon.Weapon;

import java.util.List;

/**
 * Гравець: рух, стрибки, dash, wall-slide/jump/karabkannia,
 * вибір зброї, атака, збір монет, отримання шкоди.
 */
public class Player {
    private final PhysicsComponent physics;
    private final Texture          texture;
    private Weapon                 currentWeapon;

    // Межі світу
    private float worldWidth  = Float.MAX_VALUE;
    private float worldHeight = Float.MAX_VALUE;
    public void setWorldBounds(float w, float h) {
        this.worldWidth = w;
        this.worldHeight = h;
    }

    // Рух
    private static final float MOVE_SPEED   = 200f;
    private static final float JUMP_SPEED   = 600f;
    private static final int   MAX_JUMPS    = 2;

    // Wall-jump параметри
    private static final float WALL_THRESHOLD = 5f;
    private static final float WALL_JUMP_UP   = 200f;
    private static final float WALL_JUMP_PUSH = 500f;

    // Dash
    private static final float DASH_SPEED      = 400f;
    private static final float DASH_TIME       = 0.15f;
    private static final float DASH_COOLDOWN   = 1.0f;
    private static final float DOUBLE_TAP_TIME = 0.25f;
    private float dashTimer         = 0f;
    private float dashCooldownTimer = 0f;
    private float lastLeftTapTime   = -1f;
    private float lastRightTapTime  = -1f;
    private int   dashDirection     = 0;

    // Атака
    private boolean attacking      = false;
    private float   attackTimer    = 0f;
    private float   attackCooldown = 0f;
    private static final float ATTACK_DURATION = 0.2f;
    private static final float ATTACK_COOLDOWN = 0.5f;
    private static final float ATTACK_RANGE    = 40f;
    private static final int   ATTACK_DAMAGE   = 25;

    // Отримання шкоди
    private float damageCooldown = 0f;
    private static final float DAMAGE_COOLDOWN = 1f;

    // Інші стани
    private boolean facingRight = true;
    private boolean isAlive     = true;
    private int     health      = 100;
    private final int maxHealth = 100;
    private int     coins       = 0;
    private int     jumpCount   = 0;

    public Player(float x, float y) {
        Rectangle bounds = new Rectangle(x, y, 33, 52);
        physics = new PhysicsComponent(
            bounds,
            -3000f,  // gravity
            -1000f,  // maxFallSpeed
            0.9f,    // drag
            16f,     // maxStepHeight
            200f     // stepUpSpeed
        );
        texture = new Texture("Prince.png");
        currentWeapon = new SwordWeapon();
    }

    public void update(float delta, List<Rectangle> platforms, List<BaseEnemy> enemies) {
        if (!isAlive) return;

        // Оновлення таймерів
        damageCooldown    = Math.max(0f, damageCooldown - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        attackCooldown    = Math.max(0f, attackCooldown - delta);
        if (attacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) attacking = false;
        }
        float now = TimeUtils.nanoTime()/1e9f;

        Rectangle b = physics.getBounds();

        float velY = physics.getVelocityY();

        // --- Dash ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            if (now - lastLeftTapTime <= DOUBLE_TAP_TIME && dashCooldownTimer <= 0f) {
                dashTimer = DASH_TIME;
                dashCooldownTimer = DASH_COOLDOWN;
                dashDirection = -1;
            }
            lastLeftTapTime = now;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (now - lastRightTapTime <= DOUBLE_TAP_TIME && dashCooldownTimer <= 0f) {
                dashTimer = DASH_TIME;
                dashCooldownTimer = DASH_COOLDOWN;
                dashDirection = 1;
            }
            lastRightTapTime = now;
        }
        if (dashTimer > 0f) {
            physics.setVelocityX(dashDirection * DASH_SPEED);
            dashTimer -= delta;
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                physics.setVelocityX(-MOVE_SPEED);
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                physics.setVelocityX(MOVE_SPEED);
                facingRight = true;
            }
        }

        // --- Wall-slide/jump/karabkannia ---
        boolean touchingWall = false, wallOnLeft = false, wallOnRight = false;
        if (velY < 0f) {
            for (Rectangle p : platforms) {
                if (!b.overlaps(p)) continue;
                float dxL = b.x - (p.x + p.width);
                float dxR = p.x - (b.x + b.width);
                if (Math.abs(dxL) < WALL_THRESHOLD) touchingWall = wallOnLeft = true;
                if (Math.abs(dxR) < WALL_THRESHOLD) touchingWall = wallOnRight = true;
            }
        }
        boolean sliding = touchingWall && velY < 0f;
        boolean justWallJumped = false;

        Rectangle headProbe = new Rectangle(b.x, b.y + b.height, b.width, 2f);
        boolean ceilingAbove = platforms.stream().anyMatch(p -> headProbe.overlaps(p));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (sliding) {
                physics.setVelocityY(JUMP_SPEED + WALL_JUMP_UP);
                jumpCount = MAX_JUMPS;
                physics.setVelocityX(wallOnRight ? -WALL_JUMP_PUSH : WALL_JUMP_PUSH);
                facingRight = !wallOnRight;
                justWallJumped = true;
            } else if (jumpCount < MAX_JUMPS && !ceilingAbove) {
                physics.setVelocityY(JUMP_SPEED);
                jumpCount++;
            }
        }
        if (physics.getVelocityY() == 0f) jumpCount = 0;

        if (sliding && !justWallJumped) {
            boolean holdWallKey = (wallOnLeft && Gdx.input.isKeyPressed(Input.Keys.A)) ||
                (wallOnRight && Gdx.input.isKeyPressed(Input.Keys.D));
            if (holdWallKey) {
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) physics.startClimbing(100f);
                else                                      physics.startClimbing(0f);
            } else {
                physics.stopClimbing();
            }
        } else {
            physics.stopClimbing();
        }

        // --- Physics Update ---
        physics.tryStepUp(platforms, physics.getVelocityX() >= 0f);
        physics.update(delta, platforms);

// --- Attack trigger ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            float pivotX = b.x + b.width/2f;
            float pivotY = b.y + (currentWeapon instanceof SpearWeapon
                ? b.height/2f
                : b.height * 0.7f);
            currentWeapon.startAttack(pivotX, pivotY, facingRight);
        }

// --- Weapon update & damage ---
        float pivotX = b.x + b.width/2f;
        float pivotY = b.y + (currentWeapon instanceof SpearWeapon
            ? b.height/2f
            : b.height * 0.7f);
        currentWeapon.update(delta, pivotX, pivotY, facingRight);
        currentWeapon.applyDamage(enemies);


        // --- World bounds ---
        b.x = MathUtils.clamp(b.x, 0f, worldWidth - b.width);
        b.y = Math.min(b.y, worldHeight - b.height);
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;
        Rectangle b = physics.getBounds();
        if (facingRight) batch.draw(texture, b.x, b.y, b.width, b.height);
        else             batch.draw(texture, b.x + b.width, b.y, -b.width, b.height);
    }

    public void renderHitbox(ShapeRenderer r) {
        // body hitbox - red
        r.setColor(1f, 0f, 0f, 1f);
        Rectangle b = physics.getBounds();
        r.rect(b.x, b.y, b.width, b.height);
        // weapon hitbox - green
        Rectangle hb = currentWeapon.getHitbox();
        if (hb != null) {
            r.setColor(0f, 1f, 0f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
    }

    // Getters & utility
    public boolean     isAlive()        { return isAlive; }
    public int         getHealth()      { return health; }
    public int         getMaxHealth()   { return maxHealth; }
    public int         getCoins()       { return coins; }
    public Rectangle   getBounds()      { return physics.getBounds(); }

    public void addCoin()  { coins++; }
    public void takeDamage(int dmg) {
        if (!isAlive) return;
        health -= dmg;
        if (health <= 0) isAlive = false;
    }

    public void respawn(float x, float y) {
        Rectangle b = physics.getBounds();
        b.x = x; b.y = y;
        health = maxHealth; isAlive = true;
        physics.setVelocityY(0f);
        jumpCount = 0;
    }

    public void dispose() {
        texture.dispose();
    }
}
