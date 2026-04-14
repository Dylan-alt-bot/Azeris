package ui.projecto.personajes.Enemies.Amongus;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ui.projecto.mecanicas.AnimationLoader;
import ui.projecto.mecanicas.Enemies.Enemy;
import ui.projecto.mecanicas.Enemies.EnemyPathFinder;
import ui.projecto.mecanicas.Enemies.EnemyVision;
import ui.projecto.mecanicas.Enemies.EnemyWander;
import ui.projecto.mecanicas.MapManager;
import ui.projecto.mecanicas.Vida;
import ui.projecto.personajes.Enemies.Amongus.animacion.AnimationManagerAmongus;
import ui.projecto.personajes.Enemies.Amongus.estado.AmongusState;
import ui.projecto.personajes.Enemies.Amongus.util.ConstantsAmongus;
import ui.projecto.personajes.Enemies.Skeleton.estado.SkeletonState;
import ui.projecto.personajes.Player.Player;

public class Amongus implements Enemy {
    private float x, y, tiempo = 0f;
    private final float width = 30f, height = 30f;
    private final float velocidad = ConstantsAmongus.VELOCIDAD;

    private final Vida vida = new Vida(100);
    private AmongusState state = AmongusState.IDLE, previousState = AmongusState.IDLE;
    private AnimationManagerAmongus animations = new AnimationManagerAmongus();
    private final MapManager map;

    private final EnemyVision vision;
    private final EnemyWander wander;
    private final EnemyPathFinder pathFinder;

    private boolean facingRight = false;

    private float knockbackX = 0f, knockbackY = 0f, knockbackTimer = 0f;

    public Amongus(float x, float y, MapManager map) {
        this.x = x;
        this.y = y;
        this.map = map;

        wander = new EnemyWander(velocidad, 5f, ConstantsAmongus.DETECTED_PLAYER, map, width, height);
        pathFinder = new EnemyPathFinder(map, map.getTileSize());
        vision = new EnemyVision(ConstantsAmongus.DETECTED_PLAYER);
        loadAnimation();
    }

    private void loadAnimation() {
        Texture idle = new Texture("enemy/amongus/amongus.png");

        animations.add(AmongusState.IDLE, new Animation<>(1f, AnimationLoader.load(idle, 1,1)));
    }


    @Override
    public float getX() {
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public boolean collides(float px, float py, float pw, float ph) {
        return false;
    }

    @Override
    public void update(float delta, Player player) {

    }

    @Override
    public void render(SpriteBatch batch) {

    }

    @Override
    public void recibirDolor(int cantidad, float sourceX, float sourceY) {

    }

    @Override
    public boolean isDead() {
        return false;
    }
}
