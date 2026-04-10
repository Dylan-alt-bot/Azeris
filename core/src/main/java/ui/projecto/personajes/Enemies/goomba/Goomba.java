package ui.projecto.personajes.Enemies.goomba;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.mecanicas.*;
import ui.projecto.personajes.Enemies.goomba.util.ConstantsGoomba;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Enemies.goomba.animacion.AnimationMangerGoomba;
import ui.projecto.personajes.Enemies.goomba.estado.GoombaState;

public class Goomba implements Enemy{
    private float posX, posY, tiempo = 0f;
    private final float width = 16f, height = 16f;

    private final Vida vida = new Vida(30);
    private GoombaState state = GoombaState.IDLE, previousState = GoombaState.IDLE;
    private final AnimationMangerGoomba animations = new AnimationMangerGoomba();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private float alertTimer = 0f;
    private boolean alertStarted = false;

    private float hurtTimer = 0f;
    private float damageTimer = 0f;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Goomba(float x, float y, MapManager map) {
        this.posX = x;
        this.posY = y;
        this.map = map;

        wander = new EnemyWander(ConstantsGoomba.VELOCIDAD, 10f, 100f, map, 20f, 20f);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(150f);
        loadAnimation();
    }

    private void loadAnimation(){
        Texture idle =  new Texture("enemy/goomba/goomba.png");
        Texture run = new Texture("enemy/goomba/goomba_corriendo.png");
        Texture alert = new Texture("enemy/goomba/goomba_sorprendido.png");
        Texture hurt = new Texture("enemy/goomba/goomba_dolor.png");
        Texture dead = new Texture("enemy/goomba/goomba_muerte.png");

        animations.add(GoombaState.IDLE, new Animation<>(0.08f, AnimationLoader.load(idle, 6,6)));
        animations.add(GoombaState.RUN, new Animation<>(0.4f, AnimationLoader.load(run, 2, 1)));
        animations.add(GoombaState.ALERT, new Animation<>(0.04f, AnimationLoader.load(alert, 1, 1)));
        animations.add(GoombaState.HURT, new Animation<>(0.04f, AnimationLoader.load(hurt, 3, 3)));
        animations.add(GoombaState.DEAD, new Animation<>(0.04f, AnimationLoader.load(dead, 2, 2)));
    }

    @Override
    public void update(float delta, Player player){
        tiempo += delta;

        if (vida.isMuerto()){
            state = GoombaState.DEAD;
            return;
        }

        if (damageTimer > 0f){
            damageTimer -= delta;
        }

        if (state == GoombaState.HURT){
            hurtTimer -= delta;
            if (hurtTimer <= 0){
                state = previousState != GoombaState.HURT ? previousState : GoombaState.IDLE;
            }
        }
        if (vision.isPlayerInRange(posX,posY, player.x, player.y)) {
            if (!alertStarted){
                state = GoombaState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }
            if (state == GoombaState.ALERT){
                alertTimer += delta;
                if (alertTimer >= ConstantsGoomba.ALERT_DURATION){
                    state = GoombaState.RUN;
                }
            } else if (state == GoombaState.RUN) {
                Vector2 nextStep = pathFinder.findNextStep(posX, posY, player.x, player.y);

                float dx = player.x - posX;
                float dy = player.y - posY;
                float distToPlayer = (float)Math.sqrt(dx * dx + dy * dy);

                if (distToPlayer < 20f) {
                    if (distToPlayer > 0) {
                        float moveX = dx / distToPlayer * 60f * delta;
                        float moveY = dy / distToPlayer * 60f * delta;
                        if (!map.isBlocked(posX + moveX, posY, width, height)) posX += moveX;
                        if (!map.isBlocked(posX, posY + moveY, width, height)) posY += moveY;
                    }
                    return;
                }
                if (nextStep != null) {
                    float ndx = nextStep.x - posX;
                    float ndy = nextStep.y - posY;
                    float dist = (float)Math.sqrt(ndx * ndx + ndy * ndy);

                    if (dist > 1f) {
                        float moveX = ndx / dist * 50f * delta;
                        float moveY = ndy / dist * 50f * delta;

                        boolean moved = false;
                        if (!map.isBlocked(posX + moveX, posY, width, height)) {
                            posX += moveX;
                            moved = true;
                        }
                        if (!map.isBlocked(posX, posY + moveY, width, height)) {
                            posY += moveY;
                            moved = true;
                        }
                        if (!moved && distToPlayer > 0) {
                            float tryX = dx / distToPlayer * 40f * delta;
                            float tryY = dy / distToPlayer * 40f * delta;
                            if (!map.isBlocked(posX + tryX, posY, width, height)) posX += tryX;
                            if (!map.isBlocked(posX, posY + tryY, width, height)) posY += tryY;
                        }
                    }
                }
            }
        } else {
            alertTimer = 0f;
            alertStarted = false;
            wander.update(delta, posX, posY);
            if (wander.hasTarget()) {
                state = GoombaState.RUN;
                posX = wander.moveX(posX, posY, delta);
                posY = wander.moveY(posX, posY, delta);
                if (wander.reachedTarget(posX, posY)) {
                    wander.stop();
                    state = GoombaState.IDLE;
                }
            } else {
                state = GoombaState.IDLE;
            }
        }

        if (knockbackTimer > 0f){
            knockbackTimer -= delta;

            float moveX = knockbackX * delta;
            float moveY = knockbackY * delta;

            if (!map.isBlocked(posX + moveX, posY, width, height)) posX += moveX;
            if (!map.isBlocked(posX, posY + moveY, width, height)) posY += moveY;

            return;
        }

        if (state != previousState) {
            tiempo = 0f;
            previousState = state;
        }
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null){
            TextureRegion currentFrame;
            if (state != GoombaState.DEAD){
                currentFrame = anim.getKeyFrame(tiempo, true);
            } else {
                currentFrame = anim.getKeyFrame(tiempo, false);
            }
            batch.draw(currentFrame, posX, posY, width, height);
        }
    }

    public void recibirDolor(int cantidad, float sourceX, float sourceY){
        if (damageTimer > 0f) return;

        vida.recibirDolor(cantidad);
        damageTimer = ConstantsGoomba.DAMAGE_COOLDOWN;

        float dx = posX - sourceX;
        float dy = posY - sourceY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist != 0){
            knockbackX = (dx / dist) * ConstantsGoomba.KNOCKBACK_FORCE;
            knockbackY = (dy / dist) * ConstantsGoomba.KNOCKBACK_FORCE;
        }

        knockbackTimer = ConstantsGoomba.KNOCKBACK_DURATION;

        if (vida.isMuerto()){
            state = GoombaState.DEAD;
            tiempo = 0f;
            return;
        }
        previousState = state;
        state = GoombaState.HURT;
        hurtTimer = ConstantsGoomba.DURACION_DOLOR;
    }

    public boolean isDead(){
        return vida.isMuerto();
    }

    @Override
    public float getX() {return posX;}

    @Override
    public float getY() {return posY;}

    @Override
    public float getWidth() {return width;}

    @Override
    public float getHeight() {return height;}

    @Override
    public boolean collides(float px, float py, float pw, float ph) {
        return !(px + pw < posX || px > posX + width || py + ph < posY || py > posY + height);
    }
}
