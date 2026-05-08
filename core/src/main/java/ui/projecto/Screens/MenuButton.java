package ui.projecto.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MenuButton {
    private final Texture normal;
    private final Texture hover;
    private final Rectangle bounds;
    private final float renderWidth;
    private final float renderHeight;
    private final Runnable onClick;
    private boolean isHovered = false;

    public MenuButton(Texture normal, Texture hover, float x, float y, float hitW, float hitH, float renderW, float renderH, Runnable onClick) {
        this.normal = normal;
        this.hover = hover;
        this.bounds = new Rectangle(x, y, hitW, hitH);
        this.renderWidth = renderW;
        this.renderHeight = renderH;
        this.onClick = onClick;
    }

    public void update(Vector3 mouse) {
        isHovered = bounds.contains(mouse.x, mouse.y);
        if (isHovered && Gdx.input.isButtonJustPressed(0)) {
            onClick.run();
        }
    }

    public void render(SpriteBatch batch) {
        Texture current = isHovered ? hover : normal;
        float rx = bounds.x - (renderWidth - bounds.width) / 2;
        float ry = bounds.y - (renderHeight - bounds.height) / 2;
        batch.draw(current, rx, ry, renderWidth, renderHeight);
    }
    public void dispose() {
        normal.dispose();
        hover.dispose();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setHovered(boolean value) {
        isHovered = value;
    }

    public void click() {
        onClick.run();
    }
}
