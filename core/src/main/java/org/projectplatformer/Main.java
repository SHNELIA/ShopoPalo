package org.projectplatformer;

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

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Камера на розмір екрану
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        player = new Player(100, 100);

        ground = new Rectangle(0, 50, 2000, 20); // Широка платформа для тестів
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Очистка фону
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);

        // Тестовий урон по кнопці H
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            player.takeDamage(10);
        }

        // Оновлення
        player.update(delta, ground);

        // Камера слідкує за гравцем
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

        // Шкала здоров'я
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float healthBarX = camera.position.x - 390;
        float healthBarY = camera.position.y + 200;
        float barWidth = 200;
        float barHeight = 20;
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();

        // Червоний фон
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(healthBarX, healthBarY, barWidth, barHeight);

        // Зелений — активне HP
        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
        shapeRenderer.rect(healthBarX, healthBarY, barWidth * healthPercent, barHeight);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
    }
}
