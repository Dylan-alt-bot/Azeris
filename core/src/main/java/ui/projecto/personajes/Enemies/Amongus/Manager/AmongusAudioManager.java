package ui.projecto.personajes.Enemies.Amongus.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class AmongusAudioManager {
    private final Sound run;
    private final Sound alert;
    private final Sound hurt;
    private final Sound dead;

    private final Sound attack1;
    private final Sound attack2;
    private final Sound attack3;

    private long runId = -1;
    private boolean running = false;

    public AmongusAudioManager() {
        run = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusRun.wav"));
        alert = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusAlert.wav"));
        hurt = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusHurt.wav"));
        dead = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusDead.wav"));

        attack1 = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusAttack1.wav"));
        attack2 = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusAttack2.wav"));
        attack3 = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/amongus/amongusAttack3.wav"));
    }

    public void playRun() {
        if (running) return;
        runId = run.play(0.4f);
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
        alert.play(0.3f);
    }

    public void playHurt() {
        hurt.play(0.3f);
    }

    public void playDead() {
        stopRun();
        dead.play(0.2f);
    }

    public void playAttack1() {
        attack1.play(0.1f);
    }

    public void playAttack2() {
        attack2.play(0.1f);
    }

    public void playAttack3() {
        attack3.play(0.08f);
    }

    public void dispose() {
        stopRun();

        run.dispose();
        alert.dispose();
        hurt.dispose();
        dead.dispose();

        attack1.dispose();
        attack2.dispose();
        attack3.dispose();
    }
}
