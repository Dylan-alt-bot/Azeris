package ui.projecto.mecanicas.Enemies;

import com.badlogic.gdx.math.Vector2;

public class EnemyVision {
    private final float detectionRadius;
    private float lastSeenX = 0;
    private float lastSeenY = 0;
    private float lastSeenTime = -999f;

    public EnemyVision(float detectionRadius) {
        this.detectionRadius = detectionRadius;
    }

    public boolean isPlayerInRange(float enemyX, float enemyY, float playerX, float playerY) {
        float dx = playerX - enemyX;
        float dy = playerY - enemyY;

        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= detectionRadius * detectionRadius;
    }

    public void updateLastSeenPosition(float x, float y, float currentTime) {
        this.lastSeenX = x;
        this.lastSeenY = y;
        this.lastSeenTime = currentTime;
    }

    public boolean hasRecentMemory(float currentTime, float memoryDuration) {
        return currentTime - lastSeenTime <= memoryDuration;
    }

    public float getTimeSinceLastSeen(float currentTime) {
        return currentTime - lastSeenTime;
    }

    public Vector2 getLastSeenPosition() {
        return new Vector2(lastSeenX, lastSeenY);
    }

    public void clearMemory() {
        lastSeenTime = -999f;
    }
}
