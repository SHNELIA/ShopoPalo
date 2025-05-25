package org.projectplatformer.weapon;

import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;
import org.projectplatformer.player.Player;

import java.util.List;

/**
 * Інтерфейс для зброї: описує базові операції оновлення,
 * отримання хітбоксу та нанесення шкоди ворогам чи гравцю.
 */
public interface Weapon {

    /**
     * Оновити стан зброї (таймери атаки й кулдауну),
     * а також перерахувати хітбокс навколо переданої точки pivot.
     *
     * @param delta        час кадру в секундах
     * @param pivotX       X-координата точки кріплення хітбоксу
     * @param pivotY       Y-координата точки кріплення хітбоксу
     * @param facingRight  напрям обличчям (щоб хітбокс зміщувався ліворуч/праворуч)
     */
    void update(float delta, float pivotX, float pivotY, boolean facingRight);

    /**
     * Повернути поточний хітбокс атаки, якщо атака активна,
     * або null, якщо меч не в стані завдавати шкоду.
     */
    Rectangle getHitbox();

    /**
     * Нанести шкоду всім ворогам із переданого списку,
     * що перебувають у хітбоксі.
     *
     * @param enemies  список ворогів для перевірки
     */
    void applyDamage(List<BaseEnemy> enemies);

    /**
     * Нанести шкоду гравцю, якщо він перебуває в хітбоксі атаки.
     *
     * @param player  екземпляр гравця
     */
    void applyDamage(Player player);

    /**
     * Почати атаку:
     * @param pivotX X-координата, навколо якої будується hitbox
     * @param pivotY Y-координата, навколо якої будується hitbox
     * @param facingRight напрям лицем для зміщення вперед/назад
     */
    void startAttack(float pivotX, float pivotY, boolean facingRight);
}
