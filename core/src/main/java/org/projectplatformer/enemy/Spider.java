package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;

public class Spider extends BaseEnemy {
    private static final float SCALE = 0.5f;
    private static final int   MAX_HEALTH = 50;

    /**
     * @param x         X-позиція
     * @param y         Y-позиція
     * @param texture   текстура павука
     */
    public Spider(float x, float y, Texture texture) {
        super(
            x, y,
            texture.getWidth()  * SCALE,
            texture.getHeight() * SCALE,
            texture,
            MAX_HEALTH
        );
    }

    @Override
    public void update(float delta) {
        // TODO: логіка AI павука
    }
}
