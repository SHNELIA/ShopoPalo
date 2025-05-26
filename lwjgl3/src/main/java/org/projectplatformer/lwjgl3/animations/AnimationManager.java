package org.projectplatformer.lwjgl3.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    public enum State {
        IDLE, WALK, JUMP,
        ATTACKSWORD, ATTACKSPEAR, ATTACKBOW,
        DEFEAT, SLIDING, COINCOLLECT
    }

    private final Map<State, Animation<TextureRegion>> animations = new HashMap<>();
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private boolean facingRight = true;

    public AnimationManager() {
        String base = "Player/";

        animations.put(State.IDLE,         loadAnimation(base + "Idle",         0.15f, true));
        animations.put(State.WALK,         loadAnimation(base + "Walk",         0.15f, true));
        animations.put(State.JUMP,         loadAnimation(base + "Jump",         0.15f,  false));
        animations.put(State.ATTACKSWORD,  loadAnimation(base + "Sword attack", 0.06f, false));
        animations.put(State.ATTACKSPEAR,  loadAnimation(base + "Spear attack", 0.1f,  false));
        animations.put(State.ATTACKBOW,    loadAnimation(base + "Bow attack",   0.12f, false));
        animations.put(State.DEFEAT,       loadAnimation(base + "Defeat",       0.1f,  false));
        animations.put(State.SLIDING,      loadAnimation(base + "Wall climbing",0.6f,  false));
        animations.put(State.COINCOLLECT,  loadAnimation(base + "Coin collected",0.15f, false));

        // Ensure all possible states are accounted for
        for (State state : State.values()) {
            if (!animations.containsKey(state)) {
                Gdx.app.log("AnimationManager", "Warning: No animation loaded for state: " + state);
            }
        }
    }

    private Animation<TextureRegion> loadAnimation(String dirPath, float frameDuration, boolean loop) {
        FileHandle dir = Gdx.files.internal(dirPath);
        FileHandle[] files = dir.list("png");
        Gdx.app.log("AnimMgr", "Loading '" + dirPath + "' -> exists=" + dir.exists() + ", png-files=" + files.length);

        if (files.length == 0) {
            Gdx.app.error("AnimMgr", "No frames found in " + dirPath);
            Texture fallbackTexture = new Texture("Prince.png");
            TextureRegion fallback = new TextureRegion(fallbackTexture);
            Array<TextureRegion> single = new Array<>(new TextureRegion[]{fallback});
            return new Animation<>(frameDuration, single, loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        }

        Arrays.sort(files, Comparator.comparing(FileHandle::name));
        Array<TextureRegion> frames = new Array<>();
        for (FileHandle f : files) {
            frames.add(new TextureRegion(new Texture(f)));
        }
        return new Animation<>(frameDuration, frames, loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    }

    /**
     * Оновити стан анімації.
     * Якщо змінюється state — stateTime обнуляється!
     */
    public void update(float delta, State newState, boolean facingRight) {
        if (animations.containsKey(newState)) {
            if (newState != currentState) {
                currentState = newState;
                stateTime = 0f;
            } else {
                stateTime += delta;
            }
            this.facingRight = facingRight;
        }
    }

    /**
     * Примусово задати стан анімації та обнулити таймер (наприклад для атаки з queue).
     */
    public void forceState(State newState, boolean facingRight) {
        if (animations.containsKey(newState)) {
            currentState = newState;
            stateTime = 0f;
            this.facingRight = facingRight;
        }
    }

    /**
     * Поточний кадр анімації (копія, з фліпом якщо треба).
     */
    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> animation = animations.get(currentState);
        if (animation == null) {
            Gdx.app.error("AnimationManager", "No animation for state: " + currentState);
            animation = animations.get(State.IDLE);
            if (animation == null) {
                throw new IllegalStateException("Default (IDLE) animation missing!");
            }
        }
        boolean looping = animation.getPlayMode() == Animation.PlayMode.LOOP;
        TextureRegion original = animation.getKeyFrame(stateTime, looping);

        TextureRegion region = new TextureRegion(original);
        boolean shouldFlip = !facingRight;
        if (region.isFlipX() != shouldFlip) {
            region.flip(true, false);
        }
        return region;
    }

    public State getCurrentState() {
        return currentState;
    }
    public float getStateTime() {
        return stateTime;
    }
    public boolean isAnimationFinished(State state) {
        Animation<TextureRegion> anim = animations.get(state);
        return anim != null && anim.isAnimationFinished(stateTime);
    }

    public void dispose() {
        for (Animation<TextureRegion> anim : animations.values()) {
            for (TextureRegion r : anim.getKeyFrames()) {
                r.getTexture().dispose();
            }
        }
    }
}
