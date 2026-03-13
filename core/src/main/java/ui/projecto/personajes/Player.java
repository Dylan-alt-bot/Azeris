package ui.projecto.personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;

public class Player{
    public float x, y;
    private final com.badlogic.gdx.graphics.g2d.BitmapFont font;
    private MapManager mapManager;

    private final Animation<TextureRegion> animacionIdle;
    private final Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionActual;
    private final Animation<TextureRegion> animacionAtacar;
    private final Animation<TextureRegion> animacionSprintar;
    private Animation<TextureRegion> animacionDolor;
    private Animation<TextureRegion> animacionMuerte;

    private float tiempo;
    private int direccionActual;
    private boolean sprintCooldown = false;
    private float tiempoCooldownSprint = 0f;
    private static final float DURACION_COOLDOWN_SPRINT = 2f;
    private int direccionDolor;

    private final Texture neutral;
    private final Texture correr;
    private final Texture atacar;
    private final Texture sprint;
    private Texture dolor;
    private Texture muerte;
    private Texture barraVida;

    private float velocidad;
    private static final float VELOCIDAD_NORMAL = 150f;
    private static final float VELOCIDAD_SPRINT = 300f;
    private static final float DESPLAZAMIENTO_SPRINT = 150f;

    private boolean moviendose;
    private boolean atacando;
    private boolean sprintando;
    private boolean recibiendoDany = false;
    private boolean muerto = false;

    private float tiempoAtaque;
    private static final float DURACION_ATAQUE = 0.4f;

    private float tiempoSprint;
    private static final float DURACION_SPRINT = 0.4f;

    private float tiempoDolor;
    private static final float DURACION_DOLOR = 0.3f;
    private static final float RETROCESO = 40f;

    private float tiempoMuerte;
    private static final float DURACION_MUERTE = 2.5f;

    private static final float VIRTUAL_WIDTH = 650;
    private static final float VIRTUAL_HEIGHT = 480;

    private Vida vida;

    private final int IDLE_FRAMES_X = 5;
    private final int IDLE_FRAMES_Y = 5;

    private final int RUN_FRAMES_X = 4;
    private final int RUN_FRAMES_Y = 4;

    private final int ATTACK_FRAMES_X = 4;
    private final int ATTACK_FRAMES_Y = 3;

    private final int SPRINT_FRAMES_X = 4;
    private final int SPRINT_FRAMES_Y = 3;

    private final int DOLOR_FRAMES_X = 3;
    private final int DOLOR_FRAMES_Y = 3;
    private int TOTAL_DOLOR_FRAMES = DOLOR_FRAMES_X * DOLOR_FRAMES_Y;

    private final int MUERTE_FRAMES_X = 7;
    private final int MUERTE_FRAMES_Y = 6;
    private int TOTAL_MUERTE_FRAMES =  MUERTE_FRAMES_X * MUERTE_FRAMES_Y;

    private float desplazamientoSprintRestante = 0f;
    private int direccionSprint = 0;

    private float tiempoDanyBorde = 0f;
    private static final float INTERVALO_DANY_BORDE = 0.2f;
    private static final int DANY_BORDE = 5;

    public Player(float x, float y, MapManager mapManager) {
        this.x = x;
        this.y = y;
        this.mapManager = mapManager;

        this.velocidad = VELOCIDAD_NORMAL;
        this.moviendose = false;
        this.direccionActual = 0;
        this.sprintando = false;

        this.vida = new Vida(100);
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();

        neutral = new Texture(Gdx.files.internal("player/player.png"));
        correr = new Texture(Gdx.files.internal("player/Player_corriendo.png"));
        atacar = new Texture(Gdx.files.internal("player/player_atacar.png"));
        sprint = new Texture(Gdx.files.internal("player/player_sprint.png"));
        dolor = new Texture(Gdx.files.internal("player/player_dolor.png"));
        muerte = new Texture(Gdx.files.internal("player/player_muerte.png"));

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        barraVida = new Texture(pixmap);
        pixmap.dispose();

        // animación quieto (5x5)
        TextureRegion[][] idleFramesGrid = TextureRegion.split(neutral, neutral.getWidth() / IDLE_FRAMES_X, neutral.getHeight() / IDLE_FRAMES_Y);
        int TOTAL_IDLE_FRAMES = IDLE_FRAMES_X * IDLE_FRAMES_Y;
        TextureRegion[] idleFrames = new TextureRegion[TOTAL_IDLE_FRAMES];
        int index = 0;
        for (int i = 0; i < IDLE_FRAMES_Y; i++) {
            for (int j = 0; j < IDLE_FRAMES_X; j++) {
                idleFrames[index++] = idleFramesGrid[i][j];
            }
        }

        // animación correr (4x4)
        TextureRegion[][] runFramesGrid = TextureRegion.split(correr, correr.getWidth() / RUN_FRAMES_X, correr.getHeight() / RUN_FRAMES_Y);
        int TOTAL_RUN_FRAMES = RUN_FRAMES_X * RUN_FRAMES_Y;
        TextureRegion[] runFrames = new TextureRegion[TOTAL_RUN_FRAMES];
        index = 0;
        for (int i = 0; i < RUN_FRAMES_Y; i++) {
            for (int j = 0; j < RUN_FRAMES_X; j++) {
                runFrames[index++] = runFramesGrid[i][j];
            }
        }

        // animacion atacar (4x3)
        TextureRegion[][] atacarFramesGrid = TextureRegion.split(atacar, atacar.getWidth() / ATTACK_FRAMES_X, atacar.getHeight() / ATTACK_FRAMES_Y);
        int TOTAL_ATTACK_FRAMES = ATTACK_FRAMES_X * ATTACK_FRAMES_Y;
        TextureRegion[] atacarFrames = new TextureRegion[TOTAL_ATTACK_FRAMES];
        index = 0;
        for (int i = 0; i < ATTACK_FRAMES_Y; i++) {
            for (int j = 0; j < ATTACK_FRAMES_X; j++) {
                atacarFrames[index++] = atacarFramesGrid[i][j];
            }
        }

        // animacion sprintar (4x3)
        TextureRegion[][] sprintFramesGrid = TextureRegion.split(sprint, sprint.getWidth() / SPRINT_FRAMES_X, sprint.getHeight() / SPRINT_FRAMES_Y);
        int TOTAL_SPRINT_FRAMES = SPRINT_FRAMES_X * SPRINT_FRAMES_Y;
        TextureRegion[] sprintFrames = new TextureRegion[TOTAL_SPRINT_FRAMES];
        index = 0;
        for (int i = 0; i < SPRINT_FRAMES_Y; i++) {
            for (int j = 0; j < SPRINT_FRAMES_X; j++) {
                sprintFrames[index++] = sprintFramesGrid[i][j];
            }
        }

        // animacion dolor(3x3)
        TextureRegion[][] dolorFramesGrid = TextureRegion.split(dolor,dolor.getWidth() / DOLOR_FRAMES_X, dolor.getHeight() / DOLOR_FRAMES_Y);
        TextureRegion[] dolorFrames = new TextureRegion[TOTAL_DOLOR_FRAMES];
        index = 0;
        for (int i = 0; i < DOLOR_FRAMES_Y; i++) {
            for (int j = 0; j < DOLOR_FRAMES_X; j++) {
                dolorFrames[index++] = dolorFramesGrid[i][j];
            }
        }

        // animacion muerte (7x6)
        TextureRegion[][] muerteFramesGrid = TextureRegion.split(muerte,muerte.getWidth() / MUERTE_FRAMES_X, muerte.getHeight() / MUERTE_FRAMES_Y);
        TextureRegion[] muerteFrames = new TextureRegion[TOTAL_MUERTE_FRAMES];
        index = 0;
        for (int i = 0; i < MUERTE_FRAMES_Y; i++) {
            for (int j = 0; j < MUERTE_FRAMES_X; j++) {
                muerteFrames[index++] = muerteFramesGrid[i][j];
            }
        }

        float frameDurationIdle = 0.08f;
        float frameDurationCorrer = 0.04f;
        float frameDurationAtacar = DURACION_ATAQUE / TOTAL_ATTACK_FRAMES;
        float frameDurationSprint = DURACION_SPRINT / TOTAL_SPRINT_FRAMES;
        float frameDurationDanio = DURACION_DOLOR / TOTAL_DOLOR_FRAMES;
        float frameDurationMuerte = DURACION_MUERTE / TOTAL_MUERTE_FRAMES;

        animacionIdle = new Animation<>(frameDurationIdle, idleFrames);
        animacionIdle.setPlayMode(Animation.PlayMode.LOOP);

        animacionCorrer = new Animation<>(frameDurationCorrer, runFrames);
        animacionCorrer.setPlayMode(Animation.PlayMode.LOOP);

        animacionAtacar = new Animation<>(frameDurationAtacar, atacarFrames);
        animacionAtacar.setPlayMode(Animation.PlayMode.NORMAL);

        animacionSprintar = new Animation<>(frameDurationSprint, sprintFrames);
        animacionSprintar.setPlayMode(Animation.PlayMode.NORMAL);

        animacionDolor = new Animation<>(frameDurationDanio, dolorFrames);
        animacionDolor.setPlayMode(Animation.PlayMode.NORMAL);

        animacionMuerte = new Animation<>(frameDurationMuerte, muerteFrames);
        animacionMuerte.setPlayMode(Animation.PlayMode.NORMAL);

        tiempo = 0f;
        animacionActual = animacionIdle;
    }

    public void update(float deltaTime) {
        if (vida.isMuerto()) {
            if (!muerto) {
                muerto = true;
                tiempoMuerte = 0f;
                tiempo = 0f;
                animacionActual = animacionMuerte;
            }

            tiempoMuerte += deltaTime;
            tiempo += deltaTime;

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                revivir();
            }
            return;
        }

        gestionarDanyBorde(deltaTime);

        if (recibiendoDany) {
            tiempoDolor += deltaTime;
            tiempo += deltaTime;

            aplicarRetroceso(deltaTime);

            if (tiempoDolor >= DURACION_DOLOR || animacionDolor.isAnimationFinished(tiempo)) {
                recibiendoDany = false;
                tiempoDolor = 0f;
                vida.resetRecibiendoDolor();
            }

            aplicarLimites(deltaTime);
            return;
        }

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

        float newX = x;
        float newY = y;

        // Detectar movimiento
        boolean moviendoDerecha = false;
        boolean moviendoIzquierda = false;
        boolean moviendoArriba = false;
        boolean moviendoAbajo = false;

        // Movimiento
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= velocidad * deltaTime;
            moviendose = true;
            moviendoIzquierda = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += velocidad * deltaTime;
            moviendose = true;
            moviendoDerecha = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += velocidad * deltaTime;
            moviendose = true;
            moviendoArriba = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= velocidad * deltaTime;
            moviendose = true;
            moviendoAbajo = true;
        }

        float width = getCurrentWidth() * 0.6f;
        float height = getCurrentHeight() * 0.6f;

        // Colisión horizontal
        if (!mapManager.isBlocked(newX, y) &&
            !mapManager.isBlocked(newX + width, y) &&
            !mapManager.isBlocked(newX, y + height) &&
            !mapManager.isBlocked(newX + width, y + height)) {

            x = newX;
        }

        // Colisión vertical
        if (!mapManager.isBlocked(x, newY) &&
            !mapManager.isBlocked(x + width, newY) &&
            !mapManager.isBlocked(x, newY + height) &&
            !mapManager.isBlocked(x + width, newY + height)) {

            y = newY;
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
        aplicarLimites(deltaTime);
    }

    private void gestionarDanyBorde(float deltaTime) {
        float anchoActual = getCurrentWidth();
        float altoActual = getCurrentHeight();

        boolean estaEnBorde = (x <= 0 || x >= VIRTUAL_WIDTH - anchoActual ||
            y <= 0 || y >= VIRTUAL_HEIGHT - altoActual);

        if (estaEnBorde && !vida.isMuerto() && !recibiendoDany) {
            tiempoDanyBorde += deltaTime;
            while (tiempoDanyBorde >= INTERVALO_DANY_BORDE) {
                tiempoDanyBorde -= INTERVALO_DANY_BORDE;
                int vidaAnterior = vida.getVidaActual();
                vida.recibirDolor(DANY_BORDE);

                // Si la vida bajó, activar animación de daño
                if (vida.getVidaActual() < vidaAnterior && !vida.isMuerto()) {
                    activarDany();
                }
            }
        } else {
            tiempoDanyBorde = 0f;
        }
    }

    private void activarDany() {
        if (!recibiendoDany && !vida.isMuerto()) {
            recibiendoDany = true;
            tiempoDolor = 0f;
            tiempo = 0f;
            animacionActual = animacionDolor;
            direccionDolor = direccionActual;

            // Si no hay dirección, usar derecha por defecto
            if (direccionDolor == 0) direccionDolor = 1;
        }
    }

    private void aplicarRetroceso(float deltaTime) {
        float retroceso = RETROCESO * deltaTime;
        switch (direccionDolor) {
            case 1: x -= retroceso; break; // Si mira derecha, retrocede izquierda
            case 2: x += retroceso; break; // Si mira izquierda, retrocede derecha
            case 3: y -= retroceso; break; // Si mira arriba, retrocede abajo
            case 4: y += retroceso; break; // Si mira abajo, retrocede arriba
        }
    }

    private void revivir() {
        vida.revivir();
        muerto = false;
        recibiendoDany = false;
        sprintando = false;
        atacando = false;
        tiempo = 0f;
        animacionActual = animacionIdle;
        x = VIRTUAL_WIDTH / 2;
        y = VIRTUAL_HEIGHT / 2;
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
            // Aplicar desplazamiento según la dirección
            switch (direccionSprint) {
                case 1: // Derecha
                    x += velocidad * deltaTime;
                    break;
                case 2: // Izquierda
                    x -= velocidad * deltaTime;
                    break;
                case 3: // Arriba
                    y += velocidad * deltaTime;
                    break;
                case 4: // Abajo
                    y -= velocidad * deltaTime;
                    break;
                default: // Si no hay dirección, usar derecha por defecto
                    x += velocidad * deltaTime;
                    break;
            }

        // Verificar si el sprint ha terminado
        if (animacionSprintar.isAnimationFinished(tiempo)) {
            finalizarSprint();
        }

        // Aplicar límites durante el sprint
        aplicarLimites(deltaTime);
    }

    private void finalizarSprint() {
        sprintando = false;
        tiempoSprint = 0f;
        velocidad = VELOCIDAD_NORMAL;
        switch (direccionSprint) {
            case 1: x += 5f; break;
            case 2: x -= 5f; break;
            case 3: y += 5f; break;
            case 4: y -= 5f; break;
        }
        animacionActual = animacionIdle;
    }

    public void atacar() {
        if (!atacando && !sprintando && !vida.isMuerto()) {
            atacando = true;
            tiempoAtaque = 0f;
            tiempo = 0f;
        }
    }

    public void render(final SpriteBatch batch) {
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
            } else if (animacionActual == animacionDolor) {
                drawWidth = dolor.getWidth() / (float) DOLOR_FRAMES_X;
                drawHeight = dolor.getHeight() / (float) DOLOR_FRAMES_Y;
            } else if (animacionActual == animacionMuerte) {
                drawWidth = muerte.getWidth() / (float) MUERTE_FRAMES_X;
                drawHeight = muerte.getHeight() / (float) MUERTE_FRAMES_Y;
            } else {
                drawWidth = neutral.getWidth() / (float) IDLE_FRAMES_X;
                drawHeight = neutral.getHeight() / (float) IDLE_FRAMES_Y;
            }

            int direccionDibujo;
            if (sprintando) {
                direccionDibujo = direccionSprint;
            } else if (recibiendoDany) {
                direccionDibujo = direccionDolor;
            } else {
                direccionDibujo = direccionActual;
            }

            // Dibujar según dirección
            if (direccionDibujo == 1) {
                batch.draw(frameActual, x + drawWidth, y, -drawWidth, drawHeight);
            } else {
                batch.draw(frameActual, x, y, drawWidth, drawHeight);
            }

            if (!vida.isMuerto()) {
                font.draw(batch, "Vida: " + vida.getVidaActual() + "%", 10, 440);

                // Barra de vida
                float anchoBarra = 250;
                float altoBarra = 15;
                float xBarra = 10;
                float yBarra = 450;

                batch.setColor(0.8f, 0.2f, 0.2f, 1);
                batch.draw(barraVida, xBarra, yBarra, anchoBarra, altoBarra);

                batch.setColor(0.2f, 0.8f, 0.2f, 1);
                float anchoVida = vida.getPorcentajeVida() * anchoBarra;
                batch.draw(barraVida, xBarra, yBarra, anchoVida, altoBarra);

                batch.setColor(1, 1, 1, 1);
            } else {
                font.draw(batch, "HAS MUERTO - Presiona R para revivir",
                    VIRTUAL_WIDTH/2 - 150, VIRTUAL_HEIGHT/2);
            }

            if (sprintCooldown) {
                float segundosRestantes = DURACION_COOLDOWN_SPRINT - tiempoCooldownSprint;
                font.draw(batch, String.format("Proximo Sprint: %.1f", segundosRestantes), 10, 420);
            }
        }
    }

    private void aplicarLimites(float deltaTime) {
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
        } else if (animacionActual == animacionDolor) {
            return dolor.getHeight() / (float) IDLE_FRAMES_X;
        } else if (animacionActual == animacionMuerte) {
            return muerte.getHeight() / (float) IDLE_FRAMES_X;
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
        } else if (animacionActual == animacionDolor) {
            return dolor.getHeight() / (float) IDLE_FRAMES_Y;
        } else if (animacionActual == animacionMuerte) {
            return muerte.getHeight() / (float) IDLE_FRAMES_Y;
        } else {
            return neutral.getHeight() / (float) IDLE_FRAMES_Y;
        }
    }

    public void dispose() {
        if (neutral != null) neutral.dispose();
        if (correr != null) correr.dispose();
        if (atacar != null) atacar.dispose();
        if (sprint != null) sprint.dispose();
        if (dolor != null) dolor.dispose();
        if (muerte != null) muerte.dispose();
        if (barraVida != null) barraVida.dispose();
        if (font != null) font.dispose();
    }

    public static float getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public static float getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }
}
