package org.projectplatformer.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.assets.AssetManager;
import org.projectplatformer.levellogic.Level;
import org.projectplatformer.objectslogic.Item;
import org.projectplatformer.objectslogic.Platform;
import org.projectplatformer.objectslogic.World;

/** Тестовий рівень */
public class TestLevel extends Level {

    private AssetManager assetManager;

    public TestLevel(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public void createLevel(World world) {
        // Завантажуємо текстури через assetManager
        Texture defaultTexture = assetManager.get("default.png", Texture.class);
        Texture coinTexture = assetManager.get("coin.png", Texture.class);

        // Велика земля
        Platform ground = new Platform(0, 50, 2000, 20, defaultTexture);
        world.addObject(ground);

        // Платформи для стрибків
        Platform p1 = new Platform(300, 150, 200, 20, defaultTexture);
        Platform p2 = new Platform(600, 250, 200, 20, defaultTexture);
        Platform p3 = new Platform(900, 350, 200, 20, defaultTexture);

        world.addObject(p1);
        world.addObject(p2);
        world.addObject(p3);

        // Стіни біля платформ для тесту відштовхування
        Platform wall1 = new Platform(250, 150, 20, 100, defaultTexture); // Ліва стіна біля p1
        Platform wall2 = new Platform(800, 250, 20, 100, defaultTexture); // Права стіна біля p2

        world.addObject(wall1);
        world.addObject(wall2);

        // Монета
        Item coin = new Item(950, 400, coinTexture);
        world.addObject(coin);
    }
}
