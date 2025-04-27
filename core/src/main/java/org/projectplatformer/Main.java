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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.projectplatformer.levellogic.Level;
import org.projectplatformer.levels.TestLevel;
import org.projectplatformer.objectslogic.*;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private AssetManager assetManager;

    private Stage uiStage;
    private Skin skin;
    private TextButton respawnButton;
    private Label deathLabel;

    private boolean loading = true;
    private BitmapFont font;

    private Level currentLevel;
    private World world;
    private Player player;

    private SaveData saveData;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        font = new BitmapFont();
        assetManager = new AssetManager();
        assetManager.load("default.png", Texture.class);
        assetManager.load("coin.png", Texture.class);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        setupUI();

        saveData = new SaveData();
    }

    private void setupUI() {
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);

        deathLabel = new Label("You are dead", skin);
        respawnButton = new TextButton("Respawn", skin);
        respawnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player != null && !player.isAlive()) {
                    restartLevel(); // ОНОВЛЕННЯ СЮДИ
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
            if (assetManager.update()) {
                finishLoading();
            }
            drawLoadingScreen();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            player.takeDamage(10);
        }

        if (player != null && player.isAlive()) {
            player.update(delta, world.getPlatformBounds());
            world.update(delta);
        } else {
            uiStage.act(delta);
        }

        // Перевірка підбору предметів
        ArrayList<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : world.getObjects()) {
            if (obj instanceof Item) {
                Item item = (Item) obj;
                if (player.getBounds().overlaps(item.getBounds())) {
                    if (!saveData.isItemCollected(item.getId())) {
                        saveData.collectItem(item.getId());
                        saveData.addCoins(1);
                        toRemove.add(obj);
                    }
                }
            }
        }

        // Видалення підібраних предметів
        for (GameObject obj : toRemove) {
            world.removeObject(obj);
        }

        camera.position.set(player.getX() + 32, player.getY() + 32, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.render(batch);
        player.render(batch);

        // Малюємо кількість монет у правому верхньому куті
        font.draw(batch, "Coins: " + saveData.getCoins(), camera.position.x + 300, camera.position.y + 200);

        batch.end();

        // Малюємо хітбокси
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.renderHitbox(shapeRenderer);
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float healthBarX = camera.position.x - 390;
        float healthBarY = camera.position.y + 200;
        float barWidth = 200;
        float barHeight = 20;
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(healthBarX, healthBarY, barWidth, barHeight);
        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
        shapeRenderer.rect(healthBarX, healthBarY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();

        if (!player.isAlive()) {
            uiStage.draw();
        }
    }

    private void restartLevel() {
        // Обнуляємо дані
        saveData.resetCollectedItems();
        saveData.resetCoins();

        // Створюємо новий світ
        world = new World();
        player = new Player(100, 100);

        // Створюємо рівень наново
        currentLevel.createLevel(world);

        // Повертаємо управління на сцену (якщо гравець натисне "Respawn")
        Gdx.input.setInputProcessor(null);
    }

    private void drawLoadingScreen() {
        batch.begin();
        font.draw(batch, "Loading assets...", 350, 240);
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (player != null) player.dispose();
        if (assetManager != null) assetManager.dispose();
        if (uiStage != null) uiStage.dispose();
        if (font != null) font.dispose();
    }
}
