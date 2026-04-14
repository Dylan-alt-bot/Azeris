package ui.projecto.personajes.Enemies.Amongus.animacion;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Enemies.Amongus.estado.AmongusState;

import java.util.HashMap;

public class AnimationManagerAmongus {
    private final HashMap<AmongusState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(AmongusState state, Animation<TextureRegion> anim){
        animations.put(state,anim);
    }

    public Animation<TextureRegion> get(AmongusState state){
        return animations.get(state);
    }
}
