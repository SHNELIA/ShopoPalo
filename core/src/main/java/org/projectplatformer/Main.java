package org.projectplatformer;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import org.projectplatformer.logic.Player;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Player player;
    private Rectangle ground;

    private Stage uiStage;
    private Skin skin;
    private TextButton respawnButton;
    private Label deathLabel;


    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Камера на розмір екрану
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        player = new Player(100, 100);

        ground = new Rectangle(0, 50, 2000, 20); // Широка платформа для тестів

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);

        deathLabel = new Label("You are dead", skin);
        respawnButton = new TextButton("Respawn", skin);

        respawnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                player.respawn(100, 100); // Початкова позиція
                Gdx.input.setInputProcessor(null); // повертаємо керування грі
            }
        });



        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(deathLabel).padBottom(20).row();
        table.add(respawnButton).width(200).height(50);
        uiStage.addActor(table);

    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Очистка екрану
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);

        // Тестовий урон
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            player.takeDamage(10);
        }

        if (player.isAlive()) {
            player.update(delta, ground);
        } else {
            uiStage.act(delta); // тільки UI, без логіки гравця
        }

        // Камера
        camera.position.set(player.getX() + 32, player.getY() + 32, 0);
        camera.update();

        // Платформа
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1);
        shapeRenderer.rect(ground.x, ground.y, ground.width, ground.height);
        shapeRenderer.end();

        // Гравець
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        batch.end();

        // Хітбокс
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.renderHitbox(shapeRenderer);
        shapeRenderer.end();

        // Шкала HP
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

        // Якщо мертвий — малюємо інтерфейс
        if (!player.isAlive()) {
            uiStage.draw();
        }
    }


    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
    }
}
