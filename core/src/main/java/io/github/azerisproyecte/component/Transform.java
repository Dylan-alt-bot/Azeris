package io.github.azerisproyecte.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Transform implements Component, Comparable<Transform> {
    public static final ComponentMapper<Transform> MAPPER = ComponentMapper.getFor(Transform.class);

    private final Vector2 position;
    private final int z;
    private final Vector2 size;
    private final Vector2 scaling;
    private float rotationDeg;


    public Transform(float rotationDeg, Vector2 scaling, Vector2 size, int z, Vector2 position) {
        this.rotationDeg = rotationDeg;
        this.scaling = scaling;
        this.size = size;
        this.z = z;
        this.position = position;
    }

    @Override
    public int compareTo(Transform other) {
        if (this.z != other.z) {
            return Float.compare(this.z, other.z);
        }
        if (this.position.y != other.position.y) {
            return Float.compare(this.position.y, other.position.y);
        }
        return Float.compare(this.position.x, other.position.x);
    }

    public Vector2 getSize() {
        return size;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getScaling() {
        return scaling;
    }

    public float getRotationDeg() {
        return rotationDeg;
    }
}
