package ui.projecto.mecanicas;

import java.util.Random;

public class EnemyWander {
    private float targetX, targetY;
    private boolean hasTarget = false;

    private float waitTime = 0f;
    private float currentWait = 0f;

    private final float speed;
    private final float maxWaitTime;
    private final float wanderRadius;

    private final Random random = new Random();

    private MapManager mapManager;
    private float entityWidth, entityHeight;

    public EnemyWander(float speed, float maxWaitTime, float wanderRadius, MapManager mapManager, float width, float height) {
        this.speed = speed;
        this.maxWaitTime = maxWaitTime;
        this.wanderRadius = wanderRadius;

        this.mapManager = mapManager;
        this.entityWidth = width;
        this.entityHeight = height;
    }

    public void update(float deltaTime, float x, float y) {
        if (!hasTarget) {
            currentWait += deltaTime;

            if (currentWait >= waitTime) {
                generateNewTarget(x,y);
                hasTarget = true;
            }
            return;
        }
    }

    private void generateNewTarget(float x, float y) {
        for (int attempts = 0; attempts < 10; attempts++) {
            float offsetX = (random.nextFloat() * 2 - 1) * wanderRadius;
            float offsetY = (random.nextFloat() * 2 - 1) * wanderRadius;

            float newX = x + offsetX;
            float newY = y + offsetY;
            if (!mapManager.isBlocked(newX, newY, entityWidth, entityHeight)) {
                targetX = newX;
                targetY = newY;
                waitTime = random.nextFloat() * maxWaitTime;
                currentWait = 0f;
                return;
            }
        }
        hasTarget = false;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public boolean reachedTarget(float x, float y) {
        float dx = targetX - x;
        float dy = targetY - y;
        return (dx * dx + dy * dy) <= 4f;
    }

    public float moveX(float x, float y, float delta) {
        float dx = targetX - x;
        float dy = targetY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return x;

        float newX = x + (dx / length) * speed * delta;
        if (mapManager.isBlocked(newX, y, entityWidth, entityHeight)) {
            hasTarget = false;
            return x;
        }
        return newX;
    }

    public float moveY(float x, float y, float delta) {
        float dx = targetX - x;
        float dy = targetY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return y;
        float newY = y + (dy / length) * speed * delta;

        if (mapManager.isBlocked(x, newY, entityWidth, entityHeight)) {
            hasTarget = false;
            return y;
        }
        return newY;
    }

    public void stop() {
        hasTarget = false;
    }
}
