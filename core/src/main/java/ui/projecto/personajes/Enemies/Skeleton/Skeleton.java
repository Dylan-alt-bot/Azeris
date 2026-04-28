package ui.projecto.personajes.Enemies.Skeleton;

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
import ui.projecto.personajes.Enemies.Skeleton.Manager.AnimationManagerSkeleton;
import ui.projecto.personajes.Enemies.Skeleton.Manager.SkeletonAudioManager;
import ui.projecto.personajes.Enemies.Skeleton.State.SkeletonState;
import ui.projecto.personajes.Enemies.Skeleton.Util.ConstantsSkeleton;
import ui.projecto.personajes.Player.Player;

public class Skeleton implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = ConstantsSkeleton.WIDTH, height = ConstantsSkeleton.HEIGHT;
    private final float velocidad = ConstantsSkeleton.VELOCIDAD;

    private final Vida vida = new Vida(ConstantsSkeleton.VIDA);
    private SkeletonState state = SkeletonState.IDLE, previousState = SkeletonState.IDLE;
    private final AnimationManagerSkeleton animations = new AnimationManagerSkeleton();
    private final SkeletonAudioManager audio = new SkeletonAudioManager();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;
    private boolean alertStarted = false;

    private float alertTimer = 0f;
    private float hurtTimer = 0f;
    private float attackTimer = 0f;
    private float damageTimer = 0f;

    private final float attackCooldown = ConstantsSkeleton.ATTACK_COOLDOWN;
    private final float attackRange = ConstantsSkeleton.ATTACK_RANGE;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Skeleton(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, ConstantsSkeleton.WAIT_TIMER, ConstantsSkeleton.DETECTED_PLAYER, map, width, height);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(ConstantsSkeleton.DETECTED_PLAYER);
        loadAnimation();
    }

    private void loadAnimation() {
        Texture idle = new Texture("enemy/skeleton/skeleton.png");
        Texture walk = new Texture("enemy/skeleton/skeleton_caminando.png");
        Texture alert = new Texture("enemy/skeleton/skeleton_alert.png");
        Texture run = new Texture("enemy/skeleton/skeleton_correr.png");
        Texture hurt = new Texture("enemy/skeleton/skeleton_dolor.png");
        Texture attack = new Texture("enemy/skeleton/skeleton_ataque.png");
        Texture dead = new Texture("enemy/skeleton/skeleton_muerte.png");

        animations.add(SkeletonState.IDLE, new Animation<>(0.1f, AnimationLoader.load(idle, 5,5)));
        animations.add(SkeletonState.WALK, new Animation<>(0.04f, AnimationLoader.load(walk, 5,4)));
        animations.add(SkeletonState.ALERT, new Animation<>(0.05f, AnimationLoader.load(alert, 1,1)));
        animations.add(SkeletonState.RUN, new Animation<>(0.03f, AnimationLoader.load(run, 5,4)));
        animations.add(SkeletonState.HURT, new Animation<>(0.1f, AnimationLoader.load(hurt, 3,3)));
        animations.add(SkeletonState.ATTACK, new Animation<>(0.1f, AnimationLoader.load(attack, 4,3)));
        animations.add(SkeletonState.DEAD, new Animation<>(0.08f, AnimationLoader.load(dead, 3,3)));
    }

    @Override
    public void update(float delta, Player player) {
        tiempo += delta;

        if (vida.isMuerto()) {
            if (state != SkeletonState.DEAD) {
                audio.stopRun();
                audio.playDeath();
                tiempo = 0f;
            }
            state = SkeletonState.DEAD;
            return;
        }
        if (state == SkeletonState.DEAD) {
            audio.dispose();
        }

        if (damageTimer > 0f) damageTimer -= delta;


        if (state == SkeletonState.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                state = SkeletonState.RUN;
                audio.playRun();
            }
        }

        if (state ==  SkeletonState.ATTACK) {
            float dx = player.x - x;
            float dy = player.y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            facingRight = dx > 0;

            if (dist > attackRange){
                state = SkeletonState.RUN;
                return;
            }

            if (attackTimer <= 0f){
                attackTimer = attackCooldown;
            }
        }

        if (vision.isPlayerInRange(x, y, player.x, player.y)) {
            if (!alertStarted) {
                audio.stopRun();
                audio.playAlert();
                state = SkeletonState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }
            if (state == SkeletonState.ALERT) {
                alertTimer += delta;
                if (alertTimer >= ConstantsSkeleton.ALERT_DURATION) {
                    state = SkeletonState.RUN;
                    audio.playRun();
                }
            } else if (state == SkeletonState.RUN) {
                audio.playRun();
                Vector2 nextStep = pathFinder.findNextStep(x, y, player.x, player.y);
                float oldX = x;
                float dx = player.x - x;
                float dy = player.y - y;
                float distToPlayer = (float) Math.sqrt(dx * dx + dy * dy);

                if (distToPlayer < attackRange) {
                    audio.playAttack();
                    state = SkeletonState.ATTACK;
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
                    float length = (float) Math.sqrt(ndx * ndx + ndy * ndy);
                    if (length > 1f) {
                        float moveX = ndx / length *  velocidad * delta;
                        float moveY = ndy / length * velocidad * delta;
                        if (!map.isBlocked(x + moveX, y, width, height)) x += moveX;
                        if (!map.isBlocked(x, y + moveY, width, height)) y += moveY;
                    }
                }
                facingRight = x > oldX;
            }
        } else {
            alertStarted = false;
            alertTimer = 0f;
            wander.update(delta, x, y);
            if (wander.hasTarget()) {
                if (state != SkeletonState.WALK) {
                    audio.stopRun();
                    audio.playWalk();
                }
                state = SkeletonState.WALK;
                float oldX = x;
                x = wander.moveX(x, y, delta);
                y = wander.moveY(x, y, delta);

                if (x > oldX) {
                    facingRight = true;
                } else if (x < oldX) {
                    facingRight = false;
                }

                if (wander.reachedTarget(x, y)) {
                    wander.stop();
                    state = SkeletonState.IDLE;
                }

            } else {
                audio.stopRun();
                state = SkeletonState.IDLE;
            }
        }

        if (knockbackTimer > 0f) {
            knockbackTimer -= delta;
            float moveX = knockbackX * delta;
            float moveY = knockbackY * delta;
            if (!map.isBlocked(x + moveX, y, width, height)) x += moveX;
            if (!map.isBlocked(x, y + moveY, width, height)) y += moveY;
            return;
        }

        if (state != previousState) {
            tiempo = 0f;
            previousState = state;
        }
    }

    public boolean isAttackingPlayer(Player player) {
        if (state != SkeletonState.ATTACK) return false;

        float dx = player.x - x;
        float dy = player.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist <= attackRange;
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
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null) {
            TextureRegion currentFrame;
            if (state != SkeletonState.DEAD){
                currentFrame = anim.getKeyFrame(tiempo, true);
            } else {
                currentFrame = anim.getKeyFrame(tiempo, false);
            }
            TextureRegion frame = new TextureRegion(currentFrame);
            if (facingRight && !frame.isFlipX()){
                frame.flip(true, false);
            }
            if (!facingRight && frame.isFlipX()){
                frame.flip(true, false);
            }
            batch.draw(frame, x, y, width, height);
        }
    }

    @Override
    public void recibirDolor(int cantidad, float sourceX, float sourceY) {
        if (damageTimer > 0f) return;
        vida.recibirDolor(cantidad);
        audio.stopRun();
        audio.playHurt();
        damageTimer = 0.15f;

        float dx = x - sourceX;
        float dy = y - sourceY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist != 0){
            float force = ConstantsSkeleton.KNOCKBACK_FORCE;
            knockbackX = (dx / dist) * force;
            knockbackY = (dy / dist) * force;
            knockbackTimer = ConstantsSkeleton.KNOCKBACK_COOLDOWN;
        }
        facingRight = dx > 0;
        if (vida.isMuerto()){
            audio.stopRun();
            audio.playDeath();
            state = SkeletonState.DEAD;
            tiempo = 0f;
            return;
        }
        previousState = state;
        state = SkeletonState.HURT;
        hurtTimer = 0.3f;
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
