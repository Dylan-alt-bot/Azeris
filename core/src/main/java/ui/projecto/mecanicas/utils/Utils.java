package ui.projecto.mecanicas.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Utils {
    void update(float delta);
    void render(SpriteBatch batch);
    boolean isCollected();
    void collect();
    boolean collides(float px, float py, float w, float h);
}
