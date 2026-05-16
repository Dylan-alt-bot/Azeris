package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import ui.projecto.Main;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class EndingScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;

    private Texture[] slides;
    private Texture fadeTexture;

    private final int TOTAL_SLIDES = 3;
    private int currentSlide = 0;

    private final float SLIDE_DURATION = 5f;
    private final float FADE_DURATION  = 1f;
    private float slideTimer = 0f;
    private float fadeAlpha  = 1f;

    private enum State { FADE_IN, SHOW, FADE_OUT, DONE }
    private State state = State.FADE_IN;

    private boolean fullscreen = false;

    public EndingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);

        fadeTexture = new Texture("extras/fnd_negro.png");
        slides = new Texture[TOTAL_SLIDES];
        slides[0] = new Texture("pantalla/historia/final/1.png");
        slides[1] = new Texture("pantalla/historia/final/2.png");
        slides[2] = new Texture("pantalla/historia/final/3.png");
        fadeAlpha  = 1f;
        state = State.FADE_IN;
        slideTimer = 0f;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if (currentSlide < TOTAL_SLIDES) {
            game.batch.setColor(1, 1, 1, 1);
            game.batch.draw(slides[currentSlide], 0, 0, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);
        }

        if (fadeAlpha > 0f) {
            game.batch.setColor(1, 1, 1, fadeAlpha);
            game.batch.draw(fadeTexture, 0, 0, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);
            game.batch.setColor(1, 1, 1, 1);
        }
        game.batch.end();

        slideTimer += delta;
        switch (state) {
            case FADE_IN:
                fadeAlpha = 1f - (slideTimer / FADE_DURATION);
                if (slideTimer >= FADE_DURATION) {
                    fadeAlpha = 0f;
                    slideTimer = 0f;
                    state = State.SHOW;
                }
                break;
            case SHOW:
                fadeAlpha = 0f;
                if (slideTimer >= SLIDE_DURATION) {
                    slideTimer = 0f;
                    state = State.FADE_OUT;
                }
                break;
            case FADE_OUT:
                fadeAlpha = slideTimer / FADE_DURATION;
                if (slideTimer >= FADE_DURATION) {
                    fadeAlpha = 1f;
                    currentSlide++;
                    slideTimer = 0f;
                    state = (currentSlide >= TOTAL_SLIDES) ? State.DONE : State.FADE_IN;
                }
                break;
            case DONE:
                game.setScreen(new MenuScreen(game));
                break;
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

    @Override
    public void dispose() {
        if (slides != null){
            for (Texture t : slides){
                if (t != null) t.dispose();
            }
        }
        if (fadeTexture != null) fadeTexture.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
}
