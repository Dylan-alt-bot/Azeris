package io.github.azerisproyecte.levelGenerator;

import java.util.ArrayList;
import java.util.List;

public class Room {
        public int x, y; // Grid position
        public int width, height; // In tiles
        public boolean[][] tiles; // true = wall, false = floor
        public List<Door> doors;

        public boolean hasNorthNeighbor = false;
        public boolean hasSouthNeighbor = false;
        public boolean hasEastNeighbor = false;
        public boolean hasWestNeighbor = false;

        public Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tiles = new boolean[width][height];
            this.doors = new ArrayList<>();
            generateRoom();
        }

        private void generateRoom() {
            // Fill borders with walls, center with floor
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    tiles[i][j] = (i == 0 || i == width-1 || j == 0 || j == height-1);
                }
            }
        }

    public static class Door {
        public int position; // Position in room coordinates
        public Direction direction;
        public Room connectedRoom;

        public enum Direction {
            NORTH, SOUTH, EAST, WEST
        }
    }

}


