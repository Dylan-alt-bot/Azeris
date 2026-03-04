package io.github.azerisproyecte;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

public class azerisMain extends Game{

    public static final int WORLD_WIDTH = 50;
    public static final int WORLD_HEIGHT = 50;

    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger = new FPSLogger();

    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
        this.fpsLogger = new FPSLogger();

        camera.position.set(0, 0, 0);
        camera.zoom = 1.0f;
        camera.update();

        addScreen(new FirstScreen(this));
        setScreen(FirstScreen.class);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();
    }

    public void addScreen(Screen screen) {
        screenCache.put(screen.getClass(), screen);
    }

    public void setScreen(Class<? extends Screen> screenClass) {
        Screen screen = screenCache.get(screenClass);
        if (screen == null) {
            throw new GdxRuntimeException("No screen with class " + screenClass + " found in screen cache.");

        }
        super.setScreen(screen);
    }

    @Override
    public void render() {

        camMove();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        glProfiler.reset();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();

        Gdx.graphics.setTitle("Azeris - Draw Calls: " + glProfiler.getDrawCalls());
        fpsLogger.log();
    }

    @Override
    public void dispose() {
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();

        this.batch.dispose();
    }

    public Batch getBatch() {
        return batch;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    private void camMove() {
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0,3,0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0,-3,0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-3,0,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(3,0,0);
        }
    }
}
