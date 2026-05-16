package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import ui.projecto.Main;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class StartScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private BitmapFont font;

    private boolean fullscreen = false;

    public StartScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho( false, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        font.draw(game.batch, "TIENES CUENTA CON NOSOTROS?", 200, 320);
        font.draw(game.batch, "ENTER = SI (LOGIN)", 260, 260);
        font.draw(game.batch, "N = NO (REGISTER)", 260, 220);
        font.draw(game.batch, "ESC = \"QUIZÁS MÁS TARDE\"", 230, 180);

        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new LoginScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            game.setScreen(new RegisterScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.guestMode = true;
            game.setScreen(new MenuScreen(game));
        }
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
        font.dispose();
    }
}
