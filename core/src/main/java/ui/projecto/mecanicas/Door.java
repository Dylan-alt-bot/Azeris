package ui.projecto.mecanicas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class Door {

    private final float x, y, size;

    private final Sound doorSound;

    public Door(Vector2 position, float tileSize) {
        this.x = position.x - tileSize * 0.5f;
        this.y = position.y - tileSize * 0.5f;
        this.size = tileSize * 2.5f;

        doorSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/extras/puerta.wav"));
    }

    public boolean collides(Player player) {
        return player.x < x + size &&
            player.x + ConstantsPlayer.WIDTH > x &&
            player.y < y + size &&
            player.y + ConstantsPlayer.HEIGHT > y;
    }

    public void playSound() {
        doorSound.play(0.7f);
    }

    public void dispose() {
        doorSound.dispose();
    }
}
