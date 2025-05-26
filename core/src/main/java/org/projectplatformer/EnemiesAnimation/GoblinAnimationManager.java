package org.projectplatformer.EnemiesAnimation;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GoblinAnimationManager {
    public enum State {
        WALK, ATTACK, DEATH
    }

    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> attackAnimation;
    private final Animation<TextureRegion> deathAnimation;

    private final Array<Texture> loadedTextures = new Array<>();

    private State currentState;
    private float stateTime;
    private boolean facingRight;

    public GoblinAnimationManager() {
        // WALK animation
        Array<TextureRegion> walkFrames = new Array<>();
        for (int i = 0; i < 5; i++) {
            Texture texture = loadTexture("Enemies/Goblin/Goblin" + (i + 1) + ".png");
            walkFrames.add(new TextureRegion(texture));
        }
        walkAnimation = new Animation<TextureRegion>(0.15f, walkFrames, Animation.PlayMode.LOOP);

// ATTACK animation
        Array<TextureRegion> attackFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            Texture texture = loadTexture("Enemies/Goblin/Goblin" + (6 + i) + ".png");
            attackFrames.add(new TextureRegion(texture));
        }
        attackAnimation = new Animation<TextureRegion>(0.1f, attackFrames, Animation.PlayMode.NORMAL);

// DEATH animation
        Array<TextureRegion> deathFrames = new Array<>();
        for (int i = 0; i < 3; i++) {
            Texture texture = loadTexture("Enemies/Goblin/Goblin" + (10 + 1) + ".png");
            deathFrames.add(new TextureRegion(texture));
        }
        deathAnimation = new Animation<TextureRegion>(0.2f, deathFrames, Animation.PlayMode.NORMAL);


        currentState = State.WALK;
        stateTime = 0f;
        facingRight = true;
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(path);
        loadedTextures.add(texture);
        return texture;
    }

    public void update(float delta, State newState, boolean facingRight) {
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
        this.facingRight = facingRight;
    }

    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> animation;
        boolean looping;

        switch (currentState) {
            case WALK:
                animation = walkAnimation;
                looping = true;
                break;
            case ATTACK:
                animation = attackAnimation;
                looping = false;
                break;
            case DEATH:
                animation = deathAnimation;
                looping = false;
                break;
            default:
                animation = walkAnimation;
                looping = true;
        }

        TextureRegion frame = animation.getKeyFrame(stateTime, looping);

        // Дзеркалення кадру, якщо треба
        if ((facingRight && frame.isFlipX()) || (!facingRight && !frame.isFlipX())) {
            frame.flip(true, false);
        }

        return frame;
    }

    public boolean isAnimationFinished() {
        switch (currentState) {
            case ATTACK:
                return attackAnimation.isAnimationFinished(stateTime);
            case DEATH:
                return deathAnimation.isAnimationFinished(stateTime);
            default:
                return false;
        }
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) texture.dispose();
        }
    }
}
