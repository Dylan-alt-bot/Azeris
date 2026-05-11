package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.gson.Gson;
import firebase.FirebaseAuthService;
import firebase.FirebaseFirestoreService;
import ui.projecto.Main;

public class RegisterScreen implements Screen {
    private final Main game;
    private BitmapFont font;

    private String message = "";
    private StringBuilder email;
    private StringBuilder password;

    private boolean editingEmail = true;
    private boolean fullscreen = false;

    private float messageTimer = 0f;
    private final float MESSAGE_DURATION = 2.5f;

    public RegisterScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        email = new StringBuilder();
        password = new StringBuilder();
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '\t' ||
                    character == '\n' ||
                    character == '\r' ||
                    character == '\b') return false;
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
        handleInput();
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();

        font.draw(game.batch, "REGISTRO", 270, 340);
        font.draw(game.batch, "TAB = cambiar campo", 210, 300);
        font.draw(game.batch, "ENTER = crear cuenta", 210, 270);

        String emailText = (editingEmail ? "> " : "") + "Email: " + email;
        String passwordText = (!editingEmail ? "> " : "") + "Password: " + "*".repeat(password.length());

        font.draw(game.batch, emailText, 120, 200);
        font.draw(game.batch, passwordText, 120, 160);
        font.draw(game.batch, message, 120, 100);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            editingEmail = !editingEmail;
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
            message = "Creando cuenta...";
            String response = FirebaseAuthService.register(
                email.toString(),
                password.toString()
            );
            if (response != null && response.contains("idToken")) {
                message = "REGISTRO OK";
                Gson gson = new Gson();
                FirebaseRegisterResponse res = gson.fromJson(response, FirebaseRegisterResponse.class);
                FirebaseFirestoreService.createUserProfile(res.localId, email.toString());
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Gdx.app.postRunnable(() ->
                            game.setScreen(new MenuScreen(game))
                        );
                    } catch (Exception e) {}
                }).start();
            } else {
                message = "ERROR REGISTRANDO";
                System.out.println(response);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
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
class FirebaseRegisterResponse {
    String localId;
    String idToken;
    String email;
}
