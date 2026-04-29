package ui.projecto.mecanicas.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.AnimationLoader;

public class Azeris implements Utils{
    private final float x, y;
    private final float width = 21f, height = 21f;
    private final Animation<TextureRegion> anim;
    private final Sound ambient;
    private final Sound collectSound;
    private long ambientId = -1;
    private float tiempo = 0f;

    private boolean collected = false;

    public Azeris(float x, float y) {
        this.x = x;
        this.y = y;

        Texture azeris = new Texture("utils/azeris.png");
        this.anim = new Animation<>(0.05f, AnimationLoader.load(azeris, 6,5));
        ambient = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/util/azeris/azerisIdle.wav"));
        collectSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/util/azeris/azerisCollect.wav"));
        ambientId = ambient.loop(0.01f);
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
        if (collected) return;
        collected = true;
        if (ambientId != -1){
            ambient.stop(ambientId);
            ambientId = -1;
        }
        collectSound.play(0.08f);
    }

    public boolean isCollected(){
        return collected;
    }

    public void dispose(){
        if (ambientId != -1) {
            ambient.stop(ambientId);
        }

        ambient.dispose();
        collectSound.dispose();
    }

    @Override
    public void stopAllSounds() {
        if (ambientId != -1) {
            ambient.stop(ambientId);
            ambientId = -1;
        }
    }
}
