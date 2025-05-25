package org.projectplatformer.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.player.Player;
import java.util.List;

/**
 * Простий павук, який повзе до гравця в межах певного радіусу.
 */
public class Spider extends BaseEnemy {
    private static final float CHASE_RANGE = 100f;
    private static final float MOVE_SPEED  = 80f;

    public Spider(float x, float y, Texture tex) {
        super(
            x, y,
            32f, 32f,
            tex,
            30,         // початкове здоров'я
            -2000f,     // гравітація
            -1000f,     // максимальна швидкість падіння
            0.9f,       // тертя по X
            0f,         // maxStepHeight (не використовуємо підйоми)
            0f          // stepUpSpeed
        );
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle pb = player.getBounds();
        float px = pb.x + pb.width/2f;
        Rectangle b  = physics.getBounds();
        float cx = b.x + b.width/2f;
        float dx = px - cx;

        if (Math.abs(dx) < CHASE_RANGE) {
            // рухаємося до гравця
            physics.setVelocityX(Math.signum(dx) * MOVE_SPEED);
        } else {
            // стоїмо на місці поза радіусом
            physics.setVelocityX(0f);
        }
    }
}
