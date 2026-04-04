package ui.projecto.personajes.Enemies.goomba;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.AnimationLoader;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;
import ui.projecto.personajes.Enemies.goomba.animacion.AnimationMangerGoomba;
import ui.projecto.personajes.Enemies.goomba.estado.GoombaState;

public class Goomba {
    private final float x, y;
    private final Vida vida;
    private final AnimationMangerGoomba animations;
    private GoombaState state;
    private float tiempo = 0f;
    private TextureRegion currentFrame;

    public Goomba(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;

        this.vida = new Vida(30);
        animations = new AnimationMangerGoomba();
        loadAnimation();

        state = GoombaState.RUN;
    }

    private void loadAnimation(){
        Texture run = new Texture("enemy/goomba/goomba.png");
        animations.add(GoombaState.RUN, new Animation<>(0.4f, AnimationLoader.load(run, 2, 1)));
    }

    public void update(float delta){
        tiempo += delta;

        if (vida.isMuerto()){
            state = GoombaState.DEAD;
        }
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(GoombaState.RUN);
        if (anim != null){
            currentFrame = anim.getKeyFrame(tiempo ,true);
        }

        batch.draw(currentFrame, x, y, 20, 20);
    }

    public boolean collides(float px, float py, float pw, float ph) {
        return !(px + pw < x || px > x + 16 ||
            py + ph < y || py > y + 16);
    }
}
