package org.projectplatformer.lwjgl3.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class Enemy {
    protected Rectangle bounds;
    protected int health;
    protected int maxHealth;
    protected boolean isAlive = true;

    public Enemy(float x, float y, float width, float height, int maxHealth) {
        this.bounds = new Rectangle(x, y, width, height);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);
    public abstract void renderHitbox(ShapeRenderer renderer);

    public boolean isAlive() {
        return isAlive;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void takeDamage(int amount) {
        if (!isAlive) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            isAlive = false;
            onDeath();
        }
    }

    protected void onDeath() {
        // За потреби: анімація смерті, звук, дроп і т.д.
    }

    public void dispose() {
        // Якщо є текстури — тут їх чистити
    }
}
