package ui.projecto.personajes.Enemies.Diablo.Manager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Enemies.Diablo.State.DiabloState;

import java.util.HashMap;

public class AnimationManagerDiablo {
    private final HashMap<DiabloState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(DiabloState state, Animation<TextureRegion> animation) {
        animations.put(state, animation);
    }

    public Animation<TextureRegion> get(DiabloState state) {
        return animations.get(state);
    }
}
