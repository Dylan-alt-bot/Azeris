package ui.projecto.personajes.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlayerUI {

    private final Player player;
    private final ShapeRenderer shapeRenderer;

    public PlayerUI(Player player) {
        this.player = player;
        shapeRenderer = new ShapeRenderer();
    }

    public void render() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float x = 20;
        float y = 440;
        float width = 200;
        float height = 20;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        float vidaPct = player.getVida().getPorcentajeVida();
        Color vidaColor = new Color(1 - vidaPct, vidaPct, 0, 1);
        shapeRenderer.setColor(vidaColor);
        shapeRenderer.rect(x, y, width * vidaPct, height);

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
