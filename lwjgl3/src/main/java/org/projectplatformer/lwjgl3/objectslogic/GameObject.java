package org.projectplatformer.lwjgl3.objectslogic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected Rectangle bounds;
    protected Texture texture;

    public abstract void update(float deltaTime);

    public abstract void render(SpriteBatch batch);

    public Rectangle getBounds() {
        return bounds;
    }
}

