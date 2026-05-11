package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import firebase.FirebaseAuthService;
import firebase.FirebaseFirestoreService;
import firebase.SessionManager;
import ui.projecto.Main;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class UserScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;

    private BitmapFont font;
    private Texture avatar;
    private ShapeRenderer shape;
    private String profileData = "Cargando...";
    private Sound sound;

    private Texture deleteBtn;
    private Texture deleteBtnHover;
    private boolean hoverDelete = false;
    private boolean lastHoverDelete = false;

    private Texture logoutBtn;
    private Texture logoutBtnHover;
    private boolean hoverLogout = false;
    private boolean lastHoverLogout = false;

    private boolean fullscreen = false;

    public UserScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT);
        font = new BitmapFont();
        avatar = new Texture(Gdx.files.internal("extras/cabeza_player.png"));
        shape = new ShapeRenderer();
        deleteBtn = new Texture(Gdx.files.internal("pantalla/botons/borrar.png"));
        deleteBtnHover = new Texture(Gdx.files.internal("pantalla/botons/borrar_hover.png"));
        logoutBtn = new Texture(Gdx.files.internal("pantalla/botons/cerrar.png"));
        logoutBtnHover = new Texture(Gdx.files.internal("pantalla/botons/cerrar_hover.png"));
        sound = Gdx.audio.newSound(Gdx.files.internal("pantalla/sfx/select.wav"));
        new Thread(() -> {
            String response = FirebaseFirestoreService.getUserProfile(SessionManager.localId);
            profileData = formatProfile(response);
        }).start();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        camera.update();
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        Vector3 mouse = new Vector3(mx, my, 0);
        camera.unproject(mouse);
        mx = mouse.x;
        my = mouse.y;

        if (hoverLogout && !lastHoverLogout) {
            sound.play(0.5f);
        }
        lastHoverLogout = hoverLogout;

        if (hoverLogout && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            logout();
        }

        float bx = 220;
        float by = 50;
        float bw = 190;
        float bh = 50;

        float lby = 105;

        hoverDelete = (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh);
        hoverLogout = (mx >= bx && mx <= bx + bw && my >= lby && my <= lby + bh);

        if (hoverDelete && !lastHoverDelete) {
            sound.play(0.5f);
        }
        lastHoverDelete = hoverDelete;

        if (hoverDelete && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            deleteAccount();
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shape.setProjectionMatrix(game.batch.getProjectionMatrix());
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(0.1f, 0.1f, 0.1f, 1);
        shape.rect(80, 85, 450, 310);

        shape.setColor(0.2f, 0.2f, 0.2f, 1);
        shape.rect(90, 95, 430, 300);

        shape.end();

        game.batch.begin();
        game.batch.draw(avatar, 120, 175, 100, 120);
        game.batch.draw(hoverLogout ? logoutBtnHover : logoutBtn, 220, 50, 200, 135);
        game.batch.draw(hoverDelete ? deleteBtnHover : deleteBtn, 220, 0, 200, 130);
        font.draw(game.batch, "USERNAME: " + SessionManager.username, 250, 370);
        font.draw(game.batch, "EMAIL: " + SessionManager.email, 250, 345);
        font.draw(game.batch, "ENEMIES KILLED: " + SessionManager.enemiesKilled, 250, 300);
        font.draw(game.batch, "DEATHS: " + SessionManager.deaths, 250, 275);
        font.draw(game.batch, "GAMES COMPLETED: " + SessionManager.gamesCompleted, 250, 250);
        font.draw(game.batch, "POINTS: " + SessionManager.points, 250, 225);
        font.draw(game.batch, "REGISTERED: " + SessionManager.registerDate, 250, 200);
        font.draw(game.batch, "LAST COMPLETED: " + SessionManager.lastCompletedDate, 250, 175);
        font.draw(game.batch, "ESC = volver", 100, 110);

        game.batch.end();
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    private String formatProfile(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject fields = root.getAsJsonObject("fields");
            String username = fields.getAsJsonObject("username").get("stringValue").getAsString();
            String email = fields.getAsJsonObject("email").get("stringValue").getAsString();
            String enemies = fields.getAsJsonObject("enemiesKilled").get("integerValue").getAsString();
            String deaths = fields.getAsJsonObject("deaths").get("integerValue").getAsString();
            String completed = fields.getAsJsonObject("gamesCompleted").get("integerValue").getAsString();
            String points = fields.getAsJsonObject("points").get("integerValue").getAsString();

            return
                "USERNAME: " + username + "\n" +
                    "EMAIL: " + email + "\n\n" +
                    "ENEMIES: " + enemies + "\n" +
                    "DEATHS: " + deaths + "\n" +
                    "COMPLETED: " + completed + "\n" +
                    "POINTS: " + points;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR LOADING PROFILE";
        }
    }

    private void deleteAccount() {
        String userId = SessionManager.localId;
        String idToken = SessionManager.idToken;

        new Thread(() -> {
            FirebaseFirestoreService.deleteUserProfile(userId);
            FirebaseAuthService.deleteAccount(idToken);
            SessionManager.clear();
            Gdx.app.postRunnable(() ->
                game.setScreen(new StartScreen(game))
            );
        }).start();
    }

    private void logout() {
        SessionManager.clear();
        game.setScreen(new StartScreen(game));
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
        if (sound != null) sound.dispose();
    }
}
