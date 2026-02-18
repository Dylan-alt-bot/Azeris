package ui.projecto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player{
    public float x, y;
    private final com.badlogic.gdx.graphics.g2d.BitmapFont font;

    private final Animation<TextureRegion> animacionIdle;
    private final Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionActual;
    private final Animation<TextureRegion> animacionAtacar;
    private final Animation<TextureRegion> animacionSprintar;

    private float tiempo;
    private int direccionActual;
    private boolean sprintCooldown = false;
    private float tiempoCooldownSprint = 0f;
    private static final float DURACION_COOLDOWN_SPRINT = 2f;

    private final Texture neutral;
    private final Texture correr;
    private final Texture atacar;
    private final Texture sprint;

    private float velocidad;
    private static final float VELOCIDAD_NORMAL = 150f;
    private static final float VELOCIDAD_SPRINT = 300f;
    private static final float DESPLAZAMIENTO_SPRINT = 150f;

    private boolean moviendose;
    private boolean atacando;
    private boolean sprintando;

    private float tiempoAtaque;
    private static final float DURACION_ATAQUE = 0.4f;

    private float tiempoSprint;
    private static final float DURACION_SPRINT = 0.4f;


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

    private final int SPRINT_FRAMES_X = 4;
    private final int SPRINT_FRAMES_Y = 3;
    private final int TOTAL_SPRINT_FRAMES = SPRINT_FRAMES_X * SPRINT_FRAMES_Y;

    private float desplazamientoSprintRestante = 0f;
    private int direccionSprint = 0;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocidad = VELOCIDAD_NORMAL;
        this.moviendose = false;
        this.direccionActual = 0;
        this.sprintando = false;

        neutral = new Texture(Gdx.files.internal("player/player.png"));
        correr = new Texture(Gdx.files.internal("player/Player_corriendo.png"));
        atacar = new Texture(Gdx.files.internal("player/player_atacar.png"));
        sprint = new Texture(Gdx.files.internal("player/player_sprint.png"));
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();

        // animación quieto (5x5)
        TextureRegion[][] idleFramesGrid = TextureRegion.split(neutral, neutral.getWidth() / IDLE_FRAMES_X, neutral.getHeight() / IDLE_FRAMES_Y);
        TextureRegion[] idleFrames = new TextureRegion[TOTAL_IDLE_FRAMES];
        int index = 0;
        for (int i = 0; i < IDLE_FRAMES_Y; i++) {
            for (int j = 0; j < IDLE_FRAMES_X; j++) {
                idleFrames[index++] = idleFramesGrid[i][j];
            }
        }

        // animación correr (4x4)
        TextureRegion[][] runFramesGrid = TextureRegion.split(correr, correr.getWidth() / RUN_FRAMES_X, correr.getHeight() / RUN_FRAMES_Y);
        TextureRegion[] runFrames = new TextureRegion[TOTAL_RUN_FRAMES];
        index = 0;
        for (int i = 0; i < RUN_FRAMES_Y; i++) {
            for (int j = 0; j < RUN_FRAMES_X; j++) {
                runFrames[index++] = runFramesGrid[i][j];
            }
        }

        // animacion atacar (4x3)
        TextureRegion[][] atacarFramesGrid = TextureRegion.split(atacar, atacar.getWidth() / ATTACK_FRAMES_X, atacar.getHeight() / ATTACK_FRAMES_Y);
        TextureRegion[] atacarFrames = new TextureRegion[TOTAL_ATTACK_FRAMES];
        index = 0;
        for (int i = 0; i < ATTACK_FRAMES_Y; i++) {
            for (int j = 0; j < ATTACK_FRAMES_X; j++) {
                atacarFrames[index++] = atacarFramesGrid[i][j];
            }
        }

        // animacion sprintar (4x3 pero la última línea tiene 2 sprites)
        TextureRegion[][] sprintFramesGrid = TextureRegion.split(sprint, sprint.getWidth() / SPRINT_FRAMES_X, sprint.getHeight() / SPRINT_FRAMES_Y);
        TextureRegion[] sprintFrames = new TextureRegion[TOTAL_SPRINT_FRAMES];
        index = 0;

        // Primeras dos filas completas (10 frames)
        for (int i = 0; i < SPRINT_FRAMES_Y; i++) {
            for (int j = 0; j < SPRINT_FRAMES_X; j++) {
                sprintFrames[index++] = sprintFramesGrid[i][j];
            }
        }

        float frameDurationIdle = 0.08f;
        float frameDurationCorrer = 0.04f;
        float frameDurationAtacar = DURACION_ATAQUE / TOTAL_ATTACK_FRAMES;
        float frameDurationSprint = DURACION_SPRINT / TOTAL_SPRINT_FRAMES;

        animacionIdle = new Animation<>(frameDurationIdle, idleFrames);
        animacionIdle.setPlayMode(Animation.PlayMode.LOOP);

        animacionCorrer = new Animation<>(frameDurationCorrer, runFrames);
        animacionCorrer.setPlayMode(Animation.PlayMode.LOOP);

        animacionAtacar = new Animation<>(frameDurationAtacar, atacarFrames);
        animacionAtacar.setPlayMode(Animation.PlayMode.NORMAL);

        animacionSprintar = new Animation<>(frameDurationSprint, sprintFrames);
        animacionSprintar.setPlayMode(Animation.PlayMode.NORMAL);

        tiempo = 0f;
        animacionActual = animacionIdle;
    }

    public void update(float deltaTime) {
        if (sprintCooldown){
            tiempoCooldownSprint += deltaTime;
            if (tiempoCooldownSprint >= DURACION_COOLDOWN_SPRINT){
                sprintCooldown = false;
                tiempoCooldownSprint = 0;
            }
        }
        // Si está sprintando, no permitir otras acciones
        if (sprintando) {
            actualizarSprint(deltaTime);
            return;
        }

        moviendose = false;

        if (atacando) {
            tiempoAtaque += deltaTime;
            if (tiempoAtaque >= DURACION_ATAQUE) {
                atacando = false;
                tiempoAtaque = 0f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            iniciarSprint();
            return;
        }

        // Atacar
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            atacar();
        }

        // Detectar movimiento
        boolean moviendoDerecha = false;
        boolean moviendoIzquierda = false;
        boolean moviendoArriba = false;
        boolean moviendoAbajo = false;

        // Movimiento
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= velocidad * deltaTime;
            moviendose = true;
            moviendoIzquierda = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += velocidad * deltaTime;
            moviendose = true;
            moviendoDerecha = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            y += velocidad * deltaTime;
            moviendose = true;
            moviendoArriba = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            y -= velocidad * deltaTime;
            moviendose = true;
            moviendoAbajo = true;
        }

        // Dirección para el sprite
        if (moviendoDerecha) {
            direccionActual = 1;
        } else if (moviendoIzquierda) {
            direccionActual = 2;
        } else if (moviendoArriba) {
            direccionActual = 3;
        } else if (moviendoAbajo) {
            direccionActual = 4;
        }

        // Seleccionar animación actual
        if (atacando) {
            animacionActual = animacionAtacar;
        } else if (moviendose) {
            animacionActual = animacionCorrer;
        } else {
            animacionActual = animacionIdle;
        }

        tiempo += deltaTime;

        // Aplicar límites de pantalla
        aplicarLimites();
    }

    private void iniciarSprint() {
        if (sprintCooldown) return;

        sprintando = true;
        tiempoSprint = 0f;
        tiempo = 0f;
        velocidad = VELOCIDAD_SPRINT;
        animacionActual = animacionSprintar;

        sprintCooldown = true;
        tiempoCooldownSprint = 0f;

        direccionSprint = direccionActual;

        // Establecer el desplazamiento restante
        desplazamientoSprintRestante = DESPLAZAMIENTO_SPRINT;
        if (direccionSprint == 0) {
            direccionSprint = 1;
        }
    }

    private void actualizarSprint(float deltaTime) {
        tiempoSprint += deltaTime;
        tiempo += deltaTime;

        // Aplicar desplazamiento durante el sprint
        if (desplazamientoSprintRestante > 0) {
            float desplazamientoEsteFrame = DESPLAZAMIENTO_SPRINT * (deltaTime / DURACION_SPRINT);
            desplazamientoEsteFrame = Math.min(desplazamientoEsteFrame, desplazamientoSprintRestante);

            // Aplicar desplazamiento según la dirección
            switch (direccionSprint) {
                case 1: // Derecha
                    x += desplazamientoEsteFrame;
                    break;
                case 2: // Izquierda
                    x -= desplazamientoEsteFrame;
                    break;
                case 3: // Arriba
                    y += desplazamientoEsteFrame;
                    break;
                case 4: // Abajo
                    y -= desplazamientoEsteFrame;
                    break;
                default: // Si no hay dirección, usar derecha por defecto
                    x += desplazamientoEsteFrame;
                    break;
            }

            desplazamientoSprintRestante -= desplazamientoEsteFrame;
        }

        // Verificar si el sprint ha terminado
        if (tiempoSprint >= DURACION_SPRINT || animacionSprintar.isAnimationFinished(tiempo)) {
            finalizarSprint();
        }

        // Aplicar límites durante el sprint
        aplicarLimites();
    }

    private void finalizarSprint() {
        sprintando = false;
        tiempoSprint = 0f;
        velocidad = VELOCIDAD_NORMAL;
        desplazamientoSprintRestante = 0f;
        animacionActual = animacionIdle;
    }

    public void atacar() {
        if (!atacando && !sprintando) {
            atacando = true;
            tiempoAtaque = 0f;
            tiempo = 0f;
        }
    }

    public void render(final SpriteBatch batch) {
        if (sprintCooldown) {
            float segundosRestantes = DURACION_COOLDOWN_SPRINT - tiempoCooldownSprint;
            font.draw(batch, String.format("Proximo Sprint: %.1f", segundosRestantes), 10, 30);
        }
        if (animacionActual == null) return;

        TextureRegion frameActual;

        // Obtener el frame actual según el modo de la animación
        if (animacionActual.getPlayMode() == Animation.PlayMode.NORMAL) {
            frameActual = animacionActual.getKeyFrame(tiempo, false);
        } else {
            frameActual = animacionActual.getKeyFrame(tiempo, true);
        }

        if (frameActual != null) {
            // Obtener dimensiones correctas según la animación actual
            float drawWidth, drawHeight;

            if (animacionActual == animacionSprintar) {
                // Para sprint, usar dimensiones del sprite de sprint
                drawWidth = sprint.getWidth() / (float) SPRINT_FRAMES_X;
                drawHeight = sprint.getHeight() / (float) SPRINT_FRAMES_Y;
            } else if (animacionActual == animacionAtacar) {
                drawWidth = atacar.getWidth() / (float) ATTACK_FRAMES_X;
                drawHeight = atacar.getHeight() / (float) ATTACK_FRAMES_Y;
            } else if (animacionActual == animacionCorrer) {
                drawWidth = correr.getWidth() / (float) RUN_FRAMES_X;
                drawHeight = correr.getHeight() / (float) RUN_FRAMES_Y;
            } else {
                drawWidth = neutral.getWidth() / (float) IDLE_FRAMES_X;
                drawHeight = neutral.getHeight() / (float) IDLE_FRAMES_Y;
            }

            // Dibujar según dirección
            if (direccionActual == 1 || (sprintando && direccionSprint == 1)) {
                // Mirando a la derecha - voltear horizontalmente
                batch.draw(frameActual, x + drawWidth, y, -drawWidth, drawHeight);
            } else {
                // Mirando a la izquierda o cualquier otra dirección
                batch.draw(frameActual, x, y, drawWidth, drawHeight);
            }
        }
    }

    private void aplicarLimites() {
        float anchoActual = getCurrentWidth();
        float altoActual = getCurrentHeight();

        if (x < 0) x = 0;
        if (x > VIRTUAL_WIDTH - anchoActual) x = VIRTUAL_WIDTH - anchoActual;
        if (y < 0) y = 0;
        if (y > VIRTUAL_HEIGHT - altoActual) y = VIRTUAL_HEIGHT - altoActual;
    }

    public float getCurrentWidth() {
        if (animacionActual == animacionSprintar) {
            return sprint.getWidth() / (float) SPRINT_FRAMES_X;
        } else if (animacionActual == animacionAtacar) {
            return atacar.getWidth() / (float) ATTACK_FRAMES_X;
        } else if (animacionActual == animacionCorrer) {
            return correr.getWidth() / (float) RUN_FRAMES_X;
        } else {
            return neutral.getWidth() / (float) IDLE_FRAMES_X;
        }
    }

    public float getCurrentHeight() {
        if (animacionActual == animacionSprintar) {
            return sprint.getHeight() / (float) SPRINT_FRAMES_Y;
        } else if (animacionActual == animacionAtacar) {
            return atacar.getHeight() / (float) ATTACK_FRAMES_Y;
        } else if (animacionActual == animacionCorrer) {
            return correr.getHeight() / (float) RUN_FRAMES_Y;
        } else {
            return neutral.getHeight() / (float) IDLE_FRAMES_Y;
        }
    }

    public void dispose() {
        if (neutral != null) neutral.dispose();
        if (correr != null) correr.dispose();
        if (atacar != null) atacar.dispose();
        if (sprint != null) sprint.dispose();
        if (font != null) font.dispose();
    }

    public static float getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public static float getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }
}
