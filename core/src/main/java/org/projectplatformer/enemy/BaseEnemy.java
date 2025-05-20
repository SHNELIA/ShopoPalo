package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Абстрактний базовий клас для всіх ворогів.
 */
public abstract class BaseEnemy {
    protected Rectangle bounds;  // Хітбокс ворога
    protected Texture texture;   // Текстура ворога
    protected int health;        // Здоров’я
    protected boolean alive = true;  // Стан життя

    /**
     * Ініціалізація ворога.
     *
     * @param x         початкова позиція X
     * @param y         початкова позиція Y
     * @param width     ширина хітбоксу
     * @param height    висота хітбоксу
     * @param texture   текстура ворога
     * @param initialHp початкове здоров’я
     */
    public BaseEnemy(float x, float y,
                     float width, float height,
                     Texture texture,
                     int initialHp) {
        this.bounds = new Rectangle(x, y, width, height);
        this.texture = texture;
        this.health = initialHp;
    }

    /** Оновлення стану ворога (рух, AI тощо) — реалізується в підкласах */
    public abstract void update(float delta);

    /** Рендер ворога */
    public void render(SpriteBatch batch) {
        if (!alive) return;
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /** Рендер хітбоксу для дебагу */
    public void renderHitbox(ShapeRenderer renderer) {
        if (!alive) return;
        renderer.setColor(1, 0, 1, 1); // Пурпуровий
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /** Отримати хітбокс */
    public Rectangle getBounds() {
        return bounds;
    }

    /** Перевірка, чи живий ворог */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Отримання шкоди. Якщо HP ≤ 0 — ворог помирає.
     *
     * @param amount кількість шкоди
     */
    public void takeDamage(int amount) {
        if (!alive) return;
        health -= amount;
        if (health <= 0) alive = false;
    }

    /**
     * Звільнення ресурсів.
     * Якщо використовуєте AssetManager — не потрібно нічого робити.
     */
    public void dispose() {
        // Якщо текстура не керується AssetManager-ом, тоді: texture.dispose();
    }
}
