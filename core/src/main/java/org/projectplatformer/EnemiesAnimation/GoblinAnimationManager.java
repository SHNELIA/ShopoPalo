package org.projectplatformer.EnemiesAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;

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
            frames[i] = new Texture("Enemies/Goblin/Goblin" + (i + 1) + ".png");
        }
        walkAnim   = createAnimation(0, 5, 0.15f, Animation.PlayMode.LOOP);   // Goblin1-5
        attackAnim = createAnimation(5, 9, 0.12f, Animation.PlayMode.NORMAL); // Goblin6-9 (одноразова!)
        deathAnim  = createAnimation(9, 11, 0.25f, Animation.PlayMode.NORMAL);// Goblin10-11 (одноразова!)
    }

    private Animation<TextureRegion> createAnimation(int from, int to, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = from; i < to; i++) {
            regions.add(new TextureRegion(frames[i]));
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, regions);
        anim.setPlayMode(playMode);
        return anim;
    }

    /**
     * Оновлює анімаційний стан гобліна.
     * @param delta - час з минулого кадру
     * @param newState - новий стан
     * @param facingRight - напрямок (для відзеркалення, але тут не flip-имо!)
     */
    public void update(float delta, State newState, boolean facingRight) {
        // Якщо стан змінився — скидаємо stateTime (починаємо анімацію з початку)
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }

        // Залежно від стану — потрібний режим циклічності:
        switch (currentState) {
            case WALK:
                // WALK — циклічна анімація (true: завжди loop)
                currentFrame = walkAnim.getKeyFrame(stateTime, true);
                break;
            case ATTACK:
                // ATTACK — одиночна анімація (false: дограє до кінця й стоїть на останньому кадрі)
                currentFrame = attackAnim.getKeyFrame(stateTime, false);
                break;
            case DEATH:
                // DEATH — одиночна анімація (false: не loop-ить!)
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
                break;
        }
        // Flip тут НЕ робимо!
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    public boolean isAttackAnimationFinished() {
        // ATTACK — одиночна, завершена коли дійшло до кінця
        return attackAnim.isAnimationFinished(stateTime);
    }

    public void resetAttackAnim() {
        stateTime = 0f;
    }

    public boolean isDeathAnimationFinished() {
        // DEATH — одиночна, завершена коли дійшло до кінця
        return deathAnim.isAnimationFinished(stateTime);
    }
    public void resetDeathAnim() {
        stateTime = 0f;
    }

    @Override
    public void dispose() {
        for (Texture tex : frames) {
            tex.dispose();
        }
    }
}
