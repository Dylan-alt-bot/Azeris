package ui.projecto.personajes.Player.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.personaje.PlayerState;

public class PlayerRenderer {
    private float tiempo = 0f;

    public void render(Player player, SpriteBatch batch, float delta) {
        tiempo += delta;

        TextureRegion frame;
        if (player.getState() == PlayerState.ATTACK ||
            player.getState() == PlayerState.SPRINT ||
            player.getState() == PlayerState.HURT ||
            player.getState() == PlayerState.DEAD) {
            // Animaciones no cíclicas
            frame = player.getAnimation().getKeyFrame(player.getTime(), false);
        } else {
            // Animaciones cíclicas
            frame = player.getAnimation().getKeyFrame(player.getTime(), true);
        }

        float width = frame.getRegionWidth();
        float height = frame.getRegionHeight();

        if (player.isFacingRight()) {
            batch.draw(frame, player.x + width, player.y, -width, height);
        } else {
            batch.draw(frame, player.x, player.y);
        }
    }
}
