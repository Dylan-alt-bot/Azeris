package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import firebase.FirebaseAuthService;
import ui.projecto.Main;

public class ForgotPasswordScreen implements Screen {
    private final Main game;
    private BitmapFont font;
    private StringBuilder email;
    private String message = "";
    private float messageTimer = 0f;
    private final float MESSAGE_DURATION = 4f;
    private boolean requestSent = false;
    private boolean fullscreen = false;

    public ForgotPasswordScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        font  = new BitmapFont();
        email = new StringBuilder();
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '\t' || character == '\n' ||
                    character == '\r' || character == '\b') return false;
                if (!requestSent) email.append(character);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        handleInput();
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        font.draw(game.batch, "RECUPERAR CONTRASENA", 220, 340);
        font.draw(game.batch, "Introduce tu email y pulsa ENTER", 210, 300);
        font.draw(game.batch, "> Email: " + email, 120, 240);
        font.draw(game.batch, message, 120, 180);
        font.draw(game.batch, "ESC = volver al login", 120, 100);
        game.batch.end();

        if (!message.isEmpty()) {
            messageTimer += delta;
            if (messageTimer > MESSAGE_DURATION) {
                message = "";
                messageTimer = 0f;
                if (requestSent) {
                    game.setScreen(new LoginScreen(game));
                }
            }
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (email.length() > 0 && !requestSent)
                email.deleteCharAt(email.length() - 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String emailText = email.toString().trim();
            if (!emailText.contains("@") || !emailText.contains(".")) {
                message = "Email invalido";
                messageTimer = 0f;
                return;
            }
            message = "Enviando...";
            messageTimer = 0f;
            new Thread(() -> {
                boolean ok = FirebaseAuthService.sendPasswordReset(emailText);
                Gdx.app.postRunnable(() -> {
                    if (ok) {
                        message = "Email enviado! Revisa tu bandeja de entrada";
                        requestSent  = true;
                    } else {
                        message = "Error: comprueba que el email es correcto";
                    }
                    messageTimer = 0f;
                });
            }).start();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new LoginScreen(game));
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
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        font.dispose();
    }
}
