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
import ui.projecto.personajes.Enemies.Amongus.Manager.AmongusAudioManager;
import ui.projecto.personajes.Enemies.Amongus.Manager.AnimationManagerAmongus;
import ui.projecto.personajes.Enemies.Amongus.State.AmongusState;
import ui.projecto.personajes.Enemies.Amongus.Util.ConstantsAmongus;
import ui.projecto.personajes.Player.Player;

public class Amongus implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = ConstantsAmongus.WIDTH, height = ConstantsAmongus.HEIGHT;
    private final float velocidad = ConstantsAmongus.VELOCIDAD;

    private final Vida vida = new Vida(ConstantsAmongus.VIDA);
    private AmongusState state = AmongusState.IDLE, previousState = AmongusState.IDLE;
    private final AnimationManagerAmongus animations = new AnimationManagerAmongus();
    private final AmongusAudioManager audio = new AmongusAudioManager();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;
    private boolean alertStarted = false;

    private float attackTimer = 0f;
    private float alertTimer = 0f;
    private float hurtTimer = 0f;
    private float damageTimer = 0f;

    private final float attackDuration = ConstantsAmongus.ATTACK_DURATION;
    private final float attackRange = ConstantsAmongus.ATTACK_RANGE;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Amongus(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, ConstantsAmongus.WAIT_TIMER, ConstantsAmongus.DETECTED_PLAYER, map, width, height);
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

        Texture attack1 = new Texture("enemy/amongus/amongus_ataque1.png");
        Texture attack2 = new Texture("enemy/amongus/amongus_ataque2.png");
        Texture attack3 = new Texture("enemy/amongus/amongus_ataque3.png");

        animations.add(AmongusState.IDLE, new Animation<>(1f, AnimationLoader.load(idle, 1,1)));
        animations.add(AmongusState.RUN, new Animation<>(0.06f, AnimationLoader.load(run, 4,3)));
        animations.add(AmongusState.ALERT, new Animation<>(0.04f, AnimationLoader.load(alert, 3,3)));
        animations.add(AmongusState.HURT, new Animation<>(0.03f, AnimationLoader.load(hurt, 3,3)));
        animations.add(AmongusState.DEAD, new Animation<>(0.05f, AnimationLoader.load(dead, 4,4)));

        animations.add(AmongusState.ATTACK_1, new Animation<>(0.08f, AnimationLoader.load(attack1, 3,3)));
        animations.add(AmongusState.ATTACK_2, new Animation<>(0.08f, AnimationLoader.load(attack2, 3,3)));
        animations.add(AmongusState.ATTACK_3, new Animation<>(0.08f, AnimationLoader.load(attack3, 3,3)));
    }

    @Override
    public void update(float delta, Player player) {
        tiempo += delta;

        if (vida.isMuerto()){
            if (state != AmongusState.DEAD) {
                audio.stopRun();
                audio.playDead();
            }
            state = AmongusState.DEAD;
            return;
        }

        if (damageTimer > 0f) damageTimer -= delta;

        if (state == AmongusState.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                state = AmongusState.RUN;
                audio.playRun();
            }
        }

        if (state == AmongusState.ATTACK_1 || state == AmongusState.ATTACK_2 || state == AmongusState.ATTACK_3) {
            attackTimer += delta;
            if (attackTimer >= attackDuration) {
                state = AmongusState.RUN;
            }
            return;
        }

        if (vision.isPlayerInRange(x,y, player.x, player.y)) {
            if (!alertStarted) {
                audio.stopRun();
                audio.playAlert();
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
                    audio.playRun();
                }
            } else if (state == AmongusState.RUN) {
                audio.playRun();
                float dx = player.x - x;
                float dy = player.y - y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < attackRange) {
                    startAttack();
                    return;
                }
                Vector2 nextStep = pathFinder.findNextStep(x,y,player.x,player.y);
                if (nextStep != null) {
                    float ndx = nextStep.x - x;
                    float ndy = nextStep.y - y;
                    float length = (float) Math.sqrt(ndx * ndx + ndy * ndy);

                    if (length != 0) {
                        ndx /= length;
                        ndy /= length;
                    }
                    float moveX = x + ndx * velocidad * delta;
                    float moveY = y + ndy * velocidad * delta;

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
                audio.playRun();
                state = AmongusState.RUN;

                if (x < oldX) facingRight = true;
                else if (x > oldX) facingRight = false;

                if (wander.reachedTarget(x,y)){
                    wander.stop();
                    state = AmongusState.IDLE;
                }
            } else {
                audio.stopRun();
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
            if (state == AmongusState.DEAD || state == AmongusState.HURT) {
                currentFrame = anim.getKeyFrame(tiempo, false);
            } else {
                currentFrame = anim.getKeyFrame(tiempo, true);
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

    public boolean isAttackingPlayer(Player player) {
        if (state != AmongusState.ATTACK_1 &&
            state != AmongusState.ATTACK_2 &&
            state != AmongusState.ATTACK_3) return false;

        float dx = player.x - x;
        float dy = player.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist <= attackRange;
    }

    private void startAttack() {
        audio.stopRun();
        int r = (int) (Math.random() * 3);
        switch (r) {
            case 1:{
                state = AmongusState.ATTACK_2;
                audio.playAttack2();
                break;
            }
            case 2:{
                state = AmongusState.ATTACK_3;
                audio.playAttack3();
                break;
            }
            default:{
                state = AmongusState.ATTACK_1;
                audio.playAttack1();
                break;
            }
        }
        attackTimer = 0f;
        tiempo = 0f;
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
        if (damageTimer > 0f || vida.isMuerto()) return;
        vida.recibirDolor(cantidad);
        damageTimer = ConstantsAmongus.DAMAGE_COOLDOWN;

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
            audio.stopRun();
            audio.playDead();
            state = AmongusState.DEAD;
            tiempo = 0f;
            return;
        }
        audio.stopRun();
        audio.playHurt();
        previousState = state;
        state = AmongusState.HURT;
        hurtTimer = ConstantsAmongus.HURT_TIMER;
        tiempo = 0f;
    }

    @Override
    public boolean isDead() {
        return vida.isMuerto();
    }

    @Override
    public void stopAllSounds() {
        audio.stopRun();
    }
}
