package org.projectplatformer.logic;

import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player {
    private Rectangle bounds;
    private float velocityY = 0;
    private final float gravity = -20;
    private final float moveSpeed = 200;
    private final float jumpSpeed = 600;

    private int health = 100;
    private final int maxHealth = 100;

    private float lastLeftPressTime = -1;
    private float lastRightPressTime = -1;
    private final float doubleTapTime = 0.25f;
    private final float dashSpeed = 400;
    private boolean dashing = false;
    private float dashTime = 0.15f;
    private float dashTimer = 0;
    private int dashDirection = 0;
    private final float dashCooldownTime = 1.5f;
    private float dashCooldownTimer = 0;

    private int jumpCount = 0;
    private final int maxJumps = 2;

    private boolean facingRight = true;
    private Texture texture;

    public Player(float x, float y) {
        bounds = new Rectangle(x, y, 33, 52);
        texture = new Texture("Prince.png");
    }

    public void update(float delta, Rectangle ground) {
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f;

        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            if (currentTime - lastLeftPressTime <= doubleTapTime && dashCooldownTimer <= 0) {
                startDash(-1);
            }
            lastLeftPressTime = currentTime;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (currentTime - lastRightPressTime <= doubleTapTime && dashCooldownTimer <= 0) {
                startDash(1);
            }
            lastRightPressTime = currentTime;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bounds.x -= moveSpeed * delta;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bounds.x += moveSpeed * delta;
            facingRight = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && jumpCount < maxJumps) {
            velocityY = jumpSpeed;
            jumpCount++;
        }

        velocityY += gravity;
        bounds.y += velocityY * delta;

        if (dashing) {
            bounds.x += dashDirection * dashSpeed * delta;
            dashTimer -= delta;
            if (dashTimer <= 0) {
                dashing = false;
            }
            facingRight = dashDirection == 1;
        }

        if (bounds.overlaps(ground) && velocityY <= 0) {
            bounds.y = ground.y + ground.height;
            velocityY = 0;
            jumpCount = 0;
        }
    }

    private void startDash(int direction) {
        dashing = true;
        dashDirection = direction;
        dashTimer = dashTime;
        dashCooldownTimer = dashCooldownTime;
    }

    public void render(SpriteBatch batch) {
        if (facingRight) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(texture, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);
        }
    }

    public boolean isOnGround(Rectangle ground) {
        return bounds.y <= ground.y + ground.height + 1 &&
            bounds.y >= ground.y + ground.height - 5 &&
            bounds.x + bounds.width > ground.x &&
            bounds.x < ground.x + ground.width;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public void dispose() {
        texture.dispose();
    }
}
