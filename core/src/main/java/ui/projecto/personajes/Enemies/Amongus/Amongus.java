package ui.projecto.personajes.Enemies.Amongus;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.mecanicas.AnimationLoader;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.Enemies.EnemyPathFinder;
import ui.projecto.mecanicas.Enemies.EnemyVision;
import ui.projecto.mecanicas.Enemies.EnemyWander;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;
import ui.projecto.personajes.Enemies.Amongus.animacion.AnimationManagerAmongus;
import ui.projecto.personajes.Enemies.Amongus.estado.AmongusState;
import ui.projecto.personajes.Enemies.Amongus.util.ConstantsAmongus;
import ui.projecto.personajes.Player.Player;

public class Amongus implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = 25f, height = 25f;
    private final float velocidad = ConstantsAmongus.VELOCIDAD;

    private final Vida vida = new Vida(100);
    private AmongusState state = AmongusState.IDLE, previousState = AmongusState.IDLE;
    private final AnimationManagerAmongus animations = new AnimationManagerAmongus();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;

    private float alertTimer = ConstantsAmongus.ALERT_TIMER;
    private boolean alertStarted = ConstantsAmongus.ALERT_STARTED;

    private float hurtTimer = ConstantsAmongus.HURT_TIMER;
    private float damageCooldown = ConstantsAmongus.DAMAGE_COOLDOWN;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Amongus(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, 5f, ConstantsAmongus.DETECTED_PLAYER, map, width, height);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(ConstantsAmongus.DETECTED_PLAYER);
        loadAnimation();
    }

    private void loadAnimation() {
        Texture idle = new Texture("enemy/amongus/amongus.png");
        Texture run = new Texture("enemy/amongus/amongus_corriendo.png");
        Texture alert = new Texture("enemy/amongus/amongus_sorprendido.png");
        Texture hurt = new Texture("enemy/amongus/amongus_herido.png");
        Texture dead = new Texture("enemy/amongus/amongus_muerte.png");

        animations.add(AmongusState.IDLE, new Animation<>(1f, AnimationLoader.load(idle, 1,1)));
        animations.add(AmongusState.RUN, new Animation<>(0.06f, AnimationLoader.load(run, 4,3)));
        animations.add(AmongusState.ALERT, new Animation<>(0.04f, AnimationLoader.load(alert, 3,3)));
        animations.add(AmongusState.HURT, new Animation<>(0.03f, AnimationLoader.load(hurt, 3,3)));
        animations.add(AmongusState.DEAD, new Animation<>(0.05f, AnimationLoader.load(dead, 4,4)));
    }

    @Override
    public void update(float delta, Player player) {
        tiempo += delta;

        if (vida.isMuerto()){
            state = AmongusState.DEAD;
            return;
        }
        previousState = state;

        if (damageCooldown > 0f) damageCooldown -= delta;

        if (state == AmongusState.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                state = AmongusState.IDLE;
            }
        }

        if (vision.isPlayerInRange(x,y, player.x, player.y)) {
            if (!alertStarted) {
                state = AmongusState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }

            if (state == AmongusState.ALERT) {
                alertTimer += delta;
                facingRight = player.x < x;
                if (alertTimer >= ConstantsAmongus.ALERT_DURATION){
                    state = AmongusState.RUN;
                }
            } else if (state == AmongusState.RUN) {
                Vector2 nextStep = pathFinder.findNextStep(x,y,player.x,player.y);

                if (nextStep != null) {
                    float dx = nextStep.x - x;
                    float dy = nextStep.y - y;
                    float length = (float) Math.sqrt(dx * dx + dy * dy);
                    if (length != 0) {
                        dx /= length;
                        dy /= length;
                    }
                    float moveX = x + dx * velocidad * delta;
                    float moveY = y + dy * velocidad * delta;

                    if (!map.isBlocked(moveX, y , width, height)) x = moveX;
                    if (!map.isBlocked(x, moveY , width, height)) y = moveY;

                    facingRight = dx < 0;
                }
            }
        } else {
            alertStarted = false;
            alertTimer = 0f;

            wander.update(delta, x, y);

            if (wander.hasTarget()) {
                float oldX = x;

                x = wander.moveX(x,y,delta);
                y = wander.moveY(x,y,delta);

                state = AmongusState.RUN;

                if (x < oldX) facingRight = true;
                else if (x > oldX) facingRight = false;

                if (wander.reachedTarget(x,y)){
                    wander.stop();
                    state = AmongusState.IDLE;
                }
            } else {
                state = AmongusState.IDLE;
            }
        }

        if (knockbackTimer > 0f){
            knockbackTimer -= delta;
            float moveX = knockbackX * delta;
            float moveY = knockbackY * delta;

            if (!map.isBlocked(x + moveX , y , width, height)) x += moveX;
            if (!map.isBlocked(x, y + moveY , width, height)) y += moveY;

            return;
        }
        if (state != previousState){
            tiempo = 0f;
            previousState = state;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null) {
            TextureRegion currentFrame;
            if (state != AmongusState.DEAD) {
                currentFrame = anim.getKeyFrame(tiempo, true);
            } else {
                currentFrame = anim.getKeyFrame(tiempo, false);
            }
            TextureRegion frame = new TextureRegion(currentFrame);
            if (facingRight && !frame.isFlipX()){
                frame.flip(true, false);
            }
            if (!facingRight && frame.isFlipX()){
                frame.flip(false, true);
            }
            batch.draw(frame, x, y, width, height);
        }
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean collides(float px, float py, float pw, float ph) {
        return !(px + pw < x ||
            px > x + width ||
            py + ph < y ||
            py > y + height);
    }

    @Override
    public void recibirDolor(int cantidad, float sourceX, float sourceY) {

        if (damageCooldown > 0f || vida.isMuerto()) return;
        vida.recibirDolor(cantidad);
        damageCooldown = 0.2f;

        float dx = x - sourceX;
        float dy = y - sourceY;

        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist != 0) {
            dx /= dist;
            dy /= dist;
        }

        float force = ConstantsAmongus.KNOCKBACK_FORCE;
        knockbackX = dx * force;
        knockbackY = dy * force;
        knockbackTimer = ConstantsAmongus.KNOCKBACK_DURATION;

        facingRight = dx > 0;

        if (vida.isMuerto()) {
            state = AmongusState.DEAD;
            tiempo = 0f;
            return;
        }
        previousState = state;
        state = AmongusState.HURT;
        tiempo = 0f;
        hurtTimer = 0.3f;
    }


    @Override
    public boolean isDead() {
        return vida.isMuerto();
    }
}
