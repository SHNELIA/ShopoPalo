package org.projectplatformer.lwjgl3.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.animations.AnimationManager.State;
import org.projectplatformer.lwjgl3.animations.AnimationManager;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.physics.PhysicsComponent;
import org.projectplatformer.lwjgl3.weapon.BowWeapon;
import org.projectplatformer.lwjgl3.weapon.SpearWeapon;
import org.projectplatformer.lwjgl3.weapon.SwordWeapon;
import org.projectplatformer.lwjgl3.weapon.Weapon;

import java.util.List;

public class Player {
    private final PhysicsComponent physics;
    private final AnimationManager animationManager;
    private Weapon currentWeapon;

    private float worldWidth = Float.MAX_VALUE;
    private float worldHeight = Float.MAX_VALUE;
    public void setWorldBounds(float w, float h) {
        this.worldWidth = w;
        this.worldHeight = h;
    }

    // --- Смерть/життя ---
    private boolean isAlive = true;
    private boolean dying = false;
    private float deathTimer = 0f;
    private static final float DEATH_ANIMATION_DELAY = 0.5f; // після DEFEAT перед респавном
    private static final float DEATH_RESPAWN_X = 32f;  // можна змінити під свою карту
    private static final float DEATH_RESPAWN_Y = 100f; // можна змінити під свою карту

    // --- Бойова система ---
    private boolean attacking = false;
    private boolean attackQueued = false;
    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN = 0.5f;
    private boolean hitboxActive = false;
    private boolean arrowReleased = false;

    // --- Переміщення ---
    private static final float MOVE_SPEED   = 100f;
    private static final float JUMP_SPEED   = 350f;
    private static final int   MAX_JUMPS    = 2;

    private static final float WALL_THRESHOLD = 5f;
    private static final float WALL_JUMP_UP   = 100f;
    private static final float WALL_JUMP_PUSH = 200f;
    private static final float WALL_SLIDE_SPEED = 50f;

    private static final float DASH_SPEED = 400f;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private int dashDirection = 0;

    private static final float ATTACK_ACTIVATE_FROM_SWORD = 0.13f;
    private static final float ATTACK_ACTIVATE_TO_SWORD   = 0.28f;
    private static final float ATTACK_ACTIVATE_FROM_SPEAR = 0.12f;
    private static final float ATTACK_ACTIVATE_TO_SPEAR   = 0.35f;
    private static final float BOW_ATTACK_ACTIVATE_FROM = 0.23f;
    private static final float BOW_ATTACK_ACTIVATE_TO   = 0.33f;

    private float damageCooldown = 0f;
    private static final float DAMAGE_COOLDOWN = 1f;

    private boolean coinCollect = false;
    private float collectTimer = 0f;
    private static final float COINCOLLECT_DURATION = 0.5f;

    private boolean facingRight = true;
    private int health = 100;
    private final int maxHealth = 100;
    private int coins = 0;
    private int jumpCount = 0;

    public Player(float x, float y) {
        Rectangle bounds = new Rectangle(x, y, 32, 52);
        physics = new PhysicsComponent(bounds, -1000f, -1000f, 0.9f, 16f, 200f);
        animationManager = new AnimationManager();
        currentWeapon = new SwordWeapon();
    }

    private void startAttack(List<Rectangle> platforms) {
        Rectangle b = physics.getBounds();
        float pivotX = b.x + b.width/2f;
        float pivotY = b.y + (currentWeapon instanceof SpearWeapon ? b.height / 2f : b.height * 0.7f);

        attacking      = true;
        hitboxActive   = false;
        arrowReleased  = false;
        attackCooldown = ATTACK_COOLDOWN;

        if (currentWeapon instanceof BowWeapon) {
            ((BowWeapon)currentWeapon).setPlatforms(platforms);
            animationManager.forceState(State.ATTACKBOW, facingRight);
        } else if (currentWeapon instanceof SpearWeapon) {
            currentWeapon.startAttack(pivotX, pivotY, facingRight);
            animationManager.forceState(State.ATTACKSPEAR, facingRight);
        } else {
            currentWeapon.startAttack(pivotX, pivotY, facingRight);
            animationManager.forceState(State.ATTACKSWORD, facingRight);
        }
    }

    public void update(float delta, List<Rectangle> platforms, List<BaseEnemy> enemies) {
        // --- Смерть та респавн ---
        if (dying) {
            animationManager.update(delta, State.DEFEAT, facingRight);
            deathTimer += delta;
            if (animationManager.isAnimationFinished(State.DEFEAT)) {
                // Пауза після анімації DEFEAT для "ефекту"
                if (deathTimer >= animationManager.getStateTime() + DEATH_ANIMATION_DELAY) {
                    respawn(DEATH_RESPAWN_X, DEATH_RESPAWN_Y);
                }
            }
            return;
        }
        if (!isAlive) {
            animationManager.forceState(State.DEFEAT, facingRight);
            return;
        }

        // --- Таймери ---
        damageCooldown    = Math.max(0f, damageCooldown - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        attackCooldown    = Math.max(0f, attackCooldown - delta);

        if (currentWeapon instanceof BowWeapon)
            ((BowWeapon)currentWeapon).setPlatforms(platforms);

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (!attacking) startAttack(platforms);
            else attackQueued = true;
        }

        // --- Атака та анімації атаки ---
        if (attacking) {
            float t = animationManager.getStateTime();
            State currentAnimState = animationManager.getCurrentState();
            if (currentWeapon instanceof SpearWeapon && currentAnimState == State.ATTACKSPEAR) {
                hitboxActive = t > ATTACK_ACTIVATE_FROM_SPEAR && t < ATTACK_ACTIVATE_TO_SPEAR;
                if (animationManager.isAnimationFinished(State.ATTACKSPEAR)) {
                    attacking = false; hitboxActive = false;
                    if (attackQueued) { attackQueued = false; startAttack(platforms);}
                }
            } else if (currentWeapon instanceof BowWeapon && currentAnimState == State.ATTACKBOW) {
                if (t > BOW_ATTACK_ACTIVATE_FROM && t < BOW_ATTACK_ACTIVATE_TO && !arrowReleased) {
                    Rectangle b = physics.getBounds();
                    float pivotX = b.x + b.width/2f;
                    float pivotY = b.y + b.height * 0.7f;
                    ((BowWeapon) currentWeapon).releaseArrow(pivotX, pivotY, facingRight);
                    arrowReleased = true;
                }
                if (animationManager.isAnimationFinished(State.ATTACKBOW)) {
                    attacking = false; arrowReleased = false;
                    if (attackQueued) { attackQueued = false; startAttack(platforms);}
                }
                hitboxActive = false;
            } else if (!(currentWeapon instanceof BowWeapon) && currentAnimState == State.ATTACKSWORD) {
                hitboxActive = t > ATTACK_ACTIVATE_FROM_SWORD && t < ATTACK_ACTIVATE_TO_SWORD;
                if (animationManager.isAnimationFinished(State.ATTACKSWORD)) {
                    attacking = false; hitboxActive = false;
                    if (attackQueued) { attackQueued = false; startAttack(platforms);}
                }
            } else {
                hitboxActive = false; arrowReleased = false;
            }
        } else {
            hitboxActive = false; arrowReleased = false;
        }

        Rectangle b  = physics.getBounds();
        float    velY = physics.getVelocityY();

        // --- Dash і рух ---
        if (dashTimer > 0f) {
            physics.setVelocityX(dashDirection * DASH_SPEED);
            dashTimer -= delta;
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { physics.setVelocityX(-MOVE_SPEED); facingRight = false; }
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) { physics.setVelocityX(MOVE_SPEED); facingRight = true; }
            else physics.setVelocityX(0f);
        }

        // --- Wall slide / jump ---
        boolean touchingWall = false, wallOnLeft = false, wallOnRight = false;
        if (velY < 0f) {
            for (Rectangle p : platforms) {
                boolean verticalOverlap = b.y < p.y + p.height && b.y + b.height > p.y;
                if (!verticalOverlap) continue;
                float dxL = b.x - (p.x + p.width);
                float dxR = p.x - (b.x + b.width);
                if (Math.abs(dxL) < WALL_THRESHOLD) touchingWall = wallOnLeft = true;
                if (Math.abs(dxR) < WALL_THRESHOLD) touchingWall = wallOnRight = true;
            }
        }
        boolean sliding = touchingWall && velY < 0f;
        boolean justWallJumped = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (sliding) {
                physics.setVelocityY(JUMP_SPEED + WALL_JUMP_UP);
                jumpCount = MAX_JUMPS;
                physics.setVelocityX(wallOnRight ? -WALL_JUMP_PUSH : WALL_JUMP_PUSH);
                facingRight = !wallOnRight;
                justWallJumped = true;
            } else if (jumpCount < MAX_JUMPS) {
                physics.setVelocityY(JUMP_SPEED);
                jumpCount++;
            }
        }
        if (physics.getVelocityY() == 0f) jumpCount = 0;

        if (sliding && !justWallJumped) {
            boolean holdWallKey = (wallOnLeft  && Gdx.input.isKeyPressed(Input.Keys.A))
                || (wallOnRight && Gdx.input.isKeyPressed(Input.Keys.D));
            if (holdWallKey) physics.startClimbing(-WALL_SLIDE_SPEED);
            else physics.stopClimbing();
        } else physics.stopClimbing();

        if (coinCollect) {
            collectTimer -= delta;
            if (collectTimer <= 0f) coinCollect = false;
        }

        physics.tryStepUp(platforms, physics.getVelocityX() >= 0f);
        physics.update(delta, platforms);

        // --- Зміна зброї ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) currentWeapon = new SwordWeapon();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) currentWeapon = new SpearWeapon(65f, 15f, 0.5f, 0f, 30);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) currentWeapon = new BowWeapon();

        if (currentWeapon instanceof BowWeapon) ((BowWeapon)currentWeapon).setPlatforms(platforms);

        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + (currentWeapon instanceof SpearWeapon ? b.height / 2f : b.height * 0.7f);
        currentWeapon.update(delta, pivotX, pivotY, facingRight);

        if (currentWeapon instanceof BowWeapon) currentWeapon.applyDamage(enemies);
        else if (hitboxActive) currentWeapon.applyDamage(enemies);

        // --- Стан анімації ---
        State newState;
        if (dying) newState = State.DEFEAT;
        else if (coinCollect) newState = State.COINCOLLECT;
        else if (sliding) newState = State.SLIDING;
        else if (attacking) {
            if (currentWeapon instanceof BowWeapon) newState = State.ATTACKBOW;
            else if (currentWeapon instanceof SpearWeapon) newState = State.ATTACKSPEAR;
            else newState = State.ATTACKSWORD;
        } else if (physics.getVelocityY() != 0f) newState = State.JUMP;
        else if (physics.getVelocityX() != 0f) newState = State.WALK;
        else newState = State.IDLE;

        animationManager.update(delta, newState, facingRight);

        b.x = MathUtils.clamp(b.x, 0f, worldWidth  - b.width);
        b.y = Math.min(b.y, worldHeight - b.height);
    }

    public void render(SpriteBatch batch) {
        Rectangle b = physics.getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();
        float drawX = b.x, drawY = b.y, drawW = b.width, drawH = b.height;
        if (animationManager.getCurrentState() == State.ATTACKSWORD) { float extraWidth = 40f; drawW += extraWidth; if (!facingRight) drawX -= extraWidth;}
        if (animationManager.getCurrentState() == State.ATTACKSPEAR) { float extraWidth = 40f; drawW += extraWidth; if (!facingRight) drawX -= extraWidth;}
        if (animationManager.getCurrentState() == State.ATTACKBOW)   { float extraWidth = 40f; drawW += extraWidth; if (!facingRight) drawX -= extraWidth;}

        batch.draw(frame, drawX, drawY, drawW, drawH);

        if (currentWeapon instanceof BowWeapon)
            ((BowWeapon)currentWeapon).renderProjectiles(batch);
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
        if (currentWeapon instanceof BowWeapon) ((BowWeapon)currentWeapon).renderProjectiles(r);
    }

    public boolean isAlive() { return isAlive; }
    public boolean isDying() { return dying; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getCoins() { return coins; }
    public Rectangle getBounds() { return physics.getBounds(); }
    public void addCoin() { coins++; }

    public void takeDamage(int dmg) {
        if (!isAlive || dying) return;
        health -= dmg;
        if (health <= 0) {
            health = 0;
            dying = true;
            isAlive = false;
            deathTimer = 0f;
            animationManager.forceState(State.DEFEAT, facingRight);
        }
    }

    public void respawn(float x, float y) {
        Rectangle b = physics.getBounds();
        b.x = x; b.y = y;
        health = maxHealth;
        isAlive = true;
        dying = false;
        deathTimer = 0f;
        physics.setVelocityY(0f);
        jumpCount = 0;
        animationManager.forceState(State.IDLE, true);
    }

    public void coinCollectAnimation() {
        coinCollect = true;
        collectTimer = COINCOLLECT_DURATION;
    }

    public void dispose() {
        animationManager.dispose();
    }
}
