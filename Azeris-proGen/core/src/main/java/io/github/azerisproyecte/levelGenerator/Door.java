package io.github.azerisproyecte.levelGenerator;

public class Door {
    public int position; // Position in room coordinates
    public Direction dir;
    public Room connectedRoom;

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }
}
