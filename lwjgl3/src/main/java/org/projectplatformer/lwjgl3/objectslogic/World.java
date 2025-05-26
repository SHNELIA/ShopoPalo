package org.projectplatformer.lwjgl3.objectslogic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class World {
    private final List<GameObject>   objects = new ArrayList<>();
    private final List<BaseEnemy>    enemies = new ArrayList<>();

    public void addObject(GameObject obj)      { objects.add(obj); }
    public void removeObject(GameObject obj)   { objects.remove(obj); }
    public List<GameObject> getObjects()       { return objects; }

    public void addEnemy(BaseEnemy e)          { enemies.add(e); }
    public void removeEnemy(BaseEnemy e)       { enemies.remove(e); }
    public List<BaseEnemy> getEnemies()        { return enemies; }

    /** Всі платформи (хітбокси) для колізій */
    public List<Rectangle> getPlatformBounds() {
        List<Rectangle> list = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj instanceof Platform) {
                list.add(((Platform)obj).getBounds());
            }
        }
        return list;
    }

    /** Оновлення світу — нова сигнатура */
    public void update(float delta, Player player, List<Rectangle> platforms) {
        // 1) Оновити всі нерухомі об’єкти (Item, Platform тощо)
        for (GameObject obj : objects) {
            obj.update(delta);
        }

        // 2) Оновити всіх ворогів із фізикою, AI та атакою
        Iterator<BaseEnemy> it = enemies.iterator();
        while (it.hasNext()) {
            BaseEnemy e = it.next();
            if (!e.isAlive()) {
                e.dispose();
                it.remove();
            } else {
                e.update(delta, player, platforms); // <- Тепер передаємо платформи
            }
        }
    }

    /** Рендер всіх об’єктів і ворогів */
    public void render(SpriteBatch batch) {
        for (GameObject obj : objects) obj.render(batch);
        for (BaseEnemy e   : enemies)  e.render(batch);
    }
}

