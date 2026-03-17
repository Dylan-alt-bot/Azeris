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

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;

    private float playerSpawnX;
    private float playerSpawnY;

    private int tileSize;

    public MapManager(String mapPath){
        map = new TmxMapLoader().load(mapPath);

        mapRenderer = new OrthogonalTiledMapRenderer(map);

        MapGroupLayer roomGroupLayer = (MapGroupLayer) map.getLayers().get("Room");
        collisionLayer = (TiledMapTileLayer) roomGroupLayer.getLayers().get("Paredes");

        tileSize = collisionLayer.getTileWidth();

        loadEntities();
    }

    private void loadEntities() {
        MapLayer entityLayer = map.getLayers().get("Entities");
        if(entityLayer == null) return;

        for(MapObject object : entityLayer.getObjects()){

            if(object instanceof RectangleMapObject){

                RectangleMapObject rect = (RectangleMapObject)object;

                String name = object.getName();

                if("playerSpawn".equals(name)){
                    playerSpawnX = rect.getRectangle().x;
                    playerSpawnY = rect.getRectangle().y;
                }

                if("enemy".equals(name)){
                    System.out.println("Spawn enemigo en: "
                        + rect.getRectangle().x + " "
                        + rect.getRectangle().y);
                }
            }
        }
    }
    public boolean isBlocked(float worldX, float worldY){
        int titleX = (int) worldX / tileSize;
        int titleY = (int) worldY / tileSize;

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(titleX, titleY);

        if (cell == null){
            return false;
        }
        return cell.getTile() != null;
    }

    public void render (OrthographicCamera camera){
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public float getPlayerSpawnX(){
        return playerSpawnX;
    }

    public float getPlayerSpawnY(){
        return playerSpawnY;
    }
}
