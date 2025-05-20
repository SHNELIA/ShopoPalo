package org.projectplatformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.projectplatformer.levellogic.TiledLevel;
import org.projectplatformer.objectslogic.World;
import org.projectplatformer.player.Player;
import org.projectplatformer.enemy.BaseEnemy;

import java.util.Arrays;
import java.util.List;

public class Main extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;

    private static final String IMAGES_PATH = "Levels/Images/";
    private static final String MAPS_PATH = "Levels/Maps/";
    private final List<String> levelPaths = Arrays.asList(MAPS_PATH + "Level1.tmx");
    private int currentLevelIndex = 0;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport gameViewport;

    private BitmapFont font;
    private AssetManager assetManager;
    private boolean loading = true;

    private World world;
    private Player player;
    private TiledLevel tiledLevel;

    private float fallTimer = 0f;
    private static final float FALL_DEATH_DELAY = 0.5f;

    // UI
    private Stage uiStage;
    private Skin skin;
    private Label deathLabel;
    private TextButton respawnButton;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        gameViewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        font = new BitmapFont();
        assetManager = new AssetManager();

        // Завантаження текстур
        assetManager.load(IMAGES_PATH + "default.png", Texture.class);
        assetManager.load(IMAGES_PATH + "coin.png", Texture.class);
        assetManager.load(IMAGES_PATH + "goblin.png", Texture.class);
        assetManager.load(IMAGES_PATH + "spider.png", Texture.class);

        // Завантаження рівнів (TMX)
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        for (String path : levelPaths) {
            assetManager.load(path, TiledMap.class);
        }

        setupUI();
    }

    /** Налаштування UI */
    private void setupUI() {
        uiStage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        deathLabel = new Label("You are dead", skin);
        respawnButton = new TextButton("Respawn", skin);
        respawnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (player != null && !player.isAlive()) restartLevel();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(deathLabel).padBottom(20).row();
        table.add(respawnButton).width(200).height(50);
        uiStage.addActor(table);
    }

    /** Завершення завантаження ресурсів */
    private void finishLoading() {
        assetManager.finishLoading();
        loadLevel(currentLevelIndex);
        loading = false;
    }

    /** Завантаження рівня */
    private void loadLevel(int idx) {
        world = new World();
        String mapPath = levelPaths.get(idx);
        tiledLevel = new TiledLevel(assetManager, batch, mapPath);
        tiledLevel.createLevel(world);

        player = new Player(tiledLevel.getStartX(), tiledLevel.getStartY());

        player.setWorldBounds(
            tiledLevel.getMapPixelWidth(),
            tiledLevel.getMapPixelHeight()
        );

        centerCameraOnPlayer();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Екран завантаження
        if (loading) {
            if (assetManager.update()) finishLoading();
            batch.begin();
            font.draw(batch, "Loading assets...", WORLD_WIDTH / 2f - 60, WORLD_HEIGHT / 2f);
            batch.end();
            return;
        }

        // Перемикання рівня (debug)
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            currentLevelIndex = (currentLevelIndex + 1) % levelPaths.size();
            loadLevel(currentLevelIndex);
        }

        // Оновлення логіки гри
        if (player.isAlive()) {
            player.update(delta, world.getPlatformBounds(), world.getEnemies());
            world.update(delta);

            // Перевірка падіння
            Rectangle b = player.getBounds();
            if (b.y + b.height < 0) {
                fallTimer += delta;
                if (fallTimer >= FALL_DEATH_DELAY) player.takeDamage(player.getHealth());
            } else {
                fallTimer = 0f;
            }
        }

        centerCameraOnPlayer();

        // Рендерінг
        gameViewport.apply(false);
        camera.update();
        tiledLevel.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.render(batch);
        if (player != null) player.render(batch);
        font.draw(batch, "Coins: " + (player != null ? player.getCoins() : 0),
            camera.position.x + gameViewport.getWorldWidth() / 2f - 100,
            camera.position.y + gameViewport.getWorldHeight() / 2f - 20);
        batch.end();

        // Дебаг-хітбокси
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (player != null) player.renderHitbox(shapeRenderer);
        for (BaseEnemy e : world.getEnemies()) e.renderHitbox(shapeRenderer);
        shapeRenderer.end();

        // Полоса HP
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float barX = camera.position.x - gameViewport.getWorldWidth() / 2f + 10;
        float barY = camera.position.y + gameViewport.getWorldHeight() / 2f - 30;
        float barW = 200, barH = 20;
        float pct = player != null ? (float) player.getHealth() / player.getMaxHealth() : 0f;

        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(barX, barY, barW, barH);
        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1f);
        shapeRenderer.rect(barX, barY, barW * pct, barH);
        shapeRenderer.end();

        // UI смерті
        if (player != null && !player.isAlive()) {
            if (Gdx.input.getInputProcessor() != uiStage) {
                Gdx.input.setInputProcessor(uiStage);
            }
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    /** Центрування камери на гравцеві */
    private void centerCameraOnPlayer() {
        if (player == null || tiledLevel == null) return;
        Rectangle b = player.getBounds();
        float mapW = tiledLevel.getMapPixelWidth();
        float mapH = tiledLevel.getMapPixelHeight();

        camera.position.set(b.x + b.width / 2f, b.y + b.height / 2f, 0f);

        float halfW = gameViewport.getWorldWidth() / 2f;
        float halfH = gameViewport.getWorldHeight() / 2f;
        camera.position.x = MathUtils.clamp(camera.position.x, halfW, mapW - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, mapH - halfH);
        camera.update();
    }

    /** Перезапуск рівня */
    private void restartLevel() {
        loadLevel(currentLevelIndex);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, false);
        uiStage.getViewport().update(width, height, true);
        centerCameraOnPlayer();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (player != null) player.dispose();
        if (tiledLevel != null) tiledLevel.dispose();
        if (assetManager != null) assetManager.dispose();
        if (uiStage != null) uiStage.dispose();
        if (font != null) font.dispose();
    }
}
