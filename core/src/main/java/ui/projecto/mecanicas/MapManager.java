package ui.projecto.mecanicas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import ui.projecto.mecanicas.Enemies.EnemySpawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TiledMapTileLayer collisionLayer;

    private final List<Vector2> playerSpawns;
    private final List<EnemySpawn> enemySpawns;
    private final List<Vector2> hearthSpawns;
    private final List<Vector2> azerisSpawns;

    private final int tileSize;

    public MapManager(String mapPath){
        map = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        MapGroupLayer roomGroupLayer = (MapGroupLayer) map.getLayers().get("Room");
        collisionLayer = (TiledMapTileLayer) roomGroupLayer.getLayers().get("Paredes");
        MapLayer entityLayer = roomGroupLayer.getLayers().get("Entities");
        tileSize = collisionLayer.getTileWidth();

        playerSpawns = new ArrayList<>();
        MapLayer playerLayer = roomGroupLayer.getLayers().get("Player");
        if (playerLayer != null){
            for (MapObject obj : playerLayer.getObjects()) {
                float x = obj.getProperties().get("x", float.class);
                float y = obj.getProperties().get("y", float.class);

                playerSpawns.add(new Vector2(x,y));
            }
        }

        enemySpawns = new ArrayList<>();
        if (entityLayer != null) {
            for (MapObject obj : entityLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                String type = null;
                if (obj instanceof TiledMapTileMapObject) {
                    TiledMapTileMapObject tileObj = (TiledMapTileMapObject) obj;
                    type = tileObj.getTile().getProperties().get("type", String.class);
                }
                if (type == null){
                    System.out.println("Entidad sin tipo en: " + x + ", " + y);
                    continue;
                }
                enemySpawns.add(new EnemySpawn(x, y, type));
            }
        }
        hearthSpawns = new ArrayList<>();
        azerisSpawns = new ArrayList<>();

        MapLayer utilsLayer = roomGroupLayer.getLayers().get("Utils");

        if (utilsLayer != null) {
            for (MapObject obj : utilsLayer.getObjects()) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                String type = obj.getProperties().get("type", String.class);
                if (type == null && obj instanceof TiledMapTileMapObject) {
                    TiledMapTileMapObject tileObj = (TiledMapTileMapObject) obj;
                    type = tileObj.getTile().getProperties().get("type", String.class);
                }
                if (type == null) continue;
                switch (type) {
                    case "corazon":
                        hearthSpawns.add(new Vector2(x, y));
                        break;

                    case "azeris":
                        azerisSpawns.add(new Vector2(x, y));
                        break;
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

    public Vector2 getRandomPlayerSpawn() {
        if (playerSpawns.isEmpty()) return new Vector2(0,0);

        Random random = new Random();
        return playerSpawns.get(random.nextInt(playerSpawns.size()));
    }

    public List<EnemySpawn> getRandomEnemySpawns() {
        List<EnemySpawn> selected = new ArrayList<>();
        Random random = new Random();
        if (enemySpawns.isEmpty()) return selected;
        int count = random.nextInt(enemySpawns.size()) + 1;
        List<EnemySpawn> copy = new ArrayList<>(enemySpawns);
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.get(index));
            copy.remove(index);
        }
        System.out.println("[ENEMY] Enemigos spawneados: " + count);
        return selected;
    }

    public List<Vector2> getHearthSpawns() {
        if (hearthSpawns.isEmpty()) return new ArrayList<>();
        Random random = new Random();
        int count = random.nextInt(hearthSpawns.size() - 1) + 1;
        List<Vector2> copy = new ArrayList<>(hearthSpawns);
        List<Vector2> selected = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.get(index));
            copy.remove(index);
        }
        System.out.println("[HEART] Corazones spawneados: " + count);
        return selected;
    }

    public Vector2 getAzerisSpawn() {
        if (azerisSpawns.isEmpty()) {
            System.out.println("[AZERIS] Spawn fallido");
            return null;
        }
        Random random = new Random();
        Vector2 spawn = azerisSpawns.get(random.nextInt(azerisSpawns.size()));
        System.out.println("[AZERIS] Spawn correcto");
        return spawn;
    }
    public int getTileSize() {
        return tileSize;
    }
}
