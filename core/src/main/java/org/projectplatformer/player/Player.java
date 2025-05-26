package org.projectplatformer.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import org.projectplatformer.animations.AnimationManager;
import org.projectplatformer.animations.AnimationManager.State;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.physics.PhysicsComponent;
import org.projectplatformer.weapon.BowWeapon;
import org.projectplatformer.weapon.SpearWeapon;
import org.projectplatformer.weapon.SwordWeapon;
import org.projectplatformer.weapon.Weapon;


import java.util.List;

public class Player {
    private final PhysicsComponent physics;
    private final AnimationManager animationManager;
    private Weapon currentWeapon;

    // Межі світу
    private float worldWidth = Float.MAX_VALUE;
    private float worldHeight = Float.MAX_VALUE;
    public void setWorldBounds(float w, float h) {
        this.worldWidth = w;
        this.worldHeight = h;
    }

    // Рух
    private static final float MOVE_SPEED   = 100f;
    private static final float JUMP_SPEED   = 350f;
    private static final int   MAX_JUMPS    = 2;

    // Wall-jump параметри
    private static final float WALL_THRESHOLD = 5f;
    private static final float WALL_JUMP_UP   = 100f;
    private static final float WALL_JUMP_PUSH = 200f;

    // Dash
    private static final float DASH_SPEED = 400f;
    private static final float DASH_TIME = 0.15f;
    private static final float DASH_COOLDOWN = 1.0f;
    private static final float DOUBLE_TAP_TIME = 0.25f;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float lastLeftTapTime = -1f;
    private float lastRightTapTime = -1f;
    private int dashDirection = 0;

    // Атака
    private boolean attacking = false;
    private float attackTimer = 0f;
    private float attackCooldown = 0f;
    private static final float ATTACK_DURATION = 0.2f;
    private static final float ATTACK_COOLDOWN = 0.5f;

    // Отримання шкоди
    private float damageCooldown = 0f;
    private static final float DAMAGE_COOLDOWN = 1f;

    // Збір монетки
    private boolean coinCollect = false;
    private float collectTimer = 0f;
    private static final float COINCOLLECT_DURATION = 0.3f;

    // Інші стани
    private boolean facingRight = true;
    private boolean isAlive = true;
    private int health = 100;
    private final int maxHealth = 100;

    private int     coins       = 0;
    private int     jumpCount   = 0;
    private static final float WALL_SLIDE_SPEED = 50f;

    public Player(float x, float y) {
        Rectangle bounds = new Rectangle(x, y, 32, 64);
        physics = new PhysicsComponent(
            bounds,

            -1000f,  // gravity
            -1000f,  // maxFallSpeed
            0.9f,    // drag
            16f,     // maxStepHeight
            200f     // stepUpSpeed

        );
        animationManager = new AnimationManager();
        currentWeapon = new SwordWeapon();
    }

    public void update(float delta, List<Rectangle> platforms, List<BaseEnemy> enemies) {
        // 1) Якщо вже мертвий — тільки відграємо анімацію смерті й ніде більше не ліземо
        if (!isAlive) {
            animationManager.update(delta, State.DEFEAT, facingRight);
            return;
        }

        // 2) Оновлення таймерів
        damageCooldown    = Math.max(0f, damageCooldown - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        attackCooldown    = Math.max(0f, attackCooldown - delta);
        if (attacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) attacking = false;
        }
        float now = TimeUtils.nanoTime() / 1e9f;

        Rectangle b  = physics.getBounds();
        float    velY = physics.getVelocityY();

        // --- Dash і базовий рух ---
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
            } else {
                physics.setVelocityX(0f);
            }
        }

        // --- Wall-slide / jump / climbing ---
        boolean touchingWall = false, wallOnLeft = false, wallOnRight = false;
        if (velY < 0f) {
            for (Rectangle p : platforms) {
                // перевірка вертикального перекриття
                boolean verticalOverlap =
                    b.y < p.y + p.height &&
                        b.y + b.height > p.y;
                if (!verticalOverlap) continue;

                float dxL = b.x - (p.x + p.width);
                float dxR = p.x - (b.x + b.width);
                if (Math.abs(dxL) < WALL_THRESHOLD)
                    touchingWall = wallOnLeft = true;
                if (Math.abs(dxR) < WALL_THRESHOLD)
                    touchingWall = wallOnRight = true;
            }
        }
        boolean sliding = touchingWall && velY < 0f;
        boolean justWallJumped = false;
        // стрибок / wall-jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (sliding) {
                physics.setVelocityY(JUMP_SPEED + WALL_JUMP_UP);
                jumpCount = MAX_JUMPS;
                physics.setVelocityX(
                    wallOnRight ? -WALL_JUMP_PUSH : WALL_JUMP_PUSH
                );
                facingRight = !wallOnRight;
                justWallJumped = true;
            } else if (jumpCount < MAX_JUMPS) {
                physics.setVelocityY(JUMP_SPEED);
                jumpCount++;
            }
        }

        if (physics.getVelocityY() == 0f) {
            jumpCount = 0;
        }

        // карабкання по стіні (wall-climb)
        if (sliding && !justWallJumped) {
            // тримаємо A, коли стіна зліва, або D, коли стіна справа
            boolean holdWallKey =
                (wallOnLeft  && Gdx.input.isKeyPressed(Input.Keys.A)) ||
                    (wallOnRight && Gdx.input.isKeyPressed(Input.Keys.D));

            if (holdWallKey) {
                // тільки тоді сповзаємо
                physics.startClimbing(-WALL_SLIDE_SPEED);
            } else {
                physics.stopClimbing();
            }
        } else {
            physics.stopClimbing();
        }

        // Збір монетки
        if (coinCollect) {
            collectTimer -= delta;
            if (collectTimer <= 0f) coinCollect = false;
        }

        // --- Physics Update ---
        physics.tryStepUp(platforms, physics.getVelocityX() >= 0f);
        physics.update(delta, platforms);

        // --- Зброя і атака ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            currentWeapon = new SwordWeapon();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            currentWeapon = new SpearWeapon(
                65f,   // maxLength
                15f,    // width
                0.3f,  // duration
                1.0f,  // cooldown
                30      // damage
            );
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            currentWeapon = new BowWeapon();
        }

        // --- Physics Update ---
        physics.tryStepUp(platforms, physics.getVelocityX() >= 0f);
        physics.update(delta, platforms);

// --- Block entering enemy hitboxes ---
        for (BaseEnemy e : enemies) {
            if (!e.isAlive()) continue;
            Rectangle eb = e.getBounds();
            Rectangle pb = physics.getBounds();
            if (pb.overlaps(eb)) {
                // обчислити глибину перекриття по X та Y
                float overlapX = Math.min(pb.x + pb.width,  eb.x + eb.width)
                    - Math.max(pb.x,               eb.x);
                float overlapY = Math.min(pb.y + pb.height, eb.y + eb.height)
                    - Math.max(pb.y,               eb.y);

                // рухаємося найкоротшим шляхом назовні
                if (overlapX < overlapY) {
                    // відштовхуємося по X
                    if (pb.x < eb.x) {
                        pb.x -= overlapX;
                    } else {
                        pb.x += overlapX;
                    }
                    physics.setVelocityX(0f);
                } else {
                    // відштовхуємося по Y
                    if (pb.y < eb.y) {
                        pb.y -= overlapY;
                    } else {
                        pb.y += overlapY;
                    }
                    physics.setVelocityY(0f);
                }
            }
        }


// --- Attack trigger ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            float pivotX = b.x + b.width/2f;
            float pivotY = b.y + (currentWeapon instanceof SpearWeapon
                ? b.height / 2f
                : b.height * 0.7f);
            currentWeapon.startAttack(pivotX, pivotY, facingRight);
            attacking      = true;
            attackTimer    = ATTACK_DURATION;
            attackCooldown = ATTACK_COOLDOWN;
        }
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + (currentWeapon instanceof SpearWeapon
            ? b.height / 2f
            : b.height * 0.7f);
        currentWeapon.update(delta, pivotX, pivotY, facingRight);
        currentWeapon.applyDamage(enemies);

        // --- Визначаємо найвищий пріоритет стан анімації ---
        State newState;
        if (coinCollect) {
            newState = State.COINCOLLECT; // <-- Ось ця гілка
        } else if (sliding) {
                newState = State.SLIDING;
        } else if (attacking) {
            newState = currentWeapon instanceof SpearWeapon
                ? State.ATTACKSPEAR
                : State.ATTACKSWORD;
        } else if (physics.getVelocityY() != 0f) {
            newState = State.JUMP;
        } else if (physics.getVelocityX() != 0f) {
            newState = State.WALK;
        } else {
            newState = State.IDLE;
        }
        animationManager.update(delta, newState, facingRight);

        // --- Межі світу —
        b.x = MathUtils.clamp(b.x, 0f, worldWidth  - b.width);
        b.y = Math.min(b.y, worldHeight - b.height);
    }


    public void render(SpriteBatch batch) {
        Rectangle b = physics.getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();
        batch.draw(frame, b.x, b.y, b.width, b.height);
    }

    public void renderHitbox(ShapeRenderer r) {
        r.setColor(1f, 0f, 0f, 1f);
        Rectangle b = physics.getBounds();
        r.rect(b.x, b.y, b.width, b.height);
        Rectangle hb = currentWeapon.getHitbox();
        if (hb != null) {
            r.setColor(0f,1f,0f,1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
// якщо це лук — малюємо всі стріли:
        if (currentWeapon instanceof BowWeapon) {
            ((BowWeapon)currentWeapon).renderProjectiles(r);
        }

    }

    // Геттери та утиліти
    public boolean isAlive() { return isAlive; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getCoins() { return coins; }
    public Rectangle getBounds() { return physics.getBounds(); }
    public void addCoin() { coins++; }

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

    public void coinCollectAnimation() {
        coinCollect = true;
        collectTimer = COINCOLLECT_DURATION;
    }

    public void dispose() {
        animationManager.dispose();
    }
}

