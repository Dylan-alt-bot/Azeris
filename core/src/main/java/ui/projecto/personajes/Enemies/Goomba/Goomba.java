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
import ui.projecto.personajes.Enemies.Goomba.Manager.GoombaAudioManager;
import ui.projecto.personajes.Enemies.Goomba.Util.ConstantsGoomba;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Enemies.Goomba.Manager.AnimationManagerGoomba;
import ui.projecto.personajes.Enemies.Goomba.State.GoombaState;
import ui.projecto.personajes.Player.State.PlayerState;

public class Goomba implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = ConstantsGoomba.WIDTH, height = ConstantsGoomba.HEIGHT;
    private final float velocidad = ConstantsGoomba.VELOCIDAD;

    private final Vida vida = new Vida(ConstantsGoomba.VIDA);
    private GoombaState state = GoombaState.IDLE, previousState = GoombaState.IDLE;
    private final AnimationManagerGoomba animations = new AnimationManagerGoomba();
    private final GoombaAudioManager audio = new GoombaAudioManager();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean alertStarted = false;
    private boolean facingRight = false;

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
            if (state != GoombaState.DEAD){
                audio.stopRun();
                audio.playDeath();
                tiempo = 0f;
            }
            state = GoombaState.DEAD;
            return;
        }
        if (damageTimer > 0f){
            damageTimer -= delta;
        }
        if (state == GoombaState.HURT){
            hurtTimer -= delta;
            if (hurtTimer <= 0f){
                state = GoombaState.RUN;
                audio.playRun();
            }
        }

        float dx = player.x - x;
        float dy = player.y - y;
        float distToPlayer = (float) Math.sqrt(dx * dx + dy * dy);

        boolean playerInRange = vision.isPlayerInRange(x, y, player.x, player.y);
       if (playerInRange) {
            vision.updateLastSeenPosition(player.x, player.y, tiempo);
        }
        boolean hasLineOfSight = false;
        if (playerInRange) {
            if (distToPlayer < 20f) {
                hasLineOfSight = true;
            } else {
                hasLineOfSight = pathFinder.hasLineOfSight(x, y, player.x, player.y);
            }
        }

        boolean hasRecentMemory = vision.hasRecentMemory(tiempo, ConstantsGoomba.MEMORY_DURATION);
        Vector2 targetPosition;
        if (playerInRange && hasLineOfSight) {
            targetPosition = new Vector2(player.x, player.y);
            if (!alertStarted) {
                audio.stopRun();
                audio.playAlert();
                state = GoombaState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }
            if (state == GoombaState.ALERT) {
                alertTimer += delta;
                if (alertTimer >= ConstantsGoomba.ALERT_DURATION) {
                    state = GoombaState.RUN;
                    audio.playRun();
                }
            } else if (state == GoombaState.RUN) {
                executeChase(delta, targetPosition);
            }
        }
        else if (hasRecentMemory) {
            targetPosition = vision.getLastSeenPosition();
            if (state != GoombaState.RUN) {
                audio.stopRun();
                audio.playRun();
            }
            state = GoombaState.RUN;
            executeMovementToTarget(delta, targetPosition);

            float memDx = targetPosition.x - x;
            float memDy = targetPosition.y - y;
            float distToMemory = (float) Math.sqrt(memDx * memDx + memDy * memDy);

            boolean canNowSeePlayer = pathFinder.hasLineOfSight(x, y, player.x, player.y);

            if (canNowSeePlayer) {
                vision.updateLastSeenPosition(player.x, player.y, tiempo);
                alertStarted = true;
                state = GoombaState.RUN;
                audio.playRun();
            } else if (distToMemory < 20f) {
                if (vision.getTimeSinceLastSeen(tiempo) > ConstantsGoomba.MEMORY_DURATION) {
                    vision.clearMemory();
                    alertStarted = false;
                    state = GoombaState.IDLE;
                }
            }
        }
        else {
            vision.clearMemory();
            alertStarted = false;
            alertTimer = 0f;
            wander.update(delta, x, y);

            if (wander.hasTarget()) {
                if (state != GoombaState.RUN) {
                    audio.stopRun();
                    audio.playRun();
                }
                state = GoombaState.RUN;
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
                    state = GoombaState.IDLE;
                }
            } else {
                audio.stopRun();
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
    
    private void executeChase(float delta, Vector2 target) {
        audio.playRun();
        executeMovementToTarget(delta, target);
    }

    private void executeMovementToTarget(float delta, Vector2 target) {
        Vector2 nextStep = pathFinder.findNextStep(x, y, target.x, target.y);
        if (nextStep != null) {
            float ndx = nextStep.x - x;
            float ndy = nextStep.y - y;
            float length = (float) Math.sqrt(ndx * ndx + ndy * ndy);

            if (length > 0.01f) {
                float moveX = ndx / length * velocidad * delta;
                float moveY = ndy / length * velocidad * delta;

                float newX = x + moveX;
                float newY = y + moveY;

                boolean canMoveDiagonal = !map.isBlocked(newX, newY, width, height);
                boolean canMoveX = !map.isBlocked(newX, y, width, height);
                boolean canMoveY = !map.isBlocked(x, newY, width, height);

                if (canMoveDiagonal) {
                    x = newX;
                    y = newY;
                } else if (canMoveX) {
                    x = newX;
                } else if (canMoveY) {
                    y = newY;
                } else {
                    Vector2 alternative = findAlternativeMove(target);
                    if (alternative != null) {
                        x = alternative.x;
                        y = alternative.y;
                    }
                }
                facingRight = moveX > 0;
            }
        } else {
            moveDirectlyTowardsTarget(delta, target);
        }
    }

    private Vector2 findAlternativeMove(Vector2 target) {
        float[] offsets = {-velocidad, velocidad};
        for (float ox : offsets) {
            for (float oy : offsets) {
                float newX = x + ox;
                float newY = y + oy;
                if (!map.isBlocked(newX, newY, width, height) &&
                    Math.abs(newX - target.x) < Math.abs(x - target.x)) {
                    return new Vector2(newX, newY);
                }
            }
        }
        return null;
    }

    private void moveDirectlyTowardsTarget(float delta, Vector2 target) {
        float dx = target.x - x;
        float dy = target.y - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.01f) {
            float moveX = dx / length * velocidad * delta;
            float moveY = dy / length * velocidad * delta;

            float newX = x + moveX;
            float newY = y + moveY;

            if (!map.isBlocked(newX, y, width, height)) x = newX;
            if (!map.isBlocked(x, newY, width, height)) y = newY;

            facingRight = moveX > 0;
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
            audio.stopRun();
            audio.playDeath();
            state = GoombaState.DEAD;
            tiempo = 0f;
            return;
        }
        audio.stopRun();
        audio.playHit();
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
    @Override
    public void stopAllSounds() {
        audio.stopRun();
    }
}
