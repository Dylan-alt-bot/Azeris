package ui.projecto.personajes.Enemies.goomba.animacion;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Enemies.goomba.estado.GoombaState;
import ui.projecto.personajes.Player.estado.PlayerState;

import java.util.HashMap;

public class AnimationMangerGoomba {
    private final HashMap<GoombaState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(GoombaState state, Animation<TextureRegion> anim) {
        animations.put(state, anim);
    }

    public Animation<TextureRegion> get(GoombaState state) {
        return animations.get(state);
    }
}
