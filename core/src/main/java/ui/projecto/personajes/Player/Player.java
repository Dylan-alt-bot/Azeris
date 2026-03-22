package ui.projecto.personajes.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;
import ui.projecto.personajes.Player.animacion.AnimationLoader;
import ui.projecto.personajes.Player.animacion.AnimationManager;
import ui.projecto.personajes.Player.personaje.PlayerState;
import ui.projecto.personajes.Player.render.PlayerRenderer;
import ui.projecto.personajes.Player.util.Constants;

public class Player{
    public float x, y;

    private final MapManager map;
    private final Vida vida;

    private PlayerState state;
    private final AnimationManager animations;
    private final PlayerRenderer renderer;

    private float tiempo = 0f;

    private boolean facingRight = false;
    private boolean sprintCooldown = false;

    private float sprintTimer = 0f;
    private float sprintImpulseRemaining = 0f;

    private float cooldownTimer = 0f;
    private float attackTimer = 0f;
    private float hurtTimer = 0f;

    private float velocidad = Constants.VELOCIDAD;
    private float lastDirX = 0f;
    private float lastDirY = 0f;


    public Player(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        this.vida = new Vida(100);
        this.animations = new AnimationManager();
        this.renderer = new PlayerRenderer();

        loadAnimations();
        state = PlayerState.IDLE;
    }

    private void loadAnimations(){
        Texture idle = new Texture("player/player.png");
        Texture run = new Texture("player/Player_corriendo.png");
        Texture attack = new Texture("player/player_atacar.png");
        Texture sprint = new Texture("player/player_sprint.png");
        Texture hurt = new Texture("player/player_dolor.png");
        Texture dead = new Texture("player/player_muerte.png");

        animations.add(PlayerState.IDLE, new Animation<>(0.08f, AnimationLoader.load(idle, 5, 5)));
        animations.add(PlayerState.RUN, new Animation<>(0.04f, AnimationLoader.load(run, 4, 4)));
        animations.add(PlayerState.ATTACK, new Animation<>(Constants.DURACION_ATAQUE / 12f, AnimationLoader.load(attack, 4, 3)));
        animations.add(PlayerState.SPRINT, new Animation<>(Constants.DURACION_SPRINT / 12f, AnimationLoader.load(sprint, 4, 3)));
        animations.add(PlayerState.HURT, new Animation<>(Constants.DURACION_DOLOR / 9f, AnimationLoader.load(hurt, 3, 3)));
        animations.add(PlayerState.DEAD, new Animation<>(Constants.DURACION_MUERTE / 42f, AnimationLoader.load(dead, 7, 6)));
    }

    public void update(float delta){
        tiempo += delta;

        if (vida.isMuerto()){
            state = PlayerState.DEAD;
            return;
        }

        handleCooldown(delta);
        controles(delta);
    }

    private void handleCooldown(float delta){
        if (sprintCooldown) {
            cooldownTimer += delta;
            if (cooldownTimer >= Constants.COOLDOWN_SPRINT) {
                sprintCooldown = false;
                cooldownTimer = 0;
            }
        }
    }

    private void controles(float delta){
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !sprintCooldown){
            if (state != PlayerState.SPRINT){
                state = PlayerState.SPRINT;
                velocidad = Constants.VELOCIDAD_SPRINT;
                sprintTimer = 0;
                sprintCooldown = true;
                tiempo = 0;

                if (!moving){
                    sprintImpulseRemaining = Constants.SPRINT_IMPULSE;
                    if (lastDirX == 0 && lastDirY == 0){
                        lastDirX = 1;
                        lastDirY = 0;
                        facingRight = true;
                    }
                }
            }
        }

        if (sprintImpulseRemaining > 0){
            float step = Constants.VELOCIDAD_SPRINT * delta;
            if (step > sprintImpulseRemaining) step = sprintImpulseRemaining;

            newX += facingRight ? step : -step;
            sprintImpulseRemaining -= step;
            moving = true;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
            if (state != PlayerState.ATTACK) {
                state = PlayerState.ATTACK;
                attackTimer = 0;
                tiempo = 0;
                return;
            }
        }

        if (!map.isBlocked(newX, y)) x = newX;
        if (!map.isBlocked(x, newY)) y = newY;

        if (state == PlayerState.SPRINT){
            sprintTimer += delta;
            if (sprintTimer >= Constants.DURACION_SPRINT && sprintImpulseRemaining <= 0) {
                velocidad = Constants.VELOCIDAD;
                state = PlayerState.IDLE;
            }
            return;
        }

        if (state == PlayerState.ATTACK){
            attackTimer += delta;
            if (attackTimer >= Constants.DURACION_ATAQUE){
                state = PlayerState.IDLE;
            }
            return;
        }

        state = moving ? PlayerState.RUN : PlayerState.IDLE;
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
}
