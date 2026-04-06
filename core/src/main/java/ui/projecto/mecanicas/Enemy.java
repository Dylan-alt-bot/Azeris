package ui.projecto.mecanicas;

import ui.projecto.personajes.Player.Player;

public interface Enemy {
    float getX();
    float getY();
    float getWidth();
    float getHeight();
    boolean collides(float px, float py, float pw, float ph);

    void update(float delta, Player player);
    void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch);
    void recibirDolor(int cantidad, float sourceX, float sourceY);
    boolean isDead();
}
