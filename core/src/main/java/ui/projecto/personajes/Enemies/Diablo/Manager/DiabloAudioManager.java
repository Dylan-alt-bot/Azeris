package ui.projecto.personajes.Enemies.Diablo.Manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class DiabloAudioManager {
    private final Sound idle;
    private final Sound walk;
    private final Sound alert;
    private final Sound attack;
    private final Sound hurt;
    private final Sound defeat;

    private long idleId = -1;
    private long walkId = -1;

    private boolean idling = false;
    private boolean walking = false;

    public DiabloAudioManager() {
        idle = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloIdle.wav"));
        walk = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloWalk.wav"));
        alert = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloAlert.wav"));
        attack = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloAttack.wav"));
        hurt = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloHurt.wav"));
        defeat = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/enemy/diablo/diabloDefeat.wav"));
    }

    public void playIdle() {
        if (idling) return;
        stopWalk();
        idleId = idle.play(0.2f);
        idle.setLooping(idleId, true);
        idling = true;
    }

    public void stopIdle() {
        if (!idling) return;
        idle.stop(idleId);
        idleId = -1;
        idling = false;
    }

    public void playWalk() {
        if (walking) return;
        stopIdle();
        walkId = walk.play(0.3f);
        walk.setLooping(walkId, true);
        walking = true;
    }

    public void stopWalk() {
        if (!walking) return;
        walk.stop(walkId);
        walkId = -1;
        walking = false;
    }

    public void stopMovementAudio() {
        stopIdle();
        stopWalk();
    }

    public void playAlert() {
        alert.play(0.3f);
    }

    public void playAttack() {
        attack.play(0.4f);
    }

    public void playHurt() {
        hurt.play(0.4f);
    }

    public void playDefeat() {
        stopMovementAudio();
        defeat.play(0.4f);
    }

    public void dispose() {
        stopMovementAudio();
        idle.dispose();
        walk.dispose();
        alert.dispose();
        attack.dispose();
        hurt.dispose();
        defeat.dispose();
    }
}
