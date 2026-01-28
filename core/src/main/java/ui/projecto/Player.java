package ui.projecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    public float x, y;

    private Animation<TextureRegion> animacionIdle;
    private Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionActual;
    private Animation<TextureRegion> animacionAtacar;

    private float tiempo;
    private Texture neutral;
    private Texture correr;
    private Texture atacar;
    private float velocidad;

    private boolean moviendose;
    private boolean atacando;
    private float tiempoAtaque;
    private static final float DURACION_ATAQUE = 0.4f;
    private int DireccionActual;
    private int direccionActual;

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;
    private static final int IDLE_FRAMES_X = 5;
    private static final int IDLE_FRAMES_Y = 5;
    private static final int TOTAL_IDLE_FRAMES = IDLE_FRAMES_X * IDLE_FRAMES_Y;
    private static final int RUN_FRAMES_X = 4;
    private static final int RUN_FRAMES_Y = 4;
    private static final int TOTAL_RUN_FRAMES = RUN_FRAMES_X * RUN_FRAMES_Y;
    private static final int ATTACK_FRAMES_X = 4;
    private static final int ATTACK_FRAMES_Y = 3;
    private static final int TOTAL_ATTACK_FRAMES = ATTACK_FRAMES_X * ATTACK_FRAMES_Y;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocidad = 150f;
        this.moviendose = false;
        this.direccionActual = 0;

        neutral = new Texture(Gdx.files.internal("player/player.png"));
        correr = new Texture(Gdx.files.internal("player/Player_corriendo.png"));
        atacar = new  Texture(Gdx.files.internal("player/player_atacar.png"));

        // animación quieto (5x5)
        TextureRegion[][] idleFramesGrid = TextureRegion.split(neutral, neutral.getWidth() / IDLE_FRAMES_X, neutral.getHeight() / IDLE_FRAMES_Y);

        // Crear array para animación idle (25 frames)
        TextureRegion[] idleFrames = new TextureRegion[TOTAL_IDLE_FRAMES];


        int index = 0;
        for (int i = 0; i < IDLE_FRAMES_Y; i++) {
            for (int j = 0; j < IDLE_FRAMES_X; j++) {
                idleFrames[index++] = idleFramesGrid[i][j];
            }
        }

        // animación correr (4x4)
        TextureRegion[][] runFramesGrid = TextureRegion.split(correr, correr.getWidth() / RUN_FRAMES_X, correr.getHeight() / RUN_FRAMES_Y);

        // Crear array para animación correr (16 frames)
        TextureRegion[] runFrames = new TextureRegion[TOTAL_RUN_FRAMES];

        index = 0; // Resetear índice
        for (int i = 0; i < RUN_FRAMES_Y; i++) {
            for (int j = 0; j < RUN_FRAMES_X; j++) {
                runFrames[index++] = runFramesGrid[i][j];
            }
        }

        // animacion atacar (4x3)
        TextureRegion[][] atacarFramesGrid = TextureRegion.split(atacar, atacar.getWidth() / ATTACK_FRAMES_X, atacar.getHeight() / ATTACK_FRAMES_Y);

        // Crear array para animación de atacar(16 frames)
        TextureRegion[] atacarFrames = new TextureRegion[TOTAL_ATTACK_FRAMES];

        index = 0; //Resetear índice
        for (int i = 0; i < ATTACK_FRAMES_Y; i++) {
            for (int j = 0; j < ATTACK_FRAMES_X; j++) {
                atacarFrames[index++] = atacarFramesGrid[i][j];
            }
        }

        float frameDurationIdle = 0.08f;
        float frameDurationCorrer = 0.05f;
        float frameDurationAtacar = DURACION_ATAQUE / TOTAL_ATTACK_FRAMES;

        animacionIdle = new Animation<>(frameDurationIdle, idleFrames);
        animacionIdle.setPlayMode(Animation.PlayMode.LOOP);

        animacionCorrer = new Animation<>(frameDurationCorrer, runFrames);
        animacionCorrer.setPlayMode(Animation.PlayMode.LOOP);

        animacionAtacar = new Animation<>(frameDurationAtacar, atacarFrames);
        animacionAtacar.setPlayMode(Animation.PlayMode.NORMAL);

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

        // Atacar
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            atacar();
        }
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

        if (atacando) {
            animacionActual = animacionAtacar;
        } else if (moviendose) {
            animacionActual = animacionCorrer;
        } else {
            animacionActual = animacionIdle;
        }

        tiempo += deltaTime;

        if (!atacando && (estabaMoviendose != moviendose || estabaAtacando != atacando)) {
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


    public void render(final SpriteBatch batch) {
        if (animacionActual == null) return;

        // Debug del frame actual
        int frameIndex = animacionActual.getKeyFrameIndex(tiempo);
        if (Gdx.graphics.getFrameId() % 30 == 0) {
            Gdx.app.log("Player", "Frame: " + frameIndex + "/" + (moviendose ? TOTAL_RUN_FRAMES : TOTAL_IDLE_FRAMES) + ", Tiempo: " + tiempo + ", Estado: " + (moviendose ? "CORRIENDO" : "IDLE") + ", Atacando: " + (atacando? true : false));
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
            } else {
                drawWidth = neutral.getWidth() / (float)IDLE_FRAMES_X;
                drawHeight = neutral.getHeight() / (float)IDLE_FRAMES_Y;
            }

            // Dibujar según dirección
            if (direccionActual == 1) {
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
    }

    public static float getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public static float getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }
}
