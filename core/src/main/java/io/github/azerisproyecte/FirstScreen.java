package io.github.azerisproyecte;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.azerisproyecte.asset.AssetService;
import io.github.azerisproyecte.asset.MapAsset;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen extends ScreenAdapter {

    private final azerisMain azerisMain;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetService assetService;

    private final OrthogonalTiledMapRenderer;

    public FirstScreen(azerisMain azerisMain) {

        this.azerisMain = azerisMain;
        this.assetService = azerisMain.getAssetService();
        this.viewport = azerisMain.getViewport();
        this.camera = azerisMain.getCamera();

    }

    @Override
    public void show() {
        this.assetService.load(MapAsset.MAIN);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }
}
