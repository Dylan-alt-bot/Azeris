package ui.projecto.mecanicas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;

    private int tileSize;

    public MapManager(String mapPath){
        map = new TmxMapLoader().load(mapPath);

        mapRenderer = new OrthogonalTiledMapRenderer(map);

        collisionLayer = (TiledMapTileLayer) map.getLayers().get("Paredes");

        tileSize = (int) collisionLayer.getTileWidth();
    }

    public void render (OrthographicCamera camera){
        mapRenderer.setView(camera);
        mapRenderer.render();
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
}
