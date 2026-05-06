package ui.projecto.mecanicas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

public class Door {
    private final float x, y, size;
    private Sound doorSound;

    private TransitionState state = TransitionState.NONE;
    private float timer = 0f;
    private float duration = 2f;
    private float alpha = 0f;

    private static final float BLACK_HOLD_TIME = 0.5f;
    private final Texture fadeTexture;

    public Door(Vector2 position, float tileSize) {
        this.x = position.x - tileSize * 0.5f;
        this.y = position.y - tileSize * 0.5f;
        this.size = tileSize * 2f;

        fadeTexture = new Texture("extras/fnd_negro.png");
        doorSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/extras/puerta.wav"));
    }

    public void update(float delta, Player player) {
        if (state == TransitionState.NONE && collides(player)) {
            state = TransitionState.FADING_IN;
            timer = 0f;
            alpha = 0f;
            doorSound.play(0.7f);
        }
        if (state != TransitionState.NONE) {
            timer += delta;
            switch (state) {
                case FADING_IN:
                    alpha = Math.min(1f, timer / (duration * 0.5f));
                    if (alpha >= 1f) {
                        state = TransitionState.HOLDING;
                        timer = 0f;
                    }
                    break;
                case HOLDING:
                    alpha = 1f;
                    if (timer >= BLACK_HOLD_TIME) {
                        state = TransitionState.FADING_OUT;
                        timer = 0f;
                    }
                    break;
                case FADING_OUT:
                    alpha = 1f - Math.min(1f, timer / (duration * 0.5f));
                    if (alpha <= 0f) {
                        state = TransitionState.NONE;
                        timer = 0f;
                        alpha = 0f;
                    }
                    break;
            }
        }
    }
    public void renderFade(SpriteBatch batch) {
        if (alpha > 0f) {
            batch.setColor(0, 0, 0, alpha);
            batch.draw(
                fadeTexture,
                0, 0,
                ConstantsPlayer.VIRTUAL_WIDTH,
                ConstantsPlayer.VIRTUAL_HEIGHT
            );
            batch.setColor(1, 1, 1, 1);
        }
    }
    public boolean collides(Player player) {
        return player.x < x + size &&
            player.x + ConstantsPlayer.WIDTH > x &&
            player.y < y + size &&
            player.y + ConstantsPlayer.HEIGHT > y;
    }

    public void dispose() {
        fadeTexture.dispose();
        doorSound.dispose();
    }
    private enum TransitionState {
        NONE,
        FADING_IN,
        HOLDING,
        FADING_OUT
    }
}
