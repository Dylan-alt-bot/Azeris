package ui.projecto.personajes.Enemies.Goomba.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class GoombaAudioManager {
    private final Sound run;
    private final Sound alert;
    private final Sound hit;
    private final Sound dead;

    private long runId = -1;
    private boolean running = false;

    public GoombaAudioManager() {
        run = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/goomba/goombaRun.wav"));
        alert = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/goomba/goombaAlert.wav"));
        hit = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/goomba/goombaHurt.wav"));
        dead = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/goomba/goombaDead.wav"));
    }

    public void playRun() {
        if (running) return;
        runId = run.play(0.08f);
        run.setLooping(runId, true);
        running = true;
    }

    public void stopRun() {
        if (!running) return;
        run.stop(runId);
        runId = -1;
        running = false;
    }

    public void playAlert() {
        alert.play(0.1f);
    }

    public void playHit() {
        hit.play(0.3f);
    }

    public void playDeath() {
        stopRun();
        dead.play(0.3f);
    }

    public void dispose(){
        stopRun();
        run.dispose();
        alert.dispose();
        hit.dispose();
        dead.dispose();
    }
}
