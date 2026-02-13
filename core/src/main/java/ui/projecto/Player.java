package ui.projecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import sun.jvm.hotspot.debugger.proc.riscv64.ProcRISCV64Thread;

public class Player{
    public float x, y;

    private final Animation<TextureRegion> animacionIdle;
    private final Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionActual;
    private final Animation<TextureRegion> animacionAtacar;
    private Animation<TextureRegion> animacionSprint;

    private float tiempo;
    private final Texture neutral;
    private final Texture correr;
    private final Texture atacar;
    private Texture sprint;
    private final float velocidad;
    private float velocidadSprint;

    private boolean moviendose;
    private boolean atacando;
    private boolean sprintando;

    private float tiempoAtaque;
    private float tiempoSprint;
    private static final float DURACION_ATAQUE = 0.4f;
    private static final float DURACION_SPRINT = 2.0f;

    private int direccionActual;
    private int direccionSprint;

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;
    private final int IDLE_FRAMES_X = 5;
    private final int IDLE_FRAMES_Y = 5;
    private final int TOTAL_IDLE_FRAMES = IDLE_FRAMES_X * IDLE_FRAMES_Y;
    private final int RUN_FRAMES_X = 4;
    private final int RUN_FRAMES_Y = 4;
    private final int TOTAL_RUN_FRAMES = RUN_FRAMES_X * RUN_FRAMES_Y;
    private final int ATTACK_FRAMES_X = 4;
    private final int ATTACK_FRAMES_Y = 3;
    private final int TOTAL_ATTACK_FRAMES = ATTACK_FRAMES_X * ATTACK_FRAMES_Y;
    private int SPRINT_FRAMES_X = 4;
    private int SPRINT_FRAMES_Y = 3;
    private int TOTAL_SPRINT_FRAMES = SPRINT_FRAMES_X * 2 + 2;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocidad = 150f;
        this.velocidadSprint = 300f;
        this.moviendose = false;
        this.sprintando = false;
        this.direccionActual = 0;
        this.direccionSprint = 0;

        neutral = new Texture(Gdx.files.internal("player/player.png"));
        correr = new Texture(Gdx.files.internal("player/Player_corriendo.png"));
        atacar = new  Texture(Gdx.files.internal("player/player_atacar.png"));
        sprint = new Texture(Gdx.files.internal("player/player_sprint.png"));

        // animación quieto (5x5)
        TextureRegion[][] idleFramesGrid = TextureRegion.split(neutral, neutral.getWidth() / IDLE_FRAMES_X, neutral.getHeight() / IDLE_FRAMES_Y);
        // Crear array para animación idle (25 frames)
        TextureRegion[] idleFrames = new TextureRegion[TOTAL_IDLE_FRAMES];
        int index = 0;
        for (int i = 0; i < IDLE_FRAMES_X; i++) {
            for (int j = 0; j < IDLE_FRAMES_Y; j++) {
                idleFrames[index++] = idleFramesGrid[i][j];
            }
        }

        // animación correr (4x4)
        TextureRegion[][] runFramesGrid = TextureRegion.split(correr, correr.getWidth() / RUN_FRAMES_X, correr.getHeight() / RUN_FRAMES_Y);
        // Crear array para animación correr (16 frames)
        TextureRegion[] runFrames = new TextureRegion[TOTAL_RUN_FRAMES];
        index = 0;
        for (int i = 0; i < RUN_FRAMES_X; i++) {
            for (int j = 0; j < RUN_FRAMES_Y; j++) {
                runFrames[index++] = runFramesGrid[i][j];
            }
        }

        // animacion atacar (4x3)
        TextureRegion[][] atacarFramesGrid = TextureRegion.split(atacar, atacar.getWidth() / ATTACK_FRAMES_X, atacar.getHeight() / ATTACK_FRAMES_Y);
        // Crear array para animación de atacar(12 frames)
        TextureRegion[] atacarFrames = new TextureRegion[TOTAL_ATTACK_FRAMES];
        index = 0;
        for (int i = 0; i < ATTACK_FRAMES_Y; i++) {
            for (int j = 0; j < ATTACK_FRAMES_X; j++) {
                atacarFrames[index++] = atacarFramesGrid[i][j];
            }
        }

        // animación sprint (4x3 solo que su última fila solo tiene 2 sprites)
        TextureRegion[][] sprintFramesGrid = TextureRegion.split(sprint, sprint.getWidth() / SPRINT_FRAMES_X, sprint.getHeight() / SPRINT_FRAMES_Y);
        // Crear array para animación de sprintar (10 frames)
        TextureRegion[] sprintFrames = new TextureRegion[TOTAL_SPRINT_FRAMES];
        index = 0;
        // Primera fila (4 frames)
        for (int col = 0; col < SPRINT_FRAMES_X; col++) {
            sprintFrames[index++] = sprintFramesGrid[0][col];
        }

        // Segunda fila (4 frames)
        for (int col = 0; col < SPRINT_FRAMES_X; col++) {
            sprintFrames[index++] = sprintFramesGrid[1][col];
        }

        // Tercera fila (solo 2 frames)
        for (int col = 0; col < 2; col++) {
            sprintFrames[index++] = sprintFramesGrid[2][col];
        }

        float frameDurationIdle = 0.08f;
        float frameDurationCorrer = 0.05f;
        float frameDurationAtacar = DURACION_ATAQUE / TOTAL_ATTACK_FRAMES;
        float frameDurationSprint = 0.05f;

        animacionIdle = new Animation<>(frameDurationIdle, idleFrames);
        animacionIdle.setPlayMode(Animation.PlayMode.LOOP);

        animacionCorrer = new Animation<>(frameDurationCorrer, runFrames);
        animacionCorrer.setPlayMode(Animation.PlayMode.LOOP);

        animacionAtacar = new Animation<>(frameDurationAtacar, atacarFrames);
        animacionAtacar.setPlayMode(Animation.PlayMode.NORMAL);

        animacionSprint = new Animation<>(frameDurationSprint, sprintFrames);
        animacionSprint.setPlayMode(Animation.PlayMode.LOOP);

        tiempo = 0f;
        animacionActual = animacionIdle;

        // Debug: verificar frames
        Gdx.app.log("Player", "=== DEBUG INFO ===");
        Gdx.app.log("Player", "Frames Idle totales: " + idleFrames.length);
        Gdx.app.log("Player", "Frames Correr totales: " + runFrames.length);
        Gdx.app.log("Player", "Tamaño Idle texture: " + neutral.getWidth() + "x" + neutral.getHeight());
        Gdx.app.log("Player", "Tamaño Correr texture: " + correr.getWidth() + "x" + correr.getHeight());
        Gdx.app.log("Player", "Frame size Idle: " + (neutral.getWidth() / IDLE_FRAMES_X) + "x" + (neutral.getHeight() / IDLE_FRAMES_Y));
        Gdx.app.log("Player", "Frame size Correr: " + (correr.getWidth() / RUN_FRAMES_X) + "x" + (correr.getHeight() / RUN_FRAMES_Y));
    }

    public void update(float deltaTime) {
        boolean estabaMoviendose = moviendose;
        boolean estabaAtacando = atacando;
        moviendose = false;

        // Variables para rastrear movimiento en cada eje
        boolean moviendoDerecha = false;
        boolean moviendoIzquierda = false;
        boolean moviendoArriba = false;
        boolean moviendoAbajo = false;

        if (atacando) {
            tiempoAtaque += deltaTime;
            if (tiempoAtaque >= DURACION_ATAQUE) {
                atacando = false;
                tiempoAtaque = 0f;
            }
        }

        if (sprintando){
            tiempoSprint += deltaTime;
            if (tiempoSprint >= DURACION_SPRINT) {
                sprintando = false;
                tiempoSprint = 0f;
                direccionSprint = 0;
            }
        }

        float velocidadActual = sprintando ? velocidadSprint : velocidad;

        // Sprintar
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !sprintando && !atacando && moviendose) {
            sprint();
        }

        // Atacar
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            atacar();
        }

        if (!sprintando){
            // Izquierda
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                x -= velocidad * deltaTime;
                moviendose = true;
                moviendoIzquierda = true;
            }
            // Derecha
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                x += velocidad * deltaTime;
                moviendose = true;
                moviendoDerecha = true;
            }
            // Arriba
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                y += velocidad * deltaTime;
                moviendose = true;
                moviendoArriba = true;
            }
            // Abajo
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                y -= velocidad * deltaTime;
                moviendose = true;
                moviendoAbajo = true;
            }
        } else {
            switch (direccionSprint) {
                case 1: // Derecha
                    x += velocidadActual * deltaTime;
                    moviendose = true;
                    moviendoDerecha = true;
                    break;
                case 2: // Izquierda
                    x -= velocidadActual * deltaTime;
                    moviendose = true;
                    moviendoIzquierda = true;
                    break;
                case 3: // Arriba
                    y += velocidadActual * deltaTime;
                    moviendose = true;
                    moviendoArriba = true;
                    break;
                case 4: // Abajo
                    y -= velocidadActual * deltaTime;
                    moviendose = true;
                    moviendoAbajo = true;
                    break;
            }
        }

        if (!sprintando){
            if (moviendoDerecha && (moviendoArriba || moviendoAbajo)) {
                // Movimiento diagonal derecha-arriba o derecha-abajo
                direccionActual = 1;
            } else if (moviendoIzquierda && (moviendoArriba || moviendoAbajo)) {
                // Movimiento diagonal izquierda-arriba o izquierda-abajo
                direccionActual = 2;
            } else if (moviendoDerecha) {
                // Derecha
                direccionActual = 1;
            } else if (moviendoIzquierda) {
                // Izquierda
                direccionActual = 2;
            } else if (moviendoArriba) {
                // Arriba
                direccionActual = 3;
            } else if (moviendoAbajo) {
                // Abajo
                direccionActual = 4;
            }
        }

        if (atacando) {
            animacionActual = animacionAtacar;
        } else if (moviendose) {
            animacionActual = animacionCorrer;
        } else if (sprintando) {
            animacionActual = animacionSprint;
        } else {
            animacionActual = animacionIdle;
        }

        tiempo += deltaTime;

        if (!atacando && !sprintando && (estabaMoviendose != moviendose || estabaAtacando != atacando)) {
            tiempo = 0;
        }


        if (x < 0) x = 0;
        if (x > VIRTUAL_WIDTH - getWidth()) x = VIRTUAL_WIDTH - getWidth();
        if (y < 0) y = 0;
        if (y > VIRTUAL_HEIGHT - getHeight()) y = VIRTUAL_HEIGHT - getHeight();
    }

    public void atacar() {
        if (!atacando) {
            atacando = true;
            tiempoAtaque = 0f;
            tiempo = 0f;
        }
    }
    public void sprint() {
        if (!sprintando && !atacando && direccionActual != 0) {
            sprintando = true;
            direccionSprint = direccionActual; // Bloquear dirección actual
            tiempoSprint = 0f;
            tiempo = 0f;
            Gdx.app.log("Player", "SPRINT INICIADO - Dirección: " + direccionActual);
        }
    }

    public void render(final SpriteBatch batch) {
        if (animacionActual == null) return;

        // Debug del frame actual
        int frameIndex = animacionActual.getKeyFrameIndex(tiempo);
        if (Gdx.graphics.getFrameId() % 30 == 0) {
            Gdx.app.log("Player", "Frame: " + frameIndex + "/" + (moviendose ? TOTAL_RUN_FRAMES : TOTAL_IDLE_FRAMES) + ", Tiempo: " + tiempo + ", Estado: " + (moviendose ? "CORRIENDO" : "IDLE") + ", Atacando: " + (atacando? true : false) + ", Sprintando: " + (sprintando? true : false));
        }

        TextureRegion frameActual;

        if (atacando) {
            frameActual = animacionActual.getKeyFrame(tiempo, false);
        } else {
            frameActual = animacionActual.getKeyFrame(tiempo, true);
        }

        if (frameActual != null) {
            float drawWidth, drawHeight;

            if (atacando) {
                drawWidth = atacar.getWidth() / (float)ATTACK_FRAMES_X;
                drawHeight = atacar.getHeight() / (float)ATTACK_FRAMES_Y;
            } else if (moviendose) {
                drawWidth = correr.getWidth() / (float)RUN_FRAMES_X;
                drawHeight = correr.getHeight() / (float)RUN_FRAMES_Y;
            } else if (sprintando) {
                drawWidth = sprint.getWidth() / (float)RUN_FRAMES_X;
                drawHeight = sprint.getHeight() / (float)RUN_FRAMES_Y;
            } else {
                drawWidth = neutral.getWidth() / (float)IDLE_FRAMES_X;
                drawHeight = neutral.getHeight() / (float)IDLE_FRAMES_Y;
            }

            // Dibujar según dirección
            int direccionDibujo = sprintando ? direccionSprint : direccionActual;
            if (direccionDibujo == 1) {
                batch.draw(frameActual, x + drawWidth, y, -drawWidth, drawHeight);
            } else {
                batch.draw(frameActual, x, y, drawWidth, drawHeight);
            }
        }
    }

    public float getWidth() {
        return neutral.getWidth() / (float)IDLE_FRAMES_X;
    }

    public float getHeight() {
        return neutral.getHeight() / (float)IDLE_FRAMES_Y;
    }

    public void dispose() {
        if (neutral != null) {
            neutral.dispose();
        }
        if (correr != null) {
            correr.dispose();
        }
        if (atacar != null) {
            atacar.dispose();
        }
        if (sprintando) {
            sprint.dispose();
        }
    }

    public static float getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public static float getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }
}
