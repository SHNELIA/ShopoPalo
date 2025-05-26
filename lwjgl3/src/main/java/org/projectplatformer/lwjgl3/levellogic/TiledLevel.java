package org.projectplatformer.lwjgl3.levellogic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import org.projectplatformer.lwjgl3.enemy.Goblin;
import org.projectplatformer.lwjgl3.enemy.Skeleton;
import org.projectplatformer.lwjgl3.enemy.Spider;
import org.projectplatformer.lwjgl3.objectslogic.Coin;
import org.projectplatformer.lwjgl3.objectslogic.Platform;
import org.projectplatformer.lwjgl3.objectslogic.World;

public class TiledLevel extends Level {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Texture defaultTex, coinTex;

    public TiledLevel(AssetManager am, SpriteBatch batch, String mapPath) {
        this.map      = am.get(mapPath, TiledMap.class);
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        defaultTex    = am.get("Levels/Images/default.png", Texture.class);
        coinTex       = am.get("Levels/Images/coin.png", Texture.class);
    }

    @Override
    public void createLevel(World world) {
        // 1) Знаходимо точку спавну Player
        RectangleMapObject spawnObj = null;
        MapLayer spawnLayer = map.getLayers().get("Spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects().getByType(RectangleMapObject.class)) {
                if ("PlayerSpawn".equals(obj.getName())) {
                    spawnObj = (RectangleMapObject) obj;
                    break;
                }
            }
        }
        if (spawnObj == null) {
            for (MapLayer layer : map.getLayers()) {
                for (MapObject obj : layer.getObjects().getByType(RectangleMapObject.class)) {
                    if ("PlayerSpawn".equals(obj.getName())) {
                        spawnObj = (RectangleMapObject) obj;
                        break;
                    }
                }
                if (spawnObj != null) break;
            }
        }
        if (spawnObj != null) {
            Rectangle rs = spawnObj.getRectangle();
            startX = rs.x;
            startY = rs.y;
        } else {
            startX = 0;
            startY = 0;
            System.err.println("Warning: у карті не знайдено PlayerSpawn → стартова точка (0,0)");
        }

        // 2) Платформи з шару "ground"
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) map.getLayers().get("ground");
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

        // 3) Монети з шару "Coins"
        MapLayer coinsLayer = map.getLayers().get("Coins");
        if (coinsLayer != null) {
            for (MapObject obj : coinsLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();

                TextureRegion region = new TextureRegion(coinTex);
                Array<TextureRegion> frames = new Array<>();
                frames.add(region);
                Animation<TextureRegion> idleAnim = new Animation<>(0.2f, frames, Animation.PlayMode.LOOP);
                Animation<TextureRegion> collectAnim = new Animation<>(0.1f, frames, Animation.PlayMode.NORMAL);

                world.addObject(new Coin(r.x, r.y, idleAnim, collectAnim));
            }
        }

        // 4) Вороги з шару "Enemies"
        MapLayer enemiesLayer = map.getLayers().get("Enemies");
        if (enemiesLayer != null) {
            for (MapObject obj : enemiesLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                String type = obj.getProperties().get("type", String.class);

                if ("Goblin".equals(type)) {
                    world.addEnemy(new Goblin(r.x, r.y));
                } else if ("Spider".equals(type)) {
                    world.addEnemy(new Spider(r.x, r.y));
                } else if ("Skeleton".equals(type)) {
                    world.addEnemy(new Skeleton(r.x, r.y));
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
        return props.get("width", Integer.class) * props.get("tilewidth", Integer.class);
    }

    public float getMapPixelHeight() {
        MapProperties props = map.getProperties();
        return props.get("height", Integer.class) * props.get("tileheight", Integer.class);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
