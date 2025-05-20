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

import java.util.List;

/** Гравець: рух, стрибки, даш, атака, життя, монети */
public class Player {
    private final Rectangle bounds;
    private float velocityX = 0f, velocityY = 0f;

    // Границі світу
    private float worldWidth = Float.MAX_VALUE;
    private float worldHeight = Float.MAX_VALUE;
    public void setWorldBounds(float w, float h) {
        this.worldWidth = w;
        this.worldHeight = h;
    }

    // Константи
    private static final float GRAVITY = -3000f;
    private static final float MOVE_SPEED = 200f;
    private static final float JUMP_SPEED = 600f;
    private static final int MAX_JUMPS = 2;
    private static final float WALL_THRESHOLD = 5f;
    private static final float WALL_JUMP_PUSH = 500f;
    private static final float WALL_JUMP_UP = 200f;
    private static final float DASH_SPEED = 400f;
    private static final float DASH_TIME = 0.15f;
    private static final float DASH_COOLDOWN = 1.0f;
    private static final float DOUBLE_TAP_TIME = 0.25f;
    private static final float ATTACK_RANGE = 40f;
    private static final int ATTACK_DAMAGE = 25;
    private static final float ATTACK_DURATION = 0.2f;
    private static final float ATTACK_COOLDOWN = 0.5f;
    private static final float DAMAGE_COOLDOWN = 1f;

    // Стан
    private boolean facingRight = true;
    private boolean isAlive = true;
    private int health = 100;
    private final int maxHealth = 100;
    private int jumpCount = 0;
    private int coins = 0;

    // Таймери
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private int dashDirection = 0;
    private float lastLeftTapTime = -1f;
    private float lastRightTapTime = -1f;
    private boolean attacking = false;
    private float attackTimer = 0f;
    private float attackCooldown = 0f;
    private float damageCooldown = 0f;

    private Texture texture;

    public Player(float x, float y) {
        bounds = new Rectangle(x, y, 33, 52);
        texture = new Texture("Prince.png");
    }

    /** Оновлення стану гравця */
    public void update(float delta, List<Rectangle> platforms, List<BaseEnemy> enemies) {
        if (!isAlive) return;

        // Оновлення таймерів
        damageCooldown = Math.max(0f, damageCooldown - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        attackCooldown = Math.max(0f, attackCooldown - delta);
        if (attacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) attacking = false;
        }

        float now = TimeUtils.nanoTime() / 1e9f;

        // Горизонтальний рух
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bounds.x -= MOVE_SPEED * delta;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bounds.x += MOVE_SPEED * delta;
            facingRight = true;
        }

        // Детекція стіни
        boolean touchingWall = false, wallOnLeft = false, wallOnRight = false;
        if (velocityY < 0) {
            for (Rectangle p : platforms) {
                if (bounds.overlaps(p)) {
                    float dxL = bounds.x - (p.x + p.width);
                    float dxR = p.x - (bounds.x + bounds.width);
                    if (Math.abs(dxL) < WALL_THRESHOLD) touchingWall = wallOnLeft = true;
                    if (Math.abs(dxR) < WALL_THRESHOLD) touchingWall = wallOnRight = true;
                }
            }
        }
        boolean sliding = touchingWall && velocityY < 0;

        // Стрибок / стрибок від стіни
        boolean justWallJumped = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (sliding) {
                velocityY = JUMP_SPEED + WALL_JUMP_UP;
                jumpCount = MAX_JUMPS;
                if (wallOnRight) {
                    velocityX = -WALL_JUMP_PUSH;
                    facingRight = false;
                } else {
                    velocityX = WALL_JUMP_PUSH;
                    facingRight = true;
                }
                justWallJumped = true;
            } else if (jumpCount < MAX_JUMPS) {
                velocityY = JUMP_SPEED;
                jumpCount++;
            }
        }

        // Атака
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && attackCooldown <= 0f) {
            attacking = true;
            attackTimer = ATTACK_DURATION;
            attackCooldown = ATTACK_COOLDOWN;
            float hbY = bounds.y + bounds.height * 0.25f;
            float hbH = bounds.height * 0.5f;
            Rectangle hb = facingRight
                ? new Rectangle(bounds.x + bounds.width, hbY, ATTACK_RANGE, hbH)
                : new Rectangle(bounds.x - ATTACK_RANGE, hbY, ATTACK_RANGE, hbH);
            for (BaseEnemy e : enemies) {
                if (e.isAlive() && hb.overlaps(e.getBounds())) {
                    e.takeDamage(ATTACK_DAMAGE);
                }
            }
        }

        // Даш через подвійне натискання
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

        // Гравітація та ковзання по стіні
        velocityY += GRAVITY * delta;
        if (sliding && !justWallJumped) {
            velocityY = MathUtils.clamp(velocityY, -50f, Float.MAX_VALUE);
        }
        bounds.y += velocityY * delta;

        // Застосування дашу
        if (dashTimer > 0f) {
            bounds.x += dashDirection * DASH_SPEED * delta;
            dashTimer -= delta;
        } else {
            velocityX *= 0.9f;
        }

        // Колізії з платформами
        for (Rectangle p : platforms) {
            if (!bounds.overlaps(p)) continue;
            float pb = bounds.y, pt = bounds.y + bounds.height;
            float pl = bounds.x, pr = bounds.x + bounds.width;
            float ob = pt - p.y, ot = p.y + p.height - pb;
            float ol = pr - p.x, or_ = p.x + p.width - pl;
            boolean fromTop = ot < ob && ot < ol && ot < or_;
            boolean fromBottom = ob < ot && ob < ol && ob < or_;
            boolean fromLeft = ol < or_ && ol < ot && ol < ob;
            boolean fromRight = or_ < ol && or_ < ot && or_ < ob;
            if (fromTop && velocityY <= 0) {
                bounds.y = p.y + p.height;
                velocityY = 0;
                jumpCount = 0;
            } else if (fromBottom && velocityY > 0) {
                bounds.y = p.y - bounds.height;
                velocityY = 0;
            } else if (fromLeft) {
                bounds.x = p.x - bounds.width;
                velocityX = 0;
            } else if (fromRight) {
                bounds.x = p.x + p.width;
                velocityX = 0;
            }
        }

        // Отримання шкоди від ворогів
        boolean touchingEnemy = false;
        for (BaseEnemy e : enemies) {
            if (e.isAlive() && bounds.overlaps(e.getBounds())) {
                touchingEnemy = true;
                if (damageCooldown <= 0f) {
                    takeDamage(10);
                    damageCooldown = DAMAGE_COOLDOWN;
                }
                break;
            }
        }
        if (!touchingEnemy) damageCooldown = 0f;

        // Обмеження в межах світу
        bounds.x = MathUtils.clamp(bounds.x, 0f, worldWidth - bounds.width);
        bounds.y = Math.min(bounds.y, worldHeight - bounds.height);
    }

    /** Малює гравця */
    public void render(SpriteBatch batch) {
        if (!isAlive) return;
        if (facingRight) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(texture, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);
        }
    }

    /** Малює хитбокс (для дебагу) */
    public void renderHitbox(ShapeRenderer r) {
        r.setColor(1, 0, 0, 1);
        r.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // Геттери / ігрова взаємодія
    public boolean isAlive() { return isAlive; }
    public int getCoins() { return coins; }
    public void addCoin() { coins++; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    /** Застосування шкоди */
    public void takeDamage(int dmg) {
        if (!isAlive) return;
        health -= dmg;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }

    /** Відродження гравця */
    public void respawn(float x, float y) {
        bounds.x = x;
        bounds.y = y;
        health = maxHealth;
        isAlive = true;
        velocityY = 0f;
        jumpCount = 0;
    }

    public Rectangle getBounds() { return bounds; }

    /** Вивільнення ресурсів */
    public void dispose() {
        texture.dispose();
    }
}
