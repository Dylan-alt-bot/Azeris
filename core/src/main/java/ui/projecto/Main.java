package ui.projecto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.personajes.Enemies.Enemy;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.util.Constants;

import java.util.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Viewport viewport;
    private Player jugadorPrincipal;
    private GLProfiler glProfiler;

    private MapManager mapManager;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private BitmapFont font;

    private List<Enemy> enemies;

    private boolean fullscreen = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(
            Constants.VIRTUAL_WIDTH,
            Constants.VIRTUAL_HEIGHT,
            camera
        );

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);

        mapManager = new MapManager("maps/beta/mapabase2.tmx");
        jugadorPrincipal = new Player(250, 250, mapManager);

        font = new BitmapFont();

        enemies = new ArrayList<>();
        for (float[] pos : mapManager.getRandomEnemySpawns()) {
            enemies.add(new Enemy(pos[0], pos[1], mapManager));
        }

        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
    }


    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        float deltaTime = Gdx.graphics.getDeltaTime();

        jugadorPrincipal.update(deltaTime);

        camera.position.set(
            jugadorPrincipal.x,
            jugadorPrincipal.y,
            0
        );

        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        mapManager.render(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        jugadorPrincipal.render(batch, deltaTime);

        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        batch.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        if (jugadorPrincipal.sprintOnCooldown()) {
            float remaining = jugadorPrincipal.getSprintCooldown();
            String text = String.format("Sprint: %.1f s", remaining);
            font.draw(batch, text, 20, 460);
        }

        batch.end();


        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.end();
    }


    private void toggleFullscreen() {
        if (!fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(800, 480);
        }
        fullscreen = !fullscreen;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        glProfiler.disable();
    }
}
