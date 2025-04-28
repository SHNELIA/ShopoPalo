package org.projectplatformer.objectslogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.List;

public class Player {
    private Rectangle bounds;
    private float velocityY = 0;
    private final float gravity = -3000;
    private final float moveSpeed = 200;
    private final float jumpSpeed = 600;

    private boolean isAlive = true;

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
    private final float dashCooldownTime = 1.0f;
    private float dashCooldownTimer = 0;

    private int jumpCount = 0;
    private final int maxJumps = 2;
    private boolean touchingWall = false;
    private final float wallJumpPush = 500f; // сила горизонтального відскоку від стіни
    private float velocityX = 0; // Нова змінна швидкості по X
    private final float wallJumpUpBoost = 200f; // додаткова вертикальна сила wall-jump



    private boolean facingRight = true;
    private Texture texture;

    private int coins = 0; // Кількість монет

    public void addCoin() {
        coins++;
    }

    public int getCoins() {
        return coins;
    }

    public Player(float x, float y) {
        bounds = new Rectangle(x, y, 33, 52);
        texture = new Texture("Prince.png");
    }

    public void update(float delta, List<Rectangle> platforms) {
        if (!isAlive) return;

        // 1) Поточний час для dash
        float currentTime = TimeUtils.nanoTime() / 1_000_000_000.0f;
        if (dashCooldownTimer > 0) dashCooldownTimer -= delta;

// тут краще винести wallThreshold наверх, щоб потім використати і для відштовхування
        float wallThreshold = 5f;

        // 2) Детекція торкання стіни
        touchingWall = false;
        boolean wallOnLeft = false, wallOnRight = false;
        for (Rectangle platform : platforms) {
            float pTop    = bounds.y + bounds.height;
            float pBottom = bounds.y;
            float tTop    = platform.y + platform.height;
            float tBottom = platform.y;
            if (pBottom < tTop && pTop > tBottom) {
                float distToRightWall = platform.x - (bounds.x + bounds.width);
                float distToLeftWall  = bounds.x - (platform.x + platform.width);
                if (Math.abs(distToRightWall) < wallThreshold) {
                    touchingWall = true;
                    wallOnRight = true;
                }
                if (Math.abs(distToLeftWall) < wallThreshold) {
                    touchingWall = true;
                    wallOnLeft = true;
                }
            }
        }

        // 3) Горизонтальний рух
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { bounds.x -= moveSpeed * delta; facingRight = false; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { bounds.x += moveSpeed * delta; facingRight = true; }

        // 4) Wall-hold (утримання)
        boolean holdingWall = touchingWall && (
            (wallOnRight && Gdx.input.isKeyPressed(Input.Keys.D)) ||
                (wallOnLeft  && Gdx.input.isKeyPressed(Input.Keys.A))
        );

        // якщо сповзаємо по стіні, знову можемо wall-jump
        if (touchingWall && velocityY < 0) {
            jumpCount = 0;
        }

        // 5) Стрибок і wall-jump
        boolean justWallJumped = false;
        boolean sliding = touchingWall && velocityY < 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (sliding) {
                // вертикальний + додатковий імпульс вгору
                velocityY = jumpSpeed + wallJumpUpBoost;
                jumpCount = maxJumps;

                // горизонтальний імпульс і відсаджування від стіни
                if (wallOnRight) {
                    velocityX = -wallJumpPush;
                    bounds.x -= (wallThreshold + 1);
                    facingRight = false;
                } else {
                    velocityX = wallJumpPush;
                    bounds.x += (wallThreshold + 1);
                    facingRight = true;
                }

                // щоб на цьому ж кадрі не обрубати velocityY>0
                justWallJumped = true;
            }
            else if (jumpCount < maxJumps) {
                velocityY = jumpSpeed;
                jumpCount++;
            }
        }

        // 6) Dash-детекція (подвійний тап)
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            if (currentTime - lastLeftPressTime <= doubleTapTime && dashCooldownTimer <= 0) startDash(-1);
            lastLeftPressTime = currentTime;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (currentTime - lastRightPressTime <= doubleTapTime && dashCooldownTimer <= 0) startDash(1);
            lastRightPressTime = currentTime;
        }

        // 7) Гравітація та обмежене сповзання при утриманні
        velocityY += gravity * delta;
        if (holdingWall && !justWallJumped) {
            if (velocityY > 0)  velocityY = 0;
            if (velocityY < -50) velocityY = -50;
        }

        bounds.y += velocityY * delta;

        // 8) Виконання dash
        if (dashing) {
            bounds.x += dashDirection * dashSpeed * delta;
            dashTimer -= delta;
            if (dashTimer <= 0) dashing = false;
            facingRight = (dashDirection == 1);
        }

        // 9) Горизонтальна інерція та фрикція
        bounds.x += velocityX * delta;
        if (!dashing) velocityX *= 0.9f;

        // 10) Колізії з платформами
        for (Rectangle platform : platforms) {
            if (!bounds.overlaps(platform)) continue;

            float playerBottom = bounds.y;
            float playerTop    = bounds.y + bounds.height;
            float playerLeft   = bounds.x;
            float playerRight  = bounds.x + bounds.width;

            float platformBottom = platform.y;
            float platformTop    = platform.y + platform.height;
            float platformLeft   = platform.x;
            float platformRight  = platform.x + platform.width;

            float overlapBottom = playerTop - platformBottom;
            float overlapTop    = platformTop - playerBottom;
            float overlapLeft   = playerRight - platformLeft;
            float overlapRight  = platformRight - playerLeft;

            boolean fromTop    = overlapTop < overlapBottom && overlapTop < overlapLeft && overlapTop < overlapRight;
            boolean fromBottom = overlapBottom < overlapTop && overlapBottom < overlapLeft && overlapBottom < overlapRight;
            boolean fromLeft   = overlapLeft < overlapRight && overlapLeft < overlapTop && overlapLeft < overlapBottom;
            boolean fromRight  = overlapRight < overlapLeft && overlapRight < overlapTop && overlapRight < overlapBottom;

            if (fromTop && velocityY <= 0) {
                bounds.y = platformTop;
                velocityY = 0;
                jumpCount = 0;
            } else if (fromBottom && velocityY > 0) {
                bounds.y = platformBottom - bounds.height;
                velocityY = 0;
            } else if (fromLeft) {
                bounds.x = platformLeft - bounds.width;
                velocityX = 0;
            } else if (fromRight) {
                bounds.x = platformRight;
                velocityX = 0;
            }
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

    public void renderHitbox(ShapeRenderer renderer) {
        renderer.setColor(1, 0, 0, 1);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void respawn(float x, float y) {
        bounds.x = x;
        bounds.y = y;
        health = maxHealth;
        isAlive = true;
        velocityY = 0;
        jumpCount = 0;
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
        if (!isAlive) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }

    public void dispose() {
        texture.dispose();
    }
    public Rectangle getBounds() {
        return bounds;
    }
    public boolean isFacingRight() {
        return facingRight;
    }
}
