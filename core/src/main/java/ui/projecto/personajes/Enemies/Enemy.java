package ui.projecto.personajes.Enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ui.projecto.mecanicas.MapManager;

public class Enemy {
    private float x, y;
    private Texture texture;
    private MapManager map;

    public Enemy(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;
        this.texture = new Texture("enemies/goomba/goomba_sorprendido.png");
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 64, 64);
    }

    public boolean collides(float px, float py, float pw, float ph) {
        return !(px + pw < x || px > x + 64 ||
            py + ph < y || py > y + 64);
    }
}
