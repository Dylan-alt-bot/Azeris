package ui.projecto;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import firebase.SessionManager;
import ui.projecto.Screens.MenuScreen;
import ui.projecto.Screens.StartScreen;

public class Main extends Game {
    public SpriteBatch batch;
    public boolean guestMode = false;

    @Override
    public void create() {
        if (SessionManager.loadSession()) {
            setScreen(new MenuScreen(this));
        } else {
            setScreen(new StartScreen(this));
        }
        batch = new SpriteBatch();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
