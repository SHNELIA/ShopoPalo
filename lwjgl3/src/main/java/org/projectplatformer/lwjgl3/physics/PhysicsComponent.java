package org.projectplatformer.lwjgl3.physics;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Загальна фізика: гравітація, step-up, горизонтальні переміщення та колізії.
 */
public class PhysicsComponent {
    private final Rectangle bounds;

    private float velX = 0f;
    private float velY = 0f;

    // Фізичні константи
    private final float gravity;
    private final float maxFallSpeed;
    private final float drag;
    private final float maxStepHeight;
    private final float stepUpSpeed;


    // Карабкання по стіні
    private boolean isClimbing = false;
    private float   climbSpeed = 0f;

    public PhysicsComponent(Rectangle bounds,
                            float gravity,
                            float maxFallSpeed,
                            float drag,
                            float maxStepHeight,
                            float stepUpSpeed) {
        this.bounds = bounds;
        this.gravity = gravity;
        this.maxFallSpeed = maxFallSpeed;
        this.drag = drag;
        this.maxStepHeight = maxStepHeight;
        this.stepUpSpeed = stepUpSpeed;
    }

    /**
     * Вмикає карабкання зі швидкістю speed
     */
    public void startClimbing(float speed) {
        this.isClimbing = true;
        this.climbSpeed = speed;
    }

    /**
     * Вимикає карабкання
     */
    public void stopClimbing() {
        this.isClimbing = false;
    }

    /**
     * Оновлення фізики за кадр
     */
    public void update(float delta, List<Rectangle> platforms) {
        // --- вертикальна фізика ---
        if (isClimbing) {
            // карабкання: ігнор гравітацію
            velY = climbSpeed;
        } else {
            // звичайна гравітація
            velY += gravity * delta;
            if (velY < maxFallSpeed) velY = maxFallSpeed;
        }
        bounds.y += velY * delta;

        // колізії по Y (приземлення)
        for (Rectangle p : platforms) {
            if (bounds.overlaps(p) && velY <= 0f) {
                bounds.y = p.y + p.height;
                velY = 0f;
                break;
            }
        }

        // --- горизонтальна фізика ---
        bounds.x += velX * delta;
        velX *= drag;

        // колізії по X
        for (Rectangle p : platforms) {
            if (bounds.overlaps(p)) {
                if (velX > 0) bounds.x = p.x - bounds.width;
                else          bounds.x = p.x + p.width;
                velX = 0f;
                break;
            }
        }
    }

    /**
     * Додатковий step-up: "підстрибуємо" на невеликі пороги
     */
    public void tryStepUp(List<Rectangle> platforms, boolean facingRight) {
        float probeX = facingRight
            ? bounds.x + bounds.width + 1
            : bounds.x - 1;
        Rectangle probe = new Rectangle(probeX, bounds.y, 1, bounds.height);
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                float heightDiff = p.y + p.height - bounds.y;
                if (heightDiff > 0 && heightDiff <= maxStepHeight && velY == 0f) {
                    velY = stepUpSpeed;
                }
                break;
            }
        }
    }
    public float getMaxStepHeight() {
        return maxStepHeight;
    }
    public void setVelocityX(float vx) { this.velX = vx; }
    public void setVelocityY(float vy) { this.velY = vy; }
    public float getVelocityX() { return velX; }
    public float getVelocityY() { return velY; }
    public Rectangle getBounds() { return bounds; }

    public float getStepUpSpeed() {
        return stepUpSpeed;
    }
}

