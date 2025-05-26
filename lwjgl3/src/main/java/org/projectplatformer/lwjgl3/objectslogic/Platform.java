package org.projectplatformer.lwjgl3.objectslogic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Platform extends GameObject {
    public Platform(float x, float y, float width, float height, Texture texture) {
        bounds = new Rectangle(x, y, width, height);
        this.texture = texture;
    }

    @Override
    public void update(float deltaTime) {}

    @Override
    public void render(SpriteBatch batch) {
        // нічого не малюємо, бо візуал дає TiledMapRenderer
    }
}

