package ui.projecto.personajes.Player.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class PlayerAudioManager {
    private final Sound run;
    private long runId = -1;
    private boolean running = false;

    private Sound attack;
    private Sound sprint;
    private Sound hurt;
    private Sound dead;

    public PlayerAudioManager() {
        run = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/run.wav"));
        // Implementar más tarde
    }

    public void startRun(){
        if (running) return;
        runId = run.play(0.6f);
        run.setLooping(runId, true);
        running = true;
    }

    public void stopRun(){
        if (!running) return;
        run.stop(runId);
        runId = -1;
        running = false;
    }

    public void playAttack(){
        attack.play(0.6f);
    }

    public void playSprint(){
        sprint.play(0.6f);
    }

    public void playHurt(){
        hurt.play(0.5f);
    }

    public void playDead(){
        dead.play(0.6f);
    }

    public void dispose(){
        run.dispose();
        attack.dispose();
        sprint.dispose();
        hurt.dispose();
        dead.dispose();
    }
}
