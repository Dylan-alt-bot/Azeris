package ui.projecto.personajes.Enemies.Goomba.Manager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Enemies.Goomba.State.GoombaState;

import java.util.HashMap;

public class AnimationManagerGoomba {
    private final HashMap<GoombaState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(GoombaState state, Animation<TextureRegion> anim) {
        animations.put(state, anim);
    }

    public Animation<TextureRegion> get(GoombaState state) {
        return animations.get(state);
    }
}
