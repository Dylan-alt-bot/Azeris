package ui.projecto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.Enemies.EnemySpawn;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.utils.Heart;
import ui.projecto.personajes.Enemies.Amongus.Amongus;
import ui.projecto.personajes.Enemies.Diablo.Diablo;
import ui.projecto.personajes.Enemies.Goomba.Goomba;
import ui.projecto.personajes.Enemies.Skeleton.Skeleton;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.PlayerUI;
import ui.projecto.personajes.Player.estado.PlayerState;
import ui.projecto.personajes.Player.util.ConstantsPlayer;

import java.util.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Viewport viewport;
    private Player jugadorPrincipal;
    private GLProfiler glProfiler;

    private MapManager mapManager;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private BitmapFont font;

    private PlayerUI playerUI;
    private List<Enemy> enemies;
    private List<Heart> hearts;
    private float healCooldown = 0f;

    private int enemiesKilled = 0;
    private final Set<Enemy> countEnemies = new HashSet<>();

    private boolean fullscreen = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT,
            camera
        );

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);

        mapManager = new MapManager("maps/beta/mapabase2.tmx");
        Vector2 playerSpawn = mapManager.getRandomPlayerSpawn();
        jugadorPrincipal = new Player(playerSpawn.x, playerSpawn.y, mapManager);
        playerUI = new PlayerUI(jugadorPrincipal);

        font = new BitmapFont();

        enemies = new ArrayList<>();
        for (EnemySpawn spawn : mapManager.getRandomEnemySpawns()) {
            switch (spawn.type) {
                case "goomba":
                    enemies.add(new Goomba(spawn.x, spawn.y, mapManager));
                    break;
                case "skeleton":
                    enemies.add(new Skeleton(spawn.x, spawn.y, mapManager));
                    break;
                case "among us":
                    enemies.add(new Amongus(spawn.x, spawn.y, mapManager));
                    break;
                case "diablo":
                    enemies.add(new Diablo(spawn.x, spawn.y, mapManager));
                    break;
                default:
                    System.out.println("Tipo desconocido: " + spawn.type);
            }
        }
        hearts = new ArrayList<>();
        for (Vector2 pos : mapManager.getHearthSpawns()) {
            hearts.add(new Heart(pos.x, pos.y));
        }
        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();

        float deltaTime = Gdx.graphics.getDeltaTime();

        jugadorPrincipal.update(deltaTime, enemies);

        camera.position.set(
            jugadorPrincipal.x,
            jugadorPrincipal.y,
            0
        );

        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        mapManager.render(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, jugadorPrincipal);

            if (enemy.isDead() && !countEnemies.contains(enemy)){
                enemiesKilled++;
                countEnemies.add(enemy);
            }
            enemy.render(batch);

            if (!enemy.isDead()
                && jugadorPrincipal.getState() == PlayerState.ATTACK
                && !jugadorPrincipal.isAttackHitRegistered()
                && jugadorPrincipal.attackHits(enemy)) {
                enemy.recibirDolor(10, jugadorPrincipal.x, jugadorPrincipal.y);
                jugadorPrincipal.setAttackHitRegistered(true);
            }

            if (!enemy.isDead()) {
                if (enemy instanceof Skeleton) {
                    Skeleton s = (Skeleton) enemy;
                    if (s.isAttackingPlayer(jugadorPrincipal)) {
                        jugadorPrincipal.recibirDolor(10, enemy.getX(), enemy.getY());
                    }
                } else if (enemy instanceof Amongus) {
                    Amongus a = (Amongus) enemy;
                    if (a.isAttackingPlayer(jugadorPrincipal)) {
                        jugadorPrincipal.recibirDolor(15, enemy.getX(), enemy.getY());
                    }
                } else if (enemy instanceof Diablo) {
                    Diablo d = (Diablo) enemy;
                    if (d.isAttackingPlayer(jugadorPrincipal)){
                        jugadorPrincipal.recibirDolor(20, enemy.getX(), enemy.getY());
                    }
                } else {
                    if (jugadorPrincipal.collidesWithEnemy(enemy)) {
                        jugadorPrincipal.recibirDolor(5, enemy.getX(), enemy.getY());
                    }
                }
            }
        }

        for (Heart heart : hearts) {
            heart.update(deltaTime);
            heart.render(batch);
            if (!heart.isCollected()
                && heart.collides(jugadorPrincipal.x, jugadorPrincipal.y, 32, 32)
                && healCooldown <= 0f) {
                jugadorPrincipal.getVida().curar(20);
                heart.collect();

                healCooldown = 2f;
            }
            if (healCooldown > 0f) {
                healCooldown -= deltaTime;
            }
        }

        jugadorPrincipal.render(batch, deltaTime);

        batch.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        String killsText = "Enemigos matados: " + enemiesKilled;
        font.draw(batch, killsText, 20, 430);

        if (jugadorPrincipal.sprintOnCooldown()) {
            float remaining = jugadorPrincipal.getSprintCooldown();
            String text = String.format("Sprint: %.1f s", remaining);
            font.draw(batch, text, 20, 410);
        }

        if (jugadorPrincipal.getVida().isMuerto()) {
            String mensaje = "¡Has muerto! Presiona R para revivir";

            GlyphLayout layout = new GlyphLayout(font, mensaje);
            float x = (ConstantsPlayer.VIRTUAL_WIDTH - layout.width) / 2;
            float y = (ConstantsPlayer.VIRTUAL_HEIGHT - layout.height) / 2 + 50;
            font.draw(batch, layout, x, y);
        }
        batch.end();
        playerUI.render(batch);
    }


    private void toggleFullscreen() {
        if (!fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(800, 480);
        }
        fullscreen = !fullscreen;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        glProfiler.disable();
        playerUI.dispose();
    }
}
