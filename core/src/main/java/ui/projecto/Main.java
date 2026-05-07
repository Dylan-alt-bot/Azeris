package ui.projecto;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ui.projecto.mecanicas.Door;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.Enemies.EnemySpawn;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.RoomManager.DungeonManager;
import ui.projecto.mecanicas.utils.Azeris;
import ui.projecto.mecanicas.utils.Heart;
import ui.projecto.mecanicas.utils.Utils;
import ui.projecto.personajes.Enemies.Amongus.Amongus;
import ui.projecto.personajes.Enemies.Amongus.Util.ConstantsAmongus;
import ui.projecto.personajes.Enemies.Diablo.Diablo;
import ui.projecto.personajes.Enemies.Diablo.Util.ConstantsDiablo;
import ui.projecto.personajes.Enemies.Goomba.Goomba;
import ui.projecto.personajes.Enemies.Goomba.Util.ConstantsGoomba;
import ui.projecto.personajes.Enemies.Skeleton.Skeleton;
import ui.projecto.personajes.Enemies.Skeleton.Util.ConstantsSkeleton;
import ui.projecto.personajes.Player.Player;
import ui.projecto.personajes.Player.PlayerUI;
import ui.projecto.personajes.Player.State.PlayerState;
import ui.projecto.personajes.Player.Util.ConstantsPlayer;

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
    private Music bgMusic;

    private Vector2 playerSpawn;
    private PlayerUI playerUI;
    private List<Enemy> enemies;
    private List<Utils> utils;

    private List<Door> doors;
    private Texture fadeTexture;
    private float fadeAlpha = 0f;
    private float transitionTimer = 0f;
    private final float TRANSITION_DURATION = 1.2f;
    private boolean isTransitioning = false;
    private boolean roomChanged = false;

    private DungeonManager dungeonManager;

    private List<EnemySpawn> initialEnemySpawns;
    private List<Vector2> initialHeartSpawns;

    private int enemiesKilled = 0;
    private final Set<Enemy> countEnemies = new HashSet<>();

    private boolean gamePaused = false;
    private boolean fullscreen = false;

    private TransitionState transitionState = TransitionState.NONE;

    private int currentLevel = 1;
    private boolean showingLevelScreen = false;
    private float levelScreenTimer = 0f;
    private final float LEVEL_SCREEN_DURATION = 2.5f;
    private String levelScreenText = "";

    @Override
    public void create() {
        fadeTexture = new Texture("extras/fnd_negro.png");
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(
            ConstantsPlayer.VIRTUAL_WIDTH,
            ConstantsPlayer.VIRTUAL_HEIGHT,
            camera
        );

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, ConstantsPlayer.VIRTUAL_WIDTH, ConstantsPlayer.VIRTUAL_HEIGHT);

        dungeonManager = new DungeonManager();
        mapManager = new MapManager(dungeonManager.getCurrentRoom().getMapPath());
        playerSpawn = mapManager.getRandomPlayerSpawn();
        jugadorPrincipal = new Player(playerSpawn.x, playerSpawn.y, mapManager);
        playerUI = new PlayerUI(jugadorPrincipal);

        font = new BitmapFont();

        initialEnemySpawns = mapManager.getRandomEnemySpawns();
        enemies = new ArrayList<>();
        for (EnemySpawn spawn : initialEnemySpawns) {
            Enemy enemy = createEnemy(spawn);
            if (enemy != null) enemies.add(enemy);
        }

        initialHeartSpawns = mapManager.getHearthSpawns();

        utils = new ArrayList<>();
        for (Vector2 pos : mapManager.getHearthSpawns()){
            utils.add(new Heart(pos.x, pos.y));
        }

        Vector2 azerisSpawn = mapManager.getAzerisSpawn();
        if (azerisSpawn != null){
            utils.add(new Azeris(azerisSpawn.x, azerisSpawn.y));
        }

        doors = new ArrayList<>();
        for (Vector2 doorPos : mapManager.getDoorSpawns()) {
            doors.add(new Door(doorPos, mapManager.getTileSize()));
        }

        this.glProfiler = new GLProfiler(Gdx.graphics);
        this.glProfiler.enable();

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music/musicIssac.mp3"));
        bgMusic.setVolume(0.23f);
        bgMusic.setLooping(true);
        bgMusic.play();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            gamePaused = !gamePaused;
            if (gamePaused) {
                if (bgMusic != null) bgMusic.pause();
                for (Enemy enemy : enemies) enemy.stopAllSounds();
            } else {
                if (bgMusic != null) bgMusic.play();
            }
        }
        if (gamePaused) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            font.draw(batch, "JUEGO EN PAUSA",  275f, 250f);
            batch.end();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) toggleFullscreen();
        float deltaTime = Gdx.graphics.getDeltaTime();
        updateTransition(deltaTime);
        boolean blockUpdate = isTransitioning;
        if (!blockUpdate) {
            jugadorPrincipal.update(deltaTime, enemies);
        }

        if (jugadorPrincipal.getVida().isMuerto() && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            jugadorPrincipal.getVida().revivir();

            jugadorPrincipal.x = playerSpawn.x;
            jugadorPrincipal.y = playerSpawn.y;

            resetEnemies();
            resetUtils();
        }

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
                enemy.recibirDolor(jugadorPrincipal.damage, jugadorPrincipal.x, jugadorPrincipal.y);
                jugadorPrincipal.setAttackHitRegistered(true);
            }

            if (!enemy.isDead()) {
                if (enemy instanceof Skeleton) {
                    Skeleton s = (Skeleton) enemy;
                    if (s.isAttackingPlayer(jugadorPrincipal)) {
                        jugadorPrincipal.recibirDolor(ConstantsSkeleton.DOLOR, enemy.getX(), enemy.getY());
                    }
                } else if (enemy instanceof Amongus) {
                    Amongus a = (Amongus) enemy;
                    if (a.isAttackingPlayer(jugadorPrincipal)) {
                        jugadorPrincipal.recibirDolor(ConstantsAmongus.DOLOR, enemy.getX(), enemy.getY());
                    }
                } else if (enemy instanceof Diablo) {
                    Diablo d = (Diablo) enemy;
                    if (d.isAttackingPlayer(jugadorPrincipal)){
                        jugadorPrincipal.recibirDolor(ConstantsDiablo.DOLOR, enemy.getX(), enemy.getY());
                    }
                } else {
                    if (jugadorPrincipal.collidesWithEnemy(enemy)) {
                        jugadorPrincipal.recibirDolor(ConstantsGoomba.DOLOR, enemy.getX(), enemy.getY());
                    }
                }
            }
        }
        for (Utils u : utils) {
            if (u.isCollected()) continue;
            u.update(deltaTime);
            u.render(batch);
            if (u.collides(jugadorPrincipal.x, jugadorPrincipal.y, 21, 21)) {
                if (u instanceof Heart){
                    jugadorPrincipal.getVida().curar(20);
                    u.collect();
                }
                else if (u instanceof Azeris){
                    jugadorPrincipal.applyAzerisBoost();
                    System.out.println("[AZERIS] Boost aplicado al jugador");
                    u.collect();
                }
            }
        }

        if (!isTransitioning && areAllEnemiesDead()){
            for (Door door : doors) {
                if (door.collides(jugadorPrincipal)) {
                    door.playSound();
                    isTransitioning = true;
                    transitionState = TransitionState.FADING_IN;
                    transitionTimer = 0f;
                    roomChanged = false;
                    break;
                }
            }
        }

        jugadorPrincipal.render(batch, deltaTime);
        batch.end();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        if (fadeAlpha > 0f) {
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(
                fadeTexture,
                0,
                0,
                ConstantsPlayer.VIRTUAL_WIDTH,
                ConstantsPlayer.VIRTUAL_HEIGHT
            );
            batch.setColor(1, 1, 1, 1);
        }

        String killsText = "Enemigos matados: " + enemiesKilled;
        font.draw(batch, killsText, 20, 430);
        if (!areAllEnemiesDead()) {
            font.draw(
                batch,
                "Derrota a todos los enemigos para pasar de habitación",
                180,
                410
            );
        }
        if (jugadorPrincipal.sprintOnCooldown()) {
            float remaining = jugadorPrincipal.getSprintCooldown();
            String text = String.format("Sprint: %.1f s", remaining);
            font.draw(batch, text, 20, 410);
        }
        if (jugadorPrincipal.getVida().isMuerto()) {
            bgMusic.setVolume(0.08f);
            String mensaje = "¡Has muerto! Presiona R para revivir";

            GlyphLayout layout = new GlyphLayout(font, mensaje);
            float x = (ConstantsPlayer.VIRTUAL_WIDTH - layout.width) / 2;
            float y = (ConstantsPlayer.VIRTUAL_HEIGHT - layout.height) / 2 + 50;
            font.draw(batch, layout, x, y);
        } else {
            bgMusic.setVolume(0.23f);
        }

        if (showingLevelScreen) {
            levelScreenTimer += Gdx.graphics.getDeltaTime();

            batch.setColor(0, 0, 0, 0.75f);
            batch.draw(fadeTexture, 0, 0,
                ConstantsPlayer.VIRTUAL_WIDTH,
                ConstantsPlayer.VIRTUAL_HEIGHT);
            batch.setColor(1, 1, 1, 1);

            GlyphLayout layout = new GlyphLayout(font, levelScreenText);
            float tx = (ConstantsPlayer.VIRTUAL_WIDTH - layout.width) / 2f;
            float ty = (ConstantsPlayer.VIRTUAL_HEIGHT + layout.height) / 2f;
            font.draw(batch, layout, tx, ty);

            String sub = "Continúa explorando...";
            GlyphLayout subLayout = new GlyphLayout(font, sub);
            float sx = (ConstantsPlayer.VIRTUAL_WIDTH - subLayout.width) / 2f;
            font.draw(batch, subLayout, sx, ty - 20f);

            if (levelScreenTimer >= LEVEL_SCREEN_DURATION) {
                showingLevelScreen = false;
                levelScreenTimer = 0f;
            }
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
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.dispose();
        }
        batch.dispose();
        glProfiler.disable();
        playerUI.dispose();
        for (Door door : doors) {
            door.dispose();
        }
        fadeTexture.dispose();
    }

    private Enemy createEnemy(EnemySpawn spawn){
        float x = spawn.x;
        float y = spawn.y;
        int tileSize = mapManager.getTileSize();
        boolean found = false;

        outer:
        for (int radius = 0; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    float tx = x + dx * tileSize;
                    float ty = y + dy * tileSize;
                    if (!mapManager.isBlocked(tx, ty, tileSize, tileSize)) {
                        x = tx;
                        y = ty;
                        found = true;
                        break outer;
                    }
                }
            }
        }

        if (!found) {
            System.out.println("[ENEMY] Spawn bloqueado sin salida, enemigo descartado: " + spawn.type);
            return null;
        }

        switch (spawn.type) {
            case "goomba": return new Goomba(x, y, mapManager);
            case "skeleton": return new Skeleton(x, y, mapManager);
            case "among us": return new Amongus(x, y, mapManager);
            case "diablo": return new Diablo(x, y, mapManager);
            default:
                System.out.println("Tipo desconocido: " + spawn.type);
                return null;
        }
    }

    private boolean areAllEnemiesDead() {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                return false;
            }
        }
        return true;
    }

    private void resetEnemies(){
        for (Enemy enemy : enemies){
            enemy.stopAllSounds();
        }
        enemies.clear();
        countEnemies.clear();
        enemiesKilled = 0;
        for (EnemySpawn spawn : initialEnemySpawns){
            Enemy enemy = createEnemy(spawn);
            if (enemy != null) enemies.add(enemy);
        }
    }

    private void resetUtils(){
        utils.clear();
        if (initialHeartSpawns != null){
            for (Vector2 pos : initialHeartSpawns){
                utils.add(new Heart(pos.x, pos.y));
            }
        }
        Vector2 azerisSpawn = mapManager.getAzerisSpawn();
        if (azerisSpawn != null){
            utils.add(new Azeris(azerisSpawn.x, azerisSpawn.y));
        }
    }

    private void loadRoom() {
        if (mapManager != null) {
            mapManager.dispose();
        }
        for (Enemy enemy : enemies) {
            enemy.stopAllSounds();
        }
        enemies.clear();
        utils.clear();
        doors.clear();

        mapManager = new MapManager(dungeonManager.getCurrentRoom().getMapPath());
        jugadorPrincipal.setMap(mapManager);

        playerSpawn = mapManager.getRandomPlayerSpawn();
        placePlayerSafe(playerSpawn);

        initialEnemySpawns = mapManager.getRandomEnemySpawns();
        for (EnemySpawn spawn : initialEnemySpawns) {
            Enemy enemy = createEnemy(spawn);
            if (enemy != null) enemies.add(enemy);
        }

        initialHeartSpawns = mapManager.getHearthSpawns();
        for (Vector2 pos : initialHeartSpawns) {
            utils.add(new Heart(pos.x, pos.y));
        }

        Vector2 azerisSpawn = mapManager.getAzerisSpawn();
        if (azerisSpawn != null) {
            utils.add(new Azeris(azerisSpawn.x, azerisSpawn.y));
        }
        for (Vector2 doorPos : mapManager.getDoorSpawns()) {
            doors.add(new Door(doorPos, mapManager.getTileSize()));
        }
        System.out.println("[ROOM] Nueva habitación cargada");
    }

    private void updateTransition(float delta) {
        if (!isTransitioning) return;
        transitionTimer += delta;

        switch (transitionState) {
            case FADING_IN:
                fadeAlpha = Math.min(1f, transitionTimer / (TRANSITION_DURATION * 0.5f));
                if (fadeAlpha >= 1f && !roomChanged) {
                    int levelBefore = getLevel(dungeonManager.getCurrentRoomIndex());
                    boolean success = dungeonManager.nextRoom();
                    if (success) {
                        int levelAfter = getLevel(dungeonManager.getCurrentRoomIndex());
                        if (levelAfter > levelBefore) {
                            currentLevel = levelAfter;
                            levelScreenText = dungeonManager.isLastRoom()
                                ? "¡JEFE FINAL!"
                                : "NIVEL" + currentLevel + " COMPLETADO!";
                            showingLevelScreen = true;
                            levelScreenTimer = 0f;
                        }
                        loadRoom();
                    } else {
                        levelScreenText = "¡JUEGO COMPLETADO!";
                        showingLevelScreen = true;
                        levelScreenTimer = 0f;
                        System.out.println("DUNGEON COMPLETADA");
                    }
                    roomChanged = true;
                    transitionState = TransitionState.FADING_OUT;
                    transitionTimer = 0f;
                }
                break;

            case FADING_OUT:
                fadeAlpha = 1f - Math.min(1f, transitionTimer / (TRANSITION_DURATION * 0.5f));
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    isTransitioning = false;
                    transitionState = TransitionState.NONE;
                }
                break;
        }
    }

    private void placePlayerSafe(Vector2 spawn) {
        float x = spawn.x;
        float y = spawn.y;

        int tries = 0;
        while (mapManager.isBlocked(x, y, jugadorPrincipal.getWidth(), jugadorPrincipal.getHeight()) && tries < 10) {
            x += mapManager.getTileSize();
            y += mapManager.getTileSize();
            tries++;
        }

        jugadorPrincipal.resetPosition(x, y);
    }

    private int getLevel(int roomIndex) {
        if (roomIndex <= 1) return 1;
        if (roomIndex <= 3) return 2;
        if (roomIndex <= 5) return 3;
        return 4;
    }

    @Override
    public void pause() {
        if (bgMusic != null) bgMusic.pause();
        for (Enemy enemy : enemies) {
            enemy.stopAllSounds();
        }
        for (Utils u : utils) {
            u.stopAllSounds();
        }
        jugadorPrincipal.stopAllSounds();
    }

    @Override
    public void resume() {
        if (bgMusic != null) bgMusic.play();
    }

    private enum TransitionState {
        NONE,
        FADING_IN,
        FADING_OUT
    }
}
