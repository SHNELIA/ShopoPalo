package org.projectplatformer.lwjgl3.objectslogic;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.player.Player;

public class Coin extends GameObject {
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> collectAnimation;
    private float stateTime = 0f;
    private boolean collected = false;
    private boolean finished = false;

    public Coin(float x, float y, Animation<TextureRegion> idleAnim, Animation<TextureRegion> collectAnim) {
        this.bounds = new Rectangle(x, y, 35, 35); // розміри під спрайт
        this.idleAnimation = idleAnim;
        this.collectAnimation = collectAnim;
    }

    // Оновлення стану монетки
    public void update(float deltaTime, Player player) {
        stateTime += deltaTime;
        if (!collected && bounds.overlaps(player.getBounds())) {
            collected = true;
            stateTime = 0f;
            player.addCoin();
            player.coinCollectAnimation();
        }
        if (collected && collectAnimation.isAnimationFinished(stateTime)) {
            finished = true;
        }
    }

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = collected
            ? collectAnimation.getKeyFrame(stateTime, false)
            : idleAnimation.getKeyFrame(stateTime, true);
        batch.draw(frame, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isFinished() {
        return finished;
    }
}
