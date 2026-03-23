package io.github.azerisproyecte;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.azerisproyecte.levelGenerator.Door;
import io.github.azerisproyecte.levelGenerator.DungeonGenerator;
import io.github.azerisproyecte.levelGenerator.Room;

import java.util.*;

public class FirstScreen extends ScreenAdapter implements InputProcessor {

    //public static final float WORLD_WIDTH = 25f;
    //public static final float WORLD_HEIGHT = 25f * 9f/16f;

    private final azerisMain azerisMain;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Engine engine;
    private List<Room> dungeon;
    private DungeonGenerator generator;
    private Texture wallTexture;
    private Texture floorTexture;
    private Texture doorTexture;
    private final float TILE_SIZE = 1.0f;

    private float minZoom = 0.2f;    // How far you can zoom in
    private float maxZoom = 3.0f;    // How far you can zoom out
    private float zoomSpeed = 0.1f;


    public FirstScreen(azerisMain azerisMain) {

        this.azerisMain = azerisMain;
        this.viewport = azerisMain.getViewport();
        this.camera = azerisMain.getCamera();
        this.engine = new Engine();

        generator = new DungeonGenerator(8, 8); // 16x10 grid = 160 tiles
        dungeon = generator.generateDungeon((int)(10 + Math.random() * 5)); // Generate 10-15 rooms --> (int) (10 + Math.random() * 5)

        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GRAY); // Wall color
        pixmap.fill();
        wallTexture = new Texture(pixmap);
        pixmap.dispose();

        pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BROWN); // Floor color
        pixmap.fill();
        floorTexture = new Texture(pixmap);
        pixmap.dispose();

        pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLUE); // Door Color
        pixmap.fill();
        doorTexture = new Texture(pixmap);
        pixmap.dispose();

        // Set up camera position to see the dungeon
        camera.position.set(4, 4, 0); // Center around grid position (4,4)
        camera.zoom = 1.5f; // Zoom out a bit to see more
        camera.update();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera (handles zoom from input)
        camera.update();
        azerisMain.getBatch().setProjectionMatrix(camera.combined);

        // Begin drawing
        azerisMain.getBatch().begin();

        // Draw each room
        for (Room room : dungeon) {
            drawRoom(room);
        }

        azerisMain.getBatch().end();

    }

    private void drawRoom(Room room) {
        // Get the CORRECT world position from the generator
        float worldX = generator.getRoomWorldX(room);
        float worldY = generator.getRoomWorldY(room);

        // Draw each tile in the room
        for (int i = 0; i < room.width; i++) {
            for (int j = 0; j < room.height; j++) {
                Texture texture;

                // Check if this tile is a door
                boolean isDoor = false;
                for (Door door : room.doors) {
                    // Check if door is on this wall at this position
                    if ((door.dir == Door.Direction.NORTH && j == room.height - 1 && i == door.position) ||
                        (door.dir == Door.Direction.SOUTH && j == 0 && i == door.position) ||
                        (door.dir == Door.Direction.EAST && i == room.width - 1 && j == door.position) ||
                        (door.dir == Door.Direction.WEST && i == 0 && j == door.position)) {
                        isDoor = true;
                        break;
                    }
                }

                if (isDoor) {
                    // Use a special texture for doors (create a green placeholder for now)
                    texture = doorTexture; // You'll need to create this
                } else {
                    texture = room.tiles[i][j] ? wallTexture : floorTexture;
                }

                float tileX = worldX + i * TILE_SIZE;
                float tileY = worldY + j * TILE_SIZE;

                azerisMain.getBatch().draw(texture,
                    tileX, tileY,
                    TILE_SIZE, TILE_SIZE);
            }
        }
    }

    @Override
    public void dispose() {

        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }

        if (wallTexture != null) wallTexture.dispose();
        if (floorTexture != null) floorTexture.dispose();

        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }
        Gdx.input.setInputProcessor(null);

    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {

        float zoomChange = amountY < 0 ? -zoomSpeed : zoomSpeed;
        camera.zoom = Math.max(minZoom, Math.min(maxZoom, camera.zoom + zoomChange));
        return true;
    }
}
