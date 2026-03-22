package ui.projecto.personajes.Player.animacion;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Player.personaje.PlayerState;

import java.util.HashMap;

public class AnimationManager {

    private final HashMap<PlayerState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(PlayerState state, Animation<TextureRegion> anim) {
        animations.put(state, anim);
    }

    public Animation<TextureRegion> get(PlayerState state) {
        return animations.get(state);
    }
}
