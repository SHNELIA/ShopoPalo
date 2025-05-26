package org.projectplatformer.EnemiesAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class SkeletonAnimationManager implements Disposable {
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

    public SkeletonAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;

        frames = new Texture[6];
        for (int i = 1; i < 16; i++) {
            frames[i] = new Texture("Enemies/Skeleton" + (i + 1) + ".png");
        }

        walkAnim = createAnimation(1, 6, 0.15f);
        attackAnim = createAnimation(14, 16, 0.12f);
        deathAnim = createAnimation(6, 13, 0.25f);
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
