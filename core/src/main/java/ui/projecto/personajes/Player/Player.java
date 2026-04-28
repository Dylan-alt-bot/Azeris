package ui.projecto.personajes.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;
import ui.projecto.mecanicas.AnimationLoader;
import ui.projecto.personajes.Player.Manager.AnimationManager;
import ui.projecto.personajes.Player.Manager.PlayerAudioManager;
import ui.projecto.personajes.Player.State.PlayerState;
import ui.projecto.personajes.Player.Render.PlayerRenderer;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

import java.util.List;

public class Player{
    public float x, y;
    public int damage = ConstantsPlayer.DAMAGE;
    private final float spawnX, spawnY;
    private float velocidadBase = ConstantsPlayer.VELOCIDAD;
    private float velocidad = velocidadBase;
    private PlayerState state = PlayerState.IDLE, previousState = PlayerState.IDLE;
    private final PlayerAudioManager audio;

    private final MapManager map;
    private final Vida vida = new Vida(ConstantsPlayer.VIDA);
    private final AnimationManager animations;
    private final PlayerRenderer renderer;

    private float tiempo = 0f;

    private boolean facingRight = false;
    private boolean sprintCooldown = false;
    public boolean attackHitRegistered = false;
    private boolean azerisBoost = false;

    private boolean wasRunning = false;

    private float sprintTimer = 0f;
    private float sprintImpulseRemaining = 0f;
    private float cooldownTimer = 0f;
    private float attackTimer = 0f;
    private float hurtTimer = 0f;
    private float damageCooldownTimer = 0f;

    private final float damageCooldown = ConstantsPlayer.DAMAGE_COOLDOWN;

    private float lastDirX = 0f, lastDirY = 0f;
    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Player(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.map = map;

        this.audio = new PlayerAudioManager();
        this.animations = new AnimationManager();
        this.renderer = new PlayerRenderer();

        loadAnimations();
    }

    private void loadAnimations(){
        Texture idle = new Texture("player/player.png");
        Texture run = new Texture("player/player_corriendo.png");
        Texture attack = new Texture("player/player_atacar.png");
        Texture sprint = new Texture("player/player_sprint.png");
        Texture hurt = new Texture("player/player_dolor.png");
        Texture dead = new Texture("player/player_muerte.png");

        animations.add(PlayerState.IDLE, new Animation<>(0.08f, AnimationLoader.load(idle, 5, 5)));
        animations.add(PlayerState.RUN, new Animation<>(0.04f, AnimationLoader.load(run, 4, 4)));
        animations.add(PlayerState.ATTACK, new Animation<>(ConstantsPlayer.DURACION_ATAQUE / 20f, AnimationLoader.load(attack, 4, 3)));
        animations.add(PlayerState.SPRINT, new Animation<>(ConstantsPlayer.DURACION_SPRINT / 12f, AnimationLoader.load(sprint, 4, 3)));
        animations.add(PlayerState.HURT, new Animation<>(ConstantsPlayer.DURACION_DOLOR / 9f, AnimationLoader.load(hurt, 3, 3)));
        animations.add(PlayerState.DEAD, new Animation<>(ConstantsPlayer.DURACION_MUERTE / 42f, AnimationLoader.load(dead, 7, 6)));
    }

    public void update(float delta, List<Enemy> enemies){
        tiempo += delta;

        if (knockbackTimer > 0f && state != PlayerState.DEAD){
            float newX = x + knockbackX * delta;
            float newY = y + knockbackY * delta;
            if (collides(newX, y)) x = newX;
            if (collides(x, newY)) y = newY;
            knockbackTimer -= delta;
        }

        if (vida.isMuerto()){
            if (state != PlayerState.DEAD){
                audio.triggerDeath();
                audio.stopRun();
            }
            state = PlayerState.DEAD;
            knockbackTimer = 0f;
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)){
                revivir();
            }
            return;
        }

        if (damageCooldownTimer > 0f) {
            damageCooldownTimer -= delta;
        }

        if (hurtTimer > 0f){
            hurtTimer -= delta;
            if (hurtTimer <= 0f && knockbackTimer <= 0f && !vida.isMuerto()){
                state = previousState != PlayerState.HURT ? previousState : PlayerState.IDLE;
            }
        }
        handleCooldown(delta);
        controles(delta, enemies);
    }

    private void handleCooldown(float delta){
        if (sprintCooldown) {
            cooldownTimer += delta;
            if (cooldownTimer >= ConstantsPlayer.COOLDOWN_SPRINT) {
                sprintCooldown = false;
                cooldownTimer = 0;
            }
        }
    }

    private void controles(float delta, List<Enemy> enemies){
        float newX = x;
        float newY = y;

        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) ||  Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= velocidad * delta;
            facingRight = false;
            moving = true;
            lastDirX = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||  Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += velocidad * delta;
            facingRight = true;
            moving = true;
            lastDirX = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) ||  Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += velocidad * delta;
            moving = true;
            lastDirY = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) ||  Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= velocidad * delta;
            moving = true;
            lastDirY = -1;
        }

        if (state != PlayerState.HURT){
            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !sprintCooldown && state != PlayerState.ATTACK){
                if (state != PlayerState.SPRINT){
                    audio.playSprint();
                    state = PlayerState.SPRINT;
                    velocidad = velocidadBase * (ConstantsPlayer.VELOCIDAD_SPRINT / ConstantsPlayer.VELOCIDAD);
                    sprintTimer = 0;
                    sprintCooldown = true;
                    tiempo = 0;

                    if (!moving){
                        sprintImpulseRemaining = ConstantsPlayer.SPRINT_IMPULSE;
                        if (lastDirX == 0 && lastDirY == 0){
                            lastDirX = 1;
                            lastDirY = 0;
                            facingRight = true;
                        }
                    }
                }
            }
        }

        if (sprintImpulseRemaining > 0){
            float step = ConstantsPlayer.VELOCIDAD_SPRINT * delta;
            if (step > sprintImpulseRemaining) step = sprintImpulseRemaining;

            newX += facingRight ? step : -step;
            sprintImpulseRemaining -= step;
            moving = true;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if (state != PlayerState.ATTACK && state != PlayerState.SPRINT) {
                audio.playAttack();
                sprintImpulseRemaining = 0;

                state = PlayerState.ATTACK;
                attackTimer = 0;
                tiempo = 0;

                attackHitRegistered = false;
                return;
            }
        }
        if (collidesWithEnemiesSolid(enemies, newX, y) && collides(newX, y)) x = newX;
        if (collidesWithEnemiesSolid(enemies, x, newY) && collides(x, newY)) y = newY;

        if (state == PlayerState.SPRINT){
            sprintTimer += delta;
            if (sprintTimer >= ConstantsPlayer.DURACION_SPRINT && sprintImpulseRemaining <= 0) {
                velocidad = velocidadBase;
                state = PlayerState.IDLE;
            }
            return;
        }

        if (state == PlayerState.ATTACK){
            attackTimer += delta;
            if (attackTimer >= ConstantsPlayer.DURACION_ATAQUE){
                state = PlayerState.IDLE;
            }
            return;
        }

        if (state != PlayerState.HURT){
            state = moving ? PlayerState.RUN : PlayerState.IDLE;
        }

        boolean isRunningNow = !vida.isMuerto() && (state == PlayerState.RUN) && moving && knockbackTimer <= 0f;
        if (isRunningNow && !wasRunning){
            audio.playRun();
        }
        if (!isRunningNow && wasRunning){
            audio.stopRun();
        }
        wasRunning = isRunningNow;
    }



    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta){
        renderer.render(this, batch, delta);
    }

    public Animation<TextureRegion> getAnimation(){
        return animations.get(state);
    }

    public boolean isFacingRight(){
        return facingRight;
    }

    public PlayerState getState(){
        return state;
    }

    public float getTime() {
        return tiempo;
    }

    private boolean collides(float newX, float newY){
        float hitboxWidth = 30f;
        float hitboxHeight = 10f;
        float offsetX = 10f;
        float offsetY = 10f;
        return !map.isBlocked(
            newX + offsetX,
            newY + offsetY,
            hitboxWidth,
            hitboxHeight
        );
    }

    public float getSprintCooldown(){
        if (!sprintCooldown) return 0;
        return ConstantsPlayer.COOLDOWN_SPRINT - cooldownTimer;
    }

    public boolean sprintOnCooldown(){
        return sprintCooldown;
    }

    public boolean collidesWithEnemy(Enemy enemy) {
        if (enemy.isDead()) return false;

        float width = 16f;
        float height = 16f;

        return enemy.collides(x, y, width, height);
    }

    public boolean collidesWithEnemiesSolid(List<Enemy> enemies, float nextX, float nextY) {
        float playerWidth = 36f;
        float playerHeight = 20f;
        float offsetX = 14f;
        float offsetY = 10f;

        float px = nextX + offsetX;
        float py = nextY + offsetY;

        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                if (enemy.collides(px, py, playerWidth, playerHeight)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void recibirDolor(int cantidad, float enemyX, float enemyY){
        if (damageCooldownTimer <= 0f && !vida.isMuerto()) {
            audio.stopRun();
            audio.playHurt();
            vida.recibirDolor(cantidad);

            previousState = state;

            state = PlayerState.HURT;
            tiempo = 0f;
            hurtTimer = ConstantsPlayer.DURACION_DOLOR;

            damageCooldownTimer = damageCooldown;

            float dirX = x - enemyX;
            float dirY = y - enemyY;

            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (length != 0) {
                dirX /= length;
                dirY /= length;
            }

            float force = ConstantsPlayer.KNOCKBACK_FORCE;
            knockbackX = dirX * force;
            knockbackY = dirY * force;
            knockbackTimer = ConstantsPlayer.KNOCKBACK_DURATION;
        }
    }

    public boolean attackHits(Enemy enemy){
        float range = 5f;
        float width = 60f;
        float height = 30f;

        float attackX = x;
        float attackY = y;

        if (facingRight){
            attackX += range;
        } else {
            attackX -= range;
        }

        return enemy.collides(attackX, attackY, width, height);
    }

    public void revivir(){
        audio.reset();
        vida.revivir();
        x = spawnX;
        y = spawnY;
        state = PlayerState.IDLE;
        tiempo = 0f;
        velocidad = velocidadBase;
        sprintImpulseRemaining = 0;
        sprintCooldown = false;
        cooldownTimer = 0f;
        sprintTimer = 0f;
        attackTimer = 0f;
        hurtTimer = 0f;
        damageCooldownTimer = 0f;
        wasRunning = false;
    }

    public Vida getVida(){
        return vida;
    }

    public boolean isAttackHitRegistered(){
        return attackHitRegistered;
    }

    public void setAttackHitRegistered(boolean value){
        attackHitRegistered = value;
    }

    public float getWidth(){
        return ConstantsPlayer.WIDTH;
    }

    public float getHeight(){
        return ConstantsPlayer.HEIGHT;
    }

    public void applyAzerisBoost(){
        if (azerisBoost) return;

        vida.setMaxVida(vida.getMaxVida() * ConstantsPlayer.AZERIS_HEALTH);
        vida.curar(vida.getMaxVida());

        this.damage *= ConstantsPlayer.AZERIS_DAMAGE;
        this.velocidadBase *= ConstantsPlayer.AZERIS_SPEED;
        this.velocidad = velocidadBase;
        azerisBoost = true;
        System.out.println("[PLAYER] Azeris boost aplicado:");
        System.out.println("  Vida x" + ConstantsPlayer.AZERIS_HEALTH);
        System.out.println("  Daño x" + ConstantsPlayer.AZERIS_DAMAGE);
        System.out.println("  Velocidad x" + ConstantsPlayer.AZERIS_SPEED);
    }
}
