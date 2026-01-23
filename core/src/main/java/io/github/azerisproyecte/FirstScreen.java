package io.github.azerisproyecte;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.azerisproyecte.levelGenerator.DungeonGenerator;
import io.github.azerisproyecte.levelGenerator.Room;

import java.util.List;

public class FirstScreen extends ScreenAdapter {

    public static final int WORLD_WIDTH = 16;
    public static final int WORLD_HEIGHT = 9;

    private final azerisMain azerisMain;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Engine engine;

    private List<Room> dungeon;
    private DungeonGenerator generator;
    private Texture wallTexture;
    private Texture floorTexture;
    private final int TILE_SIZE = 16;

    public FirstScreen(azerisMain azerisMain) {

        this.azerisMain = azerisMain;
        this.viewport = azerisMain.getViewport();
        this.camera = azerisMain.getCamera();
        this.engine = new Engine();

        generator = new DungeonGenerator(16, 9); // 10x10 grid
        dungeon = generator.generateDungeon(14); // Generate 8 rooms

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

        // Begin drawing
        azerisMain.getBatch().begin();

        // Draw each room
        for (Room room : dungeon) {
            drawRoom(room);
        }

        azerisMain.getBatch().end();
    }

    private void drawRoom(Room room) {
        // Calculate screen position (center rooms on screen)
        int screenX = (Gdx.graphics.getWidth() / 2) + (room.x - WORLD_WIDTH/2) * (room.width * TILE_SIZE);
        int screenY = (Gdx.graphics.getHeight() / 2) + (room.y - WORLD_HEIGHT/2) * (room.height * TILE_SIZE);

        // Draw each tile
        for (int i = 0; i < room.width; i++) {
            for (int j = 0; j < room.height; j++) {
                Texture texture = room.tiles[i][j] ? wallTexture : floorTexture;
                azerisMain.getBatch().draw(texture,
                    screenX + i * TILE_SIZE,
                    screenY + j * TILE_SIZE,
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

    }

}
