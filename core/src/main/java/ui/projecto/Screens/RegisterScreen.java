package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.gson.Gson;
import firebase.FirebaseAuthService;
import firebase.FirebaseFirestoreService;
import firebase.SessionManager;
import ui.projecto.Main;

import java.time.LocalDate;

public class RegisterScreen implements Screen {
    private final Main game;
    private BitmapFont font;

    private String message = "";
    private StringBuilder username;
    private StringBuilder email;
    private StringBuilder password;

    private int selectedField = 0;
    private boolean fullscreen = false;

    private float messageTimer = 0f;
    private final float MESSAGE_DURATION = 3f;

    public RegisterScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        username = new StringBuilder();
        email = new StringBuilder();
        password = new StringBuilder();
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '\t' ||
                    character == '\n' ||
                    character == '\r' ||
                    character == '\b') return false;
                switch (selectedField) {
                    case 0:
                        username.append(character);
                        break;
                    case 1:
                        email.append(character);
                        break;
                    case 2:
                        password.append(character);
                        break;
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

        String usernameText = (selectedField == 0 ? "> " : "") + "Username: " + username;
        String emailText = (selectedField == 1 ? "> " : "") + "Email: " + email;
        String passwordText = (selectedField == 2 ? "> " : "") + "Password: " + "*".repeat(password.length());

        font.draw(game.batch, usernameText, 120, 220);
        font.draw(game.batch, emailText, 120, 190);
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
            selectedField++;
            if (selectedField > 2) {
                selectedField = 0;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            switch (selectedField) {
                case 0:
                    if (username.length() > 0) {
                        username.deleteCharAt(username.length() - 1);
                    }
                    break;
                case 1:
                    if (email.length() > 0) {
                        email.deleteCharAt(email.length() - 1);
                    }
                    break;
                case 2:
                    if (password.length() > 0) {
                        password.deleteCharAt(password.length() - 1);
                    }
                    break;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (!validateInputs()) {
                return;
            }
            message = "Creando cuenta...";
            String response = FirebaseAuthService.register(
                email.toString(),
                password.toString()
            );
            if (response != null && response.contains("idToken")) {
                message = "REGISTRADO CORRECTAMENTE";
                Gson gson = new Gson();
                FirebaseRegisterResponse res = gson.fromJson(response, FirebaseRegisterResponse.class);
                FirebaseFirestoreService.createUserProfile(res.localId, username.toString(), email.toString());
                SessionManager.localId = res.localId;
                SessionManager.email = res.email;
                SessionManager.username = username.toString();
                SessionManager.idToken = res.idToken;
                SessionManager.registerDate = LocalDate.now().toString();
                SessionManager.lastCompletedDate = "never";
                SessionManager.saveSession();
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Gdx.app.postRunnable(() ->
                            game.setScreen(new MenuScreen(game))
                        );
                    } catch (Exception e) {}
                }).start();
            } else {
                if (response != null && response.contains("EMAIL_EXISTS")) {
                    message = "Ese email ya está registrado";
                } else if (response != null && response.contains("INVALID_EMAIL")) {
                    message = "Formato de email invalido";
                } else if (response != null && response.contains("WEAK_PASSWORD")) {
                    message = "Password demasiado debil (min. 6 caracteres)";
                } else {
                    message = "Error al registrar, intentalo de nuevo";
                }
                System.out.println("[REGISTER ERROR] " + response);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }
    }

    private boolean validateInputs() {
        String usernameText = username.toString().trim();
        String emailText = email.toString().trim();
        String passwordText = password.toString();
        if (usernameText.length() < 3) {
            message = "Username minimo 3 caracteres";
            return false;
        }
        if (!emailText.contains("@") ||
            !emailText.contains(".")) {
            message = "Email invalido";
            return false;
        }
        if (passwordText.length() < 6) {
            message = "Password minimo 6 caracteres";
            return false;
        }
        return true;
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
