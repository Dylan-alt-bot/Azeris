package ui.projecto.mecanicas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.MapObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;

    private List<float[]> enemySpawns;

    private int tileSize;

    public MapManager(String mapPath){
        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        MapGroupLayer roomGroupLayer = (MapGroupLayer) map.getLayers().get("Room");
        collisionLayer = (TiledMapTileLayer) roomGroupLayer.getLayers().get("Paredes");
        tileSize = collisionLayer.getTileWidth();

        enemySpawns = new ArrayList<>();
        MapLayer entityLayer = map.getLayers().get("Entities");
        if (entityLayer != null) {
            MapObjects objects = entityLayer.getObjects();

            for (MapObject obj : objects) {
                if (obj instanceof RectangleMapObject) {
                    RectangleMapObject rect = (RectangleMapObject) obj; // cast explícito
                    float x = rect.getRectangle().x;
                    float y = rect.getRectangle().y;
                    enemySpawns.add(new float[]{x, y});
                }

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

        int maxEnemies = Math.min(3, enemySpawns.size());
        int count = random.nextInt(maxEnemies) + 1;

        List<float[]> copy = new ArrayList<>(enemySpawns);

        for (int i = 0; i < count; i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.get(index));
            copy.remove(index);
        }

        return selected;
    }
}
