package ui.projecto.mecanicas.Enemies;

public class EnemyVision {
    private final float detectionRadius;

    public EnemyVision(float detectionRadius) {
        this.detectionRadius = detectionRadius;
    }

    public boolean isPlayerInRange(float enemyX, float enemyY, float playerX, float playerY) {
        float dx = playerX - enemyX;
        float dy = playerY - enemyY;

        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= detectionRadius * detectionRadius;
    }
}
