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

    private Texture background;
    private Animation<TextureRegion> backgroundAnim;
    private float backgroundTimer;

    private Texture complemento;
    private Animation<TextureRegion> complementoAnim;
    private float animationTimer;

    private List<MenuButton> buttons;
    private boolean fullscreen = false;

    private boolean showingDevMessage = false;
    private float devMessageTimer = 0f;
    private final float DEV_MESSAGE_DURATION = 3f;

    private int buttonsX = 50;
    private int buttonsY = 250;
    private int separation = 55;
    private int buttonsHeight = 40;
    private int buttonsWidth = 230;

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
        buttons.add(new MenuButton(
            new Texture("pantalla/jugar.png"),
            new Texture("pantalla/jugar_hover.png"),
            buttonsX,buttonsY,buttonsWidth,buttonsHeight, 230, 170,
            () ->{
                System.out.println("JUGAR");
                game.setScreen(new GameScreen(game));
            }
        ));
        buttons.add(new MenuButton(
            new Texture("pantalla/puntuacion.png"),
            new Texture("pantalla/puntuacion_hover.png"),
            buttonsX, buttonsY - separation, buttonsWidth, buttonsHeight, 230, 170,
            () -> System.out.println("PUNTUACIÓN GLOBAL")
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/ajustes.png"),
            new Texture("pantalla/ajustes_hover.png"),
            buttonsX, buttonsY - separation * 2, buttonsWidth, buttonsHeight, 230, 170,
            () -> {
                System.out.println("AJUSTES");
                showingDevMessage = true;
                devMessageTimer = 0f;
            }
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/creditos.png"),
            new Texture("pantalla/creditos_hover.png"),
            buttonsX, buttonsY - separation * 3, buttonsWidth, buttonsHeight, 230, 170,
            () -> {
                System.out.println("CRÉDITOS");
                game.setScreen(new CreditsScreen(game));
            }
        ));

        buttons.add(new MenuButton(
            new Texture("pantalla/salir.png"),
            new Texture("pantalla/salir_hover.png"),
            buttonsX, buttonsY - separation * 4, buttonsWidth, buttonsHeight, 230, 170,
            () -> Gdx.app.exit()
        ));
    }

    @Override
    public void render(float delta) {
        backgroundTimer += delta;
        animationTimer += delta;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();

        camera.update();
        mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        MenuButton hoveredButtonNow = null;
        boolean clickConsumed = false;
        for (MenuButton button : buttons) {
            boolean hovered = button.getBounds().contains(mouse.x, mouse.y);
            if (hovered && !clickConsumed) {
                button.setHovered(true);
                hoveredButtonNow = button;
                if (Gdx.input.isButtonJustPressed(0)) {
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

            if (devMessageTimer >= DEV_MESSAGE_DURATION) {
                showingDevMessage = false;
                devMessageTimer = 0f;
            }
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

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        background.dispose();
        complemento.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (hoverSound != null) hoverSound.dispose();
        for (MenuButton button : buttons) {
            button.dispose();
        }
        font.dispose();
    }
}
