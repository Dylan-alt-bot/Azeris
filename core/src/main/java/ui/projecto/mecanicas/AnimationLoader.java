package ui.projecto.mecanicas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationLoader {

    public static TextureRegion[] load (Texture texture, int xFrames, int yFrames){
        TextureRegion[][] grid = TextureRegion.split(
            texture,
            texture.getWidth() / xFrames,
            texture.getHeight() / yFrames);

        TextureRegion[] frames = new TextureRegion[xFrames * yFrames];
        int index = 0;
        for (int i = 0; i < yFrames; i++) {
            for (int j = 0; j < xFrames; j++) {
                frames[index++] = grid[i][j];
            }
        }
        return frames;
    }
}
