package org.projectplatformer.levellogic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.objectslogic.World;
import org.projectplatformer.objectslogic.Platform;
// import org.projectplatformer.objectslogic.Item;
// import org.projectplatformer.enemy.Goblin;
// import org.projectplatformer.enemy.Spider;

/**
 * Реалізація рівня, побудованого на основі TMX (Tiled Map).
 */
public class TiledLevel extends Level {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Texture defaultTex, coinTex, goblinTex, spiderTex;

    /**
     * @param am      AssetManager, з попередньо завантаженим TMX
     * @param batch   SpriteBatch для рендерингу
     * @param mapPath Шлях до TMX-файлу, напр. "Levels/Maps/Level1.tmx"
     */
    public TiledLevel(AssetManager am, SpriteBatch batch, String mapPath) {
        this.map = am.get(mapPath, TiledMap.class);
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        defaultTex = am.get("Levels/Images/default.png", Texture.class);
        coinTex    = am.get("Levels/Images/coin.png", Texture.class);
        goblinTex  = am.get("Levels/Images/goblin.png", Texture.class);
        spiderTex  = am.get("Levels/Images/spider.png", Texture.class);
    }

    @Override
    public void createLevel(World world) {
        // 1) Точка спавну гравця
        MapLayer spawnLayer = map.getLayers().get("Spawn");
        if (spawnLayer == null) throw new RuntimeException("Layer 'Spawn' not found in map");

        RectangleMapObject spawnObj = (RectangleMapObject)
            spawnLayer.getObjects().get("PlayerSpawn");
        if (spawnObj == null) throw new RuntimeException("Object 'PlayerSpawn' not found in layer 'Spawn'");

        Rectangle rs = spawnObj.getRectangle();
        startX = rs.x;
        startY = rs.y;

        // 2) Платформи з тайлованого шару "ground"
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("ground");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();

            for (int x = 0; x < groundLayer.getWidth(); x++) {
                for (int y = 0; y < groundLayer.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = groundLayer.getCell(x, y);
                    if (cell != null) {
                        world.addObject(new Platform(
                            x * tileW, y * tileH,
                            tileW, tileH,
                            defaultTex
                        ));
                    }
                }
            }
        }

        // 3) Монети
        /*
        MapLayer coinsLayer = map.getLayers().get("Coins");
        if (coinsLayer != null) {
            for (RectangleMapObject o : coinsLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = o.getRectangle();
                world.addObject(new Item(r.x, r.y, coinTex));
            }
        }
        */

        // 4) Вороги
        /*
        MapLayer enemiesLayer = map.getLayers().get("Enemies");
        if (enemiesLayer != null) {
            for (RectangleMapObject o : enemiesLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = o.getRectangle();
                String type = o.getProperties().get("type", String.class);
                if ("Goblin".equals(type)) {
                    world.addEnemy(new Goblin(r.x, r.y, goblinTex));
                } else if ("Spider".equals(type)) {
                    world.addEnemy(new Spider(r.x, r.y, spiderTex));
                }
            }
        }
        */
    }

    /** Рендер карти. Викликається до рендерингу об'єктів. */
    public void renderMap(OrthographicCamera cam) {
        renderer.setView(cam);
        renderer.render();
    }

    /** @return ширина карти в пікселях */
    public float getMapPixelWidth() {
        MapProperties props = map.getProperties();
        return props.get("width", Integer.class) * props.get("tilewidth", Integer.class);
    }

    /** @return висота карти в пікселях */
    public float getMapPixelHeight() {
        MapProperties props = map.getProperties();
        return props.get("height", Integer.class) * props.get("tileheight", Integer.class);
    }

    /** Звільнення ресурсів */
    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
