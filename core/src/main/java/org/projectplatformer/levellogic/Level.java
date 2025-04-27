package org.projectplatformer.levellogic;

import org.projectplatformer.objectslogic.GameObject;
import org.projectplatformer.objectslogic.World;

import java.util.ArrayList;
import java.util.List;

/** Базовий клас для всіх рівнів */
public abstract class Level {
    protected List<GameObject> objects = new ArrayList<>();
    protected float startX, startY;

    /** Створення рівня з доступом до світу */
    public abstract void createLevel(World world);

    public List<GameObject> getObjects() {
        return objects;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }
}
