package org.projectplatformer.lwjgl3.objectslogic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Клас предмету (наприклад, монети) */
public class Item extends GameObject {
    private final Rectangle bounds;
    private final Texture texture;
    private final String id; // Унікальний ID для SaveData

    public Item(float x, float y, Texture texture) {
        this.texture = texture;
        this.bounds = new Rectangle(x, y, 32, 32); // Розмір предмету 32x32
        this.id = generateId(x, y); // Генеруємо унікальний ID
    }

    /** Малювання предмету */
    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    /** Оновлення предмету — для монети поки нічого не робимо */
    @Override
    public void update(float delta) {
        // Нічого не оновлюємо для статичних предметів
    }

    /** Повертає прямокутник предмету */
    public Rectangle getBounds() {
        return bounds;
    }

    /** Повертає унікальний ID предмету */
    public String getId() {
        return id;
    }

    /** Генерує ID на основі координат */
    private String generateId(float x, float y) {
        return "item_" + (int)x + "_" + (int)y;
    }
}

