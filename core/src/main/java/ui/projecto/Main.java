package ui.projecto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ui.projecto.personajes.Player;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Player jugadorPrincipal;
    private GLProfiler glProfiler;
    private BitmapFont font;
    private Texture mapabeta;

    TiledMap map;
    OrthogonalTiledMapRenderer mapRenderer;

    private boolean fullscreen = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Player.getVirtualWidth(), Player.getVirtualHeight());

        viewport = new FitViewport(Player.getVirtualWidth(), Player.getVirtualHeight(), camera);

        mapabeta = new Texture(Gdx.files.internal("maps/mapabase.png"));
        map = new TmxMapLoader().load("maps/mapabase.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        jugadorPrincipal = new Player(250, 250);

        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();

        font = new BitmapFont();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        float deltaTime = Gdx.graphics.getDeltaTime();
        jugadorPrincipal.update(deltaTime);

        batch.begin();

        // FONDO (debajo del jugador)
        batch.draw(
            mapabeta,
            0,
            0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );

        // JUGADOR (encima)
        jugadorPrincipal.render(batch);

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
        jugadorPrincipal.dispose();
        glProfiler.disable();
        font.dispose();
        mapabeta.dispose();

    }
}
