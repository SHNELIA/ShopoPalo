package org.projectplatformer.objectslogic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.BaseEnemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Клас, що представляє ігровий світ: об'єкти, вороги, логіка оновлення та рендер.
 */
public class World {
    private List<GameObject> objects = new ArrayList<>();  // Усі об'єкти світу (платформи, тощо)
    private List<BaseEnemy> enemies = new ArrayList<>();   // Активні вороги

    /** Додає новий об'єкт у світ */
    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    /** Видаляє об'єкт зі світу */
    public void removeObject(GameObject obj) {
        objects.remove(obj);
    }

    /** Повертає список усіх об'єктів */
    public List<GameObject> getObjects() {
        return objects;
    }

    /** Повертає хітбокси всіх платформ (для колізій з гравцем) */
    public List<Rectangle> getPlatformBounds() {
        List<Rectangle> bounds = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj instanceof Platform) {
                bounds.add(((Platform) obj).getBounds());
            }
        }
        return bounds;
    }

    /** Додає ворога до світу */
    public void addEnemy(BaseEnemy e) {
        enemies.add(e);
    }

    /** Повертає список активних ворогів */
    public List<BaseEnemy> getEnemies() {
        return enemies;
    }

    /** Видаляє ворога зі світу */
    public void removeEnemy(BaseEnemy e) {
        enemies.remove(e);
    }

    /** Оновлює стан усіх об'єктів та ворогів */
    public void update(float deltaTime) {
        for (GameObject obj : objects) {
            obj.update(deltaTime);
        }

        Iterator<BaseEnemy> it = enemies.iterator();
        while (it.hasNext()) {
            BaseEnemy e = it.next();
            e.update(deltaTime);
            if (!e.isAlive()) {
                e.dispose();
                it.remove();
            }
        }
    }

    /** Малює всі об'єкти та ворогів */
    public void render(SpriteBatch batch) {
        for (GameObject obj : objects) {
            obj.render(batch);
        }
        for (BaseEnemy e : enemies) {
            e.render(batch);
        }
    }
}
