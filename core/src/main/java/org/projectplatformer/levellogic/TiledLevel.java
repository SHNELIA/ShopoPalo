package org.projectplatformer.levellogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.enemy.Skeleton;
import org.projectplatformer.objectslogic.World;
import org.projectplatformer.objectslogic.Platform;
import org.projectplatformer.objectslogic.Item;
import org.projectplatformer.enemy.Goblin;
import org.projectplatformer.enemy.Spider;

import java.util.List;

public class TiledLevel extends Level {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Texture defaultTex, coinTex, goblinTex, spiderTex, skeletonTex;

    public TiledLevel(AssetManager am, SpriteBatch batch, String mapPath) {
        this.map      = am.get(mapPath, TiledMap.class);
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        defaultTex    = am.get("Levels/Images/default.png", Texture.class);
        coinTex       = am.get("Levels/Images/coin.png", Texture.class);
        goblinTex     = am.get("Levels/Images/goblin.png", Texture.class);
        spiderTex     = am.get("Levels/Images/spider.png", Texture.class);
        skeletonTex  = new Texture(Gdx.files.internal("Enemies/Skeleton/Skeleton1.png"));
    }

    @Override
    public void createLevel(World world) {
        // 1) Шукаємо шар "Spawn" і в ньому об’єкт "PlayerSpawn"
        RectangleMapObject spawnObj = null;
        MapLayer spawnLayer = map.getLayers().get("Spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects().getByType(RectangleMapObject.class)) {
                if ("PlayerSpawn".equals(obj.getName())) {
                    spawnObj = (RectangleMapObject)obj;
                    break;
                }
            }
        }
        // 1.а) Якщо не знайдено, шукаємо PlayerSpawn у всіх шарах
        if (spawnObj == null) {
            for (MapLayer layer : map.getLayers()) {
                for (MapObject obj : layer.getObjects().getByType(RectangleMapObject.class)) {
                    if ("PlayerSpawn".equals(obj.getName())) {
                        spawnObj = (RectangleMapObject)obj;
                        break;
                    }
                }
                if (spawnObj != null) break;
            }
        }
        // 1.б) Якщо досі нема — дефолтна позиція + попередження
        if (spawnObj != null) {
            Rectangle rs = spawnObj.getRectangle();
            startX = rs.x;
            startY = rs.y;
        } else {
            startX = 0;
            startY = 0;
            System.err.println(
                "Warning: у карті не знайдено PlayerSpawn → стартова точка (0,0)");
        }

        // 2) Платформи зі шару "ground"
        TiledMapTileLayer groundLayer =
            (TiledMapTileLayer)map.getLayers().get("ground");
        if (groundLayer != null) {
            float tileW = groundLayer.getTileWidth();
            float tileH = groundLayer.getTileHeight();
            for (int x = 0; x < groundLayer.getWidth(); x++) {
                for (int y = 0; y < groundLayer.getHeight(); y++) {
                    if (groundLayer.getCell(x, y) != null) {
                        world.addObject(new Platform(
                            x * tileW, y * tileH,
                            tileW, tileH,
                            defaultTex
                        ));
                    }
                }
            }
        }

        // 3) Монети зі шару "Coins"
        MapLayer coinsLayer = map.getLayers().get("Coins");
        if (coinsLayer != null) {
            for (MapObject obj : coinsLayer.getObjects()
                .getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject)obj).getRectangle();
                world.addObject(new Item(r.x, r.y, coinTex));
            }
        }

        // 4) Вороги зі шару "Enemies"
        MapLayer enemiesLayer = map.getLayers().get("Enemies");
        if (enemiesLayer != null) {
            for (MapObject obj : enemiesLayer.getObjects()
                .getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject)obj).getRectangle();
                String type = obj.getProperties().get("type", String.class);
                if ("Goblin".equals(type)) {
                    world.addEnemy(new Goblin(r.x, r.y, goblinTex));
                } else if ("Spider".equals(type)) {
                    world.addEnemy(new Spider(r.x, r.y, spiderTex));
                }
                else if ("Skeleton".equals(type)) {
                    world.addEnemy(new Skeleton(r.x, r.y, skeletonTex));
                }
            }
        }
    }

    public void renderMap(OrthographicCamera cam) {
        renderer.setView(cam);
        renderer.render();
    }

    public float getMapPixelWidth() {
        MapProperties props = map.getProperties();
        return props.get("width", Integer.class)
            * props.get("tilewidth", Integer.class);
    }

    public float getMapPixelHeight() {
        MapProperties props = map.getProperties();
        return props.get("height", Integer.class)
            * props.get("tileheight", Integer.class);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}

