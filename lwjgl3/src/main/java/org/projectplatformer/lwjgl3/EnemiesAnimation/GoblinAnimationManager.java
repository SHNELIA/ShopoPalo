package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class GoblinAnimationManager implements Disposable {
    public enum State {
        WALK,
        ATTACK,
        DEATH
    }

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> deathAnim;

    private float stateTime;
    private State currentState;

    private TextureRegion currentFrame;

    private final Texture[] frames;

    public GoblinAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;

        frames = new Texture[11];
        for (int i = 0; i < 11; i++) {
            // ПРАВИЛЬНО
            frames[i] = new Texture("Enemies/Goblin/Goblin" + (i + 1) + ".png");
        }
        walkAnim = createAnimation(0, 5, 0.15f);   // Goblin1.png - Goblin5.png
        attackAnim = createAnimation(5, 9, 0.12f); // Goblin6.png - Goblin9.png
        deathAnim = createAnimation(9, 11, 0.25f); // Goblin10.png - Goblin11.png
    }

    private Animation<TextureRegion> createAnimation(int from, int to, float frameDuration) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = from; i < to; i++) {
            regions.add(new TextureRegion(frames[i]));
        }
        return new Animation<>(frameDuration, regions, Animation.PlayMode.LOOP);
    }

    public void update(float delta, State newState, boolean facingRight) {
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }

        switch (currentState) {
            case WALK:
                currentFrame = walkAnim.getKeyFrame(stateTime);
                break;
            case ATTACK:
                currentFrame = attackAnim.getKeyFrame(stateTime);
                break;
            case DEATH:
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
                break;
        }
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void dispose() {
        for (Texture tex : frames) {
            tex.dispose();
        }
    }
}
