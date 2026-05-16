package ui.projecto.personajes.Player.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class PlayerAudioManager {
    private final Sound run;
    private final Sound attack;
    private final Sound sprint;
    private final Sound hurt;
    private final Sound dead;
    private final Sound celebrate;

    private long runId = -1;
    private long deathId = -1;
    private boolean running = false;

    public PlayerAudioManager() {
        run = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/run.wav"));
        attack = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/attack.wav"));
        sprint = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/sprint.wav"));
        hurt = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/hurt.wav"));
        dead = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/dead.wav"));
        celebrate = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/player/celebrate.wav"));
    }

    public void playRun(){
        if (running) return;
        runId = run.play(0.7f);
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
        sprint.play(0.3f);
    }

    public void playHurt(){
        hurt.play(0.6f);
    }

    public void playCelebrate(){
        celebrate.play(0.6f);
    }

    public void triggerDeath(){
        stopRun();
        dead.stop();
        deathId = dead.play(0.3f);
    }

    public void reset(){
        stopRun();
        dead.stop();
        deathId = -1;
    }

    public void dispose(){
        run.dispose();
        attack.dispose();
        sprint.dispose();
        hurt.dispose();
        dead.dispose();
    }

    public void stopAllSounds(){
        run.stop();
        attack.stop();
        sprint.stop();
        hurt.stop();
        dead.stop();
    }
}
