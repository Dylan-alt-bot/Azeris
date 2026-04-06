package ui.projecto.mecanicas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TiledMapTileLayer collisionLayer;

    private final List<float[]> enemySpawns;

    private final int tileSize;

    public MapManager(String mapPath){
        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        MapGroupLayer roomGroupLayer = (MapGroupLayer) map.getLayers().get("Room");
        collisionLayer = (TiledMapTileLayer) roomGroupLayer.getLayers().get("Paredes");
        MapLayer entityLayer = roomGroupLayer.getLayers().get("Entities");
        tileSize = collisionLayer.getTileWidth();

        enemySpawns = new ArrayList<>();

        if (entityLayer != null) {
            for (MapObject obj : entityLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);

                enemySpawns.add(new float[]{x, y});
                System.out.println("Spawn añadido en: " + x + ", " + y);
            }
        }
    }

    public boolean isBlocked(float x, float y, float width, float height){
        int startX = (int) (x / tileSize);
        int startY = (int) (y / tileSize);
        int endX = (int) ((x + width - 1) / tileSize);
        int endY = (int) ((y + height - 1) / tileSize);

        for (int tx = startX; tx <= endX; tx++) {
            for (int ty = startY; ty <= endY; ty++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(tx, ty);
                if (cell != null && cell.getTile() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void render (OrthographicCamera camera){
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public List<float[]> getRandomEnemySpawns() {
        List<float[]> selected = new ArrayList<>();
        Random random = new Random();

        if (enemySpawns.isEmpty()) return selected;

        int maxEnemies = Math.min(2, enemySpawns.size());
        int count = random.nextInt(maxEnemies) + 3;

        List<float[]> copy = new ArrayList<>(enemySpawns);

        for (int i = 0; i < count; i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.get(index));
            copy.remove(index);
        }
        System.out.println("Enemy spawns detectados: " + enemySpawns.size());
        return selected;
    }

    public int getTileSize() {
        return tileSize;
    }
}
