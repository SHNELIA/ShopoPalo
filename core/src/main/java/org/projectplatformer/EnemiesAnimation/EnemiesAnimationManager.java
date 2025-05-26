package org.projectplatformer.EnemiesAnimation;

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

public class EnemiesAnimationManager {

    public enum EnemyType {
        GOBLIN, SPIDER
    }

    public enum State {
        WALK, ATTACK, DIE
    }

    private final Map<EnemyType, Map<State, Animation<TextureRegion>>> animations = new HashMap<>();
    private EnemyType currentEnemy;
    private State currentState;
    private float stateTime = 0f;

    public EnemiesAnimationManager(EnemyType enemyType) {
        this.currentEnemy = enemyType;
        this.animations.put(enemyType, new HashMap<>());

        loadAnimations(enemyType);
        // Встановити дефолтний стан
        this.currentState = State.WALK;
    }

    private void loadAnimations(EnemyType enemyType) {
        String basePath = "Enemies/";

        switch (enemyType) {
            case GOBLIN: {
                animations.get(enemyType).put(State.WALK, loadAnimation(basePath, "Goblin", new int[]{1,2,3,4,5}, 0.15f, true));
                animations.get(enemyType).put(State.ATTACK, loadAnimation(basePath, "Goblin", new int[]{6,7,9,10}, 0.20f, false));
                animations.get(enemyType).put(State.DIE, loadAnimation(basePath, "Goblin", new int[]{11,13}, 0.25f, false));
                break;
            }
            case SPIDER: {
                animations.get(enemyType).put(State.WALK, loadAnimation(basePath, "Spider", 1, 4, 0.15f, true));
                animations.get(enemyType).put(State.ATTACK, loadAnimation(basePath, "Spider", 5, 9, 0.20f, false));
                animations.get(enemyType).put(State.DIE, loadAnimation(basePath, "Spider", 10, 13, 0.25f, false));
                break;
            }
            default: {
                Gdx.app.error("EnemiesAnimMgr", "Unknown enemy type: " + enemyType);
                break;
            }
        }
    }

    private Animation<TextureRegion> loadAnimation(String basePath, String baseName, int[] frames, float frameDuration, boolean loop) {
        Array<TextureRegion> regions = new Array<>();
        for (int frame : frames) {
            regions.add(loadTextureRegion(basePath + baseName + frame + ".png"));
        }
        return new Animation<>(frameDuration, regions, loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    }

    private Animation<TextureRegion> loadAnimation(String basePath, String baseName, int start, int end, float frameDuration, boolean loop) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = start; i <= end; i++) {
            regions.add(loadTextureRegion(basePath + baseName + i + ".png"));
        }
        return new Animation<>(frameDuration, regions, loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    }

    private TextureRegion loadTextureRegion(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        return new TextureRegion(tex);
    }

    public void update(float delta, State newState) {
        if (newState != currentState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
    }

    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> anim = animations.get(currentEnemy).get(currentState);
        if (anim == null) {
            Gdx.app.error("EnemiesAnimMgr", "Animation missing for state: " + currentState);
            return null;
        }
        return anim.getKeyFrame(stateTime, currentState == State.WALK); // ходьба циклічна, атака і смерть ні
    }

    public void dispose() {
        for (Map<State, Animation<TextureRegion>> map : animations.values()) {
            for (Animation<TextureRegion> anim : map.values()) {
                for (TextureRegion tr : anim.getKeyFrames()) {
                    tr.getTexture().dispose();
                }
            }
        }
    }
}
