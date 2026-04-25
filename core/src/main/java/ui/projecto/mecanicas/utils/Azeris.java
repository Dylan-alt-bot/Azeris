package ui.projecto.mecanicas.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.AnimationLoader;

public class Azeris implements Utils{
    private final float x, y;
    private final float width = 21f, height = 21f;

    private final Animation<TextureRegion> anim;
    private float tiempo = 0f;

    private boolean collected = false;

    public Azeris(float x, float y) {
        this.x = x;
        this.y = y;

        Texture azeris = new Texture("utils/azeris.png");
        this.anim = new Animation<>(0.05f, AnimationLoader.load(azeris, 6,5));
    }

    public void update(float delta){
        if (collected) return;
        tiempo += delta;
    }

    public void render(SpriteBatch batch){
        if (collected) return;

        TextureRegion frame = anim.getKeyFrame(tiempo, true);
        batch.draw(frame, x, y, width, height);
    }

    public boolean collides(float px, float py, float w, float h){
        return px < x + width && px + w > x && py < y + height && py + h > y;
    }

    public void collect(){
        collected = true;
    }

    public boolean isCollected(){
        return collected;
    }
}
