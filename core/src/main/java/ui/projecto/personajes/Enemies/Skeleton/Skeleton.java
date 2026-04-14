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
import ui.projecto.personajes.Enemies.Skeleton.animacion.AnimationManagerSkeleton;
import ui.projecto.personajes.Enemies.Skeleton.estado.SkeletonState;
import ui.projecto.personajes.Enemies.Skeleton.util.ConstantsSkeleton;
import ui.projecto.personajes.Player.Player;

public class Skeleton implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = 32f, height = 32f;
    private final float velocidad = ConstantsSkeleton.VELOCIDAD;

    private final Vida vida = new Vida(80);
    private SkeletonState state = SkeletonState.IDLE, previousState = SkeletonState.IDLE;
    private final AnimationManagerSkeleton animations = new AnimationManagerSkeleton();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;

    private float alertTimer = ConstantsSkeleton.ALERT_TIMER;
    private boolean alertStarted = ConstantsSkeleton.ALERT_STARTED;

    private float hurtTimer = ConstantsSkeleton.HURT_TIMER;
    private float damageCooldown = ConstantsSkeleton.DAMAGE_COOLDOWN;


    private float ataqueCooldown = ConstantsSkeleton.ATAQUE_COOLDOWN;
    private final float attackCooldown = ConstantsSkeleton.ATTACK_COOLDOWN;
    private final float attackRange = ConstantsSkeleton.ATTACK_RANGE;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Skeleton(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, 2f, ConstantsSkeleton.DETECTED_PLAYER, map, width, height);
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
            state = SkeletonState.DEAD;
            return;
        }

        if (damageCooldown > 0f) damageCooldown -= delta;


        if (state == SkeletonState.HURT) {
            hurtTimer -= delta;
            if (hurtTimer <= 0f) {
                state = SkeletonState.RUN;
            }
        }

        if (state ==  SkeletonState.ATTACK) {
            float dx = player.x - x;
            float dy = player.y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            facingRight = dx > 0;

            if (dist > attackRange + 10f){
                state = SkeletonState.RUN;
                return;
            }

            if (ataqueCooldown <= 0f){
                ataqueCooldown = attackCooldown;
            }
        }

        if (vision.isPlayerInRange(x, y, player.x, player.y)) {
            if (!alertStarted) {
                state = SkeletonState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }
            if (state == SkeletonState.ALERT) {
                alertTimer += delta;
                if (alertTimer >= ConstantsSkeleton.ALERT_DURATION) {
                    state = SkeletonState.RUN;
                }
            } else if (state == SkeletonState.RUN) {
                Vector2 nextStep = pathFinder.findNextStep(x, y, player.x, player.y);
                float oldX = x;
                float dx = player.x - x;
                float dy = player.y - y;
                float distToPlayer = (float) Math.sqrt(dx * dx + dy * dy);

                if (distToPlayer < attackRange) {
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
    public boolean collides(float px, float py, float ew, float eh) {
        return !(px + ew < x ||
            px > x + width ||
            py + eh < y ||
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
        if (damageCooldown > 0f) return;

        vida.recibirDolor(cantidad);
        damageCooldown = 0.15f;

        float dx = x - sourceX;
        float dy = y - sourceY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist != 0){
            float force = ConstantsSkeleton.KNOCKBAR_FORCE;
            knockbackX = (dx / dist) * force;
            knockbackY = (dy / dist) * force;
            knockbackTimer = 0.2f;
        }

        facingRight = dx > 0;

        if (vida.isMuerto()){
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
}
