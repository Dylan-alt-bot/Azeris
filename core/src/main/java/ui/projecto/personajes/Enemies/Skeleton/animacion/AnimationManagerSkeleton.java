package ui.projecto.personajes.Enemies.Skeleton.animacion;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.personajes.Enemies.Skeleton.estado.SkeletonState;

import java.util.HashMap;

public class AnimationManagerSkeleton {
    private final HashMap<SkeletonState, Animation<TextureRegion>> animations = new HashMap<>();

    public void add(SkeletonState state, Animation<TextureRegion> anim){
        animations.put(state,anim);
    }

    public Animation<TextureRegion> get(SkeletonState state){
        return animations.get(state);
    }
}
