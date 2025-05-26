package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.List;

/**
 * Інтерфейс для зброї: описує базові операції оновлення,
 * старту атаки, отримання хітбоксу та нанесення шкоди.
 */
public interface Weapon {

    /**
     * Почати атаку: встановити внутрішні лічильники,
     * побудувати початковий hitbox і запам’ятати pivot/facing.
     *
     * @param pivotX      X-координата початку удару
     * @param pivotY      Y-координата початку удару
     * @param facingRight напрям лицем, щоб правильно розгорнути hitbox
     */
    void startAttack(float pivotX, float pivotY, boolean facingRight);

    /**
     * Оновити внутрішні таймери атаки та кулдауну,
     * а також перемістити hitbox відповідно до нового pivot/facing.
     *
     * @param delta       час кадру в секундах
     * @param pivotX      X-координата кріплення
     * @param pivotY      Y-координата кріплення
     * @param facingRight напрям лицем
     */
    void update(float delta, float pivotX, float pivotY, boolean facingRight);

    /**
     * Повернути поточний hitbox атаки, якщо вона активна (timer>0),
     * або null, якщо удар закінчився або ще не стартував.
     */
    Rectangle getHitbox();

    /**
     * Нанести шкоду списку ворогів, що попадуться в hitbox.
     *
     * @param enemies список ворогів для перевірки
     */
    void applyDamage(List<BaseEnemy> enemies);

    /**
     * Нанести шкоду гравцю (користується у ворогів).
     *
     * @param player гравець, який може потрапити в hitbox
     */
    void applyDamage(Player player);
    float getCooldownRemaining();
}

