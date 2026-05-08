package ui.projecto.personajes.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlayerUI {

    private final Player player;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final OrthographicCamera uiCamera;

    public PlayerUI(Player player, OrthographicCamera uiCamera) {
        this.player = player;
        this.uiCamera = uiCamera;
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
    }

    public void render(SpriteBatch batch) {
        float worldHeight = uiCamera.viewportHeight;

        float width = 220;
        float height = 18;
        float x = 20;
        float y = worldHeight - 40;

        float vidaPct = player.getVida().getPorcentajeVida();

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);
        Color vidaColor = new Color(1 - vidaPct, vidaPct, 0, 1);
        shapeRenderer.setColor(vidaColor);
        shapeRenderer.rect(x, y, width * vidaPct, height);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        String text = (int)(vidaPct * 100) + "%";
        font.draw(batch, text, x + width + 10, y + height - 5);
        batch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
