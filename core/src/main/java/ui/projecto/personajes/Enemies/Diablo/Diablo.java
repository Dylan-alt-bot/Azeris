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
import ui.projecto.personajes.Enemies.Diablo.animacion.AnimationManagerDiablo;
import ui.projecto.personajes.Enemies.Diablo.estado.DiabloState;
import ui.projecto.personajes.Enemies.Diablo.util.ConstantsDiablo;
import ui.projecto.personajes.Player.Player;

public class Diablo implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = 200f, height = 200f;
    private final float hitboxWidth = 90f, hitboxHeight = 160f;
    private final float velocidad = ConstantsDiablo.VELOCIDAD;

    private final Vida vida = new Vida(500);
    private DiabloState state = DiabloState.IDLE, previousState = DiabloState.IDLE;
    private final AnimationManagerDiablo animations = new AnimationManagerDiablo();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;

    private float alertTimer = ConstantsDiablo.ALERT_TIMER;
    private float alertDuration = ConstantsDiablo.ALERT_DURATION;
    private boolean alertStarted = ConstantsDiablo.ALERT_STARTED;

    private final float attackRange = ConstantsDiablo.ATTACK_RANGE;
    private final float attackCooldown = ConstantsDiablo.ATTACK_COOLDOWN;
    private float attackTimer  = ConstantsDiablo.ATTACK_TIMER;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Diablo(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, 10f, ConstantsDiablo.DETECTED_PLAYER, map, width, height);
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

        animations.add(DiabloState.IDLE, new Animation<>(0.08f, AnimationLoader.load(idle, 4,3)));
        animations.add(DiabloState.WALK, new Animation<>(0.06f, AnimationLoader.load(walk, 4,3)));
        animations.add(DiabloState.ALERT, new Animation<>(1f, AnimationLoader.load(alert, 1,1)));
        animations.add(DiabloState.ATTACK, new Animation<>(0.05f, AnimationLoader.load(attack, 4,3)));
        animations.add(DiabloState.HURT, new Animation<>(0.05f, AnimationLoader.load(hurt, 3,3)));
    }

    @Override
    public void update(float delta, Player player) {
        tiempo += delta;

        if (vida.isMuerto()){
            state = DiabloState.DEAD;
            return;
        }
        previousState = state;

        if (state == DiabloState.ATTACK) {
            attackTimer += delta;
            facingRight = player.x > x;
            if (attackTimer >= attackCooldown){
                state = DiabloState.WALK;
            }
            return;
        }
        if (vision.isPlayerInRange(x,y, player.x, player.y)){
            if (!alertStarted) {
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
                }
                return;
            }
            if (state == DiabloState.WALK) {
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
            TextureRegion baseFrame = anim.getKeyFrame(tiempo, true);
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

        float dx = (px + player.getWidth() / 2f) - (x + hitboxWidth / 2f);
        float dy = (py + player.getHeight() / 2f) - (y + hitboxHeight / 2f);

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

        if (!(px + pw < hx ||
            px > hx + hitboxWidth ||
            py + ph < hy ||
            py > hy + hitboxHeight)) {

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
