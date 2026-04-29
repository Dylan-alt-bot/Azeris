package ui.projecto.mecanicas.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.AnimationLoader;

public class Heart implements Utils{
    private final float x,y;
    private final float width = 16f,height = 16f;
    private final Animation<TextureRegion> anim;
    private final Sound collectSound;
    private float tiempo = 0f;

    private boolean collected = false;

    public Heart(float x, float y) {
        this.x = x;
        this.y = y;
        Texture heart = new Texture("utils/corazon.png");
        this.anim = new Animation<>(0.08f, AnimationLoader.load(heart, 5,4));
        collectSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/util/heart/heart.wav"));
    }

    public void update(float delta) {
        if (collected) return;
        tiempo += delta;
    }

    public void render(SpriteBatch batch) {
        if (collected) return;
        TextureRegion frame = anim.getKeyFrame(tiempo, true);
        batch.draw(frame, x, y);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        if (collected) return;

        collected = true;
        collectSound.play(0.6f);
    }

    public float getX() { return x;}
    public float getY() { return y;}

    public boolean collides(float px, float py, float w, float h) {
        return px < x + width && px + w > x && py < y + height && py + h > y;
    }

    public void dispose() {
        collectSound.dispose();
    }

    @Override
    public void stopAllSounds() {
    }
}
