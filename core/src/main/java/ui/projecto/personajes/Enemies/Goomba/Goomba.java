package ui.projecto.personajes.Enemies.Goomba;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.mecanicas.*;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.Enemies.EnemyPathFinder;
import ui.projecto.mecanicas.Enemies.EnemyVision;
import ui.projecto.mecanicas.Enemies.EnemyWander;
import ui.projecto.personajes.Enemies.Goomba.util.ConstantsGoomba;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Enemies.Goomba.animacion.AnimationManagerGoomba;
import ui.projecto.personajes.Enemies.Goomba.estado.GoombaState;

public class Goomba implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = ConstantsGoomba.WIDTH, height = ConstantsGoomba.HEIGHT;
    private final float velocidad = ConstantsGoomba.VELOCIDAD;

    private final Vida vida = new Vida(ConstantsGoomba.VIDA);
    private GoombaState state = GoombaState.IDLE, previousState = GoombaState.IDLE;
    private final AnimationManagerGoomba animations = new AnimationManagerGoomba();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean alertStarted = false;

    private float alertTimer = 0f;
    private float hurtTimer = 0f;
    private float damageTimer = 0f;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Goomba(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, ConstantsGoomba.WAIT_TIMER, ConstantsGoomba.DETECTED_PLAYER, map, width, height);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(ConstantsGoomba.DETECTED_PLAYER);
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
        animations.add(GoombaState.HURT, new Animation<>(0.1f, AnimationLoader.load(hurt, 3, 3)));
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
                state = GoombaState.RUN;
                alertStarted = true;
            }
        }
        if (vision.isPlayerInRange(x, y, player.x, player.y)) {
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
                Vector2 nextStep = pathFinder.findNextStep(x, y, player.x, player.y);

                float dx = player.x - x;
                float dy = player.y - y;
                float distToPlayer = (float)Math.sqrt(dx * dx + dy * dy);

                if (distToPlayer < 20f) {
                    if (distToPlayer > 0) {
                        float moveX = dx / distToPlayer * velocidad * delta;
                        float moveY = dy / distToPlayer * velocidad * delta;
                        if (!map.isBlocked(x + moveX, y, width, height)) x += moveX;
                        if (!map.isBlocked(x, y + moveY, width, height)) y += moveY;
                    }
                    return;
                }
                if (nextStep != null) {
                    float ndx = nextStep.x - x;
                    float ndy = nextStep.y - y;
                    float dist = (float)Math.sqrt(ndx * ndx + ndy * ndy);

                    if (dist > 1f) {
                        float moveX = ndx / dist * velocidad * delta;
                        float moveY = ndy / dist * velocidad * delta;

                        boolean moved = false;
                        if (!map.isBlocked(x + moveX, y, width, height)) {
                            x += moveX;
                            moved = true;
                        }
                        if (!map.isBlocked(x, y + moveY, width, height)) {
                            y += moveY;
                            moved = true;
                        }
                        if (!moved && distToPlayer > 0) {
                            float tryX = dx / distToPlayer * velocidad * delta;
                            float tryY = dy / distToPlayer * velocidad * delta;
                            if (!map.isBlocked(x + tryX, y, width, height)) x += tryX;
                            if (!map.isBlocked(x, y + tryY, width, height)) y += tryY;
                        }
                    }
                }
            }
        } else {
            alertTimer = 0f;
            alertStarted = false;
            wander.update(delta, x, y);
            if (wander.hasTarget()) {
                state = GoombaState.RUN;
                x = wander.moveX(x, y, delta);
                y = wander.moveY(x, y, delta);
                if (wander.reachedTarget(x, y)) {
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

            if (!map.isBlocked(x + moveX, y, width, height)) x += moveX;
            if (!map.isBlocked(x, y + moveY, width, height)) y += moveY;

            if (knockbackTimer <= 0f && state == GoombaState.HURT) {
                state = GoombaState.RUN;
            }
            return;
        }

        if (state != previousState) {
            tiempo = 0f;
            previousState = state;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null){
            TextureRegion currentFrame;
            if (state != GoombaState.DEAD){
                currentFrame = anim.getKeyFrame(tiempo, true);
            } else {
                currentFrame = anim.getKeyFrame(tiempo, false);
            }
            batch.draw(currentFrame, x, y, width, height);
        }
    }

    @Override
    public void recibirDolor(int cantidad, float sourceX, float sourceY){
        if (damageTimer > 0f) return;

        vida.recibirDolor(cantidad);
        damageTimer = ConstantsGoomba.DAMAGE_COOLDOWN;

        float dx = x - sourceX;
        float dy = y - sourceY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist != 0){
            float knockback_force = ConstantsGoomba.KNOCKBACK_FORCE;
            knockbackX = (dx / dist) * knockback_force;
            knockbackY = (dy / dist) * knockback_force;
        }

        knockbackTimer = ConstantsGoomba.KNOCKBACK_DURATION;

        if (vida.isMuerto()){
            state = GoombaState.DEAD;
            tiempo = 0f;
            return;
        }
        previousState = state;
        state = GoombaState.HURT;
        hurtTimer = ConstantsGoomba.DURACION_HURT;
    }

    public boolean isDead(){
        return vida.isMuerto();
    }

    @Override
    public float getX() {return x;}

    @Override
    public float getY() {return y;}

    @Override
    public float getWidth() {return width;}

    @Override
    public float getHeight() {return height;}

    @Override
    public boolean collides(float px, float py, float pw, float ph) {
        return !(px + pw < x || px > x + width || py + ph < y || py > y + height);
    }
}
