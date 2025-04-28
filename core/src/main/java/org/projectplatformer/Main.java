package org.projectplatformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.projectplatformer.levellogic.Level;
import org.projectplatformer.levels.TestLevel;
import org.projectplatformer.objectslogic.GameObject;
import org.projectplatformer.objectslogic.Item;
import org.projectplatformer.objectslogic.Player;
import org.projectplatformer.objectslogic.World;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {
    private static final float WORLD_WIDTH  = 800;
    private static final float WORLD_HEIGHT = 480;
    // швидкість “плавності” камери (чим більше — тим швидше підтягується)
    private static final float CAMERA_LERP = 3f;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport gameViewport;

    private Stage uiStage;
    private Skin skin;
    private TextButton respawnButton;
    private Label deathLabel;

    private BitmapFont font;
    private AssetManager assetManager;
    private boolean loading = true;

    private Level currentLevel;
    private World world;
    private Player player;
    private SaveData saveData;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        gameViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        font = new BitmapFont();
        assetManager = new AssetManager();
        assetManager.load("default.png", Texture.class);
        assetManager.load("coin.png", Texture.class);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        setupUI();

        saveData = new SaveData();
    }

    private void setupUI() {
        uiStage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
        deathLabel = new Label("You are dead", skin);
        respawnButton = new TextButton("Respawn", skin);
        respawnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (player != null && !player.isAlive()) {
                    restartLevel();
                }
            }
        });
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(deathLabel).padBottom(20).row();
        table.add(respawnButton).width(200).height(50);
        uiStage.addActor(table);
    }

    private void finishLoading() {
        assetManager.finishLoading();
        world = new World();
        player = new Player(100, 100);
        currentLevel = new TestLevel(assetManager);
        currentLevel.createLevel(world);
        loading = false;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);

        if (loading) {
            if (assetManager.update()) finishLoading();
            drawLoadingScreen();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            player.takeDamage(10);
        }

        if (player != null && player.isAlive()) {
            // гра отримує введення
            if (Gdx.input.getInputProcessor() != null) {
                Gdx.input.setInputProcessor(null);
            }

            // оновлення гри
            player.update(delta, world.getPlatformBounds());
            world.update(delta);

            // підбір предметів
            ArrayList<GameObject> toRemove = new ArrayList<>();
            for (GameObject obj : world.getObjects()) {
                if (obj instanceof Item) {
                    Item item = (Item) obj;
                    if (player.getBounds().overlaps(item.getBounds()) &&
                        !saveData.isItemCollected(item.getId())) {
                        saveData.collectItem(item.getId());
                        saveData.addCoins(1);
                        toRemove.add(obj);
                    }
                }
            }
            for (GameObject obj : toRemove) world.removeObject(obj);

            // ————— Нова логіка камери —————
            // 1) Центр персонажа
            float px = player.getX() + player.getBounds().width  / 2f;
            float py = player.getY() + player.getBounds().height / 2f;
            // 2) Зсув вперед/назад: гравець на 1/3 ширини зліва (2/3 — попереду)
            float bias = WORLD_WIDTH / 6f;
            float targetX = px + (player.isFacingRight() ?  bias : -bias);
            float targetY = py;
            // 3) Плавна інтерполяція (lerp)
            camera.position.x += (targetX - camera.position.x) * CAMERA_LERP * delta;
            camera.position.y += (targetY - camera.position.y) * CAMERA_LERP * delta;
            // 4) Наносимо зміни у viewport та оновлюємо камеру
            gameViewport.apply(false);
            camera.update();
            // ————————————————————————————

            // рендер світу
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            world.render(batch);
            player.render(batch);
            font.draw(batch, "Coins: " + saveData.getCoins(),
                camera.position.x + 300,
                camera.position.y + 200);
            batch.end();

            // хітбокси та health bar
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            player.renderHitbox(shapeRenderer);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float healthBarX = camera.position.x - 390;
            float healthBarY = camera.position.y + 200;
            float barW = 200, barH = 20;
            float pct = (float) player.getHealth() / player.getMaxHealth();
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
            shapeRenderer.rect(healthBarX, healthBarY, barW, barH);
            shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
            shapeRenderer.rect(healthBarX, healthBarY, barW * pct, barH);
            shapeRenderer.end();

        } else {
            // UI приймає введення
            if (Gdx.input.getInputProcessor() != uiStage) {
                Gdx.input.setInputProcessor(uiStage);
            }
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        // зберігаємо поточний центр, щоб не “стрибали” під час resize
        float cx = camera.position.x;
        float cy = camera.position.y;

        gameViewport.update(width, height, false);
        uiStage.getViewport().update(width, height, true);

        // повертаємо камеру на попередній центр
        camera.position.set(cx, cy, 0);
        camera.update();
    }

    private void restartLevel() {
        saveData.resetCollectedItems();
        saveData.resetCoins();
        world = new World();
        player = new Player(100, 100);
        currentLevel.createLevel(world);
        Gdx.input.setInputProcessor(null);
    }

    private void drawLoadingScreen() {
        batch.begin();
        font.draw(batch, "Loading assets...", WORLD_WIDTH / 2f - 60, WORLD_HEIGHT / 2f);
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null)         batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (player != null)        player.dispose();
        if (assetManager != null)  assetManager.dispose();
        if (uiStage != null)       uiStage.dispose();
        if (font != null)          font.dispose();
    }
}
