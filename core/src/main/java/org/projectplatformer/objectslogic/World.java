package org.projectplatformer.objectslogic;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class World {
    private List<GameObject> objects = new ArrayList<>();

    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    public void update(float deltaTime) {
        for (GameObject obj : objects) {
            obj.update(deltaTime);
        }
    }

    public void render(SpriteBatch batch) {
        for (GameObject obj : objects) {
            obj.render(batch);
        }
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public List<Rectangle> getPlatformBounds() {
        List<Rectangle> bounds = new ArrayList<>();
        for (GameObject obj : objects) {
            if (obj instanceof Platform) {
                bounds.add(((Platform) obj).getBounds());
            }
        }
        return bounds;
    }
    public void removeObject(GameObject obj) {
        objects.remove(obj);
    }

}
