package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.player.Player;

import java.util.List;
import java.util.ArrayList;

public class Witch extends BaseEnemy {
    private static final float FLY_SPEED         = 100f;
    private static final float CHASE_SPEED       = 180f;
    private static final float HOVER_HEIGHT      = 64f;  // px над платформою
    private static final float AGGRO_RANGE       = 400f;
    private static final float FIREBALL_COOLDOWN = 2.0f;
    private static final int   FIREBALL_DAMAGE   = 20;

    private float fireballTimer = 0f;
    private final List<Fireball> fireballs = new ArrayList<>();

    public Witch(float x, float y, Texture tex) {
        super(x, y, 48, 56, tex, 200, 0, 0, 0, 0, 0); // no gravity, drag, step
        this.attackRange = 48f; // хітбокс торкання відьми
        this.attackDamage = 24;
        this.attackCooldown = 1.2f; // якщо гравець торкається відьми
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        if (!alive) return;

        // Hover logic (парити)
        float px = player.getBounds().x + player.getBounds().width / 2f;
        float py = player.getBounds().y + player.getBounds().height / 2f;

        Rectangle bounds = physics.getBounds();
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;

        // Відьма бачить гравця
        float dx = px - cx;
        float dy = py - cy;
        float distSq = dx * dx + dy * dy;

        if (distSq < AGGRO_RANGE * AGGRO_RANGE) {
            // Агриться та летить на гравця (по прямій)
            float dist = (float)Math.sqrt(distSq);
            float velX = (dx / dist) * CHASE_SPEED;
            float velY = (dy / dist) * CHASE_SPEED;
            physics.setVelocityX(velX);
            physics.setVelocityY(velY);

            // Стріляти фаєрболом якщо cooldown
            fireballTimer -= delta;
            if (fireballTimer <= 0f) {
                fireballs.add(new Fireball(cx, cy, dx, dy));
                fireballTimer = FIREBALL_COOLDOWN;
            }
        } else {
            // Просто вільно літає (рандомна "плавність" або патруль)
            physics.setVelocityX((float)Math.sin(System.currentTimeMillis() * 0.001) * FLY_SPEED);
            physics.setVelocityY((float)Math.cos(System.currentTimeMillis() * 0.0007) * FLY_SPEED);
        }

        // Оновити фаєрболи
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fb = fireballs.get(i);
            fb.update(delta);
            if (fb.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(FIREBALL_DAMAGE);
                fireballs.remove(i);
            } else if (fb.isOffscreen()) {
                fireballs.remove(i);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        for (Fireball fb : fireballs) fb.render(batch);
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        super.renderHitbox(r);
        for (Fireball fb : fireballs) fb.renderHitbox(r);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Fireball fb : fireballs) fb.dispose();
    }

    // --- Внутрішній клас Fireball ---
    private static class Fireball {
        private static final float SPEED = 260f;
        private static final float SIZE  = 14f;
        private float x, y;
        private float vx, vy;
        private Rectangle bounds;

        // Для спрайту фаєрболу:
        private static final Texture fireballTex = new Texture("fireball.png");

        public Fireball(float startX, float startY, float dx, float dy) {
            this.x = startX - SIZE/2f;
            this.y = startY - SIZE/2f;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            vx = (dx / dist) * SPEED;
            vy = (dy / dist) * SPEED;
            bounds = new Rectangle(x, y, SIZE, SIZE);
        }

        public void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            bounds.setPosition(x, y);
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public boolean isOffscreen() {
            return x < -SIZE || x > 1600 || y < -SIZE || y > 1000; // залежить від розміру карти
        }

        public void render(SpriteBatch batch) {
            batch.draw(fireballTex, x, y, SIZE, SIZE);
        }

        public void renderHitbox(ShapeRenderer r) {
            r.setColor(1, 0.5f, 0, 1);
            r.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        public void dispose() {
            fireballTex.dispose();
        }
    }
}
