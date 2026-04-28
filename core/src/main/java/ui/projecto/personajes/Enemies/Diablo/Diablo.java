package ui.projecto.personajes.Enemies.Diablo;

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
import ui.projecto.personajes.Enemies.Diablo.Manager.AnimationManagerDiablo;
import ui.projecto.personajes.Enemies.Diablo.Manager.DiabloAudioManager;
import ui.projecto.personajes.Enemies.Diablo.State.DiabloState;
import ui.projecto.personajes.Enemies.Diablo.Util.ConstantsDiablo;
import ui.projecto.personajes.Player.Player;

public class Diablo implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = ConstantsDiablo.WIDTH, height = ConstantsDiablo.HEIGHT;
    private final float hitboxWidth = ConstantsDiablo.HITBOX_WIDTH, hitboxHeight = ConstantsDiablo.HITBOX_HEIGHT;
    private final float velocidad = ConstantsDiablo.VELOCIDAD;

    private final Vida vida = new Vida(ConstantsDiablo.VIDA);
    private DiabloState state = DiabloState.IDLE;
    private final AnimationManagerDiablo animations = new AnimationManagerDiablo();
    private final DiabloAudioManager audio = new DiabloAudioManager();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;
    private boolean alertStarted = false;

    private float alertTimer = 0f;
    private float attackTimer  = 0f;
    private float hurtTimer  = 0f;

    private float alertDuration = ConstantsDiablo.ALERT_DURATION;
    private final float attackRange = ConstantsDiablo.ATTACK_RANGE;
    private final float attackCooldown = ConstantsDiablo.ATTACK_COOLDOWN;
    private final float hurtDuration = ConstantsDiablo.HURT_DURATION;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Diablo(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, ConstantsDiablo.WAIT_TIME, ConstantsDiablo.DETECTED_PLAYER, map, width, height);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(ConstantsDiablo.DETECTED_PLAYER);
        loadAnimation();
    }

    private void loadAnimation() {
        Texture idle = new Texture("enemy/diablo/diablo.png");
        Texture walk = new Texture("enemy/diablo/diablo_caminando.png");
        Texture alert = new Texture("enemy/diablo/diablo_sorprendido.png");
        Texture attack = new Texture("enemy/diablo/diablo_atacando.png");
        Texture hurt = new Texture("enemy/diablo/diablo_herido.png");
        Texture defeat = new Texture("enemy/diablo/diablo_derrotado.png");

        animations.add(DiabloState.IDLE, new Animation<>(0.08f, AnimationLoader.load(idle, 4,3)));
        animations.add(DiabloState.WALK, new Animation<>(0.06f, AnimationLoader.load(walk, 4,3)));
        animations.add(DiabloState.ALERT, new Animation<>(1f, AnimationLoader.load(alert, 1,1)));
        animations.add(DiabloState.ATTACK, new Animation<>(0.05f, AnimationLoader.load(attack, 4,3)));
        animations.add(DiabloState.HURT, new Animation<>(0.05f, AnimationLoader.load(hurt, 3,3)));
        animations.add(DiabloState.DEFEAT, new Animation<>(0.08f, AnimationLoader.load(defeat, 7,6)));
    }

    @Override
    public void update(float delta, Player player) {
        tiempo += delta;

        if (vida.isMuerto()){
            attackTimer = 0f;
            alertTimer = 0f;
            wander.stop();
            if (state != DiabloState.DEFEAT) {
                audio.playDefeat();
                state = DiabloState.DEFEAT;
                tiempo = 0f;
            }
            return;
        }
        DiabloState previousState = state;

        if (state == DiabloState.ATTACK) {
            attackTimer += delta;
            updateFacing(player.x - x);
            if (attackTimer >= attackCooldown){
                state = DiabloState.WALK;
            }
            return;
        }

        if (state == DiabloState.HURT){
            hurtTimer -= delta;
            updateFacing(player.x - x);
            if (knockbackTimer > 0f){
                knockbackTimer -= delta;
                float moveX = knockbackX * velocidad * delta;
                float moveY = knockbackY * velocidad * delta;

                if (!map.isBlocked(x + moveX, y, width, height)) x += moveX;
                if (!map.isBlocked(x, y + moveY, width, height)) y += moveY;
            }

            if (hurtTimer <= 0f){
                state = DiabloState.WALK;
                audio.playWalk();
                hurtTimer = 0f;
            }
            return;
        }

        if (vision.isPlayerInRange(x,y, player.x, player.y)){
            if (!alertStarted) {
                audio.stopMovementAudio();
                audio.playAlert();
                state = DiabloState.ALERT;
                alertTimer = 0f;
                alertStarted = true;
                wander.stop();
            }
            if (state == DiabloState.ALERT){
                alertTimer += delta;
                updateFacing(player.x - x);
                if (alertTimer >= ConstantsDiablo.ALERT_DURATION){
                    state = DiabloState.WALK;
                    audio.playWalk();
                }
                return;
            }
            if (state == DiabloState.WALK) {
                audio.playWalk();
                Vector2 nextStep = pathFinder.findNextStep(x, y, player.x, player.y);
                float oldX = x;
                float oldY = y;
                if (nextStep != null){
                    float dx = nextStep.x - x;
                    float dy = nextStep.y - y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > 1f){
                        float moveX = (dx / dist) * velocidad * delta;
                        float moveY = (dy / dist) * velocidad * delta;

                        if (!map.isBlocked(getHitboxX() + moveX, getHitboxY(), width, height)) {
                            x += moveX;
                            if (collidesWithPlayer(player)) x = oldX;
                        }
                        if (!map.isBlocked(getHitboxX(), getHitboxY() + moveY, width, height)) {
                            y += moveY;
                            if (collidesWithPlayer(player)) y = oldY;
                        }
                    }
                    if (isPlayerInAttackRange(player)){
                        audio.stopWalk();
                        audio.playAttack();
                        state = DiabloState.ATTACK;
                        attackTimer = 0f;
                    }
                }
                updateFacing(x - oldX);
                resolvePlayerCollision(player);
            }
        } else {
            alertStarted = false;
            alertTimer = 0f;
            if (!wander.hasTarget()){
                wander.update(delta, x, y);
            }
            if (wander.hasTarget()){
                audio.playWalk();
                state = DiabloState.WALK;

                float oldX = x;
                x = wander.moveX(x,y,delta);
                y = wander.moveY(x,y,delta);
                updateFacing(x - oldX);
                if (wander.reachedTarget(x,y)){
                    wander.stop();
                    state = DiabloState.IDLE;
                }
            } else {
                audio.playIdle();
                state = DiabloState.IDLE;
            }
            resolvePlayerCollision(player);
        }

        if (knockbackTimer > 0f) {
            knockbackTimer -= delta;
            float moveX = knockbackX * delta;
            float moveY = knockbackY * delta;
            if (!map.isBlocked(x + moveX, y, width, height)) x = x + moveX;
            if (!map.isBlocked(x, y + moveY, width, height)) y = y + moveY;
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

        if (anim != null) {
            boolean loop = state != DiabloState.DEFEAT;
            TextureRegion baseFrame = anim.getKeyFrame(tiempo, loop);
            TextureRegion frame = new TextureRegion(baseFrame);

            if (facingRight && frame.isFlipX()) frame.flip(true, false);
            if (!facingRight && !frame.isFlipX()) frame.flip(true, false);

            batch.draw(frame, x, y, width, height);
        }
    }

    private void updateFacing(float dx) {
        if (Math.abs(dx) > 0.01f) {
            facingRight = dx < 0;
        }
    }

    public boolean isAttackingPlayer(Player player) {
        if (state != DiabloState.ATTACK) return false;
        if (vida.isMuerto()) return false;

        return isPlayerInAttackRange(player);
    }

    private boolean collidesWithPlayer(Player player) {
        float hx = getHitboxX();
        float hy = getHitboxY();

        float pw = player.getWidth();
        float ph = player.getHeight();

        return !(player.x + pw < hx ||
            player.x > hx + hitboxWidth ||
            player.y + ph < hy ||
            player.y > hy + hitboxHeight);
    }

    private boolean isPlayerInAttackRange(Player player) {
        float px = player.x;
        float py = player.y;

        float playerCenterX = px + player.getWidth() / 2f;
        float playerCenterY = py + player.getHeight() / 2f;

        float diabloCenterX = getHitboxX() + hitboxWidth / 2f;
        float diabloCenterY = getHitboxY() + hitboxHeight / 2f;

        float dx = playerCenterX - diabloCenterX;
        float dy = playerCenterY - diabloCenterY;

        float dist2 = dx * dx + dy * dy;

        return dist2 <= attackRange * attackRange;
    }

    private void resolvePlayerCollision(Player player) {
        float hx = getHitboxX();
        float hy = getHitboxY();

        float px = player.x;
        float py = player.y;
        float pw = player.getWidth();
        float ph = player.getHeight();

        if (!(px + pw < hx || px > hx + hitboxWidth ||
            py + ph < hy || py > hy + hitboxHeight)) {

            float overlapX = (hx + hitboxWidth / 2f) - (px + pw / 2f);
            float overlapY = (hy + hitboxHeight / 2f) - (py + ph / 2f);

            if (Math.abs(overlapX) > Math.abs(overlapY)) {
                if (overlapX > 0) {
                    x += 2f;
                } else {
                    x -= 2f;
                }
            } else {
                if (overlapY > 0) {
                    y += 2f;
                } else {
                    y -= 2f;
                }
            }
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
        float hx = getHitboxX();
        float hy = getHitboxY();
        return !(px + pw < hx ||
            px > hx + hitboxWidth ||
            py + ph < hy ||
            py > hy + hitboxHeight);
    }

    @Override
    public void recibirDolor(int cantidad, float sourceX, float sourceY) {
        if (vida.isMuerto()) return;
        vida.recibirDolor(cantidad);
        audio.stopMovementAudio();
        audio.playHurt();
        state = DiabloState.HURT;
        tiempo = 0f;
        hurtTimer = hurtDuration;
        float dx = x - sourceX;
        float dy = y - sourceY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist != 0){
            knockbackX = dx / dist;
            knockbackY = dy / dist;
            knockbackTimer = ConstantsDiablo.KNOCKBACK_DURATION;
        }
    }

    @Override
    public boolean isDead() {
        return vida.isMuerto();
    }

    private float getHitboxX() {
        return x + (width - hitboxWidth) / 2f;
    }

    private float getHitboxY() {
        return y;
    }
}
