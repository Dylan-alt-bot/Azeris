package ui.projecto.mecanicas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class Door {

    private final float x, y, width, height;
    private final Sound doorSound;
    private static final float HITBOX_EXTRA_X = 12f;
    private static final float HITBOX_EXTRA_Y = 12f;

    public Door(Vector2 position, float tileSize) {
        this.x = position.x - HITBOX_EXTRA_X;
        this.y = position.y - HITBOX_EXTRA_Y;
        this.width  = tileSize + HITBOX_EXTRA_X * 2;
        this.height = tileSize + HITBOX_EXTRA_Y * 2;

        doorSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/extras/puerta.wav"));
    }

    public boolean collides(Player player) {
        return player.x < x + width  &&
            player.x + ConstantsPlayer.WIDTH > x &&
            player.y < y + height  &&
            player.y + ConstantsPlayer.HEIGHT > y;
    }

    public void playSound() {
        doorSound.play(0.7f);
    }

    public void dispose() {
        doorSound.dispose();
    }
}
