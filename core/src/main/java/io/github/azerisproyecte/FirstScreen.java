package io.github.azerisproyecte;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.azerisproyecte.asset.AssetService;
import io.github.azerisproyecte.asset.MapAsset;
import io.github.azerisproyecte.system.RenderSystem;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen extends ScreenAdapter {

    private final azerisMain azerisMain;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;
    private final Engine engine;

    public FirstScreen(azerisMain azerisMain) {

        this.azerisMain = azerisMain;
        this.assetService = azerisMain.getAssetService();
        this.viewport = azerisMain.getViewport();
        this.camera = azerisMain.getCamera();
        this.engine = new Engine();

        this.engine.addSystem(new RenderSystem(this.batch, this.viewport));
    }

    @Override
    public void show() {
        this.assetService.load(MapAsset.MAIN);
        this.engine.getSystem(RenderSystem.class).setMap(this.assetService.get(MapAsset.MAIN));
    }

    @Override
    public void hide() {
        this.engine.removeAllEntities();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1/30f);
        this.engine.update(delta);
    }

    @Override
    public void dispose() {

        for (EntitySystem system : this.engine.getSystems()) {
            if (system instanceof Disposable disposableSystem) {
                disposableSystem.dispose();
            }
        }

        this.mapRenderer.dispose();
    }

}
