package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import ui.projecto.Main;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private Music menuMusic;
    private Sound hoverSound;
    private BitmapFont font;
    private MenuButton lastHoverButton;
    private Vector3 mouse;
    private Texture fadeTexture;

    private Texture background;
    private Animation<TextureRegion> backgroundAnim;

    private Texture complemento;
    private Animation<TextureRegion> complementoAnim;
    private List<MenuButton> buttons;

    private boolean showingDevMessage = false;
    private boolean fullscreen = false;
    private boolean isFading = false;

    private float backgroundTimer;
    private float animationTimer;
    private float devMessageTimer = 0f;
    private float fadeAlpha = 0f;
    private final float FADE_SPEED = 2f;

    private Runnable pendingAction = null;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT);

        font = new BitmapFont();

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("pantalla/sfx/fuego.wav"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.05f);
        menuMusic.play();

        hoverSound = Gdx.audio.newSound(Gdx.files.internal("pantalla/sfx/select.wav"));
        background = new Texture("pantalla/fondo.png");
        fadeTexture = new Texture("extras/fnd_negro.png");
        TextureRegion[][] bgtmp = TextureRegion.split(
            background,
            background.getWidth() / 3,
            background.getHeight() / 2
        );
        TextureRegion[] bgframes = new TextureRegion[6];
        int index = 0;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                bgframes[index++] = bgtmp[y][x];
            }
        }
        backgroundAnim = new Animation<>(0.1f, bgframes);
        backgroundTimer = 0f;

        complemento = new Texture("pantalla/complemento.png");
        TextureRegion[][] tmp = TextureRegion.split(
            complemento,
            complemento.getWidth() / 3,
            complemento.getHeight() / 3
        );
        TextureRegion[] frames = new TextureRegion[9];
        index = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                frames[index++] = tmp[y][x];
            }
        }
        complementoAnim = new Animation<>(0.12f, frames);
        animationTimer = 0f;
        buttons = new ArrayList<>();

        int buttonsX = 50;
        int buttonsY = 250;
        int buttonsWidth = 230;
        int buttonsHeight = 40;

        buttons.add(new MenuButton(
            new Texture("pantalla/botons/jugar.png"),
            new Texture("pantalla/botons/jugar_hover.png"),
            buttonsX, buttonsY, buttonsWidth, buttonsHeight, 230, 170,
            () -> startFade(() -> {
                System.out.println("JUGAR");
                menuMusic.pause();
                game.setScreen(new StoryScreen(game));
            })
        ));
        int separation = 55;
        buttons.add(new MenuButton(
            new Texture("pantalla/botons/puntuacion.png"),
            new Texture("pantalla/botons/puntuacion_hover.png"),
            buttonsX, buttonsY - separation, buttonsWidth, buttonsHeight, 230, 170,
            () ->  {
                Gdx.net.openURI("https://frontend-azeris.vercel.app/azeris/points");
            }
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/botons/ajustes.png"),
            new Texture("pantalla/botons/ajustes_hover.png"),
            buttonsX, buttonsY - separation * 2, buttonsWidth, buttonsHeight, 230, 170,
            () -> {
                showingDevMessage = true;
                devMessageTimer = 0f;
            }
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/botons/creditos.png"),
            new Texture("pantalla/botons/creditos_hover.png"),
            buttonsX, buttonsY - separation * 3, buttonsWidth, buttonsHeight, 230, 170,
            () -> startFade (() -> {
                System.out.println("CRÉDITOS");
                menuMusic.pause();
                game.setScreen(new CreditsScreen(game));
            })
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/botons/salir.png"),
            new Texture("pantalla/botons/salir_hover.png"),
            buttonsX, buttonsY - separation * 4, buttonsWidth, buttonsHeight, 230, 170,
            () -> startFade(Gdx.app::exit)
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/botons/usuario.png"),
            new Texture("pantalla/botons/usuario_hover.png"),
            380, 420, buttonsWidth, buttonsHeight, 230, 170,
            () -> startFade(() -> {
                menuMusic.pause();
                game.setScreen(new UserScreen(game));
            })
        ));
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        backgroundTimer += delta;
        animationTimer += delta;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        MenuButton hoveredButtonNow = null;
        boolean clickConsumed = false;

        if (isFading) {
            fadeAlpha += delta * FADE_SPEED;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                if (pendingAction != null) {
                    pendingAction.run();
                    pendingAction = null;
                }
            }
        }

        for (MenuButton button : buttons) {
            boolean hovered = button.getBounds().contains(mouse.x, mouse.y);
            if (hovered && !clickConsumed) {
                button.setHovered(true);
                hoveredButtonNow = button;
                if (Gdx.input.isButtonJustPressed(0) && !isFading) {
                    button.click();
                    clickConsumed = true;
                }
            } else {
                button.setHovered(false);
            }
        }
        if (hoveredButtonNow != null && hoveredButtonNow != lastHoverButton) {
            hoverSound.play(0.5f);
        }
        lastHoverButton = hoveredButtonNow;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);

        TextureRegion bgFrame = backgroundAnim.getKeyFrame(backgroundTimer, true);
        game.batch.draw(
            bgFrame,
            0,
            0,
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT
        );

        TextureRegion currentFrame = complementoAnim.getKeyFrame(animationTimer, true);
        game.batch.draw(currentFrame,
            300,
            0,
            360,
            400
        );

        for (MenuButton button : buttons) {
            button.render(game.batch);
        }

        if (showingDevMessage) {
            devMessageTimer += delta;
            String msg = "Función no implementada, en construcción...";
            GlyphLayout layout = new GlyphLayout(font, msg);
            float tx = (ConstantsPlayer.VIRTUAL_WIDTH - layout.width) / 2f;
            float ty = 460f;
            font.draw(game.batch, layout, tx, ty);

            float DEV_MESSAGE_DURATION = 3f;
            if (devMessageTimer >= DEV_MESSAGE_DURATION) {
                showingDevMessage = false;
                devMessageTimer = 0f;
            }
        }

        if (fadeAlpha > 0f) {
            game.batch.setColor(1, 1, 1, fadeAlpha);
            game.batch.draw(fadeTexture, 0, 0, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);
            game.batch.setColor(1, 1, 1, 1);
        }

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

    private void startFade(Runnable action) {
        if (isFading) return;
        isFading = true;
        fadeAlpha = 0f;
        pendingAction = action;
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        background.dispose();
        complemento.dispose();
        if (fadeTexture != null) fadeTexture.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (hoverSound != null) hoverSound.dispose();
        for (MenuButton button : buttons) {
            button.dispose();
        }
        font.dispose();
    }
}
