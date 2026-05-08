package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ui.projecto.Main;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class CreditsScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private Texture background;
    private BitmapFont fontTitle;
    private BitmapFont fontBody;
    private ShapeRenderer shapeRenderer;

    private static final float WINDOW_X = 80f;
    private static final float WINDOW_Y = 30f;
    private static final float WINDOW_WIDTH = 480f;
    private static final float WINDOW_HEIGHT = 360f;
    private static final float PADDING = 15f;
    private boolean fullscreen = false;

    private static final String[][] LINES = {
        {"Créditos", "title"},
        {"", "gap"},
        {"Este juego comenzó como un simple proyecto de clase,", "body"},
        {"pero terminó convirtiéndose en algo mucho más personal.", "body"},
        {"Entre errores, noches largas y muchas ganas de mejorar,", "body"},
        {"pasó a ser un juego hecho con amor.", "body"},
        {"", "gap"},
        {"Mario David Taipe Flores", "section"},
        {"Diseño artístico · Programación principal · Dungeons", "body"},
        {"Mecánicas de combate · IA de enemigos · Sonido", "body"},
        {"Optimización y resolución de bugs", "body"},
        {"", "gap"},
        {"Dylan Marshall Castro Cabeza", "section"},
        {"Creación de salas · Interfaz de usuario", "body"},
        {"Dirección creativa del proyecto", "body"},
        {"", "gap"},
        {"Muchas gracias por jugar", "title"},
        {"", "gap"},
        {"Esperamos que hayas disfrutado esta aventura", "body"},
        {"tanto como nosotros disfrutamos creándola.", "body"},
        {"", "gap"},
        {"Gracias por apoyar proyectos indie hechos con cariño", "footer"},
    };

    public CreditsScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);

        background = new Texture("extras/fnd_negro.png"); // Nota: Agregar un fondo personalizado
        shapeRenderer = new ShapeRenderer();

        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(1.3f);

        fontBody = new BitmapFont();
        fontBody.getData().setScale(0.9f);
    }

    private float lineHeight(String type) {
        switch (type) {
            case "title": return 26f;
            case "section": return 22f;
            case "footer": return 20f;
            case "gap": return 8f;
            default:
                return 16f;
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0,
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT);
        game.batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        float cursorY = WINDOW_Y + WINDOW_HEIGHT - PADDING;
        for (String[] line : LINES) {
            String text = line[0];
            String type = line[1];
            if (!text.isEmpty()) {
                BitmapFont f = (type.equals("title") || type.equals("section"))
                    ? fontTitle : fontBody;
                switch (type) {
                    case "title":   f.setColor(Color.GOLD); break;
                    case "section": f.setColor(Color.CYAN); break;
                    case "footer":  f.setColor(Color.LIGHT_GRAY); break;
                    default:        f.setColor(Color.WHITE); break;
                }
                GlyphLayout gl = new GlyphLayout(f, text);
                float tx = WINDOW_X + (WINDOW_WIDTH - gl.width) / 2f;
                f.draw(game.batch, gl, tx, cursorY);
            }
            cursorY -= lineHeight(type);
        }
        fontBody.setColor(Color.GRAY);
        fontBody.draw(game.batch, "ESC - Volver", WINDOW_X + PADDING, WINDOW_Y + 14f);

        game.batch.end();
    }

    private void toggleFullscreen() {
        if (!fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(640, 480);
        }
        fullscreen = !fullscreen;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        fontTitle.dispose();
        fontBody.dispose();
        shapeRenderer.dispose();
    }
}
