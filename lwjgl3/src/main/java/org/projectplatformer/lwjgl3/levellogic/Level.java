package org.projectplatformer.lwjgl3.levellogic;

import org.projectplatformer.lwjgl3.objectslogic.GameObject;
import org.projectplatformer.lwjgl3.objectslogic.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактний базовий клас для всіх рівнів.
 * Містить спільну логіку та API для створення рівнів.
 */
public abstract class Level {
    protected List<GameObject> objects = new ArrayList<>(); // Усі об'єкти рівня
    protected float startX, startY; // Координати спавну гравця

    /**
     * Метод для створення рівня.
     * Реалізація має наповнити світ об'єктами, ворогами, платформами тощо.
     * Після створення координати startX/startY використовуються для появи гравця.
     *
     * @param world Ігровий світ, у який додаються об'єкти
     */
    public abstract void createLevel(World world);

    /** @return список об'єктів рівня */
    public List<GameObject> getObjects() {
        return objects;
    }

    /** @return початкова X-координата гравця */
    public float getStartX() {
        return startX;
    }

    /** @return початкова Y-координата гравця */
    public float getStartY() {
        return startY;
    }

    /** Звільнення ресурсів (наприклад, текстур, мап тощо) */
    public abstract void dispose();
}

