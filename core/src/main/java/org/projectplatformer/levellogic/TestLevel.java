package org.projectplatformer.levellogic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import org.projectplatformer.enemy.Goblin;
import org.projectplatformer.enemy.Spider;
import org.projectplatformer.objectslogic.Item;
import org.projectplatformer.objectslogic.Platform;
import org.projectplatformer.objectslogic.World;

/**
 * Тестовий рівень без TMX: створюється вручну в коді.
 */
public abstract class TestLevel extends Level {
    private final AssetManager assetManager;

    public TestLevel(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public void createLevel(World world) {
        // Завантаження ресурсів (одноразово для тестового рівня)
        assetManager.load("default.png", Texture.class);
        assetManager.load("coin.png",    Texture.class);
        assetManager.load("goblin.png",  Texture.class);
        assetManager.load("spider.png",  Texture.class);
        assetManager.finishLoading();

        // Отримання текстур
        Texture defaultTex = assetManager.get("default.png", Texture.class);
        Texture coinTex    = assetManager.get("coin.png",    Texture.class);
        Texture goblinTex  = assetManager.get("goblin.png",  Texture.class);
        Texture spiderTex  = assetManager.get("spider.png",  Texture.class);

        // 1) Основна платформа (земля)
        Platform ground = new Platform(0, 50, 2000, 20, defaultTex);
        world.addObject(ground);

        // 2) Точка спавну гравця
        this.startX = 100;
        this.startY = ground.getBounds().y + ground.getBounds().height;

        // 3) Платформи для стрибків
        world.addObject(new Platform(300, 150, 200, 20, defaultTex));
        world.addObject(new Platform(600, 250, 200, 20, defaultTex));
        world.addObject(new Platform(900, 350, 200, 20, defaultTex));

        // 4) Стінки для тестування стрибків зі стін
        world.addObject(new Platform(250, 150, 20, 400, defaultTex));
        world.addObject(new Platform(800, 250, 20, 100, defaultTex));
        world.addObject(new Platform(100, 150, 20, 400, defaultTex));

        // 5) Тестова монета
        world.addObject(new Item(950, 400, coinTex));

        // 6) Вороги
        world.addEnemy(new Goblin(400, 70, goblinTex));
        world.addEnemy(new Spider(800, 70, spiderTex));
    }
}
