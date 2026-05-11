package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import firebase.FirebaseAuthService;
import ui.projecto.Main;

public class LoginScreen implements Screen {
    private final Main game;
    private BitmapFont font;

    private String message = "";
    private StringBuilder email;
    private StringBuilder password;

    private boolean editingEmail = true;
    private boolean fullscreen = false;

    private float messageTimer = 0f;
    private final float MESSAGE_DURATION = 2.5f;

    public LoginScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        email = new StringBuilder();
        password = new StringBuilder();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '\t') return false;
                if (character == '\r') return false;
                if (character == '\n') return false;
                if (character == '\b') return false;
                if (editingEmail) {
                    email.append(character);
                } else {
                    password.append(character);
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        handleInput();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        font.draw(game.batch, "PANTALLA LOGIN", 270, 380);
        font.draw(game.batch, "TAB = cambiar campo",  255, 300);
        font.draw(game.batch, "ENTER = login", 280, 270);
        font.draw(game.batch, "ESC = volver", 285, 240);

        String emailText = (editingEmail ? "> " : "") + "Email: " + email;
        String hiddenPassword = "*".repeat(password.length());
        String passwordText = (!editingEmail ? "> " : "") + "Password: " + hiddenPassword;
        font.draw(game.batch, emailText, 120, 180);
        font.draw(game.batch, passwordText, 120, 140);
        font.draw(game.batch, message, 120, 80);


        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }

        if (!message.isEmpty()) {
            messageTimer += delta;

            if (messageTimer > MESSAGE_DURATION) {
                message = "";
                messageTimer = 0f;
            }
        }
        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            editingEmail = !editingEmail;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (editingEmail && email.length() > 0) {
                email.deleteCharAt(email.length() - 1);
            }
            if (!editingEmail && password.length() > 0) {
                password.deleteCharAt(password.length() - 1);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            message = "Iniciando sesion...";
            String response = FirebaseAuthService.login(email.toString(), password.toString());
            if (response != null && response.contains("idToken")) {
                message = "LOGIN CORRECTO";
                messageTimer = 0f;
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Gdx.app.postRunnable(() ->
                            game.setScreen(new MenuScreen(game))
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                message = "LOGIN FALLIDO";
            }
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
