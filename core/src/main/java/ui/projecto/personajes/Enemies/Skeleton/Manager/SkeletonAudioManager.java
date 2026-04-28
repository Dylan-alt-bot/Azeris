package ui.projecto.personajes.Enemies.Skeleton.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SkeletonAudioManager {
    private final Sound walk;
    private final Sound run;
    private final Sound alert;
    private final Sound attack;
    private final Sound hurt;
    private final Sound dead;

    private long runId = -1;
    private boolean running = false;

    public SkeletonAudioManager(){
        walk = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonWalk.wav"));
        run = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonRun.wav"));
        alert = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonAlert.wav"));
        attack = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonAttack.wav"));
        hurt = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonHurt.wav"));
        dead = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/skeleton/skeletonDead.wav"));
    }

    public void playWalk() {
        if (running) return;
        runId = walk.play(0.1f);
        walk.setLooping(runId, true);
        running = true;
    }

    public void playRun() {
        if (running) return;
        runId = run.play(0.1f);
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

    public void playAttack() {
        attack.play(0.2f);
    }

    public void playHurt() {
        hurt.play(0.2f);
    }

    public void playDeath() {
        stopRun();
        dead.play(0.3f);
    }

    public void dispose(){
        stopRun();

        walk.dispose();
        run.dispose();
        alert.dispose();
        attack.dispose();
        hurt.dispose();
        dead.dispose();
    }
}
