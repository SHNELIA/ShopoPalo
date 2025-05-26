package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.physics.PhysicsComponent;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.List;

public abstract class BaseEnemy {
    protected final PhysicsComponent physics;
    protected final Texture          texture;
    protected int                    health;
    protected boolean                alive = true;

    // --- Атака ворога ---
    protected float   attackRange;
    protected int     attackDamage;
    protected float   attackCooldown;
    private   float   attackTimer = 0f;
    protected Rectangle attackHitbox = new Rectangle();

    public BaseEnemy(float x, float y, float width, float height,
                     Texture tex, int initialHp,
                     float gravity, float maxFallSpeed,
                     float drag, float maxStepHeight,
                     float stepUpSpeed) {
        Rectangle bounds = new Rectangle(x, y, width, height);
        this.physics = new PhysicsComponent(
            bounds, gravity, maxFallSpeed, drag,
            maxStepHeight, stepUpSpeed);
        this.texture = tex;
        this.health  = initialHp;
    }

    public void update(float delta, Player player, List<Rectangle> platforms) {
        if (!alive) return;

        // AI логіка: встановлює швидкості через physics.setVelocityX/Y
        aiMove(delta, player, platforms);

        // Спроба "step-up" якщо потрібно
        physics.tryStepUp(platforms, physics.getVelocityX() >= 0);

        // Оновлюємо фізику (гравітація + колізії)
        physics.update(delta, platforms);

        // Оновлюємо bounds з physics
        Rectangle bounds = physics.getBounds();

        // Обробка атаки гравця
        attackTimer = Math.max(0f, attackTimer - delta);
        float cx = bounds.x + bounds.width/2f;
        float cy = bounds.y + bounds.height/2f;
        Rectangle pb = player.getBounds();
        float px = pb.x + pb.width/2f;
        float py = pb.y + pb.height/2f;
        float dx2 = (px-cx)*(px-cx), dy2 = (py-cy)*(py-cy);
        if (dx2 + dy2 <= attackRange*attackRange && attackTimer == 0f) {
            computeAttackHitbox(player);
            if (attackHitbox.overlaps(pb)) {
                player.takeDamage(attackDamage);
            }
            attackTimer = attackCooldown;
        }
    }

    protected abstract void aiMove(float delta, Player player, List<Rectangle> platforms);

    protected void computeAttackHitbox(Player player) {
        float w = 20f, h = 10f;
        boolean faceRight = player.getBounds().x + player.getBounds().width/2f
            > physics.getBounds().x + physics.getBounds().width/2f;
        float x = faceRight ? physics.getBounds().x + physics.getBounds().width : physics.getBounds().x - w;
        float y = physics.getBounds().y + physics.getBounds().height/2f - h/2f;
        attackHitbox.set(x, y, w, h);
    }

    public void render(SpriteBatch batch) {
        if (!alive) return;
        Rectangle b = physics.getBounds();
        batch.draw(texture, b.x, b.y, b.width, b.height);
    }

    public void renderHitbox(ShapeRenderer r) {
        if (!alive) return;
        Rectangle b = physics.getBounds();
        r.setColor(1f, 0f, 1f, 1f);
        r.rect(b.x, b.y, b.width, b.height);
        if (attackTimer > 0f) {
            r.setColor(1f, 1f, 0f, 1f);
            r.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }
    }

    public Rectangle getBounds() { return physics.getBounds(); }
    public boolean   isAlive()   { return alive; }

    public void takeDamage(int dmg) {
        if (!alive) return;
        health -= dmg;
        if (health <= 0) alive = false;
    }

    public void dispose() {}
}

